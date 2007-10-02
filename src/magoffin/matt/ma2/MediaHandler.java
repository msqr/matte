/* ===================================================================
 * MediaHandler.java
 * 
 * Created Mar 3, 2006 8:54:42 PM
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
 * $Id: MediaHandler.java,v 1.6 2007/07/29 08:42:43 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2;

import java.io.File;
import java.util.Map;

import magoffin.matt.ma2.domain.MediaItem;

/**
 * API for performing operations on media items.
 * 
 * <p>This is the main API Matte uses for processing media items. It serves 
 * primarily to allow Matte to:</p>
 * 
 * <ol>
 *   <li>Add new media items into the application, via the 
 *   {@link #createNewMediaItem(File)} method.</li>
 *   
 *   <li>Respond to requests for media items, along with request 
 *   processing instructions such as the desired size, quality, etc,
 *   via the {@link #handleMediaRequest(MediaItem, MediaRequest, MediaResponse)}
 *   method.</li>
 * </ol>
 * 
 * @author matt.magoffin
 * @version $Revision: 1.6 $ $Date: 2007/07/29 08:42:43 $
 */
public interface MediaHandler {
	
	/**
	 * Create a new instance of MediaItem from a File.
	 * 
	 * <p>This method will be called when a new media item is to be 
	 * added into Matte. This method should populate as much information
	 * as it can extract from the file into a {@link MediaItem} instance,
	 * including populating any {@link magoffin.matt.ma2.domain.Metadata}
	 * it can extract from the file.</p>
	 * 
	 * <p>This method does not need to persist the {@code MediaItem} instance,
	 * just populate it and return it.</p>
	 * 
	 * @param inputFile the file to create the media item from
	 * @return an instance of MediaItem
	 */
	MediaItem createNewMediaItem(File inputFile);
	
	/**
	 * Get the preferred file extension for this handler.
	 * 
	 * <p>Some handlers might return a different file extension for 
	 * a given request then the default file extension for their 
	 * type (for example a PNG image handler that returns JPEG images
	 * when resizing the images.</p>
	 * 
	 * @param item the media item to process
	 * @param request the request
	 * @return a file extension, without the period
	 */
	String getFileExtension(MediaItem item, MediaRequest request);
	
	/**
	 * Get a MediaEffect instance for a specific key.
	 * 
	 * <p>Implementaions must define the keys they support, and define 
	 * the parameters that go with it.</p>
	 * 
	 * @param key the key of the desired effect
	 * @param effectParameters a Map of parameters (optional)
	 * @return a MediaEffect instance
	 */
	MediaEffect getEffect(String key, Map<String,?> effectParameters);

	/**
	 * Handle a media request by processing the specified media item and 
	 * writing the result to {@link MediaResponse#getOutputStream()}.
	 * 
	 * @param item the media item to process
	 * @param request the request
	 * @param response the response
	 */
	void handleMediaRequest(MediaItem item, MediaRequest request, MediaResponse response);

}
