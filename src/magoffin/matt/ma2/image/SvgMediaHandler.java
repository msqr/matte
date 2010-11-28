/* ===================================================================
 * SvgMediaHandler.java
 * 
 * Created Feb 2, 2007 12:32:40 PM
 * 
 * Copyright (c) 2007 Matt Magoffin (spamsqr@msqr.us)
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

package magoffin.matt.ma2.image;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.MediaResponse;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.support.Geometry;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.springframework.core.io.Resource;

/**
 * SVG media handler using Apache Batik.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class SvgMediaHandler extends BaseImageMediaHandler {

	private Class<Transcoder> transcoderClass = null;
	private Map<Object, Object> transcoderHints = Collections.emptyMap();
	private String rasterFileExtension = ImageConstants.DEFAULT_PNG_FILE_EXTENSION;

	/**
	 * Constructor.
	 */
	public SvgMediaHandler() {
		super(ImageConstants.SVG_MIME);
		setPreferredFileExtension(ImageConstants.DEFAULT_SVG_FILE_EXTENSION);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaHandler#createNewMediaItem(java.io.File)
	 */
	public MediaItem createNewMediaItem(File inputFile) {
		MediaItem item = getDomainObjectFactory().newMediaItemInstance();
		item.setMime(getMime());
		return item;
	}

	@Override
	public String getFileExtension(MediaItem item, MediaRequest request) {
		if ( request.isOriginal() ) {
			return getPreferredFileExtension();
		}
		return rasterFileExtension;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaHandler#handleMediaRequest(magoffin.matt.ma2.domain.MediaItem, magoffin.matt.ma2.MediaRequest, magoffin.matt.ma2.MediaResponse)
	 */
	public void handleMediaRequest(MediaItem item, MediaRequest request,
			MediaResponse response) {
		Resource itemResource = getMediaBiz().getMediaItemResource(item);
		if ( request.isOriginal() ) {
			defaultHandleRequestOriginal(item, itemResource, request, response);
			return;
		}
		
		// rasterize SVG into image
		try {
			Transcoder transcoder = transcoderClass.newInstance();
			transcoder.setTranscodingHints(getTranscoderHints());
			
			// set output size
			Geometry geometry = getMediaBiz().getGeometry(request.getSize());
			transcoder.addTranscodingHint(ImageTranscoder.KEY_WIDTH, 
					new Float(geometry.getWidth()));
			transcoder.addTranscodingHint(ImageTranscoder.KEY_MAX_HEIGHT, 
					new Float(geometry.getHeight()));
			
			transcoder.transcode(new TranscoderInput(itemResource.getInputStream()), 
				new TranscoderOutput(response.getOutputStream()));
		} catch ( IOException e ) {
			log.warn("IOException rasterizing SVG: " +e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return the rasterFileExtension
	 */
	public String getRasterFileExtension() {
		return rasterFileExtension;
	}
	
	/**
	 * @param rasterFileExtension the rasterFileExtension to set
	 */
	public void setRasterFileExtension(String rasterFileExtension) {
		this.rasterFileExtension = rasterFileExtension;
	}
	
	/**
	 * @return the transcoderClass
	 */
	public Class<Transcoder> getTranscoderClass() {
		return transcoderClass;
	}
	
	/**
	 * @param transcoderClass the transcoderClass to set
	 */
	public void setTranscoderClass(Class<Transcoder> transcoderClass) {
		this.transcoderClass = transcoderClass;
	}
	
	/**
	 * @return the transcoderHints
	 */
	public Map<Object, Object> getTranscoderHints() {
		return transcoderHints;
	}
	
	/**
	 * @param transcoderHints the transcoderHints to set
	 */
	public void setTranscoderHints(Map<Object, Object> transcoderHints) {
		this.transcoderHints = transcoderHints;
	}

}
