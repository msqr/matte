/* ===================================================================
 * CollectionDao.java
 * 
 * Created Mar 2, 2006 7:14:27 PM
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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2.dao;

import java.util.List;

import magoffin.matt.dao.BatchableDao;
import magoffin.matt.dao.GenericDao;
import magoffin.matt.ma2.domain.MediaItem;

/**
 * DAO for MediaItem objects.
 * 
 * @author matt.magoffin
 * @version $Revision$ $Date$
 */
public interface MediaItemDao extends GenericDao<MediaItem, Long>, BatchableDao<MediaItem> {
	
	/** The batch processing name to process a list of MediaItem objects by ID. */
	static final String BATCH_NAME_PROCESS_MEDIA_IDS = "batch.media.ids";
	
	/** The batch processing name to index MediaItem objects. */
	static final String BATCH_NAME_INDEX = "batch.index";
	
	/**
	 * The batch processing parameter key for a List of MediaItem IDs to process.
	 */
	static final String BATCH_PROCESS_PARAM_MEDIA_IDS_LIST = "media.ids";
	
	/**
	 * Find a MediaItem by its path and a specific collection.
	 * 
	 * @param collectionId the ID of the collection the item is in
	 * @param path the item's path
	 * @return the found item, or <em>null</em> if not found
	 */
	MediaItem getItemForPath(Long collectionId, String path);
	
	
	/**
	 * Find all MediaItems for a specific collection.
	 * 
	 * @param collectionId the ID of the collection
	 * @return the MediaItems of the collection
	 */
	List<MediaItem> findItemsForCollection(Long collectionId);
	
	/**
	 * Find all MediaItems for a specific album.
	 * 
	 * @param albumId the ID of the album
	 * @return the MediaItems of the album
	 */
	List<MediaItem> findItemsForAlbum(Long albumId);
	
	/**
	 * Find all MediaItems for a specific collection and 
	 * then remove them from any album they reside in.
	 * 
	 * @param collectionId the ID of the collection
	 * @return the media items removed
	 */
	List<MediaItem> removeItemsOfCollectionFromAlbums(Long collectionId);
	
	/**
	 * Remove a set of MediaItems from any albums they reside in.
	 * 
	 * @param itemIds the IDs of the items to remove from albums
	 * @return the media items removed
	 */
	List<MediaItem> removeItemsFromAlbums(Long[] itemIds);
	
	/**
	 * Remove a set of media items from the collections they are in.
	 * 
	 * @param itemIds the IDs of the items to remove from collections
	 * @return the media items removed
	 */
	List<MediaItem> removeItemsFromCollections(Long[] itemIds);

	/**
	 * Get a MediaItem fully populated (with metadata, etc).
	 * 
	 * @param itemId the ID of the item to get
	 * @return the media item
	 */
	MediaItem getMediaItemWithInfo(Long itemId);
}
