/* ===================================================================
 * AlbumMediaCriteria.java
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
 * $Id: AlbumMediaCriteria.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao;

/**
 * Criteria object for album media DAO.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public interface AlbumMediaCriteria extends AbstractCriteria 
{	
	/** 
	 * Search type for finding all album media for an album. 
	 * 
	 * <p>Specify the album ID to search for with {@link #setQuery(Object)}.</p>
	 */
	public static final int ALBUM_MEDIA_FOR_ALBUM = 1;
	
	/**
	 * Search type for finding a set of media items within an album.
	 * 
	 * <p>Specify the album ID and media item IDs to search for as 
	 * an array of Integer objects via {@link #setQuery(Object)}. 
	 * The first Integer in the array will be used as the album ID
	 * and all others as media item IDs.</p>
	 */
	public static final int ALBUM_MEDIA_SUBSET = 2;
	
}
