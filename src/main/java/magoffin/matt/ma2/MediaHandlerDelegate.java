/* ===================================================================
 * ImageHelper.java
 * 
 * Created Jan 24, 2007 10:42:48 AM
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

package magoffin.matt.ma2;

import org.springframework.core.io.Resource;

import magoffin.matt.ma2.domain.MediaItem;

/**
 * API for delegate media requests.
 * 
 * <p>This API is for chaining one media request to another, eg.
 * for processing embedded media within one media type. For example
 * an MP3 handler might want to delegate to a JPEG handler for an
 * embedded album cover image.</p>
 * 
 * <p>This allows a normal {@link magoffin.matt.ma2.MediaHandler} 
 * implementation to also serve as a delegate for another implementation.
 * Thus a single JPEG implementation of {@link magoffin.matt.ma2.MediaHandler}
 * might also implement this API so it can be used as a delegate, and 
 * the JPEG processing code can be implemented once and shared by both
 * request processing functions.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public interface MediaHandlerDelegate {

	/**
	 * Handle a delegate media request.
	 * 
	 * <p>The <code>mediaResource</code> is not the original {@code MediaItem}
	 * resource, rather it is the media resource extracted <em>from</em>
	 * the MediaItem, e.g. the metadata resource.</p>
	 * 
	 * @param mediaResource the media data to process
	 * @param mimeType the MIME type of the media data in <code>mediaResource</code>
	 * @param item the MediaItem being processed
	 * @param request the request
	 * @param response the response
	 */
	void handleDelegateMediaRequest(Resource mediaResource, String mimeType, 
			MediaItem item, MediaRequest request, MediaResponse response);

	/**
	 * Get the preferred file extension for this handler.
	 * 
	 * <p>The <code>mediaResource</code> is not the original {@code MediaItem}
	 * resource, rather it is the media resource extracted <em>from</em>
	 * the MediaItem, e.g. the metadata resource.</p>
	 * 
	 * <p>Some handlers might return a different file extension for 
	 * a given request then the default file extension for their 
	 * type (for example a PNG image handler that returns JPEG images
	 * when resizing the images.</p>
	 * 
	 * @param mediaResource the media data to process
	 * @param mimeType the MIME type of the media data in <code>mediaResource</code>
	 * @param item the MediaItem being processed
	 * @param request the request
	 * @return a file extension, without the period
	 */
	String getDelegateFileExtension(Resource mediaResource, String mimeType,
			MediaItem item, MediaRequest request);

}
