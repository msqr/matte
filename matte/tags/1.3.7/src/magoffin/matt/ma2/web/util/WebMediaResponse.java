/* ===================================================================
 * WebMediaResponse.java
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

package magoffin.matt.ma2.web.util;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma2.MediaResponse;
import magoffin.matt.ma2.domain.MediaItem;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Web implementation of {@link MediaResponse}.
 * 
 * @author matt.magoffin
 * @version $Revision$ $Date$
 */
public class WebMediaResponse implements MediaResponse {
	
	private HttpServletResponse webResponse = null;
	private String filename = null;
	private boolean download = false;
	private boolean original = false;
	private long modDate = 0;
	private long fileLength = 0;

	/**
	 * Default constructor.
	 */
	public WebMediaResponse() {
		super();
	}

	/**
	 * Construct from an {@code HttpServletResponse}.
	 * @param webResponse the HttpServletResponse to respond with
	 * @param filename a filename to set on the response via a 
	 * HTTP Content-Disposition header
	 */
	public WebMediaResponse(HttpServletResponse webResponse,
			String filename) {
		this.webResponse = webResponse;
		this.filename = filename;
	}

	/**
	 * Construct from an {@code HttpServletResponse}.
	 * @param webResponse the HttpServletResponse to respond with
	 * @param download <em>true</em> if should generate a download
	 * HTTP Content-Disposition header
	 * @param original <em>true</em> if this is an original media
	 * request, and should support ranges
	 */
	public WebMediaResponse(HttpServletResponse webResponse,
			boolean download, boolean original) {
		this.webResponse = webResponse;
		this.download = download;
		this.original = original;
	}

	public void setMimeType(String mime) {
		webResponse.setContentType(mime);
	}
	
	private void setETag() {
		String etag = String.format("%d-%d", modDate, fileLength);
		etag = DigestUtils.md5Hex(etag);
		webResponse.setHeader("ETag", etag);
		if ( original ) {
			webResponse.setHeader("Accept-Ranges", "0-" +fileLength);
		}
	}
	
	public void setMediaLength(long length) {
		fileLength = length;
		webResponse.setContentLength((int)length);
		setETag();
	}

	public void setItem(MediaItem item) {
		if ( this.filename == null ) {
			this.filename = item.getName();
		}
	}
	
	public void setPartialResponse(long start, long end, long total) {
		fileLength = total;
		String val = String.format("bytes %d-%d/%d", start, end, total);
		webResponse.setHeader("Content-Range", val);
		webResponse.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
		webResponse.setContentLength((int)(end - start) + 1);
		setETag();
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaResponse#hasOutputStream()
	 */
	public boolean hasOutputStream() {
		return webResponse != null;
	}

	public OutputStream getOutputStream() {
		try {
			return webResponse.getOutputStream();
		} catch ( IOException e ) {
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaResponse#setFilename(java.lang.String)
	 */
	public void setFilename(String filename) {
		this.filename = filename;
		if ( this.filename != null && this.download ) {
			// for download responses, add a filename header
			webResponse.setHeader(
				"Content-Disposition","attachment; filename=\"" 
				+this.filename+ "\"");
		}
	}

	public void setModifiedDate(long date) {
		modDate = date;
	}

}
