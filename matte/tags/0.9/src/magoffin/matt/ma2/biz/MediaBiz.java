/* ===================================================================
 * MediaBiz.java
 * 
 * Created Mar 2, 2006 9:27:59 PM
 * 
 * Copyright (c) 2006 Matt Magoffin.
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
 * $Id: MediaBiz.java,v 1.40 2007/09/10 00:10:51 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.biz;

import java.io.File;
import java.util.List;

import magoffin.matt.ma2.MediaHandler;
import magoffin.matt.ma2.MediaQuality;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.MediaSize;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.KeyNameType;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.support.Geometry;
import magoffin.matt.ma2.support.MediaInfoCommand;
import magoffin.matt.ma2.support.MoveItemsCommand;
import magoffin.matt.ma2.support.ShareAlbumCommand;
import magoffin.matt.ma2.support.SortAlbumsCommand;
import magoffin.matt.ma2.support.SortMediaItemsCommand;
import magoffin.matt.ma2.support.UserCommentCommand;

import org.springframework.core.io.Resource;

/**
 * API for media item actions.
 * 
 * @author matt.magoffin
 * @version $Revision: 1.40 $ $Date: 2007/09/10 00:10:51 $
 */
public interface MediaBiz {
	
	/**
	 * Return <em>true</em> if a file is supported by some
	 * configured {@link MediaHandler}.
	 * 
	 * @param file the file
	 * @return boolean
	 */
	boolean isFileSupported(File file);
	
	/**
	 * Get a MediaHandler instance for a given file.
	 * 
	 * @param file the file
	 * @return MediaHandler instance
	 * @throws IllegalArgumentException if the file is not supported
	 */
	MediaHandler getMediaHandler(File file);
	
	/**
	 * Get a MediaHandler instance for a given MIME type.
	 * 
	 * @param mime the MIME type
	 * @return MediaHandler instance
	 * @throws IllegalArgumentException if the file is not supported
	 */
	MediaHandler getMediaHandler(String mime);
	
	/**
	 * Get the Collection for a given MediaItem.
	 * 
	 * @param item the item
	 * @return the Collection
	 */
	Collection getMediaItemCollection(MediaItem item);
	
	/**
	 * Get all MediaItems that belong to a given Collection.
	 * 
	 * @param collection the Collection
	 * @param context the current context
	 * @return the list of items
	 */
	List<MediaItem> getMediaItemsForCollection(Collection collection, 
			BizContext context);
	
	/**
	 * Get a Collection along with the MediaItems of that collection.
	 * 
	 * @param collectionId the ID of the Collection to get
	 * @param context the current context
	 * @return the Collection
	 */
	Collection getCollectionWithItems(Long collectionId, BizContext context);
	
	/**
	 * Get an Album along with the MediaItems of that album.
	 * 
	 * @param albumId the ID of the Album to get
	 * @param context the current context
	 * @return the Album
	 */
	Album getAlbumWithItems(Long albumId, BizContext context);
	
	/**
	 * Get a MediaItem along with the meta data and other info populated.
	 * 
	 * @param itemId the ID of the item to get
	 * @param context the current context
	 * @return the MediaItem
	 */
	MediaItem getMediaItemWithInfo(Long itemId, BizContext context);
	
	/**
	 * Set the rating for a media item.
	 * 
	 * @param itemIds the IDs of the items to set the rating for
	 * @param rating the rating to set
	 * @param context the current context
	 */
	void storeMediaItemRating(Long[] itemIds, short rating, BizContext context);
	
	/**
	 * Store info for media items.
	 * 
	 * @param command the command data
	 * @param context the current context
	 */
	void storeMediaItemInfo(MediaInfoCommand command, BizContext context);
	
	/**
	 * Store the poster item for an album.
	 * 
	 * @param itemId the ID of the item to use as the poster
	 * @param albumId the ID of the album to set the poster for
	 * @param context the current context
	 */
	void storeMediaItemPoster(Long itemId, Long albumId, BizContext context);
	
	/**
	 * Delete a Collection and physically delete the media items in the 
	 * collection.
	 * 
	 * @param collectionId the ID of the collection to delete
	 * @param context the current context
	 * @return the media items deleted
	 */
	public List<MediaItem> deleteCollectionAndItems(Long collectionId, BizContext context);
	
	/**
	 * Move a set of media items from the collection they currently are in to 
	 * a different collection.
	 * 
	 * @param command the command object
	 * @param context the current context
	 */
	public void moveMediaItems(MoveItemsCommand command, BizContext context);
	
	/**
	 * Delete an album, and any nested albums.
	 * 
	 * @param albumId the ID of the album to delete
	 * @param context the current context
	 * @return the deleted album, or <em>null</em> if the album was not found
	 */
	public Album deleteAlbum(Long  albumId, BizContext context);
	
	/**
	 * Remove a list of item IDs from an Album.
	 * 
	 * @param albumId the ID of the album to remove the items from
	 * @param itemIds the IDs of the items to remove
	 * @param context the current context
	 * @return the number of items removed
	 */
	public int removeMediaItemsFromAlbum(Long albumId, Long[] itemIds, BizContext context);
	
	/**
	 * Physically delete media items.
	 * 
	 * @param itemIds the media item IDs to delete
	 * @param context the current context
	 * @return the number of items deleted
	 */
	public int deleteMediaItems(Long[] itemIds, BizContext context);
	
	/**
	 * Get all MediaItems that belong to a given Album.
	 * 
	 * @param album the Album
	 * @param context the current context
	 * @return the list of items
	 */
	List<MediaItem> getMediaItemsForAlbum(Album album, BizContext context);
	
	/**
	 * Get a Resource for a given MediaItem.
	 * 
	 * @param item the item
	 * @return the Resource
	 */
	Resource getMediaItemResource(MediaItem item);
	
	/**
	 * Get a geometry for a given size.
	 * 
	 * @param size the size
	 * @return the geometry
	 */
	Geometry getGeometry(MediaSize size);
	
	/**
	 * Get a quality value, where the value ranges from <kbd>0.0</kbd> to 
	 * <kdb>1.0</kbd> and <kdb>1.0</kbd> represents the hightest quality possible.
	 * 
	 * @param quality the quality
	 * @return the quality value
	 */
	float getQualityValue(MediaQuality quality);
	
	/**
	 * Creates a Geometry scaled to the desired {@link MediaSize}
	 * set on the request.
	 * 
	 * @param item the media item for the current request
	 * @param request the current request
	 * @return the scaled Geometry
	 */
	public Geometry getScaledGeometry(MediaItem item, MediaRequest request);
	
	/**
	 * Add a set of media items to an album.
	 * 
	 * @param album the album
	 * @param mediaItemIds the IDs of the media items to add
	 * @param context the context
	 * @return the number of items added to the album
	 */
	public int addMediaItemsToAlbum(Album album, Long[] mediaItemIds, BizContext context);
	
	/**
	 * Sort the media items within an album based on the Albums' sort mode.
	 * @param album the album to sort
	 */
	public void sortAlbumItems(Album album);
	
	/**
	 * Share an album.
	 * 
	 * @param command the share album command
	 * @param context the context
	 * @return the anonymous key used to view the shared album
	 */
	public String shareAlbum(ShareAlbumCommand command, BizContext context);
	
	/**
	 * Unshare an album.
	 * @param albumId the ID of the album to unshare
	 * @param context the context
	 */
	public void unShareAlbum(Long albumId, BizContext context);
	
	/**
	 * Get a shared album.
	 * 
	 * @param key the anonymous key of the album
	 * @param context the context
	 * @return the album
	 */
	public Album getSharedAlbum(String key, BizContext context);
	
	/**
	 * Get an Album.
	 * 
	 * <p>The Album may not have any MediaItem instances populated
	 * in it. Use the {@link #getAlbumWithItems(Long, BizContext)}
	 * method to get an Album with its items at the same time.</p>
	 * 
	 * @param albumId the ID of the Album to get
	 * @param context the context
	 * @return the Album
	 */
	public Album getAlbum(Long albumId, BizContext context);
	
	/**
	 * Get the parent of an album, if one exists.
	 * 
	 * @param childAlbumId the ID of the child album to get the parent of
	 * @param context the context
	 * @return the parent album, or <em>null</em> if album has no parent
	 */
	public Album getAlbumParent(Long childAlbumId, BizContext context);
	
	/**
	 * Store an Album.
	 * 
	 * <p>This method will accept new albums as well as updates 
	 * to existing albums.</p>
	 * 
	 * @param album the Album to store
	 * @param context the context
	 * @return the ID of the saved album
	 */
	public Long storeAlbum(Album album, BizContext context);
	
	/**
	 * Make an album a child of another album.
	 * 
	 * <p>If <code>parentAlbumId</code> is <em>null</em> then the album
	 * for <code>childAlbumId</code> should be made a top-level album.</p>
	 * 
	 * @param childAlbumId the ID of the child album
	 * @param parentAlbumId the ID of the parent album
	 * @param context the context
	 */
	public void storeAlbumParent(Long childAlbumId, Long parentAlbumId, BizContext context);
	
	/**
	 * Set the ording for the child albums of an album.
	 * 
	 * @param command the sort command
	 * @param context the context
	 */
	public void storeAlbumOrdering(SortAlbumsCommand command, BizContext context);
	
	/**
	 * Set the ording for the media items of an album.
	 * 
	 * @param command the sort command
	 * @param context the context
	 */
	public void storeMediaItemOrdering(SortMediaItemsCommand command, BizContext context);
	
	/**
	 * Add a new {@link magoffin.matt.ma2.domain.UserComment} to a MediaItem.
	 * 
	 * @param command user comment command
	 * @param context the context
	 */
	public void storeMediaItemUserComment(UserCommentCommand command, BizContext context);
	
	/**
	 * Increment the hits value for a particular MediaItem object
	 * and return the incremented value.
	 * 
	 * @param itemId the ID of the item to increment
	 * @return the incremented value
	 */
	int incrementMediaItemHits(Long itemId);
	
	/**
	 * Get the list of support album sort types.
	 * 
	 * <p>This is to allow the available sort types to be displayed on the 
	 * front-end.</p>
	 * 
	 * @param context the context
	 * @return list of sort types (never null)
	 */
	List<KeyNameType> getAlbumSortTypes(BizContext context);
	
}
