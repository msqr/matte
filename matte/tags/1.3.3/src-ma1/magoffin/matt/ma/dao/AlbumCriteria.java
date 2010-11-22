/* ===================================================================
 * AlbumCriteria.java
 * 
 * Created Dec 15, 2003 8:30:46 AM
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
 * $Id: AlbumCriteria.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao;

/**
 * Criteria object for album DAO.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public interface AlbumCriteria extends AbstractCriteria {
	
	/** 
	 * Search type for finding all albums a user owns. 
	 * 
	 * <p>Specify the user ID to search for with {@link #setQuery(Object)}.</p>
	 */
	public static final int ALBUMS_FOR_OWNER = 1;
	
	/** 
	 * Search type for finding an album by its key. 
	 * 
	 * <p>Specify the key to search for with {@link #setQuery(Object)}.</p>
	 */
	public static final int ALBUM_FOR_KEY_SEARCH = 2;
	
	/** 
	 * Search type for finding children albums of an album. 
	 * 
	 * <p>Specify the ID of the parent album with {@link #setQuery(Object)}.</p>
	 */
	public static final int CHILDREN_OF_ALBUM_SEARCH = 3;
	
	/** 
	 * Search type for finding an album based on the album's key and the ID of 
	 * an item within the album. 
	 * 
	 * <p>Specify the item ID and the album key as an Object array with {@link #setQuery(Object)}.</p>
	 */
	public static final int ALBUM_FOR_ITEM_KEY = 4;
	
	/** 
	 * Search type for finding albums based on the album permissions
	 * allowing viewing by a user ID.
	 * 
	 * <p>Specify the user ID to search for with {@link #setQuery(Object)}.</p>
	 */
	public static final int VIEWABLE_BY_USER = 5;
	
	/** 
	 * Search type for finding albums based on the album permissions
	 * allowing viewing by a group ID.
	 * 
	 * <p>Specify the group ID to search for with {@link #setQuery(Object)}.</p>
	 */
	public static final int VIEWABLE_BY_GROUP = 6;
	
	/**
	 * Search type for finding count of items available for viewing by 
	 * one user of another user.
	 * 
	 * <p>Specify the owner ID and acting user ID as an Integer[] 
	 * with {@link #setQuery(Object)}</p>
	 */
	public static final int ITEM_COUNT_FOR_USER = 7;
	
	/**
	 * Search type for finding albums available for viewing by 
	 * one user of another user.
	 * 
	 * <p>Specify the owner ID and acting user ID as an Integer[] 
	 * with {@link #setQuery(Object)}</p>
	 */
	public static final int ALBUMS_FOR_USER = 8;
	
	/**
	 * Search type for finding albums that use any of a set of media items.
	 * 
	 * <p>Specify the media item IDs as an Integer[] 
	 * with {@link #setQuery(Object)}</p>
	 */
	public static final int ALBUMS_CONTAINING_ITEMS = 9;
	
	/**
	 * Search type for returning the count of albums using a particular
	 * theme.
	 */
	public static final int ALBUMS_USING_THEME_COUNT = 10;
	
	/**
	 * Search type for returning the count of albums using the default theme.
	 */
	public static final int ALBUMS_USING_DEFAULT_THEME_COUNT = 11;
	
}
