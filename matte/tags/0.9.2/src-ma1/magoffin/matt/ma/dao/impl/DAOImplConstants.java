/* ===================================================================
 * DAOImplConstants.java
 * 
 * Created Dec 2, 2003 9:54:05 PM
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
 * $Id: DAOImplConstants.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao.impl;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.util.config.Config;

/**
 * Constants for DAO implementations.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public class DAOImplConstants {

	/** The album table reference key: <code>album</code> */
	public static final String ALBUM_TABLE_REFERENCE_KEY = "album";
	
	/** The album media table reference key: <code>media</code> */
	public static final String ALBUM_MEDIA_TABLE_REFERENCE_KEY = "media";
	
	/** The album permissions table reference key: <code>aperm</code> */
	public static final String ALBUM_PERMISSIONS_TABLE_REFERENCE_KEY = "aperm";
	
	/** The album theme table reference key: <code>theme</code> */
	public static final String ALBUM_THEME_TABLE_REFERENCE_KEY = "theme";
	
	/** The collection table reference key: <code>collection</code> */
	public static final String COLLECTION_TABLE_REFERENCE_KEY = "collection";

	/** The free data table reference key: <code>fdata</code> */
	public static final String FREE_DATA_TABLE_REFERENCE_KEY = "fdata";

	/** The free data types table reference key: <code>fdatatype</code> */
	public static final String FREE_DATA_TYPES_TABLE_REFERENCE_KEY = "fdatatype";

	/** The friend table reference key: <code>friend</code> */
	public static final String FRIEND_TABLE_REFERENCE_KEY = "friend";

	/** The group table reference key: <code>group</code> */
	public static final String GROUP_TABLE_REFERENCE_KEY = "group";
	
	/** The invitation table reference key: <code>invitation</code> */
	public static final String INVITATION_TABLE_REFERENCE_KEY = "invitation";
	
	/** The lightbox table reference key: <code>lightbox</code> */
	public static final String LIGHTBOX_TABLE_REFERENCE_KEY = "lightbox";
	
	/** The media item table reference key: <code>item</code> */
	public static final String MEDIA_ITEM_TABLE_REFERENCE_KEY = "item";

	/** The media item comment table reference key: <code>comment</code> */
	public static final String MEDIA_ITEM_COMMENT_TABLE_REFERENCE_KEY = "comment";

	/** The media item rating table reference key: <code>rating</code> */
	public static final String MEDIA_ITEM_RATING_TABLE_REFERENCE_KEY = "rating";

	/** The member table reference key: <code>member</code> */
	public static final String MEMBER_TABLE_REFERENCE_KEY = "member";

	/** The permission table reference key: <code>permission</code> */
	public static final String PERMISSIONS_TABLE_REFERENCE_KEY = "permission";
	
	/** The registration table reference key: <code>register</code> */
	public static final String REGISTRATION_TABLE_REFERENCE_KEY = "register";
	
	/** The user table reference key: <code>user</code> */
	public static final String USER_TABLE_REFERENCE_KEY = "user";
	
	/** The user category types table reference key: <code>ucattype</code> */
	public static final String USER_CATEGORY_TYPES_TABLE_REFERENCE_KEY = "ucattype";

	/** 
	 * The message key to match for a duplicate key SQL error from 
	 * {@link magoffin.matt.ma.ApplicationConstants#CONFIG_ENV}:
	 * <code>sql.duplicate.key.match</code>
	 */
	public static final String SQL_DUPLICATE_KEY_ERROR_MATCH = 
			Config.get(ApplicationConstants.CONFIG_ENV,"sql.duplicate.key.match");
	
}
