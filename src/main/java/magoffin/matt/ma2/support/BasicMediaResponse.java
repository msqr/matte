/* ===================================================================
 * BasicMediaResponse.java
 * 
 * Created Mar 16, 2006 10:34:48 PM
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
 */

package magoffin.matt.ma2.support;

import java.io.OutputStream;
import magoffin.matt.ma2.MediaResponse;
import magoffin.matt.ma2.domain.MediaItem;

/**
 * Basic implementation of {@link magoffin.matt.ma2.MediaResponse}.
 * 
 * @author matt.magoffin
 * @version 1.1
 */
public class BasicMediaResponse implements MediaResponse {

	private String mime = null;
	private String filename = null;
	private long length = 0;
	private long modifiedDate = 0;
	private MediaItem item = null;
	private OutputStream outputStream = null;

	/**
	 * Default constructor.
	 */
	public BasicMediaResponse() {
		// nothing to do
	}

	/**
	 * Construct with some parameters.
	 * 
	 * @param outputStream
	 *        the output stream
	 */
	public BasicMediaResponse(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@Override
	public void setMimeType(String mime) {
		this.mime = mime;
	}

	@Override
	public void setMediaLength(long length) {
		this.length = length;
	}

	@Override
	public void setModifiedDate(long date) {
		this.modifiedDate = date;
	}

	@Override
	public void setItem(MediaItem item) {
		this.item = item;
	}

	@Override
	public boolean hasOutputStream() {
		return outputStream != null;
	}

	@Override
	public OutputStream getOutputStream() {
		return outputStream;
	}

	@Override
	public void setFilename(String filename) {
		this.filename = filename;
	}

	@Override
	public void setPartialResponse(long start, long end, long total) {
		// not supported here
	}

	/**
	 * @return Returns the length.
	 */
	public long getLength() {
		return length;
	}

	/**
	 * @param length
	 *        The length to set.
	 */
	public void setLength(long length) {
		this.length = length;
	}

	/**
	 * @return Returns the mime.
	 */
	public String getMime() {
		return mime;
	}

	/**
	 * @param mime
	 *        The mime to set.
	 */
	public void setMime(String mime) {
		this.mime = mime;
	}

	/**
	 * @return Returns the item.
	 */
	public MediaItem getItem() {
		return item;
	}

	/**
	 * @return Returns the modifiedDate.
	 */
	public long getModifiedDate() {
		return modifiedDate;
	}

	/**
	 * @param outputStream
	 *        The outputStream to set.
	 */
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

}
