/* ===================================================================
 * MediaItemBiz.java
 * 
 * Created Dec 22, 2003 12:46:08 PM
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
 * $Id: MediaItemBiz.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.biz;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;

import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MediaRequestHandler;
import magoffin.matt.ma.MediaRequestHandlerParams;
import magoffin.matt.ma.MediaResponse;
import magoffin.matt.ma.NotAuthorizedException;
import magoffin.matt.ma.util.ComparatorUtil;
import magoffin.matt.ma.util.EmailOptions;
import magoffin.matt.ma.util.Geometry;
import magoffin.matt.ma.xsd.FreeData;
import magoffin.matt.ma.xsd.ItemComment;
import magoffin.matt.ma.xsd.ItemRating;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.ma.xsd.MediaSpec;
import magoffin.matt.ma.xsd.User;

/**
 * Biz interface for Media Album item maintenance.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public interface MediaItemBiz 
{
	/**
	 * The error code when creating a media item but the user's quota is exceeded:
	 * <code>quota.exceeded</code>
	 */
	public static final String ERROR_QUOTA_EXCEEDED = "quota.exceeded";
	
	/**
	 * The error code when deleting a media item but unable to delete the file:
	 * <code>delete.item.error.file</code>
	 */
	public static final String ERROR_UNABLE_DELETE_ITEM_FILE = "delete.item.error.file";

	/**
	 * The error message key for when a user is not allowed to update free data:
	 * <code>fdata.update.forbidden</code>
	 */
	public static final String ERROR_AUTH_UPDATE_FREE_DATA = "fdata.update.forbidden";
	
	/**
	 * The error message key for when a user is not allowed to delete the cache
	 * files of another user:
	 * <code>media.cache.delete.forbidden</code>
	 */
	public static final String ERROR_AUTH_DELETE_CACHE_FILES = "media.cache.delete.forbidden";
	
	public static final String ERROR_UNABLE_TO_EMAIL = "email.send.failure";
	
	/** Message key for when an item email has been sent. */
	public static final String MSG_EMAIL_ITEM_SENT = "email.item.sent";
	
	/** Message key for when an item email with multiple items has been sent. */
	public static final String MSG_EMAIL_ITEM_MULTI_SENT = "email.item.multi.sent";
	
	public static final String ERROR_ITEM_NOT_FOUND = "media.item.not.found";

	/** Sort media items by creation date. */
	public static final Comparator SORT_ITEM_BY_DATE = 
			new ComparatorUtil.MediaItemDateSort();
	
	/** Sort media items by average rating. */
	public static final Comparator SORT_ITEM_BY_AVERAGE_RATING = 
			new ComparatorUtil.MediaItemAverageRatingSort();
	
	/** Sort media items by hits. */
	public static final Comparator SORT_ITEM_BY_HITS = 
			new ComparatorUtil.MediaItemHitSort();
	
	/** Sort item ratings by item ID. */
	public static final Comparator SORT_ITEM_RATING_BY_ITEM_ID = 
			new ComparatorUtil.ItemRatingItemIdSort();
	
	/**
	 * The minimum (worst) rating for a media item: <code>0</code>
	 */
	public static final int MIN_RATING = 0;
	
	/**
	 * The maximum (best) rating for a media item: <code>10</code>
	 */
	public static final int MAX_RATING = 10;
	
/**
 * Get a media item by its ID.
 * 
 * @param id the ID of the media item to get
 * @param allowCached if <em>true</em> then allow returning a cached media item
 * @return the media item
 * @throws MediaAlbumException if an error occurs
 */
public MediaItem getMediaItemById(Integer id, boolean allowCached) 
throws MediaAlbumException;

/**
 * Get an array of media items by their IDs.
 * 
 * @param ids the IDs of the media items to get
 * @param allowCached if <em>true</em> then allow returning a cached media item
 * @return the media items
 * @throws MediaAlbumException if an error occurs
 */
public MediaItem[] getMediaItemsById(Integer[] ids, boolean allowCached) 
throws MediaAlbumException;

/**
 * Get a media item by its collection and path.
 * 
 * @param collectionId the ID of the collection the item belongs to
 * @param path the path of the media item
 * @param allowCached if <em>true</em> then allow returning a cached media item
 * @return the media item
 * @throws MediaAlbumException if an error occurs
 */
public MediaItem getMediaItemByPath(Integer collectionId, String path, boolean allowCached) 
throws MediaAlbumException;

/**
 * Check if a user has permission to update a media item.
 * 
 * @param actingUser the user wishing to update the media item
 * @param itemId the ID of the media item the user wants to update
 * @return <em>true</em> if user has permission to update media item
 * @throws MediaAlbumException if an error occurs
 */
public boolean canUserUpdateMediaItem(User actingUser, Integer itemId) 
throws MediaAlbumException;

/**
 * Check if a user has permission to delete a media item.
 * 
 * @param actingUser the user wishing to delete the media item
 * @param itemId the ID of the media item the user wants to delete
 * @return <em>true</em> if user has permission to delete media item
 * @throws MediaAlbumException if an error occurs
 */
public boolean canUserDeleteMediaItem(User actingUser, Integer itemId) 
throws MediaAlbumException;

/**
 * Delete a media item.
 * 
 * <p>This method should call {@link #canUserDeleteMediaItem(User, Integer)} and if
 * that returns false then throw a {@link NotAuthorizedException}
 * exception.</p>
 * 
 * @param id the ID of the media item to delete
 * @param actingUser the user deleting the media item
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if not authorized to delete media item
 */
public void deleteMediaItem(Integer id, User actingUser) 
throws MediaAlbumException, NotAuthorizedException;

/**
 * Delete any cache files associated with a media item.
 * @param item the media item to delete cache files for
 * @param actingUser the acting user
 * @throws MediaAlbumException if an error occurs
 */
public void deleteMediaItemCacheFiles(MediaItem item, User actingUser)
throws MediaAlbumException;

/**
 * Save a new media item to the backend.
 * 
 * @param item the media item to create
 * @param collectionId the ID of the collection to create the media item in
 * @param actingUser the user creating the media item
 * @return the media item, with updated data from creation
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if not authorized to create the media item
 */
public MediaItem createMediaItem(MediaItem item, Integer collectionId, User actingUser) 
throws MediaAlbumException, NotAuthorizedException;

/**
 * Update an exisitng media item in the backend.
 * 
 * @param item the media item to update
 * @param actingUser the user updating the media item
 * @return the media item, with updated data
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if not authorized to update the media item
 */
public MediaItem updateMediaItem(MediaItem item, User actingUser) 
throws MediaAlbumException, NotAuthorizedException;

/**
 * Update multiple exisitng media items in the backend.
 * 
 * @param items the media items to update
 * @param actingUser the user updating the media item
 * @return the media item, with updated data
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if not authorized to update the media item
 */
public MediaItem[] updateMediaItems(MediaItem[] items, User actingUser) 
throws MediaAlbumException, NotAuthorizedException;

/**
 * Check if a user has permission to create a media item.
 * 
 * @param actingUser the user wishing to delete the media item
 * @param collectionId the ID of the collection the user wants to create the media item in
 * @return <em>true</em> if user has permission to create the media item
 * @throws MediaAlbumException if an error occurs
 */
public boolean canUserCreateMediaItem(User actingUser, Integer collectionId) 
throws MediaAlbumException;

/**
 * Update media item data for a "hit".
 * 
 * @param item the media item populated with appropriate "hit" data
 * @throws MediaAlbumException if an error occurs
 */
public void updateMediaItemHits(MediaItem item)
throws MediaAlbumException;

/**
 * Return true if one user has permission to view a media item.
 * 
 * @param itemId the ID of the item to test
 * @param actingUser the acting user
 * @return <em>true</em> if <var>actingUser</var> has permission to view
 * the media item
 * @throws MediaAlbumException
 */
public boolean canUserViewMediaItem(Integer itemId, User actingUser)
throws MediaAlbumException;

/**
 * Populate media item comments for a set of media items.
 * 
 * @param items the items to populate comments for
 * @param allowCached if <em>true</em> then allow using a cached media item comments
 * @throws MediaAlbumException if an error occurs
 */
public void populateItemComments(MediaItem[] items, boolean allowCached)
throws MediaAlbumException;

/**
 * Add a comment to a media item.
 * 
 * @param itemId the ID of the media item to add the comment to
 * @param comment the comment
 * @param actingUser the acting user
 * @throws MediaAlbumException if an error occurs
 */
public void addComment(Integer itemId, String comment, User actingUser)
throws MediaAlbumException;

/**
 * Get the comments for a specific ietm.
 * 
 * @param itemId the ID of the media item to get the comments for
 * @param allowCached if <em>true</em> then allow using a cached media item comments
 * @return array of comments (never <em>null</em>)
 * @throws MediaAlbumException if an error occurs
 */
public ItemComment[] getMediaItemComments(Integer itemId, boolean allowCached)
throws MediaAlbumException;

/**
 * Populate media item ratings for a set of media items.
 * 
 * @param items the items to populate ratings for
 * @param allowCached if <em>true</em> then allow using a cached media item ratings
 * @throws MediaAlbumException if an error occurs
 */
public void populateItemRatings(MediaItem[] items, boolean allowCached)
throws MediaAlbumException;

/**
 * Populate media FreeData for a set of media items.
 * 
 * @param items the items to populate data for
 * @param allowCached if <em>true</em> then allow using a cached objects
 * @throws MediaAlbumException if an error occurs
 */
public void populateItemFreeData(MediaItem[] items, boolean allowCached)
throws MediaAlbumException;

/**
 * Populate combinations of data into a set of media items.
 * 
 * <p>The <var>mode</var> parameter shouldbe a bit-wise OR'ed combination
 * of <code>POPULATE_MODE_*</code> constants defined in 
 * {@link magoffin.matt.ma.ApplicationConstants}.</p>
 * 
 * <p>This method is a shortcut to calling the other <code>populateItem*</code>
 * methods of this interface.</p>
 * 
 * @param items the items to populate data for
 * @param allowCached if <em>true</em> then allow using a cached objects
 * @throws MediaAlbumException if an error occurs
 */
public void populateItems(MediaItem[] items, int mode, boolean allowCached)
throws MediaAlbumException;

/**
 * Set a rating for a media item.
 * 
 * @param itemId the ID of the media item to add the comment to
 * @param rating the rating
 * @param actingUser the acting user
 * @throws MediaAlbumException if an error occurs
 */
public void setRating(Integer itemId, int rating, User actingUser)
throws MediaAlbumException;

/**
 * Get an item rating by the item ID and the user ID who created it.
 * 
 * @param itemId the media item ID
 * @param userId the user ID who created the rating
 * @throws MediaAlbumException if an error occurs
 */
public ItemRating getRatingByItemUserIds(Integer itemId, Object userId)
throws MediaAlbumException;

/**
 * Get all media items viewable from one user by another user.
 * 
 * @param user the owner of the items
 * @param actingUser the acting user
 * @return all media items viewable by <var>actingUser</var>, owned by 
 * <var>user</var>
 * @throws MediaAlbumException
 */
public MediaItem[] getMediaItemsViewableForUser(User user, User actingUser)
throws MediaAlbumException;

/**
 * Get the average user rating for a media item.
 * 
 * @param item the item to get the average rating for
 * @param actingUser the acting user
 * @return average rating
 * @throws MediaAlbumException if an error occurs
 */
public float getAverageUserRating(MediaItem item, User actingUser)
throws MediaAlbumException;

/**
 * Get the owner's rating for a media item.
 * 
 * @param item the item to get the owner's rating for
 * @param actingUser the acting user
 * @return rating, or <em>null</em> if not rated by owner
 * @throws MediaAlbumException if an error occurs
 */
public Short getOwnerRating(MediaItem item, User actingUser)
throws MediaAlbumException;

/**
 * Set the timezone offset for a set of items.
 * 
 * @param ids the IDs of the item to update
 * @param tzCode the time zone code (eg. America/Los_Angeles)
 * @throws MediaAlbumException if an error occurs
 */
public void setTimezone(Integer[] ids, String tzCode)
throws MediaAlbumException;

/**
 * Set the custom type for a set of items.
 * 
 * @param ids the IDs of the items to update
 * @param customTypeId the custom type ID
 * @param actingUser the acting user
 * @throws MediaAlbumException if an error occurs
 */
public void setCustomType(Integer[] ids, Integer customTypeId, User actingUser)
throws MediaAlbumException, NotAuthorizedException;

/**
 * Get all free data available for a media item.
 * 
 * @param itemId the ID of the media item to get the free data for
 * @param allowCached if <em>true</em> then allow returning cached objects
 * @return the free data (never <em>null</em>)
 * @throws MediaAlbumException if an error occurs
 */
public FreeData[] getFreeData(Integer itemId, boolean allowCached) 
throws MediaAlbumException;

/**
 * Save the free data for a media item, replacing any free data currently saved.
 * 
 * @param itemId the ID of the media item to set the free data for
 * @param data the free data
 * @param actingUser the acting user
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if the acting user does not have 
 * permission to update the user's free data
 */
public void setFreeData(Integer itemId, FreeData[] data, User actingUser)
throws MediaAlbumException, NotAuthorizedException;

/**
 * Save the free data for a set of media items, replacing any free data 
 * currently saved.
 * 
 * @param itemIds the ID of the media item to set the free data for
 * @param data the free data
 * @param actingUser the acting user
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if the acting user does not have 
 * permission to update the user's free data
 */
public void setFreeData(Integer[] itemIds, FreeData[] data, User actingUser)
throws MediaAlbumException, NotAuthorizedException;

/**
 * Set the copyright and/or keywords for a set of media items;
 * 
 * @param itemIds the item IDs to update
 * @param data the copyright / keywords data
 * @param actingUser the acting user
 * @throws MediaAlbumException if an error occurs
 * @throws NotAuthorizedException if the acting user does not have 
 * permission to update the user's free data
 */
public void setCopyrightKeywords(Integer[] itemIds, FreeData[] data, User actingUser)
throws MediaAlbumException, NotAuthorizedException;

/**
 * Get the MIME type associated with the file extension.
 * @param extension the file extension
 * @return the MIME type
 * @throws MediaAlbumException if an error occurs
 */
public String getMIMEforExtension(String extension) throws MediaAlbumException;

/**
 * Get the file extension associated with a MIME type.
 * @param mime the MIME type
 * @return the file extension
 * @throws MediaAlbumException if an error occurs
 */
public String getExtensionForMIME(String mime) throws MediaAlbumException;

/**
 * Delete all cache files associated with a user.
 * 
 * @param user the user to delete the cache files for
 * @param actingUser the acting user
 * @throws MediaAlbumException if an error occurs
 */
public void deleteUserCacheFiles(User user, User actingUser) throws MediaAlbumException;

/**
 * Get a media request handler for an item.
 * 
 * @param item the item to get the request handler for
 * @return the request handler
 * @throws MediaAlbumException if an error occurs
 */
public MediaRequestHandler getHandlerForItem(MediaItem item) throws MediaAlbumException;

/**
 * Get a media request handler for a file type.
 * 
 * @param extension the file type
 * @return the request handler
 * @throws MediaAlbumException if an error occurs
 */
public MediaRequestHandler getHandlerForExtension(String extension) throws MediaAlbumException;

/**
 * Get the file extension for a media item.
 * 
 * @param item the item to get the extension for
 * @return the extension, e.g. "jpg"
 * @throws MediaAlbumException if an error occurs
 */
public String getExtensionForItem(MediaItem item) throws MediaAlbumException;

/**
 * Return <em>true</em> if the file's name (via extension) is supported.
 * @param name the file name
 * @return <em>true</em> if the extension is supported
 * @throws MediaAlbumException if an error occurs
 */
public boolean isFileTypeSupported(String name) throws MediaAlbumException;

/**
 * Process a MediaItem to an output stream.
 * 
 * <p><b>Note:</b> upon returning from this method the <var>params</var>
 * object will be invalidated and should not be used again.</p>
 * 
 * @param out the output stream
 * @param item the media item
 * @param wantOriginal if <em>true</em> then want the original media item
 * @param handler the media request handler
 * @param params the request handler parmeters
 * @param response the media request resopnse
 * @throws IOException if an IO error occurs
 * @return the actual file used in the response (could be cache, could be original)
 * @throws MediaAlbumException if an error occurs
 */
public File handleMediaItem(
		OutputStream out,
		MediaItem item, 
		boolean wantOriginal,
		MediaRequestHandler handler,
		MediaRequestHandlerParams params,
		MediaResponse response) 
throws IOException, MediaAlbumException;

/**
 * Get a Geometry object for a media album at a given size and compression
 * values.
 * 
 * @param item the media item
 * @param sizeKey the size key
 * @param compressionKey the compression key
 * @return the Geometry
 * @throws MediaAlbumException if an error occurs
 */
public Geometry getMediaItemGeometry(MediaItem item, String sizeKey, 
		String compressionKey)
throws MediaAlbumException;

/**
 * Get a Set of all file types (extensions) supported.
 * @return Set of String extensions
 * @throws MediaAlbumException if an error occurs
 */
public Set getSupportedFileTypes() throws MediaAlbumException;

/**
 * Calculate the size, in bytes, of a set of media albums sized 
 * to a specific size.
 * 
 * @param itemIds the IDs of the media items to calculate from
 * @param spec the requested size of the items, or <em>null</em>
 * to calculate based on original file size
 * @param useIcons <em>true</em> to use icon sizes for media items 
 * that can not be scaled to the <var>spec</var>, <em>false</em> to 
 * use the original media instead
 * @return the total size, in bytes
 * @throws MediaAlbumException if an error occurs
 */
public long calculateItemSize(Integer[] itemIds, MediaSpec spec, boolean useIcons)
throws MediaAlbumException;

/**
 * Email one or more items.
 * 
 * <p>Either the <var>itemIds</var> or <var>url</var> parameter should be 
 * provided, but not both. If <var>itemIds</var> are provided then those 
 * media items will be attached to the email. If <var>url</var> is provided
 * the URL will be embedded in the email.</p>
 * 
 * @param itemIds the IDs of the items to email
 * @param url a URL to insert into the email
 * @param options the email options
 * @param actingUser the acting user
 * @throws MediaAlbumException if an error occurs
 */
public void emailItems(Integer[] itemIds, String url, EmailOptions options, 
		User actingUser)
throws MediaAlbumException;

/**
 * Get a media item's true creation date.
 * 
 * <p>This method will find a media item's true creation date, as 
 * defined in the item's meta data or file creation time. It can be 
 * used to find the actual creation date after the creation date 
 * has been altered by a user-defined time.</p>
 * 
 * @param itemId the ID of the item to find the creation date for
 * @return the creation date
 * @throws MediaAlbumException if an error occurs
 */
public Date getItemCreationDate(Integer itemId) 
throws MediaAlbumException;

}
