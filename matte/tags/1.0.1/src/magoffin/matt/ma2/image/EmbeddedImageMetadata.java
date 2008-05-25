/* ===================================================================
 * EmbeddedImageMetadata.java
 * 
 * Created Jan 13, 2007 6:29:55 PM
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

import java.awt.image.BufferedImage;

import org.springframework.core.io.Resource;

/**
 * API for metadata that supports an embedded image.
 * 
 * <p>This API can be implemented by {@link magoffin.matt.ma2.MediaMetadata}
 * implementations that are able to extract image resources from the metadata
 * of certain file types. For example, many MP3 files embed the album cover
 * image as a JPEG or PNG image inside an ID3v2 tag of the MP3 file. A 
 * {@link magoffin.matt.ma2.MediaMetadata} implementation could support 
 * finding and extracting that image so it can be used as the icon for that
 * media item within Matte.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public interface EmbeddedImageMetadata {
	
	/**
	 * Return <em>true</em> if an embedded image is available.
	 * 
	 * @return boolean
	 */
	boolean hasEmbeddedImage();

	/**
	 * Get a BufferedImage for the embedded image.
	 * 
	 * <p>If {@link #hasEmbeddedImage()} returns <em>true</em>, this
	 * method will extract the image data and return it as a 
	 * {@link BufferedImage}.</p>
	 * 
	 * @return BufferedImage
	 */
	BufferedImage getEmbeddedImage();
	
	/**
	 * Get the embedded image as a Resource (original image data).
	 * 
	 * <p>If {@link #hasEmbeddedImage()} returns <em>true</em>, this
	 * method will extract the image data and return it as-is, without
	 * any alteration, so it can be returned directly as the original 
	 * image data.</p>
	 * 
	 * @return Resource for the original embedded image data
	 */
	Resource getEmbeddedImageResource();
	
	/**
	 * Get the MIME type of the embedded image.
	 * 
	 * @return MIME type
	 */
	String getEmbeddedImageMimeType();
	
	/**
	 * Get the width, in pixels, of the embedded image.
	 * 
	 * @return width
	 */
	int getEmbeddedImageWidth();
	
	/**
	 * Get the height, in pixels, of the embedded image.
	 * @return height
	 */
	int getEmbeddedImageHeight();
	
}
