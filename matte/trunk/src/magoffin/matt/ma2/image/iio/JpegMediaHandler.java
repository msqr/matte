/* ===================================================================
 * JpegMediaHandler.java
 * 
 * Created Mar 3, 2006 9:42:56 PM
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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageReader;

import magoffin.matt.ma2.MediaHandlerDelegate;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.MediaResponse;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.image.ImageConstants;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * JPEG media handler using ImageIO.
 * 
 * @author matt.magoffin
 * @version $Revision$ $Date$
 */
public class JpegMediaHandler extends BaseImageIOMediaHandler implements MediaHandlerDelegate {

	/** The ImageIO JPEG format name constant. */
	public static final String FORMAT_NAME_IIO_JPEG_1_0 = "javax_imageio_jpeg_image_1.0";
	
	/**
	 * Default constructor.
	 */
	public JpegMediaHandler() {
		super(ImageConstants.JPEG_MIME);
		setPreferredFileExtension(ImageConstants.DEFAULT_JPEG_FILE_EXTENSION);
	}

	/**
	 * Construct with a different MIME type.
	 * @param mimeType the MIME type
	 */
	protected JpegMediaHandler(String mimeType) {
		super(mimeType);
		setPreferredFileExtension(ImageConstants.DEFAULT_JPEG_FILE_EXTENSION);
	}
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaHandler#createNewMediaItem(java.io.File)
	 */
	public MediaItem createNewMediaItem(File inputFile) {
		MediaItem item = getDomainObjectFactory().newMediaItemInstance();
		ImageReader reader = null;
		try {
			reader = getImageMediaHelper().getReaderForFile(inputFile);
			setupBaseItemProperties(item, reader);
			handleMetadata(null, new FileSystemResource(inputFile), item);			
		} catch ( IOException e ) {
			throw new RuntimeException("Exception reading image", e);
		} finally {
			if ( reader != null ) {
				reader.dispose();
			}
		}
		return item;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaHandlerDelegate#handleDelegateMediaRequest(org.springframework.core.io.Resource, java.lang.String, magoffin.matt.ma2.domain.MediaItem, magoffin.matt.ma2.MediaRequest, magoffin.matt.ma2.MediaResponse)
	 */
	public void handleDelegateMediaRequest(Resource mediaResource, String mimeType, 
			MediaItem item, MediaRequest request, MediaResponse response) {
		BufferedImage image = defaultHandleDelegateRequest(mediaResource, mimeType, item, request, response);	
		writeJpegStream(item, request, response, image);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaHandlerDelegate#getDelegateFileExtension(org.springframework.core.io.Resource, java.lang.String, magoffin.matt.ma2.domain.MediaItem, magoffin.matt.ma2.MediaRequest)
	 */
	public String getDelegateFileExtension(Resource mediaResource, String mimeType, MediaItem item, MediaRequest request) {
		return getPreferredFileExtension();
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaHandler#handleMediaRequest(magoffin.matt.ma2.domain.MediaItem, magoffin.matt.ma2.MediaRequest, magoffin.matt.ma2.MediaResponse)
	 */
	public void handleMediaRequest(MediaItem item, MediaRequest request, MediaResponse response) {
		BufferedImage image = defaultHandleRequest(item,request,
				getMediaBiz().getMediaItemResource(item),response);
		writeJpegStream(item, request, response, image);
	}
	
	private void writeJpegStream(MediaItem item, MediaRequest request, MediaResponse response, BufferedImage image) {
		if ( image == null ) {
			return;
		}
		response.setMimeType(getMime());
		int quality = Math.round(getMediaBiz().getQualityValue(request.getQuality()) * 100.0f);
		getImageMediaHelper().writeJpegStream(image, item.getItemId(), quality, 
				response.getOutputStream());
	}

}
