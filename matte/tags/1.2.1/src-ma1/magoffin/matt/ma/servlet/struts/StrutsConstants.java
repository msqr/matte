/* ===================================================================
 * StrutsConstants.java
 * 
 * Created Apr 16, 2004 10:12:43 AM
 * 
 * Copyright (c) 2004 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: StrutsConstants.java,v 1.1 2006/06/03 22:26:19 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.struts;

/**
 * Constants for Struts components.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:19 $
 */
public final class StrutsConstants
{
	/** The name of the default forward for a successful action: <code>ok</code> */
	public static final String DEFAULT_OK_FORWARD = "ok";
	
	/** The name of the default forward for an action that fails: <code>err</code> */
	public static final String DEFAULT_ERROR_FORWARD = "err";
	
	/** The name of the default forward for an action that cancels: <code>cancel</code> */
	public static final String DEFAULT_CANCEL_FORWARD = "cancel";
	
	/** The name of the global MediaServer forward: <code>media-server</code> */
	public static final String MEDIA_SERVER_FORWARD = "media-server";
	
	/** The name of the global forward to view an album slideshow: <code>view-album</code> */
	public static final String ALBUM_SLIDESHOW_FORWARD = "view-album";
	
	/** The name of the global forward to browse albums: <code>browse-albums</code> */
	public static final String BROWSE_ALBUMS_FORWARD = "browse-albums";
	
	/** The name of the forward to search: <code>search</code> */
	public static final String SEARCH_FORWARD = "search";
}
