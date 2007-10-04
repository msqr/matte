/* ===================================================================
 * AlbumDao.java
 * 
 * Created Sep 19, 2005 2:39:19 PM
 * 
 * Copyright (c) 2005 Matt Magoffin.
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
 * $Id: AlbumDao.java,v 1.17 2007/09/20 05:05:35 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.dao;

import java.util.Calendar;
import java.util.List;

import magoffin.matt.dao.GenericDao;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.Theme;

/**
 * DAO for Album objects.
 * 
 * @author matt.magoffin
 * @version $Revision: 1.17 $ $Date: 2007/09/20 05:05:35 $
 */
public interface AlbumDao  extends GenericDao<Album,Long> {

	/**
	 * Find all top-level albums owned by a given user.
	 * 
	 * @param userId the ID of the user to find the albums for
	 * @return list of found albums, or empty List if none found
	 */
	List<Album> findAlbumsForUser(Long userId);
	
	/**
	 * Find top-level albums owned by a given user, ordered in descending order
	 * by date, with an optional maximum number of albums to return.
	 * 
	 * @param userId the ID of the user to find the albums for
	 * @param max the maximum number of albums to return, if greater
	 * than zero (otherwise return all albums)
	 * @param anonymousOnly if <em>true</em> then only get albums which allow 
	 * anonymous access
	 * @param browseOnly if <em>true</em> then only get albums which allow
	 * browse access
	 * @param feedOnly if <em>true</em> then only get albums which allow
	 * feed access
	 * @return list of found albums, or empty list if none found
	 */
	List<Album> findAlbumsForUserByDate(Long userId, int max, 
			boolean anonymousOnly, boolean browseOnly, boolean feedOnly);
	
	
	/**
	 * Find top-level albums owned by a given user, ordered in descending order
	 * by date, for all albums created on or since a given date.
	 * 
	 * @param userId the ID of the user to find the albums for
	 * @param since the date to get albums since
	 * @param anonymousOnly if <em>true</em> then only get albums which allow 
	 * anonymous access
	 * @param browseOnly if <em>true</em> then only get albums which allow
	 * browse access
	 * @param feedOnly if <em>true</em> then only get albums which allow
	 * feed access
	 * @return list of found albums, or empty list if none found
	 */
	List<Album> findAlbumsForUserByDate(Long userId, Calendar since, 
			boolean anonymousOnly, boolean browseOnly, boolean feedOnly);
	
	/**
	 * Find top-level albums owned by a given user named a particular name.
	 * 
	 * @param userId the ID of the user to find the albums for
	 * @param name the name of the album to find
	 * @return list of found albums, or empty list if none found
	 */
	List<Album> findAlbumsForUserAndName(Long userId, String name);
	
	/**
	 * Get an Album for an anonymous key.
	 * 
	 * @param anonymousKey the anonymous key
	 * @return the found Album, or <em>null</em> if not found
	 */
	Album getAlbumForKey(String anonymousKey);
	
	/**
	 * Get the parent album for another album.
	 * 
	 * @param childAlbumId the child album ID
	 * @return the parent Album, or <em>null</em> if the album
	 * has no parent
	 */
	Album getParentAlbum(Long childAlbumId);
	
	/**
	 * Get an Album with it's items fully populated.
	 * 
	 * @param albumId the ID of the album
	 * @return the Album, or <em>null</em> if not found
	 */
	Album getAlbumWithItems(Long albumId);
	
	/**
	 * Reassign all Albums using a particular Theme to a new
	 * Theme.
	 * 
	 * @param oldTheme the Theme to search for
	 * @param newTheme the new Theme to replace the old Theme with
	 * @return the number of Albums updated
	 */
	int reassignAlbumsUsingTheme(Theme oldTheme, Theme newTheme);
	
	/**
	 * Get a list of all Albums that contain a given MediaItem and are
	 * shared.
	 * 
	 * @param item the item
	 * @return list of albums, or empty list if none found
	 */
	List<Album> findSharedAlbumsContainingItem(MediaItem item);

}
