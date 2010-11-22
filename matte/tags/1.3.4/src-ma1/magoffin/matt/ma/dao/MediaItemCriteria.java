/* ===================================================================
 * MediaItemCriteria.java
 * 
 * Created Dec 22, 2003 12:50:45 PM
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
 * $Id: MediaItemCriteria.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao;

/**
 * Criteria for media item.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public interface MediaItemCriteria extends AbstractCriteria 
{
	/** 
	 * Search for a media items for a collection.
	 * 
	 * <p>Specify the collection ID with {@link #setQuery(Object)}.</p>
	 */
	public static final int ITEMS_FOR_COLLECTION = 1;
	
	/** 
	 * Search for a media items for an album.
	 * 
	 * <p>Specify the album ID with {@link #setQuery(Object)}.</p>
	 */
	public static final int ITEMS_FOR_ALBUM = 2;
	
	/** 
	 * Search for a media items by its collection ID and path.
	 * 
	 * <p>Specify the collection ID and path as an Object array with {@link #setQuery(Object)}.</p>
	 */
	public static final int ITEM_FOR_PATH = 3;
	
	/**
	 * The update criteria for updating the hit data for an item.
	 */
	public static final int UPDATE_FOR_HITS = 4;
	
	/**
	 * The update criteria for updating the time zone for an item.
	 */
	public static final int UPDATE_FOR_TIMEZONE = 5;
	
	/**
	 * Search for all media items viewable by one user of another user.
	 * 
	 * <p>Specify the owner ID and acting user ID as an Integer[] 
	 * with {@link #setQuery(Object)}</p>
	 */
	public static final int ITEMS_VIEWABLE_FOR_USER = 6;
	
	/**
	 * Return a count of all media items viewable by one user of another user.
	 * 
	 * <p>Specify the owner ID and acting user ID as an Integer[] 
	 * with {@link #setQuery(Object)}</p>
	 */
	public static final int ITEMS_COUNT_VIEWABLE_FOR_USER = 7;
	
	/**
	 * Return a count of either 0 or 1 for a specific item if it 
	 * is viewable by one user of another user.
	 * 
	 * <p>Specify the media item ID, owner ID and acting user ID as an Integer[] 
	 * with {@link #setQuery(Object)}</p>
	 */
	public static final int IS_ITEM_VIEWABLE_FOR_USER = 8;

	/** Search to return all media items. */
	public static final int ALL_MEDIA_ITEMS = 9;

	/** Search to return all media items specially for indexing. */
	public static final int ALL_MEDIA_ITEMS_INDEX = 10;
	
	/** The update criteria for updating the custom type for an item. */
	public static final int UPADTE_FOR_CUSTOM_TYPE = 11;
	
	/** 
	 * Search to return the total size value for all media items owned by a user.
	 * 
	 * <p>Specify the user ID with {@link #setQuery(Object)}.</p>
	 */
	public static final int TOTAL_SIZE_FOR_OWNER = 12;
}
