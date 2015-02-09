/* ===================================================================
 * CollectionBizImpl.java
 * 
 * Created Dec 14, 2003 1:17:59 PM
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
 * $Id: CollectionBizImpl.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.biz.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import magoffin.matt.biz.BizInitializer;
import magoffin.matt.dao.CriteriaObjectPoolFactory;
import magoffin.matt.dao.DAO;
import magoffin.matt.dao.DAOException;
import magoffin.matt.dao.PrimaryKeyObjectPoolFactory;
import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MediaAlbumRuntimeException;
import magoffin.matt.ma.NotAuthorizedException;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.CollectionBiz;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.biz.UserBiz;
import magoffin.matt.ma.dao.CollectionCriteria;
import magoffin.matt.ma.dao.CollectionPK;
import magoffin.matt.ma.dao.MediaItemCriteria;
import magoffin.matt.ma.util.MediaUtil;
import magoffin.matt.ma.xsd.Collection;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.ma.xsd.User;
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
public class CollectionBizImpl extends AbstractBiz implements CollectionBiz {
	
	private static final Logger log = Logger.getLogger(CollectionBizImpl.class);

	private File baseCollectionPath = null;
	private Map collectionPathCache = null; // FIXME change to SimpleCache
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.CollectionBiz#getCollection(java.lang.Object)
 */
public Collection getCollectionById(Integer id, boolean allowCached) throws MediaAlbumException {
	if ( id == null ) {
		throw new MediaAlbumException("Null id passed to getCollectionById");
	}

	Collection result = (Collection)getCachedObject(
			allowCached,ApplicationConstants.CacheFactoryKeys.COLLECTION,id.toString());
	if ( result != null ) {
		// clone data because app may put items into collection
		try {
			return (Collection)BeanUtils.cloneBean(result);
		} catch ( Exception e ) {
			throw new MediaAlbumException("Unable to clone data", e);
		}
	}
	
	CollectionPK pk = null;
	ObjectPool pool = null;
	try {
		pool = PrimaryKeyObjectPoolFactory.getInstance().getPrimaryKeyObjectPool(Collection.class);
		pk = (CollectionPK)borrowObjectFromPool(pool);
		
		pk.setId(id);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Collection.class);
		
		result = (Collection)dao.get(pk);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,pk);
	}

	cacheObject(ApplicationConstants.CacheFactoryKeys.COLLECTION,id.toString(),result);
	
	try {
		return (Collection)BeanUtils.cloneBean(result);
	} catch ( Exception e ) {
		throw new MediaAlbumException("Unable to clone data", e);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.CollectionBiz#updateCollection(magoffin.matt.ma.xsd.Collection)
 */
public void updateCollection(Collection collection) throws MediaAlbumException {
	if ( collection == null ) {
		throw new MediaAlbumException("Null collection passed to updateCollection");
	}
	
	try {
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Collection.class);
		dao.update(collection);
	} catch ( DAOException e ) {
		throw new MediaAlbumException("DAO exception",e);
	}

	removeObjectFromCache(ApplicationConstants.CacheFactoryKeys.COLLECTION,collection.getCollectionId().toString());
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.CollectionBiz#createCollection(java.lang.String, magoffin.matt.ma.xsd.User)
 */
public Collection createCollection(String name, User owner) throws MediaAlbumException 
{
	if ( name == null || owner == null ) {
		throw new MediaAlbumException("Null name or owner passed to createCollection");
	}
	
	// create the new collection now
	File collectionDir = new File(baseCollectionPath, owner.getUserId().toString());
	collectionDir = new File(collectionDir,String.valueOf(System.currentTimeMillis()));
	if ( log.isDebugEnabled() ) {
		log.debug("Creatig new collection '" +collectionDir +"' for user " +owner.getUsername());
	}
	
	try {
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Collection.class);
		
		Collection collection = new Collection();
		collection.setOwner(owner.getUserId());
		collection.setName(name);
		collection.setPath(collectionDir.getAbsolutePath().substring(
				baseCollectionPath.getAbsolutePath().length() + 1)
				+ File.separatorChar);
		
		dao.create(collection);
		if ( !collectionDir.exists() ) {
			collectionDir.mkdirs();
		}

		return collection;

	} catch ( DAOException e ) {
		throw new MediaAlbumException("DAO exception",e);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.biz.Biz#init(magoffin.matt.biz.BizInitializer)
 */
public void init(BizInitializer initializer) {
	super.init(initializer);

	// get collection-base-file-path
	String collectionBaseFilePath = Config.get(ApplicationConstants.CONFIG_ENV,
			ApplicationConstants.ENV_BASE_FILE_PATH_COLLECTION);
	if ( collectionBaseFilePath == null ) {
		log.fatal("Required environment property '" +ApplicationConstants.ENV_BASE_FILE_PATH_COLLECTION
				+"' not provided.");
		throw new MediaAlbumRuntimeException(
			"Error configuring application, see error log for details.");
	}
	baseCollectionPath = new File(collectionBaseFilePath);
	if ( !baseCollectionPath.exists() || !baseCollectionPath.isDirectory() ) {
		log.fatal("Collection base dir " +collectionBaseFilePath +" not accessible.");
		throw new MediaAlbumRuntimeException(
			"Error configuring application, see error log for details.");
	}
	if ( log.isInfoEnabled() ) {
		log.info("Collection base dir is " +baseCollectionPath.getAbsolutePath());
	}
	
	collectionPathCache = new HashMap();
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.CollectionBiz#getAllCollections()
 */
public Collection[] getAllCollections() throws MediaAlbumException {
	CollectionCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(Collection.class);
		crit = (CollectionCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(CollectionCriteria.ALL_COLLECTIONS);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Collection.class);
		
		return (Collection[])dao.findByCriteria(crit);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.CollectionBiz#getMediaItemsForCollection(java.lang.Integer, boolean)
 */
public MediaItem[] getMediaItemsForCollection(Integer collectionId, boolean allowCached, User actingUser)
throws MediaAlbumException 
{
	if ( collectionId == null ) {
		throw new MediaAlbumException("Null id passed to getMediaItemsForCollection");
	}
	
	MediaItem[] result = (MediaItem[])getCachedObject(
			allowCached,ApplicationConstants.CacheFactoryKeys.COLLECTION_ITEMS,
			collectionId);
	
	if ( result != null ) {
		return result;
	}
	
	MediaItemCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				MediaItem.class);
		crit = (MediaItemCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(MediaItemCriteria.ITEMS_FOR_COLLECTION);
		crit.setQuery(collectionId);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				MediaItem.class);
		
		result = (MediaItem[])dao.findByCriteria(crit);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
	
	// adjust times if necessary
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	Collection gal = getCollectionById(collectionId,ApplicationConstants.CACHED_OBJECT_ALLOWED);
	User collectionOwner = userBiz.getUserById(gal.getOwner(),
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	MediaUtil.translateItemTime(collectionOwner.getTzCode(),result);

	cacheObject(ApplicationConstants.CacheFactoryKeys.COLLECTION_ITEMS,collectionId,
			result);
	
	MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);
	itemBiz.populateItems(result,ApplicationConstants.POPULATE_MODE_NONE,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
	return result; // not cloning results
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.CollectionBiz#deleteCollection(Integer, boolean, Integer, User)
 */
public void deleteCollection(
	Integer collectionId,
	boolean move,
	Integer moveToCollectionId,
	User actingUser)
	throws MediaAlbumException 
{
	// first get all media items for collection
	MediaItem[] items = getMediaItemsForCollection(collectionId,
			ApplicationConstants.CACHED_OBJECT_NOT_ALLOWED, actingUser);
	
	MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);
	
	// get the current collection
	Collection collection = getCollectionById(collectionId,
			ApplicationConstants.CACHED_OBJECT_NOT_ALLOWED);
	
	// now handle the move or delete of the individual items
	if ( move ) {
		
		if ( moveToCollectionId != null && moveToCollectionId.toString().equals(collection.getCollectionId().toString())) {
			// can't move to self!
			throw new MediaAlbumException(ERROR_DELETE_MOVE_TO_SELF);
		}
		
		doMoveItemsToCollection(moveToCollectionId,items,actingUser);
		
	} else {
		// delete media items
		if ( items != null ) {
			for ( int i = 0; i < items.length; i++ ) {
				itemBiz.deleteMediaItem(items[i].getItemId(),actingUser);
			}
		}
	}
	
	// delete the collection
	CollectionPK pk = null;
	ObjectPool pool = null;
	try {
		pool = PrimaryKeyObjectPoolFactory.getInstance().getPrimaryKeyObjectPool(Collection.class);
		pk = (CollectionPK)borrowObjectFromPool(pool);
		
		pk.setId(collectionId);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Collection.class);
		
		dao.remove(pk);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,pk);
	}
	
	// finally, delete the collection directory
	File collectionDir = getBaseCollectionDirectory(collection);
	delete(collectionDir);
}

private void delete(File f) {
	
	if ( f.isDirectory() ) {
		File[] children = f.listFiles();
		if (children != null ){
			for (int i = 0; i <children.length; i++) {
				delete(children[i]);
			}
		}
	}
	f.delete();
}

private void doMoveItemsToCollection(Integer collectionId, MediaItem[] items, User actingUser)
throws MediaAlbumException, NotAuthorizedException
{
	if ( collectionId == null ) {
		throw new MediaAlbumException("No collection specified for move");
	}
	
	MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);
	
	// get the move to collection
	Collection moveToCollection = getCollectionById(collectionId,
			ApplicationConstants.CACHED_OBJECT_NOT_ALLOWED);
	
	collectionId = moveToCollection.getCollectionId();
	
	// move items
	if ( items != null ) {
		for ( int i = 0; i < items.length; i++ ) {
			
			if ( items[i].getCollection().equals(collectionId) ) {
				// no need to move to self, so skip
				continue;
			}
			
			// get original collection
			Collection collection = getCollectionById(items[i].getCollection(),
					ApplicationConstants.CACHED_OBJECT_ALLOWED);
			
			items[i].setCollection(moveToCollection.getCollectionId());
			itemBiz.updateMediaItem(items[i],actingUser);
			
			// move actual file
			File oldPath = new File(getBaseCollectionDirectory(collection),items[i].getPath());
			File newPath = new File(getBaseCollectionDirectory(moveToCollection),items[i].getPath());
			File newParent = newPath.getParentFile();
			if ( !newParent.exists() ) {
				newPath.getParentFile().mkdirs(); // ensure dirs exist
			} else if ( newPath.exists() ){
				// if file exists, overwrite
				newPath.delete();
			}
			oldPath.renameTo(newPath);

			removeObjectFromCache(ApplicationConstants.CacheFactoryKeys.COLLECTION_ITEMS,
					collection.getCollectionId());
		}
	}
	
	removeObjectFromCache(ApplicationConstants.CacheFactoryKeys.COLLECTION_ITEMS,
			collectionId);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.CollectionBiz#moveItemsToCollection(java.lang.Object, magoffin.matt.ma.xsd.MediaItem[], magoffin.matt.ma.xsd.User)
 */
public void moveItemsToCollection(
	Integer collectionId,
	MediaItem[] items,
	User actingUser)
	throws MediaAlbumException, NotAuthorizedException 
{
	doMoveItemsToCollection(collectionId,items,actingUser);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.CollectionBiz#getBaseCollectionDirectory(magoffin.matt.ma.xsd.Collection)
 */
public File getBaseCollectionDirectory(Collection collection)
throws MediaAlbumException 
{
	Integer collectionId = collection.getCollectionId();
	if ( collectionPathCache.containsKey(collectionId) ) {
		return (File)collectionPathCache.get(collectionId);
	}
	File collectionDirectory = new File(baseCollectionPath,collection.getPath());
	collectionPathCache.put(collectionId,collectionDirectory);
	return collectionDirectory;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.CollectionBiz#canUserUpdateAlbum(magoffin.matt.ma.xsd.User, java.lang.Object)
 */
public boolean canUserUpdateCollection(User actingUser, Integer collectionId)
throws MediaAlbumException 
{
	Collection collection = getCollectionById(collectionId, ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
	// must be owner
	if ( collectionId != null && actingUser.getUserId().equals(collection.getOwner()) ) {
		return true;
	}
	return false;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.CollectionBiz#canUserViewCollection(magoffin.matt.ma.xsd.User, java.lang.Object)
 */
public boolean canUserViewCollection(User actingUser, Integer collectionId)
throws MediaAlbumException 
{
	Collection collection = getCollectionById(collectionId, ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
	// must be owner
	if ( collection != null && actingUser.getUserId().equals(collection.getOwner()) ) {
		return true;
	}
	return false;
}

}
