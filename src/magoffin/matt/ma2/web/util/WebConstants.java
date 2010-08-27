/* ===================================================================
 * WebConstants.java
 * 
 * Created Aug 10, 2004 10:58:25 AM
 * 
 * Copyright (c) 2005 Matt Magoffin (spamsqr@msqr.us)
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

import magoffin.matt.xweb.util.XwebConstants;

/**
 * Constants to share in the web world.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public final class WebConstants extends XwebConstants {
	
	/** Ignore errors. */
	public static final String IGNORE_ERRORS = "magoffin.matt.ma2.IGNORE_ERRORS";

	/** Save URL key. */
	public static final String REQ_KEY_SAVE_SAVED_URL = "magoffin.matt.ma2.URLsave";
	
	/** User session data. */
	public static final String SES_KEY_USER_SESSION_DATA = "magoffin.matt.ma2.USD";
	
	/** Session key for viewed MediaItem ID set. */
	public static final String SES_KEY_VIEWED_MEDIA_ITEMS = "magoffin.matt.ma2.ViewedItems";
	
	/** Session key for user's work tickets set. */
	public static final String SES_KEY_WORK_TICKETS = "magoffin.matt.ma2.WorkTickets";
	
	/** Request key for a theme resource content type. */
	public static final String REQ_KEY_THEME_RESOURCE_CONTENT_TYPE = "magoffin.matt.ma2.ThemeResourceContentType";
	
}
