/* ===================================================================
 * AlbumThemeCriteria.java
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
 * $Id: AlbumThemeCriteria.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao;

/**
 * Criteria object for album theme DAO.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public interface AlbumThemeCriteria extends AbstractCriteria 
{	
	/** 
	 * Search type for finding all album themes owned by a user. 
	 * 
	 * <p>Specify the user ID to search for with {@link #setQuery(Object)}.</p>
	 */
	public static final int SEARCH_BY_OWNER = 1;
	
	/** 
	 * Search type for finding all album themes globally accessible. 
	 */
	public static final int SEARCH_BY_GLOBAL = 2;
	
	/**
	 * Search type for finding all album themes viewable for a user.
	 * 
	 * <p>Specify the user ID to search for with {@link #setQuery(Object)}.</p>
	 */
	public static final int SEARCH_VIEWABLE_FOR_USER = 3;
	
	/**
	 * Search type for finding all album themes.
	 */
	public static final int SEARCH_ALL = 4;
	
	/** 
	 * Search type for finding all album themes globally accessible
	 * but ignoring a specified owner.
	 * 
	 * <p>Specify the user ID of the owner to ignore with {@link #setQuery(Object)}.</p>
	 */
	public static final int SEARCH_BY_GLOBAL_EXCEPT_USER = 5;
	
}
