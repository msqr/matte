/* ===================================================================
 * MediaItemBizImpl.java
 * 
 * Created Dec 22, 2003 1:56:24 PM
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
 * $Id: MediaItemBizImpl.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.biz.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import magoffin.matt.biz.BizInitializer;
import magoffin.matt.dao.CriteriaObjectPoolFactory;
import magoffin.matt.dao.DAO;
import magoffin.matt.dao.DAOException;
import magoffin.matt.dao.DataObject;
import magoffin.matt.dao.DuplicateKeyException;
import magoffin.matt.dao.PrimaryKeyObjectPoolFactory;
import magoffin.matt.gerdal.dataobjects.CountData;
import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MediaAlbumRuntimeException;
import magoffin.matt.ma.MediaMetadata;
import magoffin.matt.ma.MediaRequestHandler;
import magoffin.matt.ma.MediaRequestHandlerParams;
import magoffin.matt.ma.MediaResponse;
import magoffin.matt.ma.MessageConstants;
import magoffin.matt.ma.NotAuthorizedException;
import magoffin.matt.ma.NullMediaResponse;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.CollectionBiz;
import magoffin.matt.ma.biz.FreeDataBiz;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.biz.SearchBiz;
import magoffin.matt.ma.biz.ThemeBiz;
import magoffin.matt.ma.biz.UserBiz;
import magoffin.matt.ma.biz.WorkBiz;
import magoffin.matt.ma.dao.AlbumCriteria;
import magoffin.matt.ma.dao.CollectionCriteria;
import magoffin.matt.ma.dao.FreeDataCriteria;
import magoffin.matt.ma.dao.FreeDataPK;
import magoffin.matt.ma.dao.ItemCommentCriteria;
import magoffin.matt.ma.dao.ItemRatingCriteria;
import magoffin.matt.ma.dao.MediaItemCriteria;
import magoffin.matt.ma.dao.MediaItemPK;
import magoffin.matt.ma.util.EmailOptions;
import magoffin.matt.ma.util.Geometry;
import magoffin.matt.ma.util.InternalMediaResponse;
import magoffin.matt.ma.util.MediaAlbumConfigUtil;
import magoffin.matt.ma.util.MediaItemCacheFileFilter;
import magoffin.matt.ma.util.MediaSpecUtil;
import magoffin.matt.ma.util.MediaUtil;
import magoffin.matt.ma.xsd.Album;
import magoffin.matt.ma.xsd.Collection;
import magoffin.matt.ma.xsd.FreeData;
import magoffin.matt.ma.xsd.ItemComment;
import magoffin.matt.ma.xsd.ItemRating;
import magoffin.matt.ma.xsd.MediaAlbumConfig;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.ma.xsd.MediaServerConfig;
import magoffin.matt.ma.xsd.MediaSpec;
import magoffin.matt.ma.xsd.User;
import magoffin.matt.mail.MailProcessingException;
import magoffin.matt.mail.biz.MailBiz;
import magoffin.matt.util.FileUtil;
import magoffin.matt.util.StringUtil;
import magoffin.matt.util.config.Config;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.pool.ObjectPool;
import org.apache.log4j.Logger;

/**
 * Biz implementation for CollectionBizIntf.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class MediaItemBizImpl extends AbstractBiz implements MediaItemBiz 
{
	public static final String EMAIL_ITEM_PARAM_LINK_URL = "linkUrl";
	
	public static final String EMAIL_ITEM_PARAM_BODY = "body";
	
	private static final Logger LOG = Logger.getLogger(AlbumBizImpl.class);
	
	private static final ItemComment[] NO_COMMENTS = new ItemComment[0];
	
	private static final FreeData[] NO_FREE_DATA = new FreeData[0];
	
	private static final ItemRating[] NO_RATINGS = new ItemRating[0];

	/**
	 * The mail merge template resource path for emailing an item, from
	 * {@link ApplicationConstants#CONFIG_ENV}:
	 * <code>mail.template.email.item</code>
	 */
	private String emailItemMailTemplate = null;

	private Map mediaMimeHandlerMap = null;
	private Map mediaExtMimeMap = null;
	private Map mediaMimeExtMap = null;
	
	private File baseCacheDir = null;
	
	private ObjectPool nullMediaResponsePool = null;

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#getCollectionById(java.lang.Object, boolean)
 */
public MediaItem getMediaItemById(Integer id, boolean allowCached)
throws MediaAlbumException 
{
	if ( id == null ) {
		throw new MediaAlbumException("Null id passed to getMediaItemById");
	}
	
	MediaItem result = (MediaItem)getCachedObject(
			allowCached,ApplicationConstants.CacheFactoryKeys.ITEM,id);
	if ( result != null ) {
		return result;
		/*
		 try {
			return (MediaItem)BeanUtils.cloneBean(result);
		} catch ( Exception e ) {
			throw new MediaAlbumException("Unable to clone data", e);
		}
		*/
	}
	
	MediaItemPK pk = null;
	ObjectPool pool = null;
	try {
		pool = PrimaryKeyObjectPoolFactory.getInstance().getPrimaryKeyObjectPool(
				MediaItem.class);
		pk = (MediaItemPK)borrowObjectFromPool(pool);
		
		pk.setId(id);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				MediaItem.class);
		
		result = (MediaItem)dao.get(pk);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,pk);
	}
	
	if ( result == null ) return null;
	
	CollectionBiz collectionBiz = (CollectionBiz)getBiz(BizConstants.COLLECTION_BIZ);
	Collection collection = collectionBiz.getCollectionById(result.getCollection(),
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	User owner = userBiz.getUserById(collection.getOwner(),
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	MediaUtil.translateItemTime(owner.getTzCode(),result);
	
	cacheObject(ApplicationConstants.CacheFactoryKeys.ITEM,id,
			result);
	
	populateAppData(result);
	
	return result;
	
	/*
	try {
		return (MediaItem)BeanUtils.cloneBean(result);
	} catch ( Exception e ) {
		throw new MediaAlbumException("Unable to clone data", e);
	}*/
}

private void populateAppData(MediaItem item) 
{
	if ( item.getUseIcon().booleanValue() ) {
		item.setIconWidth(ApplicationConstants.ICON_WIDTH);
		item.setIconHeight(ApplicationConstants.ICON_HEIGHT);
	}
}

private void populateAppData(MediaItem[] items) 
{
	if ( items == null ) return;
	for ( int i = 0; i < items.length; i++ ) {
		populateAppData(items[i]);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#getCollectionByPath(java.lang.Object, java.lang.String, boolean)
 */
public MediaItem getMediaItemByPath(
	Integer collectionId,
	String path,
	boolean allowCached)
	throws MediaAlbumException 
{
	if ( collectionId == null || path == null) {
		throw new MediaAlbumException("Null id or path passed to getMediaItemByPath");
	}
	
	MediaItemCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				MediaItem.class);
		crit = (MediaItemCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(MediaItemCriteria.ITEM_FOR_PATH);
		crit.setQuery(new Object[] {collectionId,path});
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				MediaItem.class);
		
		DataObject[] results = dao.findByCriteria(crit);
		
		if ( results != null && results.length > 0 ) {
			MediaItem item = (MediaItem)results[0];
			populateAppData(item);
			return item;
		}
		return null;
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#canUserUpdateAlbum(magoffin.matt.ma.xsd.User, java.lang.Object)
 */
public boolean canUserUpdateMediaItem(User actingUser, Integer itemId)
throws MediaAlbumException 
{
	if ( actingUser == null ) {
		return true;
	}
	MediaItem item = getMediaItemById(itemId,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	return canUserUpdateMediaItem(item,actingUser);
}

private boolean canUserUpdateMediaItem(MediaItem item, User actingUser) 
throws MediaAlbumException
{
	if ( actingUser == null ) return true;
	
	// right now only checking that user is owner of collection
	CollectionBiz collectionBiz = (CollectionBiz)getBiz(BizConstants.COLLECTION_BIZ);
	Collection dir = collectionBiz.getCollectionById(item.getCollection(), 
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
	if ( item != null && actingUser.getUserId().equals(dir.getOwner()) ) {
		return true;
	}
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	return userBiz.isUserSuperUser(actingUser.getUserId());
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#canUserDeleteMediaItem(magoffin.matt.ma.xsd.User, java.lang.Object)
 */
public boolean canUserDeleteMediaItem(User actingUser, Integer itemId)
throws MediaAlbumException {
	MediaItem item = getMediaItemById(itemId,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	return canUserDeleteMediaItem(actingUser,item);
}

private boolean canUserDeleteMediaItem(User actingUser, MediaItem item) 
throws MediaAlbumException 
{
	if ( actingUser == null ) return true;
	
	// right now only checking that user is owner of collection
	CollectionBiz collectionBiz = (CollectionBiz)getBiz(BizConstants.COLLECTION_BIZ);
	Collection collection = collectionBiz.getCollectionById(item.getCollection(), 
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
	if ( item != null && actingUser.getUserId().equals(collection.getOwner()) ) {
		return true;
	}
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	return userBiz.isUserSuperUser(actingUser.getUserId());
}

private boolean canUserUpdateFreeData(MediaItem item, User actingUser)
throws MediaAlbumException
{
	return canUserUpdateMediaItem(item,actingUser);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#deleteAlbum(java.lang.Object, magoffin.matt.ma.xsd.User)
 */
public void deleteMediaItem(Integer id, User actingUser)
throws MediaAlbumException, NotAuthorizedException 
{
	if ( !canUserDeleteMediaItem(actingUser,id) ) {
		throw new NotAuthorizedException(actingUser.getUsername(),
				MessageConstants.ERR_AUTH_UPDATE_ITEM);
	}
	
	CollectionBiz collectionBiz = (CollectionBiz)getBiz(BizConstants.COLLECTION_BIZ);
	
	MediaItem item = getMediaItemById(id,
			ApplicationConstants.CACHED_OBJECT_NOT_ALLOWED);
	Collection collection = collectionBiz.getCollectionById(item.getCollection(),
			ApplicationConstants.CACHED_OBJECT_NOT_ALLOWED);
	
	MediaItemPK pk = null;
	ObjectPool pool = null;
	try {
		pool = PrimaryKeyObjectPoolFactory.getInstance().getPrimaryKeyObjectPool(
				MediaItem.class);
		pk = (MediaItemPK)borrowObjectFromPool(pool);
		
		pk.setId(id);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				MediaItem.class);
		
		updatedItem(id);
		
		dao.remove(pk);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,pk);
	}
	
	SearchBiz searchBiz = (SearchBiz)getBiz(BizConstants.SEARCH_BIZ);
	searchBiz.removeMediaItem(id);
		
	// delete the actual file
	File f = new File(collectionBiz.getBaseCollectionDirectory(collection),item.getPath());
	try {
		f.delete();
	} catch ( SecurityException e ) {
		throw new MediaAlbumException(ERROR_UNABLE_DELETE_ITEM_FILE,
				new Object[] {e.getMessage()});
	}
	
	// delete the cache files
	deleteMediaItemCacheFiles(item,actingUser);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#createMediaItem(magoffin.matt.ma.xsd.MediaItem, java.lang.Object, magoffin.matt.ma.xsd.User)
 */
public MediaItem createMediaItem(
	MediaItem item,
	Integer collectionId,
	User actingUser)
	throws MediaAlbumException, NotAuthorizedException 
{
	if ( item == null ) {
		throw new MediaAlbumException("Null album passed to createCollection");
	}
	
	if ( !canUserCreateMediaItem(actingUser,collectionId) ) {
		throw new NotAuthorizedException(actingUser.getUsername(),
				MessageConstants.ERR_AUTH_UPDATE_ITEM);
	}
	
	CollectionBiz collectionBiz = (CollectionBiz)getBiz(BizConstants.COLLECTION_BIZ);
	
	Collection collection = collectionBiz.getCollectionById(collectionId,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Creating new media item: " +item.getPath() +" in collection "
				+collectionId);
	}
	
	// need user for timezone, quota
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	User owner = userBiz.getUserById(collection.getOwner(), 
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
	try {
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				MediaItem.class);
		
		MediaItem newItem = (MediaItem)BeanUtils.cloneBean(item);
		newItem.setCollection(collection.getCollectionId());
		if ( newItem.getCreationDate() == null ) {
			newItem.setCreationDate(new Date());
		}
		// set item time zone from owner if not set in item
		if ( newItem.getTzCode() == null ) {
			newItem.setTzCode(owner.getTzCode());
		}
		
		if ( newItem.getCustomDate() == null ) {
			newItem.setCustomDate(Boolean.FALSE);
		}
		
		// check quota
		if ( owner.getQuota() != null && newItem.getFileSize() != null ) {
			long quota = owner.getQuota().longValue() * 1000; // stored as 1000s of bytes
			long du = userBiz.getDiskUsage(owner.getUserId());
			long val = du + newItem.getFileSize().intValue();
			if ( val > quota ) {
				// delete file if exists
				File f = getMediaItemFile(newItem,collectionBiz);
				if ( f != null ) {
					f.delete();
				}
				throw new MediaAlbumException("Quota exceeded: " +val,
						ERROR_QUOTA_EXCEEDED);
			}
		}
		
		sanitize(newItem);
		
		dao.create(newItem);
		
		removeObjectFromCache(ApplicationConstants.CacheFactoryKeys.COLLECTION_ITEMS,
				collection.getCollectionId());
		
		indexItem(newItem.getItemId());
		
		populateAppData(newItem);
		
		return newItem;
		
	} catch ( MediaAlbumException e ) {
		throw e;
	} catch ( DAOException e ) {
		throw new MediaAlbumException("DAO exception",e);
	} catch ( Exception e ) {
		throw new MediaAlbumException("Unknown exception creating new album",e);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#updateMediaItem(magoffin.matt.ma.xsd.MediaItem, magoffin.matt.ma.xsd.User)
 */
public MediaItem updateMediaItem(MediaItem item, User actingUser)
throws MediaAlbumException, NotAuthorizedException 
{
	if ( item == null ) {
		throw new MediaAlbumException("Null item passed to updateMediaItem");
	}
	
	if ( !canUserUpdateMediaItem(item,actingUser) ) {
		throw new NotAuthorizedException(actingUser.getUsername(),
				MessageConstants.ERR_AUTH_UPDATE_ITEM);
	}
	
	// clone
	try {
		item = (MediaItem)BeanUtils.cloneBean(item);
	} catch ( Exception e ) {
		throw new MediaAlbumException("Unable to clone data",e);
	}
	
	if ( item.getCustomDate() == null ) {
		item.setCustomDate(Boolean.FALSE);
	}
	
	sanitize(item);
	
	// handle creation date time zone
	CollectionBiz collectionBiz = (CollectionBiz)getBiz(BizConstants.COLLECTION_BIZ);
	Collection collection = collectionBiz.getCollectionById(item.getCollection(),
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	User owner = userBiz.getUserById(collection.getOwner(),
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	MediaUtil.untranslateItemTime(owner.getTzCode(),item);
	
	try {
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				MediaItem.class);
		dao.update(item);
		updatedItem(item.getItemId());
	} catch ( DAOException e ) {
		throw new MediaAlbumException("DAO exception",e);
	}

	indexItem(item.getItemId());
	return item;
}

/**
 * Clean up any potentially bad data from a MediaItem.
 * @param item the item to sanitize
 */
private void sanitize(MediaItem item) 
{
	item.setComment(StringUtil.normalizeWhitespace(item.getComment()));
	item.setName(StringUtil.normalizeWhitespace(item.getName()));
}

/**
 * Clean up any potentially bad data from a ItemComment.
 * @param item the item to sanitize
 */
private void sanitize(ItemComment comment) 
{
	comment.setContent(StringUtil.normalizeWhitespace(comment.getContent()));
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#canUserCreateMediaItem(magoffin.matt.ma.xsd.User, java.lang.Object)
 */
public boolean canUserCreateMediaItem(User actingUser, Integer collectionId)
throws MediaAlbumException {
	if ( actingUser == null ) {
		return true;
	}
	CollectionBiz collectionBiz = (CollectionBiz)getBiz(BizConstants.COLLECTION_BIZ);
	Collection collection = collectionBiz.getCollectionById(collectionId, 
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	return canUserCreateMediaItem(actingUser,collection);
}

private boolean canUserCreateMediaItem(User actingUser, Collection collection)
throws MediaAlbumException
{
	if ( actingUser == null ) return true;
	
	// right now only checking that user is owner of collection
	if ( collection != null && actingUser.getUserId().equals(collection.getOwner()) ) {
		return true;
	}
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	return userBiz.isUserSuperUser(actingUser.getUserId());
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#updateMediaItemHits(magoffin.matt.ma.xsd.MediaItem)
 */
public void updateMediaItemHits(MediaItem item)
throws MediaAlbumException 
{
	if ( item == null ) {
		throw new MediaAlbumException("Null item passed to updateMediaItem");
	}
	
	MediaItemCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				MediaItem.class);
		crit = (MediaItemCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(MediaItemCriteria.UPDATE_FOR_HITS);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				MediaItem.class);
		dao.update(item,crit);
		
		updatedItem(item.getItemId());
		
	} catch ( DAOException e ) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#getMediaItemsById(java.lang.Integer[], boolean)
 */
public MediaItem[] getMediaItemsById(Integer[] ids, boolean allowCached)
throws MediaAlbumException 
{
	if ( ids == null ) {
		throw new MediaAlbumException("Null IDs passed to getMediaItemById");
	}
	
	List results = new ArrayList(ids.length);
	
	for ( int i = 0; i < ids.length; i++ ) {
		MediaItem item = getMediaItemById(ids[i],allowCached);
		if ( item != null ) {
			populateAppData(item);
			results.add(item);
		}
	}
	
	return (MediaItem[])results.toArray(new MediaItem[results.size()]);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#canUserViewMediaItem(java.lang.Object, magoffin.matt.ma.xsd.User)
 */
public boolean canUserViewMediaItem(Integer itemId, User actingUser)
throws MediaAlbumException 
{
	// get the item
	MediaItem item = getMediaItemById(itemId,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
	CollectionBiz collectionBiz = (CollectionBiz)getBiz(BizConstants.COLLECTION_BIZ);
	Collection collection = collectionBiz.getCollectionById(item.getCollection(),
			ApplicationConstants.CACHED_OBJECT_ALLOWED);

	// if user provided, see if they are the owner
	if ( actingUser != null ) {
		if ( collection.getOwner().equals(actingUser.getUserId()) ) {
			return true;
		}
		// check if super user
		UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
		if ( userBiz.isUserSuperUser(actingUser.getUserId()) ) {
			return true;
		}
	}
	
	// run query to see if viewing allowed
	MediaItemCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				MediaItem.class);
		crit = (MediaItemCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(MediaItemCriteria.IS_ITEM_VIEWABLE_FOR_USER);
		Integer[] query = new Integer[] {
				item.getItemId(),
				collection.getOwner(),
				actingUser == null 
					? ApplicationConstants.ANONYMOUS_USER_ID 
					: actingUser.getUserId()};
		crit.setQuery(query);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				MediaItem.class);
		
		int count = ((CountData[])dao.findByCriteria(crit))[0].getCount();
		
		return count > 0 ? true : false;
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#populateItemComments(magoffin.matt.ma.xsd.MediaItem[], boolean)
 */
public void populateItemComments(MediaItem[] items, boolean allowCached)
throws MediaAlbumException 
{
	ItemComment[] results = null;
	ItemCommentCriteria crit = null;
	ObjectPool pool = null;
	Integer[] itemIds = null;
	
	if ( allowCached ) {
		List itemIdList = new ArrayList(items.length);
		for ( int i = 0; i < items.length; i++ ) {
			ItemComment[] tmp = (ItemComment[])getCachedObject(allowCached,
					ApplicationConstants.CacheFactoryKeys.ITEM_COMMENTS,
					items[i].getItemId());
			if ( tmp == null ) {
				itemIdList.add(items[i].getItemId());
			} else {
				items[i].setUserComment(tmp);
			}
		}
		if ( itemIdList.size() < 1 ) {
			// all done!
			return;
		}
		itemIds = (Integer[])itemIdList.toArray(new Integer[itemIdList.size()]);
	} else {
		// set to all item ids
		itemIds = new Integer[items.length];
		for ( int i = 0; i < items.length; i++ ) {
			itemIds[i] = items[i].getItemId();
		}
	}
	
	// sort itemIds
	Arrays.sort(itemIds);
	
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				ItemComment.class);
		crit = (ItemCommentCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(ItemCommentCriteria.COMMENTS_FOR_ITEMS_SEARCH);
		
		if ( itemIds == null ) {
			// set to all item ids
			itemIds = new Integer[items.length];
			for ( int i = 0; i < items.length; i++ ) {
				itemIds[i] = items[i].getItemId();
			}
		}
		
		crit.setQuery(itemIds);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				ItemComment.class);
		
		DataObject[] res = dao.findByCriteria(crit);
		
		results = (ItemComment[])res;
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
	
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	
	// fill in user names for those with user ID set
	for ( int i = 0; i < results.length; i++ ) {
		if ( results[i].getUserId() == null ) continue;
		User u = userBiz.getUserById(results[i].getUserId(),
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
		results[i].setUserName(u.getName());
	}
	
	Map idMap = (Map)borrowObjectFromPool(hashMapPool);
	try {
		populateIdMap(idMap,items);
		int dataIdx = 0;
		for ( int i = 0; i < itemIds.length; i++ ) {
			Integer itemId = itemIds[i];
			MediaItem item = (MediaItem)idMap.get(itemId);
			item.clearUserComment();
			
			if ( dataIdx >= results.length || !itemId.equals(results[dataIdx].getItemId()) ) {
				cacheObject(ApplicationConstants.CacheFactoryKeys.ITEM_COMMENTS,itemId,
						NO_COMMENTS);
				continue;
			}
			
			while ( dataIdx < results.length && itemId.equals(results[dataIdx].getItemId()) ) {
				item.addUserComment(results[dataIdx]);
				dataIdx++;
			}
			cacheObject(ApplicationConstants.CacheFactoryKeys.ITEM_COMMENTS,itemId,
					item.getUserComment());
		}
	} finally {
		returnObjectToPool(hashMapPool,idMap);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#getMediaItemComments(java.lang.Integer, boolean)
 */
public ItemComment[] getMediaItemComments(Integer itemId,
		boolean allowCached) throws MediaAlbumException 
{
	ItemComment[] results = (ItemComment[])getCachedObject(allowCached,
			ApplicationConstants.CacheFactoryKeys.ITEM_COMMENTS,
			itemId);
	if ( results != null ) {
		return results;
	}
	
	ItemCommentCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				ItemComment.class);
		crit = (ItemCommentCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(ItemCommentCriteria.COMMENTS_FOR_ITEM_SEARCH);
		crit.setQuery(itemId);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				ItemComment.class);
		
		DataObject[] res = dao.findByCriteria(crit);
		
		if ( res != null && res.length > 0 ) {
			results = (ItemComment[])res;
		} else {
			results = NO_COMMENTS;
		}
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
	
	cacheObject(ApplicationConstants.CacheFactoryKeys.ITEM_COMMENTS,itemId,
			results);
	
	return results;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#addComment(java.lang.Integer, magoffin.matt.ma.xsd.User)
 */
public void addComment(Integer itemId, String comment, User actingUser)
throws MediaAlbumException 
{
	ItemComment icom = new ItemComment();
	icom.setContent(comment);
	icom.setItemId(itemId);
	icom.setCreationDate(new Date());
	if ( actingUser != null ) {
		icom.setUserId(actingUser.getUserId());
	}
	
	sanitize(icom);
	
	if ( icom.getContent() == null || icom.getContent().length() < 1 ) {
		// do not add empty comment
		return;
	}
	
	try {
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				ItemComment.class);
		
		dao.create(icom);
		
		removeObjectFromCache(ApplicationConstants.CacheFactoryKeys.ITEM_COMMENTS,
				itemId);
		updatedItem(itemId);
		
		indexItem(itemId);
		
	} catch ( DAOException e ) {
		throw new MediaAlbumException("DAO exception",e);
	} catch ( Exception e ) {
		throw new MediaAlbumException("Unknown exception adding item comment",e);
	}
}

/**
 * Helper method to get a media item by ID and re-index it.
 * @param itemId the ID of the item to index
 * @throws MediaAlbumException if an error occurs
 */
private void indexItem(Integer itemId) throws MediaAlbumException
{
	SearchBiz searchBiz = (SearchBiz)getBiz(BizConstants.SEARCH_BIZ);
	searchBiz.index(itemId);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#populateItemRatings(magoffin.matt.ma.xsd.MediaItem[], boolean)
 */
public void populateItemRatings(MediaItem[] items, boolean allowCached)
throws MediaAlbumException
{
	ItemRating[] results = null;
	ItemRatingCriteria crit = null;
	ObjectPool pool = null;
	Integer[] itemIds = null; // list of IDs to lookup ratings from back end
	
	if ( allowCached ) {
		// limit back end search to only those ratings not already cached
		
		List itemIdList = new ArrayList(items.length);
		for ( int i = 0; i < items.length; i++ ) {
			ItemRating[] tmp = (ItemRating[])getCachedObject(allowCached,
					ApplicationConstants.CacheFactoryKeys.ITEM_RATINGS,
					items[i].getItemId());
			if ( tmp == null ) {
				itemIdList.add(items[i].getItemId());
			} else {
				items[i].setUserRating(tmp);
			}
		}
		if ( itemIdList.size() < 1 ) {
			// all done!
			return;
		}
		itemIds = (Integer[])itemIdList.toArray(new Integer[itemIdList.size()]);
	} else {
		// set to all item ids
		itemIds = new Integer[items.length];
		for ( int i = 0; i < items.length; i++ ) {
			itemIds[i] = items[i].getItemId();
		}
	}
	
	// sort itemIds
	Arrays.sort(itemIds);

	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				ItemRating.class);
		crit = (ItemRatingCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(ItemRatingCriteria.RATINGS_FOR_ITEMS_SEARCH);
		
		crit.setQuery(itemIds);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				ItemRating.class);
		
		DataObject[] res = dao.findByCriteria(crit);
		
		results = (ItemRating[])res; // assume sorted by media id
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
	
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	
	// fill in user names for those with user ID set
	for ( int i = 0; i < results.length; i++ ) {
		if ( results[i].getUserId() == null ) continue;
		User u = userBiz.getUserById(results[i].getUserId(),
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
		results[i].setUserName(u.getName());
	}
	
	Map idMap = (Map)borrowObjectFromPool(hashMapPool);
	try {
		populateIdMap(idMap,items);
		
		int dataIdx = 0;
		for ( int i = 0; i < itemIds.length; i++ ) {
			Integer itemId = itemIds[i];
			MediaItem item = (MediaItem)idMap.get(itemId);
			item.clearUserRating();
			
			if ( dataIdx >= results.length || !itemId.equals(results[dataIdx].getItemId()) ) {
				cacheObject(ApplicationConstants.CacheFactoryKeys.ITEM_RATINGS,itemId,
						NO_RATINGS);
				continue;
			}
			
			while ( dataIdx < results.length && itemId.equals(results[dataIdx].getItemId()) ) {
				item.addUserRating(results[dataIdx]);
				dataIdx++;
			}
			cacheObject(ApplicationConstants.CacheFactoryKeys.ITEM_RATINGS,itemId,
					item.getUserRating());
		}
	} finally {
		returnObjectToPool(hashMapPool,idMap);
	}
}

/**
 * Populate a Map with item ID keys and their corresponding items.
 * @param idMap the Map to populate
 * @param items the items to populate the Map with
 */
private void populateIdMap(Map idMap, MediaItem[] items) {
	if ( idMap == null || items == null ) return;
	for ( int i = 0; i < items.length; i++ ) {
		idMap.put(items[i].getItemId(),items[i]);
	}
}

/*
 * Find a media item by its ID in an unsorted array of media items.
 * 
 * @param itemId the ID to search for
 * @param items the media items to search
 * @return the found media item (or <em>null</em> if not found)
 *
private MediaItem findItem(Integer itemId, MediaItem[] items) {
	if ( itemId == null || items == null ) return null;
	for ( int i = 0; i < items.length; i++ ) {
		if ( itemId.equals(items[i].getItemId()) ) return items[i];
	}
	return null;
}*/

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#setRating(java.lang.Integer, int, magoffin.matt.ma.xsd.User)
 */
public void setRating(Integer itemId, int rating, User actingUser)
throws MediaAlbumException
{
	if ( rating < MIN_RATING ) {
		rating = MIN_RATING;
	} else if ( rating > MAX_RATING ) {
		rating = MAX_RATING;
	}
	
	ItemRating irate = new ItemRating();
	irate.setRating(new Short((short)rating));
	irate.setItemId(itemId);
	irate.setCreationDate(new Date());
	if ( actingUser != null ) {
		irate.setUserId(actingUser.getUserId());
	}
	
	DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
			ItemRating.class);

	try {
		dao.create(irate);
	} catch ( DuplicateKeyException e ) {
		
		ItemRating irate2 = getRatingByItemUserIds(itemId,irate.getUserId());
		irate2.setCreationDate(irate.getCreationDate());
		irate2.setRating(irate.getRating());
		
		try {
			dao.update(irate2);
		} catch ( DAOException e2 ) {
			throw new MediaAlbumException("DAO exception",e2);
		}
		
	} catch ( DAOException e ) {
		throw new MediaAlbumException("DAO exception",e);
	} catch ( Exception e ) {
		throw new MediaAlbumException("Unknown exception adding item comment",e);
	}

	removeObjectFromCache(ApplicationConstants.CacheFactoryKeys.ITEM_RATINGS,
			itemId);
	updatedItem(itemId);
	indexItem(itemId);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#getRatingByItemUserIds(java.lang.Integer, java.lang.Object)
 */
public ItemRating getRatingByItemUserIds(Integer itemId, Object userId)
throws MediaAlbumException
{
	ItemRatingCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				ItemRating.class);
		crit = (ItemRatingCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(ItemRatingCriteria.RATING_FOR_USER);
		
		Object[] query = new Object[] {itemId,userId};
		crit.setQuery(query);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				ItemRating.class);
		
		DataObject[] res = dao.findByCriteria(crit);
		
		if ( res == null || res.length < 1 ) {
			return null;
		}
		
		return (ItemRating)res[0];
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
	
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#getMediaItemsViewableForUser(magoffin.matt.ma.xsd.User, magoffin.matt.ma.xsd.User)
 */
public MediaItem[] getMediaItemsViewableForUser(User user, User actingUser)
throws MediaAlbumException
{
	MediaItem[] result = null;
	MediaItemCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				MediaItem.class);
		crit = (MediaItemCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(MediaItemCriteria.ITEMS_VIEWABLE_FOR_USER);
		Integer[] query = new Integer[] {
				user.getUserId(),actingUser == null 
					? new Integer(-1) 
					: actingUser.getUserId()};
		crit.setQuery(query);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				MediaItem.class);
		
		result = (MediaItem[])dao.findByCriteria(crit);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
	
	// adjust times if necessary
	MediaUtil.translateItemTime(user.getTzCode(),result);
	
	populateAppData(result);
	
	return result;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#getAverageUserRating(magoffin.matt.ma.xsd.MediaItem, magoffin.matt.ma.xsd.User)
 */
public float getAverageUserRating(MediaItem item, User actingUser)
throws MediaAlbumException
{
	if ( item == null ) return 0f;
	
	int c = item.getUserRatingCount();
	
	if ( c == 0 ) return 0f;
	
	float average = 0;
	for ( int i = 0; i < c; i++ ) {
		average += item.getUserRating(i).getRating().shortValue();
	}
	average = average / c;
	
	return average;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#getOwnerRating(magoffin.matt.ma.xsd.MediaItem, magoffin.matt.ma.xsd.User)
 */
public Short getOwnerRating(MediaItem item, User actingUser)
		throws MediaAlbumException 
{
	if ( item == null ) return null;
	
	int c = item.getUserRatingCount();
	
	if ( c == 0 ) return null;
	
	for ( int i = 0; i < c; i++ ) {
		ItemRating r = item.getUserRating(i);
		if ( r.getUserId().equals(actingUser.getUserId()) ) {
			return r.getRating();
		}
	}
	
	return null;
}
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#populateItemFreeData(magoffin.matt.ma.xsd.MediaItem[], boolean)
 */
public void populateItemFreeData(MediaItem[] items, boolean allowCached)
throws MediaAlbumException 
{
	FreeData[] results = null;
	FreeDataCriteria crit = null;
	ObjectPool pool = null;
	Integer[] itemIds = null;
	
	if ( allowCached ) {
		List itemIdList = new ArrayList(items.length);
		for ( int i = 0; i < items.length; i++ ) {
			FreeData[] tmp = (FreeData[])getCachedObject(allowCached,
					ApplicationConstants.CacheFactoryKeys.ITEM_FREE_DATA,
					items[i].getItemId());
			if ( tmp == null ) {
				itemIdList.add(items[i].getItemId());
			} else {
				items[i].setData(tmp);
			}
		}
		if ( itemIdList.size() < 1 ) {
			// all done!
			return;
		}
		itemIds = (Integer[])itemIdList.toArray(new Integer[itemIdList.size()]);
	} else {
		// set to all item ids
		itemIds = new Integer[items.length];
		for ( int i = 0; i < items.length; i++ ) {
			itemIds[i] = items[i].getItemId();
		}
	}
	
	// sort itemIds
	Arrays.sort(itemIds);
	
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				FreeData.class);
		crit = (FreeDataCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(FreeDataCriteria.FREE_DATA_FOR_ITEMS);
		crit.setQuery(itemIds);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				FreeData.class);
		
		DataObject[] res = dao.findByCriteria(crit);
		
		if ( res != null ) {
			try {
				results = (FreeData[])res;
			} catch ( ClassCastException e ) {
				LOG.error("ClassCastException, expecting " +FreeData.class
						+" but have " +res.getClass());
				throw e;
			}
		}
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
	
	FreeDataBiz fdBiz = (FreeDataBiz)getBiz(BizConstants.FREE_DATA_BIZ);
	fdBiz.populateFreeDataTypeNames(results,allowCached);
	
	Map idMap = (Map)borrowObjectFromPool(hashMapPool);
	try {
		populateIdMap(idMap,items);
		int dataIdx = 0;
		for ( int i = 0; i < itemIds.length; i++ ) {
			Integer itemId = itemIds[i];
			MediaItem item = (MediaItem)idMap.get(itemId);
			item.clearData();
			
			if ( dataIdx >= results.length || !itemId.equals(results[dataIdx].getItemId()) ) {
				cacheObject(ApplicationConstants.CacheFactoryKeys.ITEM_FREE_DATA,itemId,
						NO_FREE_DATA);
				continue;
			}
			
			while ( dataIdx < results.length && itemId.equals(results[dataIdx].getItemId()) ) {
				item.addData(results[dataIdx]);
				dataIdx++;
			}
			cacheObject(ApplicationConstants.CacheFactoryKeys.ITEM_FREE_DATA,itemId,
					item.getData());
		}
	} finally {
		returnObjectToPool(hashMapPool,idMap);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#populateItems(magoffin.matt.ma.xsd.MediaItem[], int, boolean)
 */
public void populateItems(MediaItem[] items, int mode, boolean allowCached)
throws MediaAlbumException 
{
	if (items == null || items.length < 1 ) return;
	if ( (mode & ApplicationConstants.POPULATE_MODE_FREE_DATA) == ApplicationConstants.POPULATE_MODE_FREE_DATA ) {
		populateItemFreeData(items,
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
	}		
	if ( (mode & ApplicationConstants.POPULATE_MODE_COMMENTS) == ApplicationConstants.POPULATE_MODE_COMMENTS ) {
		populateItemComments(items, 
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
	}
	if ( (mode & ApplicationConstants.POPULATE_MODE_RATINGS) == ApplicationConstants.POPULATE_MODE_RATINGS ) {
		populateItemRatings(items, 
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
	}
	populateAppData(items);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#setTimezone(java.lang.Integer[], java.lang.String)
 */
public void setTimezone(Integer[] ids, String tzCode)
throws MediaAlbumException 
{
	if ( ids == null ) {
		throw new MediaAlbumException("Null ID passed to setItemTimezone");
	}
	
	MediaItem item = new MediaItem();
	item.setTzCode(tzCode);
	
	MediaItemCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				MediaItem.class);
		crit = (MediaItemCriteria)borrowObjectFromPool(pool);
		crit.setSearchType(MediaItemCriteria.UPDATE_FOR_TIMEZONE);
		
		for ( int i = 0; i < ids.length; i++ ) {
			item.setItemId(ids[i]);
			DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
					MediaItem.class);
			dao.update(item,crit);
		}
	} catch ( DAOException e ) {
		throw new MediaAlbumException("DAO exception",e);
	} catch (Exception e) {
		throw new MediaAlbumException("Unknown exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
	
	updatedItems(ids);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#setCustomType(java.lang.Integer[], java.lang.Integer, magoffin.matt.ma.xsd.User)
 */
public void setCustomType(Integer[] ids, Integer customTypeId,
		User actingUser) throws MediaAlbumException, NotAuthorizedException
{
	if ( ids == null ) {
		throw new MediaAlbumException("Null IDs passed to setCustomType");
	}
	
	for ( int i = 0; i < ids.length; i++ ) {
		if ( !canUserUpdateMediaItem(actingUser,ids[i]) ) {
			throw new NotAuthorizedException(actingUser.getUsername(),
					MessageConstants.ERR_AUTH_UPDATE_ITEM);
		}
	}
	
	MediaItem item = new MediaItem();
	item.setCustomType(customTypeId);
	
	MediaItemCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				MediaItem.class);
		crit = (MediaItemCriteria)borrowObjectFromPool(pool);
		crit.setSearchType(MediaItemCriteria.UPADTE_FOR_CUSTOM_TYPE);
		
		for ( int i = 0; i < ids.length; i++ ) {
			item.setItemId(ids[i]);
			DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
					MediaItem.class);
			dao.update(item,crit);
		}
	} catch ( DAOException e ) {
		throw new MediaAlbumException("DAO exception",e);
	} catch (Exception e) {
		throw new MediaAlbumException("Unknown exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
	
	updatedItems(ids);
}

private void updatedItem(Integer id) throws MediaAlbumException 
{
	Integer[] ids = new Integer[]{id};
	updatedItems(ids);
}

private void updatedItems(MediaItem[] items) throws MediaAlbumException
{
	Integer[] ids = new Integer[items.length];
	for ( int i = 0; i < items.length; i++ ) {
		ids[i] = items[i].getItemId();
	}
	updatedItems(ids);
}
	
private void updatedItems(Integer[] ids) throws MediaAlbumException 
{
	// 1) remove from item cache
	for ( int i = 0; i < ids.length; i++ ) {
		removeObjectFromCache(ApplicationConstants.CacheFactoryKeys.ITEM,
				ids[i]);
	}
	
	// 2) get all albums utilizing any of these items and remove from album 
	//    items cache
	Album[] albums = getAlbumsContainingMediaItems(ids);
	for ( int i = 0; i < albums.length; i++ ) {
		removeObjectFromCache(ApplicationConstants.CacheFactoryKeys.ALBUM_ITEMS,
				albums[i].getAlbumId());
	}
	
	// 3) get all collections utilizing any of these items and remove from 
	//    collection items cache
	Collection[] collections = getCollectionsContainingMediaItems(ids);
	for ( int i = 0; i < collections.length; i++ ) {
		removeObjectFromCache(ApplicationConstants.CacheFactoryKeys.COLLECTION_ITEMS,
				collections[i].getCollectionId());
	}
}

private Album[] getAlbumsContainingMediaItems(Integer[] ids)
throws MediaAlbumException
{
	AlbumCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				Album.class);
		crit = (AlbumCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(AlbumCriteria.ALBUMS_CONTAINING_ITEMS);
		crit.setQuery(ids);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Album.class);
		
		return (Album[])dao.findByCriteria(crit);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} catch ( Exception e ) {
		throw new MediaAlbumException("Unable to get data", e);
	} finally {
		returnObjectToPool(pool,crit);
	}
}

private Collection[] getCollectionsContainingMediaItems(Integer[] ids)
throws MediaAlbumException
{
	CollectionCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				Collection.class);
		crit = (CollectionCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(CollectionCriteria.COLLECTIONS_CONTAINING_ITEMS);
		crit.setQuery(ids);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Collection.class);
		
		return (Collection[])dao.findByCriteria(crit);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} catch ( Exception e ) {
		throw new MediaAlbumException("Unable to get data", e);
	} finally {
		returnObjectToPool(pool,crit);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#getFreeData(java.lang.Integer, boolean)
 */
public FreeData[] getFreeData(Integer itemId, boolean allowCached)
throws MediaAlbumException 
{
	FreeData[] result = (FreeData[])getCachedObject(allowCached,
			ApplicationConstants.CacheFactoryKeys.ITEM_FREE_DATA,
			itemId);
	if ( result != null ) {
		return result;
	}
	
	FreeDataCriteria crit = null;
	ObjectPool pool = null;
	
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				FreeData.class);
		crit = (FreeDataCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(FreeDataCriteria.FREE_DATA_FOR_ITEM);
		crit.setQuery(itemId);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				FreeData.class);
		
		FreeData[] data = (FreeData[])dao.findByCriteria(crit);
		
		if ( data == null ) {
			result = NO_FREE_DATA;
		} else {
			result = data;
		}
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
	
	cacheObject(ApplicationConstants.CacheFactoryKeys.ITEM_FREE_DATA,
			itemId,
			result);
	
	return result;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#setFreeData(java.lang.Integer, magoffin.matt.ma.xsd.FreeData[], magoffin.matt.ma.xsd.User)
 */
public void setFreeData(Integer itemId, FreeData[] data, User actingUser)
throws MediaAlbumException, NotAuthorizedException 
{
	setFreeData(itemId,data,actingUser,true);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#setFreeData(java.lang.Integer[], magoffin.matt.ma.xsd.FreeData[], magoffin.matt.ma.xsd.User)
 */
public void setFreeData(Integer[] itemIds, FreeData[] data, User actingUser)
throws MediaAlbumException, NotAuthorizedException 
{
	for ( int i = 0; i < itemIds.length; i++ ) {
		setFreeData(itemIds[i],data,actingUser,false);
	}
	updatedItems(itemIds);
}

private void setFreeData(Integer itemId, FreeData[] data, User actingUser, 
		boolean updateCache) throws MediaAlbumException
{
	MediaItem item = getMediaItemById(itemId,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	if ( !canUserUpdateFreeData(item,actingUser) ) {
		throw new NotAuthorizedException(actingUser.getUsername(),
				ERROR_AUTH_UPDATE_FREE_DATA);
	}
	
	// first delete existing user free data
	FreeDataPK pk = null;
	ObjectPool pool = null;
	DAO dao = null;
	
	try {
		pool = PrimaryKeyObjectPoolFactory.getInstance().getPrimaryKeyObjectPool(
				FreeData.class);
		pk = (FreeDataPK)borrowObjectFromPool(pool);
		
		pk.setItemId(itemId);
		
		dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				FreeData.class);
		
		dao.remove(pk);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,pk);
	}
	
	createFreeData(itemId, data, updateCache, item, dao);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#setCopyrightKeywords(java.lang.Integer[], magoffin.matt.ma.xsd.FreeData[], magoffin.matt.ma.xsd.User)
 */
public void setCopyrightKeywords(Integer[] itemIds, FreeData[] data,
		User actingUser) throws MediaAlbumException, NotAuthorizedException 
{
	for ( int i = 0; i < itemIds.length; i++ ) {
		setCopyrightKeywords(itemIds[i],data,actingUser,false);
	}
	updatedItems(itemIds);
}

private void setCopyrightKeywords(Integer itemId, FreeData[] data,
		User actingUser, boolean updateCache) 
throws MediaAlbumException, NotAuthorizedException 
{
	MediaItem item = getMediaItemById(itemId,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	if ( !canUserUpdateFreeData(item,actingUser) ) {
		throw new NotAuthorizedException(actingUser.getUsername(),
				ERROR_AUTH_UPDATE_FREE_DATA);
	}
	
	// first delete existing user free data
	FreeDataPK pk = null;
	ObjectPool pool = null;
	DAO dao = null;
	
	try {
		pool = PrimaryKeyObjectPoolFactory.getInstance().getPrimaryKeyObjectPool(
				FreeData.class);
		pk = (FreeDataPK)borrowObjectFromPool(pool);
		
		pk.setItemId(itemId);
		pk.setDataTypeId(ApplicationConstants.FREE_DATA_TYPE_KEYWORD);
		
		dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				FreeData.class);
		
		dao.remove(pk);
		
		pk.setDataTypeId(ApplicationConstants.FREE_DATA_TYPE_COPYRIGHT);
		
		dao.remove(pk);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,pk);
	}
	
	createFreeData(itemId, data, updateCache, item, dao);
}

private void createFreeData(Integer itemId, FreeData[] data, boolean updateCache, MediaItem item, DAO dao) 
throws MediaAlbumException 
{
	// find owner
	CollectionBiz collecitonBiz = (CollectionBiz)getBiz(BizConstants.COLLECTION_BIZ);
	Collection collection = collecitonBiz.getCollectionById(item.getCollection(),
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
	// now create new item free data
	FreeData[] newFd = new FreeData[data.length];
	for ( int i = 0; i < data.length; i++ ) {
		try {
			newFd[i] = (FreeData)BeanUtils.cloneBean(data[i]);
		} catch ( Exception e ) {
			throw new MediaAlbumException("Unable to clone data",e);
		}
		newFd[i].setOwner(collection.getOwner());
		newFd[i].setAlbumId(null);
		newFd[i].setCollectionId(null);
		newFd[i].setItemId(itemId);
		newFd[i].setDataId(null);
	}
	
	try {
		dao.create(newFd);
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	}
	
	removeObjectFromCache(
			ApplicationConstants.CacheFactoryKeys.ITEM_FREE_DATA,
			itemId);
	
	if ( updateCache ) {
		updatedItem(itemId);
	}
	
	indexItem(itemId);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#getMIMEforExtension(java.lang.String)
 */
public String getMIMEforExtension(String extension)
throws MediaAlbumException 
{
	return (String)mediaExtMimeMap.get(extension);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#getExtensionForMIME(java.lang.String)
 */
public String getExtensionForMIME(String mime) throws MediaAlbumException {
	return (String)mediaMimeExtMap.get(mime);
}
	
/* (non-Javadoc)
 * @see magoffin.matt.biz.Biz#init(magoffin.matt.biz.BizInitializer)
 */
public void init(BizInitializer initializer) 
{
	super.init(initializer);
	MediaAlbumConfig appConfig = this.initializer.getAppConfig();
	MediaServerConfig sConfig = appConfig.getMediaServer();
	if ( sConfig == null ) {
		throw new MediaAlbumRuntimeException("Server configuration not available");
	}

	// get the base cache directory
	String path = Config.get(
			ApplicationConstants.CONFIG_ENV,
			ApplicationConstants.ENV_BASE_FILE_PATH_MEDIA_CACHE);
	if ( path != null ) {
		if ( LOG.isInfoEnabled() ) {
			LOG.info("Initializing cache dir " +path);
		}
		baseCacheDir = new File(path);
	}

	mediaMimeHandlerMap = Collections.unmodifiableMap(MediaAlbumConfigUtil
			.getMediaHandlerMap(appConfig, MediaAlbumConfigUtil
					.getPoolFactory(appConfig)));
	mediaMimeExtMap = Collections.unmodifiableMap(MediaAlbumConfigUtil
			.getMediaHandlerMimeExtensionMap(sConfig, mediaMimeHandlerMap));
	mediaExtMimeMap = Collections.unmodifiableMap(MediaAlbumConfigUtil
			.getMediaHandlerExtensionMimeMap(appConfig.getMediaServer(),
					mediaMimeHandlerMap));

	if ( LOG.isInfoEnabled() ) {
		LOG.info("Got MIME -> Handler map: " +mediaMimeHandlerMap);
	}
	
	emailItemMailTemplate = Config.getNotEmpty(
			ApplicationConstants.CONFIG_ENV,"mail.template.email.item");
	
	nullMediaResponsePool = poolFactory.getPoolInstance(
			NullMediaResponse.class);
}

private boolean canUserDeleteCacheFiles(User user, User actingUser)
throws MediaAlbumException
{
	if ( user.getUserId().equals(actingUser.getUserId()) ) {
		return true;
	}
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	return userBiz.isUserSuperUser(actingUser.getUserId());
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#deleteUserCacheFiles(magoffin.matt.ma.xsd.User, magoffin.matt.ma.xsd.User)
 */
public void deleteUserCacheFiles(User user, User actingUser)
throws MediaAlbumException 
{
	// verify user can delete
	if ( !canUserDeleteCacheFiles(user,actingUser) ) {
		throw new NotAuthorizedException(actingUser.getUsername(),
				ERROR_AUTH_DELETE_CACHE_FILES);
	}
	
	// get all collections for user
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	Collection[] collections = userBiz.getCollectionsForUser(user.getUserId());
	CollectionBiz collectionBiz = (CollectionBiz)getBiz(BizConstants.COLLECTION_BIZ);
	if ( collections == null ) return;
	for ( int i = 0; i < collections.length; i++ ) {
		File baseCollectionDir = collectionBiz.getBaseCollectionDirectory(
				collections[i]);
		File cacheDir = new File(baseCacheDir,baseCollectionDir.getName());
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Deleting cache directory " +cacheDir.getAbsolutePath());
		}	
		FileUtil.deleteRecursive(cacheDir, true);
	}
}

private File getMediaItemFile(MediaItem item, CollectionBiz collectionBiz) 
throws MediaAlbumException
{
	Collection collection = collectionBiz.getCollectionById(item.getCollection(),
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	File baseCollectionDir = collectionBiz.getBaseCollectionDirectory(collection);
	return new File(baseCollectionDir,item.getPath());
}

private File getCacheFile(File baseCollectionDir, MediaItem item, 
		MediaRequestHandler handler, MediaRequestHandlerParams params)
throws MediaAlbumException
{
	String cacheKey = handler.getCacheKey(item,params);
	if ( cacheKey  != null ) {
		return new File(baseCacheDir,baseCollectionDir.getName()
				+File.separator +cacheKey+"."+mediaMimeExtMap.get(item.getMime()));
	}/* else {
		// cache not enabled, return full item
		CollectionBiz collectionBiz = (CollectionBiz)getBiz(BizConstants.COLLECTION_BIZ);
		return getMediaItemFile(item,collectionBiz);
	}*/
	return null;
}

private File getCacheDir(Integer collectionId) throws MediaAlbumException
{
	CollectionBiz collectionBiz = (CollectionBiz)getBiz(
			BizConstants.COLLECTION_BIZ);
	Collection c = collectionBiz.getCollectionById(collectionId,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	File baseCollectionDir = collectionBiz.getBaseCollectionDirectory(c);
	return new File(baseCacheDir,baseCollectionDir.getName());
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#handleMediaItem(java.io.OutputStream, magoffin.matt.ma.xsd.MediaItem, boolean, magoffin.matt.ma.MediaRequestHandler, magoffin.matt.ma.MediaRequestHandlerParams, magoffin.matt.ma.MediaResponse)
 */
public File handleMediaItem(
		OutputStream out,
		MediaItem item, 
		boolean wantOriginal,
		MediaRequestHandler handler,
		MediaRequestHandlerParams params,
		MediaResponse response) 
throws IOException, MediaAlbumException
{
	CollectionBiz collectionBiz = (CollectionBiz)getBiz(BizConstants.COLLECTION_BIZ);
	WorkBiz workBiz = (WorkBiz)getBiz(BizConstants.WORK_BIZ);
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	Collection collection = collectionBiz.getCollectionById(item.getCollection(),
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	File baseCollectionDir = collectionBiz.getBaseCollectionDirectory(collection);
	
	try {
		String mime = handler.getOutputMime(item,params);
		File original = new File(baseCollectionDir,item.getPath());
		
		if ( wantOriginal ) {
			// don't do any processing, just return unaltered media stream
			handleFile(response, out,original,item,mime);
			return original;
		}
			
		if ( !mediaMimeHandlerMap.containsKey(item.getMime()) ) {
			throw new MediaAlbumException("No handler defiend for MIME type "+item.getMime());
		}
		
		// handle watermark for image's owner
		User owner = userBiz.getUserById(collection.getOwner(),
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
		
		// if owner has watermark set, add to params now
		if ( owner.getWatermark() != null ) {
			ThemeBiz themeBiz = (ThemeBiz)getBiz(BizConstants.THEME_BIZ);
			File f = themeBiz.getUserThemeResource(owner, owner.getWatermark());
			if ( f  != null ) {
				params.setParam(MediaRequestHandlerParams.WATERMARK,f);
			}
			// check for watermark params
			FreeData[] freeData = userBiz.getFreeData(owner,
					ApplicationConstants.CACHED_OBJECT_ALLOWED);
			if ( freeData.length > 0 ) {
				List wmParams = new ArrayList(freeData.length);
				for ( int i = 0; i < freeData.length; i++ ) {
					FreeData fd = freeData[i];
					if ( ApplicationConstants.FREE_DATA_TYPE_WATERMARK_PARAM.equals(
							fd.getDataTypeId()) ) {
						wmParams.add(fd.getDataValue());
					}
				}
				if ( wmParams.size() > 0 ) {
					String[] wmp = (String[])wmParams.toArray(
							new String[wmParams.size()]);
					params.setParam(MediaRequestHandlerParams.WATERMARK_PARAM, wmp);
				}
			}
		}

		// pre-process handler params
		handler.preProcessParams(item,params);
	
		// check if image cached already
		File cacheFile = getCacheFile(baseCollectionDir,item,handler,params);
		if ( cacheFile != null && handleCacheFile(response,out,cacheFile,item,mime) ) {
			return cacheFile;
		}
		
		// cache miss or cache disabled
			
		// set the work biz
		params.setWorkBiz(workBiz);
	
		// get the input stream to the media file
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Target media path = " +original.getAbsolutePath());
		}
		
		InputStream in = null;
		
		if ( handler.useStreamsForWrite() ) {
			in = new BufferedInputStream( new FileInputStream( original ) );
		}
		
		params.setParam(MediaRequestHandlerParams.INPUT_FILE,original);

		if ( cacheFile != null ) {
			cacheFile.getParentFile().mkdirs();
			OutputStream cacheOut = null;
			
			if ( handler.useStreamsForWrite() ) {
				cacheOut = new BufferedOutputStream(
					new FileOutputStream( cacheFile ) );	
			}
			
			params.setParam(MediaRequestHandlerParams.OUTPUT_FILE,cacheFile);
			
			try {
				handler.writeMedia(item,cacheOut,in,params);
			} finally {
				if ( cacheOut != null ) {
					try {
					cacheOut.flush();
					cacheOut.close();
					} catch ( Exception e ) {
						LOG.warn("Unable to flush and close cache file output stream: " +e.getMessage());
					}
				}
			}
			
			// now write cached file to output stream
			if ( response.getClass() != NullMediaResponse.class ) {
				handleCacheFile(response,out,cacheFile,item,mime);
				if ( out != null ) {
					out.flush();
				}
			}
			return cacheFile;
		} else if ( response.getClass() != NullMediaResponse.class && out != null ) {
			handler.writeMedia(item,out,in,params);
			if ( out != null ) {
				out.flush();
			}
		}
		return original;
	} finally {
		if ( handler != null ) {
			LOG.debug("Calling postProcessParams on " +params);
			handler.postProcessParams(item,params);
		}
	}
}

private void handleFile(
		MediaResponse response, 
		OutputStream out,
		File f, 
		MediaItem item,
		String mime ) 
throws IOException
{
	if ( LOG.isDebugEnabled() ) {
	LOG.debug("Returning file stream " +item.getPath() +"; length = "
			+f.length());
	}
	response.setMediaLength((int)f.length());
	response.setMimeType(mime);
	response.setModifiedDate(f.lastModified());
	if ( out != null ) {
		FileUtil.slurp(f,out);
	}
}

/**
 * Handle a media item using cache if possible.
 * 
 * <p>If the media item was found in the cache, the file will be passed to the 
 * output stream and <em>true</em> will be returned. Otherwise nothing will
 * be passed to the output stream and <em>false</em> will be returned.</p>
 * 
 * @param response the response
 * @param out the output stream
 * @param item the media item to handle
 * @param mime the output MIME type
 * @return <em>true</em> if the media was returned frmo cache
 * @throws IOException if an error occurs
 */
private boolean handleCacheFile(
	MediaResponse response, 
	OutputStream out,
	File cacheFile,
	MediaItem item,
	String mime)
throws IOException
{
	//File baseDir = new File(baseCacheDir,item.getCollection().toString());
	//File cacheFile = getCacheFile(item,key,baseDir, mediaMimeExtensionMap);
	
	if ( cacheFile.canRead() && cacheFile.length() > 0 ) {
		if ( response.getClass() != NullMediaResponse.class ) {
			handleFile(response, out, cacheFile, item, mime);
		}
		return true;
	} else if ( out == null && response.getClass() != InternalMediaResponse.class ) {
		// cache file doesn't exist but null output, so don't bother resizing image
		// note we don't get to set output size then :-( but hopefully we only want
		// the modification date, which we set to now
		response.setMimeType(mime);
		response.setModifiedDate(System.currentTimeMillis());
		return true;
	}
	
	return false;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#getHandlerForItem(magoffin.matt.ma.xsd.MediaItem)
 */
public MediaRequestHandler getHandlerForItem(MediaItem item)
throws MediaAlbumException 
{
	return (MediaRequestHandler)mediaMimeHandlerMap.get(item.getMime());
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#getExtensionForItem(magoffin.matt.ma.xsd.MediaItem)
 */
public String getExtensionForItem(MediaItem item)
throws MediaAlbumException 
{
	return (String)mediaMimeExtMap.get(item.getMime());
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#isFileTypeSupported(java.lang.String)
 */
public boolean isFileTypeSupported(String name) throws MediaAlbumException
{
	// extract extension if possible
	int idx = name.lastIndexOf('.');
	if ( idx > 0  && (idx+1) < name.length() ) { // don't allow dot-file names
		name = name.substring(idx+1);
	}
	
	if ( mediaExtMimeMap.containsKey(name.toLowerCase()) ) {
		return true;
	}
	return false;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#getSupportedFileTypes()
 */
public Set getSupportedFileTypes() throws MediaAlbumException
{
	return mediaExtMimeMap.keySet();
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#getHandlerForExtension(java.lang.String)
 */
public MediaRequestHandler getHandlerForExtension(String extension)
throws MediaAlbumException
{
	String mime = getMIMEforExtension(extension);
	if ( mime == null ) return null;
	return (MediaRequestHandler)mediaMimeHandlerMap.get(mime);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#emailItems(java.lang.Integer[], java.lang.String, magoffin.matt.ma.util.EmailOptions, magoffin.matt.ma.xsd.User)
 */
public void emailItems(Integer[] itemIds, String url, EmailOptions options,
		User actingUser) throws MediaAlbumException
{
	Map params = (Map)borrowObjectFromPool(hashMapPool);
	List toList = (List)borrowObjectFromPool(arrayListPool);
	try {
		if ( url != null && url.length() > 0 ) {
			params.put(EMAIL_ITEM_PARAM_LINK_URL,url);
		}
		params.put(EMAIL_ITEM_PARAM_BODY,options.getBody());
		
		MailBiz mailBiz = (MailBiz)getBiz(BizConstants.MAIL_BIZ);
		
		String from = actingUser == null 
			? options.getFrom() 
			: mailBiz.formatMailAddress(actingUser.getName(),actingUser.getEmail());
		
		String template = getMailTemplateResourcePath(emailItemMailTemplate);
			
		if ( params.containsKey(EMAIL_ITEM_PARAM_LINK_URL) ) {
			mailBiz.sendResourceMailMerge(from,options.getTo(),null,null,
					options.getSubject(),template,params);
		} else {
			// get those files to attach
			MediaItem[] items = getMediaItemsById(itemIds,
					ApplicationConstants.CACHED_OBJECT_ALLOWED);
			if ( items == null || items.length < 1 ) {
				throw new MediaAlbumException(ERROR_ITEM_NOT_FOUND,(Object[])null);
			}
			
			// we want resized items, so we have to call handle
			NullMediaResponse mr = null;
			try {
				mr = (NullMediaResponse)borrowObjectFromPool(nullMediaResponsePool);
				File[] files = new File[items.length];
				String[] names = new String[items.length];
				CollectionBiz collectionBiz = (CollectionBiz)getBiz(
						BizConstants.COLLECTION_BIZ);

				for ( int i = 0; i < items.length; i++ ) {
					MediaItem item = items[i];
					MediaRequestHandler handler = getHandlerForItem(item);
					MediaRequestHandlerParams hParams = handler.getParamInstance();
					hParams.setParam(MediaRequestHandlerParams.SIZE,
							MediaSpecUtil.SIZE_NORMAL);
					hParams.setParam(MediaRequestHandlerParams.COMPRESSION,
							MediaSpecUtil.COMPRESS_MEDIUM);
					
					// "fake" ourselves into creating cache file if possible
					files[i] = handleMediaItem(System.out,item,false,handler,
							hParams,mr);
					
					// make sure to get original file name, not cache file name
					File f = getMediaItemFile(item,collectionBiz);
					names[i] = f.getName();
				}
				mailBiz.sendResourceMailMerge(from,options.getTo(),null,null,
						options.getSubject(),template,params,files,names);
			} catch ( IOException e ) {
				throw new MediaAlbumException("IOException generating email",e);
			} finally {
				returnObjectToPool(nullMediaResponsePool,mr);
			}
		}
		
	} catch (MailProcessingException e) {
		MediaAlbumException ex = new MediaAlbumException(ERROR_UNABLE_TO_EMAIL,
				new Object[] {e.getMessage()});
		ex.setNestedException(e);
		throw ex;
	} finally {
		returnObjectToPool(hashMapPool,params);
		returnObjectToPool(arrayListPool,toList);
	}
}


/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#updateMediaItems(magoffin.matt.ma.xsd.MediaItem[], magoffin.matt.ma.xsd.User)
 */
public MediaItem[] updateMediaItems(MediaItem[] items, User actingUser)
		throws MediaAlbumException, NotAuthorizedException
{
	if ( items == null ) {
		throw new MediaAlbumException("Null item passed to updateMediaItem");
	}
	
	if ( items.length < 1 ) return items;
	
	// verify security and clone data
	MediaItem[] cloned = new MediaItem[items.length];
	for ( int i = 0; i < items.length; i++ ) {
		if ( !canUserUpdateMediaItem(items[i],actingUser) ) {
			throw new NotAuthorizedException(actingUser.getUsername(),
					MessageConstants.ERR_AUTH_UPDATE_ITEM);
		}
		try {
			cloned[i] = (MediaItem)BeanUtils.cloneBean(items[i]);
			sanitize(cloned[i]);			
		} catch ( Exception e ) {
			throw new MediaAlbumException("Unable to clone data",e);
		}
	}
	
	items = cloned;
	
	// handle creation date time zone
	CollectionBiz collectionBiz = (CollectionBiz)getBiz(BizConstants.COLLECTION_BIZ);
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	//Calendar cal = Calendar.getInstance();
	for ( int i = 0; i < items.length; i++ ) {
		Collection collection = collectionBiz.getCollectionById(
				items[i].getCollection(),
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
		User owner = userBiz.getUserById(collection.getOwner(),
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
		MediaUtil.untranslateItemTime(owner.getTzCode(),items[i]);
	}
	
	try {
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				MediaItem.class);
		dao.update(items);
		updatedItems(items);
	} catch ( DAOException e ) {
		throw new MediaAlbumException("DAO exception",e);
	}
	
	// update index
	for ( int i = 0; i < items.length; i++ ) {
		indexItem(items[i].getItemId());
	}
	return items;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#getItemCreationDate(java.lang.Integer)
 */
public Date getItemCreationDate(Integer itemId) throws MediaAlbumException {
	// get item
	MediaItem item = getMediaItemById(itemId,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	if ( item == null ) {
		throw new MediaAlbumException("Meida item not found by ID: " +itemId);
	}
	
	// clone item so we can change its data
	try {
		item = (MediaItem)BeanUtils.cloneBean(item);
	} catch ( Exception e ) {
		throw new MediaAlbumException("Unable to clone data", e);
	}
	
	// get handler for item
	MediaRequestHandler handler = getHandlerForItem(item);
	if ( handler == null ) {
		throw new MediaAlbumException("No handler defined for media item "
				+itemId +" with MIME " +item.getMime());
	}
	
	// use handler to update creation date
	CollectionBiz collectionBiz = (CollectionBiz)getBiz(BizConstants.COLLECTION_BIZ);
	File mediaFile = getMediaItemFile(item,collectionBiz);
	MediaMetadata meta = handler.setMediaItemParameters(mediaFile,item);
	
	// return date as set by handler, or revert to file date if not found via meta
	if ( meta != null &&  meta.getCreationDate() != null ) {
		return meta.getCreationDate();
	}
	return new Date(mediaFile.lastModified());
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#calculateItemSize(java.lang.Integer[], magoffin.matt.ma.xsd.MediaSpec, boolean)
 */
public long calculateItemSize(Integer[] itemIds, MediaSpec spec, boolean useIcons) 
throws MediaAlbumException 
{
	if ( itemIds == null ) return 0;
	
	long result = 0;
	for ( int i = 0; i < itemIds.length; i++ ) {
		MediaItem item = getMediaItemById(itemIds[i],
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
		MediaRequestHandler handler = getHandlerForItem(item);
		MediaRequestHandlerParams params = handler.getParamInstance();
		if ( spec != null && !useIcons && item.getUseIcon().booleanValue() ) {
			params.setParam(MediaRequestHandlerParams.WANT_ORIGINAL,Boolean.TRUE);
		} else if ( spec != null ) {
			params.setParam(MediaRequestHandlerParams.SIZE,spec.getSize());
			params.setParam(MediaRequestHandlerParams.COMPRESSION,spec.getCompress());
		}
		try {
			File f = handleMediaItem(null,item,
					params.hasParamSet(MediaRequestHandlerParams.WANT_ORIGINAL),
					handler, params, InternalMediaResponse.INTERNAL_RESPONSE);
			result += f.length();
		} catch ( IOException e ) {
			throw new MediaAlbumException("Unable to calculate media size",e);
		}
	}
	return result;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#deleteMediaItemCacheFiles(magoffin.matt.ma.xsd.MediaItem, magoffin.matt.ma.xsd.User)
 */
public void deleteMediaItemCacheFiles(MediaItem item, User actingUser)
	throws MediaAlbumException 
{
	// get collection dir for item, then get list of cache files, 
	// then delete those files
	
	File dir = getCacheDir(item.getCollection());
	MediaRequestHandler handler = getHandlerForItem(item);
	FileFilter filter = new MediaItemCacheFileFilter(item,handler);
	File[] cacheFiles = dir.listFiles(filter);
	if ( cacheFiles == null ) return;
	for ( int i = 0; i < cacheFiles.length; i++ ) {
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Deleting cache file " +cacheFiles[i].getAbsolutePath());
		}
		cacheFiles[i].delete();
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.MediaItemBiz#getMediaItemGeometry(magoffin.matt.ma.xsd.MediaItem, java.lang.String, java.lang.String)
 */
public Geometry getMediaItemGeometry(MediaItem item, String sizeKey,
		String compressionKey) throws MediaAlbumException 
{
	MediaRequestHandler handler = getHandlerForItem(item);
	MediaRequestHandlerParams params = null;
	try {
		params = handler.getParamInstance();
		params.setParam(MediaRequestHandlerParams.SIZE,sizeKey);
		params.setParam(MediaRequestHandlerParams.COMPRESSION,compressionKey);
		handler.preProcessParams(item,params);
		return handler.getOutputGeometry(item,params);
	} finally {
		if ( params != null ) {
			handler.postProcessParams(item,params);
		}
	}
}

}
