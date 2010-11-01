/* ===================================================================
 * HttpMediaResponse.java
 * 
 * Copyright (c) 2003-4 Matt Magoffin. Created Mar 3, 2003.
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
 * $Id: HttpMediaResponse.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaResponse;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.util.config.Config;

import javax.servlet.http.HttpServletResponse;

/**
 * MediaResponse implementation for HTTP.
 * 
 * <p>Created Mar 3, 2003 8:01:11 AM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class HttpMediaResponse implements MediaResponse 
{
	private static final String HTTP_CACHE_CONTROL = Config.get(
			ApplicationConstants.CONFIG_ENV,"http.cache.control");
	
	private HttpServletResponse response;
	private MediaItem item;

public void setHttpServletResponse(HttpServletResponse response)
{
	this.response = response;
}

public HttpServletResponse getHttpServletResponse()
{
	return response;
}

public MediaItem getItem() {
	return item;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaResponse#setItem(magoffin.matt.ma.xsd.MediaItem)
 */
public void setItem(MediaItem item) {
	this.item = item;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaResponse#setMimeType(java.lang.String)
 */
public void setMimeType(String mime) {
	response.setContentType(mime);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaResponse#setMediaLength(long)
 */
public void setMediaLength(long length) {
	response.setContentLength((int)length);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaResponse#reset()
 */
public void reset() {
	this.response = null;
	this.item = null;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaResponse#setModifiedDate(long)
 */
public void setModifiedDate(long date) {
	response.setDateHeader("Last-Modified",date);
	response.setHeader("Cache-Control",HTTP_CACHE_CONTROL);
}

}
