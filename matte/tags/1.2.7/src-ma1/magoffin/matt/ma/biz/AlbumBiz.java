/* ===================================================================
 * AlbumBiz.java
 * 
 * Created Dec 16, 2003 7:36:14 PM
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
 * $Id: AlbumBiz.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.biz;

import java.util.Comparator;

import magoffin.matt.biz.Biz;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.NotAuthorizedException;
import magoffin.matt.ma.util.ComparatorUtil;
import magoffin.matt.ma.xsd.Album;
import magoffin.matt.ma.xsd.AlbumMedia;
import magoffin.matt.ma.xsd.AlbumPermissions;
import magoffin.matt.ma.xsd.AlbumTheme;
import magoffin.matt.ma.xsd.FreeData;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.ma.xsd.User;

/**
 * Biz interface for Media Album album maintenance.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public interface AlbumBiz extends Biz {
	
	/** Constant for unlimited descent in {@link #getMediaItemsForAlbum(Integer, int, boolean, User)} */
	public static final int UNLIMITED_DESCENT = -1;
	
	/** Sort by album and/or creation date. */
	public static final Comparator SORT_ALBUM_BY_DATE = new ComparatorUtil.AlbumDateSort();
	
/**
 * Get an album by its ID.
 * 
 * @param id the ID of the album to get
 * @param actingUser if non-null then will verify if user has permission to view album
 * @param allowCached if <em>true</em> then allow returning a cached album
 * @return the album
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if user not allowed to view album
 */
public Album getAlbumById(Integer id, User actingUser, boolean allowCached) 
throws MediaAlbumException, NotAuthorizedException;

/**
 * Get a set of albums by their IDs.
 * 
 * @param albumIds the album IDs of the albums to get
 * @param actingUser if non-null then will verify if user has permission to view album
 * @param allowCached if <em>true</em> then allow returning a cached album
 * @return the albums
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if user not allowed to view album
 */
public Album[] getAlbumsById(Integer[] albumIds, User actingUser, boolean allowCached) 
throws MediaAlbumException, NotAuthorizedException;

/**
 * Get an album by its key.
 * 
 * @param key the key of the album to get
 * @param actingUser the user wishing to view the album
 * @return the album
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if user not allowed to view album
 */
public Album getAlbumByKey(String key, User actingUser) 
throws MediaAlbumException, NotAuthorizedException;

/**
 * Delete an album.
 * 
 * <p>This method should call {@link #canUserDeleteAlbum(User, Integer)} and if
 * that returns false then throw a {@link NotAuthorizedException}
 * exception.</p>
 * 
 * @param id the ID of the album to delete
 * @param actingUser the user deleting the album
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException
 */
public void deleteAlbum(Integer id, User actingUser) 
throws MediaAlbumException, NotAuthorizedException;

/**
 * Get an album based on its key and only if it contains
 * a media item with a specified ID.
 * 
 * @param itemId the ID the album must contain
 * @param key the album key
 * @return the album, or <em>null</em> if album not available
 * @throws MediaAlbumException if an error occurs
 */
public Album getAlbumForItemKey(Integer itemId, String key) throws MediaAlbumException;

/**
 * Get all children albums for a given album ID.
 * 
 * @param albumId the parent album ID to get children for
 * @return array of albums, or <em>null</em> if none available
 * @throws MediaAlbumException if an error occurs
 */
public Album[] getAlbumChildren(Integer albumId) throws MediaAlbumException;

/**
 * Check if a user has permission to delete an album.
 * 
 * @param actingUser the user wishing to delete the album
 * @param albumId the ID of the album the user wants to delete
 * @return <em>true</em> if user has permission to delete album
 * @throws MediaAlbumException if an error occurs
 */
public boolean canUserDeleteAlbum(User actingUser, Integer albumId) throws MediaAlbumException;

/**
 * Check if a user has permission to view an album.
 * 
 * @param actingUser the user wishing to view the album
 * @param albumId the ID of the album the user wants to view
 * @return <em>true</em> if user has permission to view album
 * @throws MediaAlbumException if an error occurs
 */
public boolean canUserViewAlbum(User actingUser, Integer albumId) throws MediaAlbumException;

/**
 * Check if a user has permission to update an album.
 * 
 * @param actingUser the user wishing to update the album
 * @param albumId the ID of the album the user wants to update
 * @return <em>true</em> if user has permission to update album
 * @throws MediaAlbumException if an error occurs
 */
public boolean canUserUpdateAlbum(User actingUser, Integer albumId) throws MediaAlbumException;

/**
 * Save a new album to the backend.
 * 
 * @param album the album to create
 * @param actingUser the user creating the album
 * @return the album, with updated data from creation
 * @throws MediaAlbumException if an error occurs
 */
public Album createAlbum(Album album, User actingUser) throws MediaAlbumException;

/**
 * Update an exisitng album in the backend.
 * 
 * @param album the album to update
 * @param actingUser the user updating the album
 * @return the album, with updated data
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if user not allowed to view album
 */
public Album updateAlbum(Album album, User actingUser) 
throws MediaAlbumException, NotAuthorizedException;

// TODO canUserCreateAlbum(User actingUser)

/**
 * Get all media items for a particular album.
 * 
 * <p>The <var>itemPopulateMode</var> should be a bit-wise OR'ed combination 
 * of any of the <code>POPULATE_MODE_*</code> constants defined 
 * {@link magoffin.matt.ma.ApplicationConstants}.</p>
 * 
 * @param albumId the ID of the album to get the media items for
 * @param itemPopulateMode a bit mask of item population modes
 * @param allowCached if <em>true</em> then allow returning a cached set of items
 * @return array of media items, or <em>null</em> if album is empty
 * @throws MediaAlbumException
 */
public MediaItem[] getMediaItemsForAlbum(Integer albumId, int itemPopulateMode, boolean allowCached, User actingUser) 
throws MediaAlbumException;

/**
 * Set the media items for an album.
 * @param albumId the ID of the album to update
 * @param items the media items to set for the album
 * @param actingUser the user updating the album
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if user not allowed to view album
 */
public void setAlbumMediaItems(Integer albumId, AlbumMedia[] items, User actingUser)
throws MediaAlbumException, NotAuthorizedException;

/**
 * Get the theme for a given album.
 * 
 * @param albumId the ID of the album to get the theme for
 * @param allowCached if <em>true</em> then allow returning a cached set of items
 * @return the theme for the album
 * @throws MediaAlbumException if an error occurs
 */
public AlbumTheme getAlbumThemeForAlbum(Integer albumId, boolean allowCached)
throws MediaAlbumException;

/**
 * Get the permissions for an album.
 * 
 * @param albumId the ID of the album to get the permissions for
 * @param allowCached if <em>true</em> then allow returning a cached set of items
 * @return the permissions or <em>null</em> if none available
 * @throws MediaAlbumException if an error occurs
 */
public AlbumPermissions[] getAlbumPermissions(Integer albumId, boolean allowCached)
throws MediaAlbumException;

/**
 * Save permissions for an album.
 * 
 * <p>This will replace any permissions set for the album with those provided.</p>
 * 
 * @param albumId the album ID
 * @param permissions the permissions
 * @param actingUser the acting user
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if user not allowed to update album
 */
public void updateAlbumPermissions(
		Integer albumId,
		AlbumPermissions[] permissions,
		User actingUser)
throws MediaAlbumException, NotAuthorizedException;

/**
 * Popluate any  child albums for a given album.
 * 
 * <p>This will only add albums that the user has permission to view.</p>
 * 
 * @param album the album to get children albums for
 * @param fillInItemsAlbumId the ID of the album to populate media items into
 * @param itemPopulateMode a bit mask of item population modes
 * @param actingUser the acting user
 * @return the Album for <var>fillInItemsAlbumId</var>
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if user not allowed to update album
 * @see #getMediaItemsForAlbum(Integer, int, boolean, User)
 */
public Album fillInChildAlbums(Album album, Integer fillInItemsAlbumId, int itemPopulateMode, User actingUser, int maxLevels)
throws MediaAlbumException, NotAuthorizedException;

/**
 * Sort array of media items according to the sort mode of an album.
 * 
 * @param album the album
 * @param items the items to sort
 */
public void sortAlbumItems(Album album, MediaItem[] items) throws MediaAlbumException;


/**
 * Set the theme for an album.
 * 
 * @param themeId the theme ID
 * @param albumId the album ID
 * @param actingUser the acting user
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if the acting user does not have permission to 
 * set the album theme, or does not have permission to use the theme
 */
public void setAlbumTheme(Integer themeId, Integer albumId, User actingUser)
throws MediaAlbumException, NotAuthorizedException;

/**
 * Populate album FreeData for a set of albums.
 * 
 * @param albums the albums to populate data for
 * @param allowCached if <em>true</em> then allow using a cached objects
 * @throws MediaAlbumException if an error occurs
 */
public void populateAlbumFreeData(Album[] albums, boolean allowCached)
throws MediaAlbumException;

/**
 * Get the current free data for an album.
 * @param albumId the ID of the album to get the free data for
 * @param allowCached if <em>true</em> then allow using a cached objects
 * @return the free data (never <em>null</em>)
 * @throws MediaAlbumException if an error occurs
 */
public FreeData[] getFreeData(Integer albumId, boolean allowCached)
throws MediaAlbumException;

/**
 * Traverse an album heirarchy for a specific album and return that album.
 * 
 * @param albums the albums to search through (including children)
 * @param albumId the ID of the album to look for
 * @return the album, or <em>null</em> if not found
 */
public Album findAlbum(Album[] albums, Integer albumId) throws MediaAlbumException;

/**
 * Add media items to an album.
 * 
 * <p>If a media item is already in the album, it will not be added again.</p>
 * 
 * @param albumId the ID of the album to add the media items to
 * @param itemIds the IDs of the media items to add to the album
 * @param actingUser the acting user
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if the acting user does not have permission to 
 * add the items to the album, or does not have permission to view the items
 */
public void addMediaItemsToAlbum(Integer albumId, Integer[] itemIds,
		User actingUser)
throws MediaAlbumException, NotAuthorizedException;

/**
 * Remove media items from an album.
 * 
 * <p>If any of the <var>itemIds</var> are not in the album an error
 * will <em>not</em> be thrown.</p>
 * 
 * @param albumId the album ID to remove from
 * @param itemIds the media item IDs to remove
 * @param actingUser the acting user
 * @return the number of items actually removed
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if the acting user does not have permission to 
 * add the items to the album, or does not have permission to view the items
 */
public int removeMediaItemsFromAlbum(Integer albumId, Integer[] itemIds,
		User actingUser)
throws MediaAlbumException, NotAuthorizedException;

}
