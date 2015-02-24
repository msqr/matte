/* ===================================================================
 * CollectionBiz.java
 * 
 * Created Dec 14, 2003 12:40:08 PM
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
 * $Id: CollectionBiz.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.biz;

import java.io.File;
import java.util.Comparator;

import magoffin.matt.biz.Biz;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.NotAuthorizedException;
import magoffin.matt.ma.util.ComparatorUtil;
import magoffin.matt.ma.xsd.Collection;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.ma.xsd.User;

/**
 * Biz interface for Media Album collection maintenance.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public interface CollectionBiz extends Biz {
	
	/** 
	 * The error message key for trying to move collection items to the collection being 
	 * deleted: <code>collection.delete.error.moveself</code> 
	 */
	public static final String ERROR_DELETE_MOVE_TO_SELF = "collection.delete.error.moveself";
	
	/**
	 * Comparator to sort collections by name (ignoring case).
	 */
	public static final Comparator SORT_COLLECTION_BY_NAME = new ComparatorUtil.CollectionNameSort();

/**
 * Get a collection by its ID.
 * 
 * @param id the ID of the collection to get
 * @param allowCached if <em>true</em> then allow returning a cached collection
 * @return the collection
 * @throws MediaAlbumException if an error occurs
 */
public Collection getCollectionById(Integer id, boolean allowCached) throws MediaAlbumException;

/**
 * Save a new collection to the backend.
 * @param name the name of the collection
 * @param owner the owner of the collection 
 * @return the newly saved collection
 * @throws MediaAlbumException if an error occurs
 */
public Collection createCollection(String name, User owner) throws MediaAlbumException;

/**
 * Save changes to an existing collection to the backend.
 * @param collection the collection to save
 * @throws MediaAlbumException if an error occurs
 */
public void updateCollection(Collection collection) throws MediaAlbumException;

/**
 * Get all collections in the system.
 * @return array of collections
 * @throws MediaAlbumException
 */
public Collection[] getAllCollections() throws MediaAlbumException;

/**
 * Get all media items for a particular collection.
 * 
 * @param collectionId the ID of the collection to get the media items for
 * @param allowCached if <em>true</em> then allow returning a cached set of items
 * @return array of media items, or <em>null</em> if collection is empty
 * @throws MediaAlbumException
 */
public MediaItem[] getMediaItemsForCollection(Integer collectionId, boolean allowCached, User actingUser) 
throws MediaAlbumException;

/**
 * Delete a collection.
 * 
 * <p>When deleting a collection there are two ways to handle any media items that are
 * currently in the collection:</p>
 * 
 * <ol>
 * <li>Move all media items to another collection</li>
 * <li>Delete all media items</li>
 * </ol>
 * 
 * <p>To perform the first, pass <em>true</em> as the <var>move</var> parameter,
 * and provide the ID of the collection to move the items to with
 * <var>moveToCollectionId</var>. To perform the second pass <em>false</em>
 * as the <var>move</var> parameter.</p>
 * 
 * @param collectionId the collection to delete
 * @param move if <em>true</em> then move all media items within the collection to
 * another collection specified by <var>moveToCollectionId</var>, otherwise delete
 * any media items within the collection
 * @param moveToCollectionId if <var>move</var> is <em>true</em> then move all 
 * media items within the collection to the collection specified by this ID
 * @param actingUser the user deleting the collection
 * @throws MediaAlbumException if an error occurs
 */
public void deleteCollection(
		Integer collectionId, 
		boolean move, 
		Integer moveToCollectionId,
		User actingUser)
throws MediaAlbumException;

/**
 * Move a set of items to a specified collection.
 * 
 * @param collectionId the collection to move the items to
 * @param items the items to move
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if not authorized to update the collection
 * or items
 */
public void moveItemsToCollection(Integer collectionId, MediaItem[] items, User actingUser)
throws MediaAlbumException, NotAuthorizedException;

/**
 * Get the directory where collection files are stored.
 * 
 * @param collection the collection to get the base directory for
 * @return the base directory for the collection
 * @throws MediaAlbumException if an error occurs
 */
public File getBaseCollectionDirectory(Collection collection) throws MediaAlbumException;

/**
 * Check if a user has permission to update a collection.
 * 
 * @param actingUser the user wishing to update the collection
 * @param collectionId the ID of the collection the user wants to update
 * @return <em>true</em> if user has permission to update collection
 * @throws MediaAlbumException if an error occurs
 */
public boolean canUserUpdateCollection(User actingUser, Integer collectionId) 
throws MediaAlbumException;

/**
 * Check if a user has permission to view a collection.
 * 
 * @param actingUser the user wishing to view the collection
 * @param collectionId the ID of the collection the user wants to view
 * @return <em>true</em> if user has permission to view collection
 * @throws MediaAlbumException if an error occurs
 */
public boolean canUserViewCollection(User actingUser, Integer collectionId) 
throws MediaAlbumException;

}
