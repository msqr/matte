/* ===================================================================
 * PngMediaHandler.java
 * 
 * Created Jan 24, 2007 3:44:58 PM
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
 */

package magoffin.matt.ma2.image.jmagick;

import org.springframework.core.io.Resource;

import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.image.ImageConstants;

/**
 * PNG media handler using JMagick.
 * 
 * <p>This handler returns JPEG images unless the original image is 
 * requested, in which case the original PNG image is returned.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public class PngMediaHandler extends JpegMediaHandler {

	/**
	 * Constructor.
	 */
	public PngMediaHandler() {
		super(ImageConstants.PNG_MIME);
	}

	@Override
	public String getFileExtension(MediaItem item, MediaRequest request) {
		if ( request.isOriginal() ) {
			return ImageConstants.DEFAULT_PNG_FILE_EXTENSION;
		}
		return super.getFileExtension(item, request);
	}

	@Override
	public String getDelegateFileExtension(Resource mediaResource, String mimeType, 
			MediaItem item, MediaRequest request) {
		if ( request.isOriginal() ) {
			return ImageConstants.DEFAULT_PNG_FILE_EXTENSION;
		}
		return super.getDelegateFileExtension(mediaResource, mimeType, item, request);
	}

}
