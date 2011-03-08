/* ===================================================================
 * MessageConstants.java
 * 
 * Copyright (c) 2003 Matt Magoffin.
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
 * $Id: MessageConstants.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma;

/**
 * Static constants for application message keys.
 * 
 * <p>Created Feb 7, 2003 2:14:26 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public final class MessageConstants 
{
	/** Error when user not authorized to update a Album object. */
	public static final String ERR_AUTH_UPDATE_ALBUM = "update.album.forbidden";

	/** Error when user not authorized to update a Collection object. */
	public static final String ERR_AUTH_UPDATE_COLLECTION = "update.collection.forbidden";

	/** Error when user not authorized to update a MediaItem object. */
	public static final String ERR_AUTH_UPDATE_ITEM = "update.item.forbidden";

	/** Error when user not authorized to update an album theme. */
	public static final String ERR_AUTH_UPDATE_THEME = "update.theme.forbidden";
	
	/** Error when user not authorized to view a Album object. */
	public static final String ERR_AUTH_VIEW_ALBUM = "view.album.forbidden";

	/** Error when user not authorized to view a Collection object. */
	public static final String ERR_AUTH_VIEW_COLLECTION = "view.collection.forbidden";
	
	/** Error when user not authorized to view a MediaItem object. */
	public static final String ERR_AUTH_VIEW_MEDIA_ITEM = "view.item.forbidden";

	/** Error when user not authorized to view an album theme. */
	public static final String ERR_AUTH_VIEW_THEME = "view.theme.forbidden";
	
	/** Error when admin is required for an action. */
	public static final String ERR_AUTH_ADMIN_REQUIRED = "admin.required";

	/** General error for a DAOException; accepts a single parameter.  */
	public static final String ERR_DAO_GENERAL = "error.dao";
	
	/** Error for an uncaught exception; accepts a single parameter. */
	public static final String ERR_UNKNOWN = "error.unknown";
	
	/** Error for a user session (not logged in) error. */
	public static final String ERR_USER_SESSION = "user.session.error";
	
	/** 
	 * Error for when user attempts to load a browse page for an album ID 
	 * that is not available to them and perhaps they need to log on.
	 */
	public static final String MSG_BROWSE_ALBUM_NOT_AVAILABLE = 
		"browse.album.unavailable.logon";
	
	/** Message key for user's email. */
	public static final String USER_EMAIL = "user.form.email.displayName";
	
	/** Message key for user's name. */
	public static final String USER_NAME = "user.form.name.displayName";
	
	/** Message key for user's password. */
	public static final String USER_PASSWORD = "user.form.password.displayName";
	
	/** Message key for user's username. */
	public static final String USER_USERNAME = "user.form.username.displayName";
	
	/** The date/time user input format pattern. */
	public static final String DATE_TIME_FORMAT_PATTERN = "datetime.input.format";
	
	/** The date user input format pattern. */
	public static final String DATE_FORMAT_PATTERN = "date.input.format";
	
} 
