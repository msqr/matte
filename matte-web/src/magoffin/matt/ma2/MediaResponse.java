/* ===================================================================
 * MediaResponse.java
 * 
 * Copyright (c) 200304 Matt Magoffin. Created Mar 2, 2003.
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

package magoffin.matt.ma2;

import java.io.OutputStream;

import magoffin.matt.ma2.domain.MediaItem;

/**
 * API to allow setting response values during a media request and returning
 * the result of request processing.
 * 
 * <p>This API represents a response to a specific media item. In some ways it is 
 * similar to the java {@link javax.servlet.ServletResponse} API. When a user 
 * wants to view a particular media item, the application will create an instance
 * of this class and populate the {@link OutputStream} instance on that 
 * class so that {@link #getOutputStream()} returns the stream to which the 
 * result of the request procesing should be written to.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public interface MediaResponse {

	/**
	 * Set the MIME type of a response.
	 * 
	 * <p>The MIME type of the response might be different from the MIME type
	 * of the {@link MediaItem} in the request. For example a request for 
	 * an MP3 resource might actually return a JPG image extracted from the 
	 * metadata of that MP3 file.</p>
	 * 
	 * @param mime the MIME type of the response
	 */
	public void setMimeType(String mime);

	/**
	 * Set the length of the media of the response.
	 * 
	 * <p>This should be the lenght, in bytes, if the data representing 
	 * the result of the media request processing.</p>
	 * 
	 * @param length the length of the content
	 */
	public void setMediaLength(long length);

	/**
	 * Set the modification date of the response.
	 * 
	 * <p>If possible, this method should be called and set with the 
	 * last modification date of the result of processing this media request.
	 * This is to aid client caching, for clients that request media items 
	 * with a "if modified since" directive, such as HTTP clients.</p>
	 * 
	 * @param date the modification date
	 */
	public void setModifiedDate(long date);

	/**
	 * Set the media item used in the response.
	 * 
	 * <p>The {@link MediaRequest} object only contains the ID of the 
	 * {@link MediaItem} to process. This method should be called with 
	 * the actual {@link MediaItem} for that ID.</p>
	 * 
	 * @param item the media item
	 */
	public void setItem(MediaItem item);
	
	/**
	 * Set a file name to associate with the response.
	 * 
	 * <p>This can be used to associate a filename with the response that
	 * is different from the name of the item.</p>
	 * 
	 * @param filename the file name
	 */
	public void setFilename(String filename);
	
	/**
	 * Return <em>true</em> if this response supports an OutputStream.
	 * 
	 * @return boolean
	 */
	public boolean hasOutputStream();
	
	/**
	 * Get the output stream.
	 * 
	 * <p>This is the {@link OutputStream} that the result of processing the 
	 * request should be written to. During request processing, the 
	 * {@link magoffin.matt.ma2.MediaHandler} doing the processing should 
	 * write the resulting media item data to this stream.</p>
	 * 
	 * @return output stream to write the result to
	 */
	public OutputStream getOutputStream();
	
	/**
	 * Indicate that the server is returning only a partial content response.
	 * 
	 * <p>Note the {@link #setMediaLength(long)} should <b>not</b> be called
	 * if this method is used, as this method should set the media length
	 * automatically to the byte range specified (end - start + 1).</p>
	 * 
	 * @param start the starting byte position
	 * @param end the ending byte position
	 * @param total the total bytes in the media resource
	 */
	public void setPartialResponse(long start, long end, long total);

}
