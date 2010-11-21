/* ===================================================================
 * FreeDataCriteria.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Mar 24, 2004 10:56:22 AM.
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
 * $Id: FreeDataCriteria.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao;

/**
 * Criteria interface for FreeData DAO.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public interface FreeDataCriteria extends AbstractCriteria 
{
	/** 
	 * Search type for finding all free data for a user. 
	 * 
	 * <p>Specify the user ID to search for with {@link #setQuery(Object)}.</p>
	 */
	public static final int FREE_DATA_FOR_OWNER = 1;

	/** 
	 * Search type for finding all free data for a set of media items. 
	 * 
	 * <p>Specify the media item IDs to search for as an array 
	 * of Integer objects with {@link #setQuery(Object)}.</p>
	 */
	public static final int FREE_DATA_FOR_ITEMS = 2;

	/** 
	 * Search type for finding all free data for a media item. 
	 * 
	 * <p>Specify the media item ID to search for with 
	 * {@link #setQuery(Object)}.</p>
	 */
	public static final int FREE_DATA_FOR_ITEM = 3;

	/** 
	 * Search type for finding all free data for a set of albums. 
	 * 
	 * <p>Specify the album IDs to search for as an array 
	 * of Integer objects with {@link #setQuery(Object)}.</p>
	 */
	public static final int FREE_DATA_FOR_ALBUMS = 4;
	
	/** 
	 * Search type for finding all free data for an album. 
	 * 
	 * <p>Specify the album ID to search for with 
	 * {@link #setQuery(Object)}.</p>
	 */
	public static final int FREE_DATA_FOR_ALBUM = 5;

	/**
	 * Search type for finding all free data for users 
	 * watching for notifications from another user. 
	 * 
	 * <p>Specify the watched user ID with 
	 * {@link #setQuery(Object)}.</p>
	 */
	public static final int FREE_DATA_FOR_WATCHING_USER = 6;
}
