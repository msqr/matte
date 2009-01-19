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
	 */
	public WebMediaResponse(HttpServletResponse webResponse,
			boolean download) {
		this.webResponse = webResponse;
		this.download = download;
	}

	public void setMimeType(String mime) {
		webResponse.setContentType(mime);
	}
	
	public void setMediaLength(long length) {
		webResponse.setContentLength((int)length);
	}

	public void setItem(MediaItem item) {
		if ( this.filename == null ) {
			this.filename = item.getName();
		}
	}

	public OutputStream getOutputStream() {
		if ( this.filename != null && this.download ) {
			// for download responses, add a filename header
			webResponse.setHeader(
				"Content-Disposition","attachment; filename=\"" 
				+this.filename+ "\"");
		}
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
	}

	public void setModifiedDate(long date) {
		// nothing here
	}

}
