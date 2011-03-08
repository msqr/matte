/* ===================================================================
 * ItemRatingCriteria.java
 * 
 * Created Feb 23, 2004 2:34:04 PM
 * 
 * Copyright (c) 2004 Matt Magoffin.
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
 * $Id: ItemRatingCriteria.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao;

/**
 * Criteria interface for ItemRating DAO.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public interface ItemRatingCriteria extends AbstractCriteria
{
	/** 
	 * Search type for finding all ratings for an item. 
	 * 
	 * <p>Specify the item ID to search for with 
	 * {@link #setQuery(Object)}.</p>
	 */
	public static final int RATINGS_FOR_ITEM_SEARCH = 1;
	
	/** 
	 * Search type for finding all ratings for multiple items. 
	 * 
	 * <p>Specify the item IDs to search for as an array 
	 * with {@link #setQuery(Object)}.</p>
	 */
	public static final int RATINGS_FOR_ITEMS_SEARCH = 2;
	
	/**
	 * Search type for finding a rating for an item and user ID.
	 * 
	 * <p>Specify the item ID and user ID as an array
	 * with {@link #setQuery(Object)}.</p>
	 */
	public static final int RATING_FOR_USER = 3;
}
