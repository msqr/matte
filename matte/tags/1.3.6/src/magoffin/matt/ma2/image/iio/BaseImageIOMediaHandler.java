/* ===================================================================
 * BaseImageIOMediaHandler.java
 * 
 * Created Mar 3, 2006 9:41:29 PM
 * 
 * Copyright (c) 2006 Matt Magoffin.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 * 02111-1307 USA
 * ===================================================================
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2.image.iio;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;

import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.MediaResponse;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.image.ImageMediaHelper;
import magoffin.matt.ma2.image.awt.AwtMediaEffect;
import magoffin.matt.ma2.image.awt.BaseAwtImageMediaHandler;
import magoffin.matt.ma2.support.Geometry;

import org.springframework.core.io.Resource;

/**
 * Base implemenation of {@link magoffin.matt.ma2.MediaHandler} 
 * using the Java ImageIO library for image operations.
 * 
 * @author matt.magoffin
 * @version $Revision$ $Date$
 */
public abstract class BaseImageIOMediaHandler extends BaseAwtImageMediaHandler {

	private ImageMediaHelper imageMediaHelper;
	
	/**
	 * Construct with MIME type.
	 * @param mime the MIME type
	 */
	public BaseImageIOMediaHandler(String mime) {
		super(mime);
	}

	/**
	 * Handle a delegate media request.
	 * 
	 * <p>This method will operate in the following way:</p>
	 * 
	 * <ol>
	 *   <li>Call the {@link #needToAlter(MediaItem, MediaRequest)} method. If
	 *   this returnes <em>false</em> then call 
	 *   {@link magoffin.matt.ma2.support.AbstractMediaHandler#defaultHandleRequestOriginal}
	 *   and return.</li>
	 *   
	 *   <li>Create a {@code BufferedImage} by calling {@link #getBufferedImage}.</li>
	 *   
	 *   <li>Put the {@code BufferedImage} instance onto the request parameters
	 *   using the {@link AwtMediaEffect#INPUT_BUFFERED_IMAGE_KEY} key.</li>
	 *   
	 *   <li>Call the {@link magoffin.matt.ma2.support.AbstractMediaHandler#needToRotate} 
	 *   method.</li>
	 *   
	 *   <li>Call the {@link magoffin.matt.ma2.support.AbstractMediaHandler#applyEffects} 
	 *   method.</li>
	 *   
	 *   <li>Return the {@link BufferedImage} object on the request parameter
	 *   {@link AwtMediaEffect#INPUT_BUFFERED_IMAGE_KEY} key.</li>
	 * </ol>
	 * 
	 * <p>These steps allow for standard request processing, and effects are 
	 * assumed to alter the {@link BufferedImage} instance as sneeded.</p>
	 * 
	 * @param mediaResource the resource data to process
	 * @param mimeType the <code>mediaResource</code>'s MIME type
	 * @param item the media item being processed
	 * @param request the request
	 * @param response the response
	 * @return BufferedImage if any changes were processed, <em>null</em> otherwise
	 */
	protected BufferedImage defaultHandleDelegateRequest(Resource mediaResource, 
			String mimeType, MediaItem item, 
			MediaRequest request, MediaResponse response) {
		try {
			if ( !needToAlter(item, request) ) {
				defaultHandleRequestOriginal(item, mediaResource, request, response);
				return null;
			}
			
			BufferedImage result = getBufferedImage(mimeType, item, request, mediaResource);
			request.getParameters().put(AwtMediaEffect.INPUT_BUFFERED_IMAGE_KEY, result);
			needToRotate(item, request);
			applyEffects(item, request, response);
			result = (BufferedImage)request.getParameters().get(AwtMediaEffect.INPUT_BUFFERED_IMAGE_KEY);
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Handle a media request.
	 * 
	 * <p>This method will simply call the 
	 * {@link #defaultHandleDelegateRequest}, passing in the 
	 * {@link MediaItem#getMime()} type of the {@code item} parameter.</p>
	 * 
	 * @param item the media item
	 * @param request the request
	 * @param itemResource the media item's Resource
	 * @param response the response
	 * @return BufferedImage if any changes were processed, <em>null</em> otherwise
	 */
	protected BufferedImage defaultHandleRequest(MediaItem item, MediaRequest request, 
			Resource itemResource, MediaResponse response) {
		return defaultHandleDelegateRequest(itemResource, item.getMime(), 
				item, request, response);
	}

	/**
	 * Get a BufferedImage for a MediaItem.
	 * 
	 * @param mimeType of the resouce being handled (may be different from item's MIME
	 * if handling a delegate request)
	 * @param item the media item
	 * @param request the request
	 * @param itemResource the item Resource
	 * @return BufferedImage
	 */
	protected BufferedImage getBufferedImage(String mimeType, MediaItem item, 
			MediaRequest request, Resource itemResource) {
		Geometry geometry = getMediaBiz().getScaledGeometry(item, request);
		
		if ( log.isDebugEnabled() ) {
			log.debug("Reading buffered image: ID = "
				+item.getItemId() 
				+", dimensions = " +item.getWidth() +"x" +item.getHeight() 
				+", output dimensions = " +geometry.getWidth() +"x" +geometry.getHeight() );
		}
		
		int itemWidth = item.getWidth();
		int itemHeight = item.getHeight();
		int width = geometry.getWidth();
		int height = geometry.getHeight();
		
		// read in a reduced number of pixels if possible to conserve memory
		ImageReader reader = null;
		BufferedImage image = null;
		try {
			reader = getImageMediaHelper().getReaderForMIME(mimeType,
					itemResource.getInputStream());
			ImageReadParam param = reader.getDefaultReadParam();
			if ( width == itemWidth && height == itemHeight ) {
				// not changing size
				image = reader.read(0);
			} else {
				// changing size
				if (param.canSetSourceRenderSize() ) {
					param.setSourceRenderSize(new Dimension(width, height));
					return reader.read(0,param);
				}
				
				// read in sub-sampled image if size < 1/2 original
				int periodX = (int)Math.floor(itemWidth / (double)width / 2.0d );
				int periodY = (int)Math.floor(itemHeight / (double)height / 2.0d );
				if ( periodX < 1 ) {
					periodX = 1;
				}
				if ( periodY < 1 ) {
					periodY = 1;
				}
				if ( log.isDebugEnabled() ) {
					log.debug("Source sampling = " +periodX +"x" +periodY);
				}
				param.setSourceSubsampling(periodX,periodY,0,0);
				image = reader.read(0,param);
			}
		} catch ( IOException e ) {
			throw new RuntimeException("IOException reading image data for item ["
				+item.getPath() +"]",e);
		} finally {
			if ( reader != null ) {
				reader.dispose();
			}
		}
			
		return image;
	}

	/**
	 * Setup some basic properties from an ImageReader.
	 * 
	 * <p>This will set up the item's width, height, and MIME type.</p>
	 * 
	 * @param item the MediaItem to setup
	 * @param reader the ImageReader to read from
	 * @throws IOException if an error occurs
	 */
	protected void setupBaseItemProperties(MediaItem item, ImageReader reader) throws IOException {
		// set width, height
		item.setWidth(new Integer(reader.getWidth(0)));
		item.setHeight(new Integer(reader.getHeight(0)));
		
		// set MIME
		item.setMime(getMime());
	}
	
	/**
	 * @return the imageMediaHelper
	 */
	public ImageMediaHelper getImageMediaHelper() {
		return imageMediaHelper;
	}
	
	/**
	 * @param imageMediaHelper the imageMediaHelper to set
	 */
	public void setImageMediaHelper(ImageMediaHelper imageMediaHelper) {
		this.imageMediaHelper = imageMediaHelper;
	}

}
