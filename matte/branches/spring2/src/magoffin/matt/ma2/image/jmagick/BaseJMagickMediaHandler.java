/* ===================================================================
 * BaseJMagickMediaHandler.java
 * 
 * Created Dec 28, 2006 9:32:56 PM
 * 
 * Copyright (c) 2006 Matt Magoffin (spamsqr@msqr.us)
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

package magoffin.matt.ma2.image.jmagick;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

import magick.ImageInfo;
import magick.InterlaceType;
import magick.MagickException;
import magick.MagickImage;
import magoffin.matt.ma2.MediaEffect;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.MediaResponse;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.image.BaseImageMediaHandler;
import magoffin.matt.ma2.support.Geometry;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

/**
 * Base implementation of {@link magoffin.matt.ma2.MediaHandler} that uses the JMagick
 * for image processing.
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl class="class-properties">
 *   <dt>outputDepth</dt>
 *   <dd>Force the otuput depth to a specific value. Defaults to 8.</dd>
 *   
 *   
 *   <dt>jmagickMediaEffectMap</dt>
 *   <dd>A Map of effect keys to {@link JMagickMediaEffect} implementations.</dd>
 * </dl>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public abstract class BaseJMagickMediaHandler extends BaseImageMediaHandler {
	
	/** The default value for the <code>outputDepth</code> property. */
	public static final int DEFAULT_OUTPUT_DEPTH = 8;

	private Map<String, JMagickMediaEffect> jmagickMediaEffectMap;
	private int outputDepth = DEFAULT_OUTPUT_DEPTH;
	
	/**
	 * Construct with a MIME type.
	 * @param mime the MIME type
	 */
	public BaseJMagickMediaHandler(String mime) {
		super(mime);
	}

	@Override
	public MediaEffect getEffect(String key, Map<String, ?> effectParameters) {
		return jmagickMediaEffectMap.get(key);
	}

	/**
	 * @return the jmagickMediaEffectMap
	 */
	public Map<String, JMagickMediaEffect> getJmagickMediaEffectMap() {
		return jmagickMediaEffectMap;
	}

	/**
	 * @param jmagickMediaEffectMap the jmagickMediaEffectMap to set
	 */
	public void setJmagickMediaEffectMap(
			Map<String, JMagickMediaEffect> jmagickMediaEffectMap) {
		this.jmagickMediaEffectMap = jmagickMediaEffectMap;
	}

	/**
	 * Basic JMagick implementation of createNewMediaItem.
	 * 
	 * <p>This implementation creates a new MediaItem instance and then
	 * calls {@link #setupBaseItemProperties(MediaItem, ImageInfo)}
	 * followed by {@link #handleMetadata(MediaRequest, Resource, MediaItem)}.</p>
	 */
	public MediaItem createNewMediaItem(File inputFile) {
		MediaItem item = getDomainObjectFactory().newMediaItemInstance();
		ImageInfo info = null;
		try {
			info = new ImageInfo(inputFile.getAbsolutePath());
			
			setupBaseItemProperties(item, info);
			
			handleMetadata(null, new FileSystemResource(inputFile), item);

		} catch ( MagickException e ) {
			throw new RuntimeException("Exception reading image", e);
		}
		return item;
	}

	/**
	 * Basic JMagick implementation of handleMediaRequest.
	 * 
	 * <p>This implementation simply calls 
	 * {@link #defaultHandleRequest(MediaItem, MediaRequest, MediaResponse)}.</p>
	 */
	public void handleMediaRequest(MediaItem item, MediaRequest request,
			MediaResponse response) {
		defaultHandleRequest(item, request, response);
	}
	
	/**
	 * Setup some basic properties from an ImageReader.
	 * 
	 * <p>This will set up the item's width, height, and MIME type.</p>
	 * 
	 * @param item the MediaItem to setup
	 * @param info the JMagick image
	 * @throws MagickException if a JMagick error occurs
	 */
	protected void setupBaseItemProperties(MediaItem item, ImageInfo info) throws MagickException {
		// set width, height
		if ( info.getSize() != null ) {
			// set width, height
			String[] dimensions = info.getSize().split("x", 2);
			item.setWidth(Integer.valueOf(dimensions[0]));
			item.setHeight(Integer.valueOf(dimensions[1]));
		} else {
			// load image and see
			MagickImage image = new MagickImage(info);
			Dimension dim = image.getDimension();
			item.setWidth((int)dim.getWidth());
			item.setHeight((int)dim.getHeight());
		}

		// set MIME
		item.setMime(getMime());
	}
	
	/**
	 * Default handler for JMagick requests.
	 * 
	 * <p>This implementation gets a {@link Resource} via 
	 * {@link MediaBiz#getMediaItemResource(MediaItem)} and passes that 
	 * to {@link #defaultHandleResource(MediaItem, MediaRequest, MediaResponse, Resource)}.</p>
	 * 
	 * @param item the item
	 * @param request the request
	 * @param response the response
	 */
	protected void defaultHandleRequest(MediaItem item, MediaRequest request,
			MediaResponse response) {
		Resource itemResource = getMediaBiz().getMediaItemResource(item);
		defaultHandleResource(item, request, response, itemResource);

	}
	
	/**
	 * Get the MIME type to set in the response.
	 * 
	 * <p>This implementation merely calls {@link #getMime()} but extending classes
	 * may need to override this.</p>
	 * 
	 * @param item the MediaItem being processed
	 * @param request the request
	 * @param itemResource the item resource being processed
	 * @return MIME
	 */
	protected String getResponseMime(
			MediaItem item, 
			MediaRequest request, 
			Resource itemResource) {
		return getMime();
	}

	/**
	 * Default handler for JMagick resource request.
	 * 
	 * <p>This can be used to service {@link magoffin.matt.ma2.MediaHandlerDelegate}
	 * requests, if extending classes wish to support that API.</p>
	 * 
	 * @param item the item
	 * @param request the request
	 * @param response the response
	 * @param itemResource the media resource being operated on
	 */
	protected void defaultHandleResource(MediaItem item, MediaRequest request, MediaResponse response, Resource itemResource) {
		try {
			if ( !needToAlter(item, request) ) {
				defaultHandleRequestOriginal(item, itemResource, response);
				return;
			}
			
			// set response MIME
			response.setMimeType(getResponseMime(item, request, itemResource));

			int quality = Math.round(getMediaBiz().getQualityValue(request.getQuality()) * 100.0f);
			Geometry geometry = getMediaBiz().getGeometry(request.getSize());

			ImageInfo inInfo = new ImageInfo(itemResource.getFile().getAbsolutePath());
			inInfo.setQuality(quality);
			inInfo.setSize(geometry.toString()); // as hint to reading image
			if ( log.isDebugEnabled() ) {
				log.debug("Size: " +geometry.toString() +", quality: " +quality);
			}
			
			// read image into memory
			MagickImage image = new MagickImage(inInfo);
			MagickImage result = image;
			
			// stash ImageInfo, MagickImage (in) and MagickImage (result) onto request
			request.getParameters().put(JMagickMediaEffect.INPUT_IMAGE_INFO_KEY, inInfo);
			request.getParameters().put(JMagickMediaEffect.INPUT_MAGICK_IMAGE_KEY, image);
			request.getParameters().put(JMagickMediaEffect.OUTPUT_MAGICK_IMAGE_KEY, result);
			
			needToRotate(item, request);
			applyEffects(item, request, response);
			
			result = (MagickImage)request.getParameters().get(JMagickMediaEffect.OUTPUT_MAGICK_IMAGE_KEY);
			
			// set filename for output
			File outFile = null;
			if ( request.getParameters().containsKey(MediaRequest.OUTPUT_FILE_KEY) ) {
				outFile = (File)request.getParameters().get(MediaRequest.OUTPUT_FILE_KEY);
			} else {
				// use temp file
				outFile = File.createTempFile("JMagickTemp-", 
						"."+getFileExtension(item, request));
			}
			result.setFileName(outFile.getAbsolutePath());
			
			// write output image
			ImageInfo outInfo = new ImageInfo();
			outInfo.setQuality(quality);
			outInfo.setDepth(outputDepth);
			outInfo.setSize(geometry.toString());
			outInfo.setInterlace(InterlaceType.PlaneInterlace); // make configurable?
			result.writeImage(outInfo);
			
			// if used temp file, then copy to output stream and delete now
			if ( !request.getParameters().containsKey(MediaRequest.OUTPUT_FILE_KEY) ) {
				try {
					FileCopyUtils.copy(new FileInputStream(outFile), response.getOutputStream());
				} finally {
					outFile.delete();
				}
			}
		} catch ( Exception e ) {
			throw new RuntimeException("Exception writing media: "+e, e);
		}
	}
	
	/**
	 * @return the outputDepth
	 */
	public int getOutputDepth() {
		return outputDepth;
	}
	
	/**
	 * @param outputDepth the outputDepth to set
	 */
	public void setOutputDepth(int outputDepth) {
		this.outputDepth = outputDepth;
	}

}
