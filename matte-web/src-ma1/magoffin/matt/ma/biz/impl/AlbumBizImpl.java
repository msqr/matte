/* ===================================================================
 * AlbumBizImpl.java
 * 
 * Created Dec 16, 2003 9:05:36 PM
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
 * $Id: AlbumBizImpl.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.biz.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import magoffin.matt.dao.CriteriaObjectPoolFactory;
import magoffin.matt.dao.DAO;
import magoffin.matt.dao.DAOException;
import magoffin.matt.dao.PrimaryKeyObjectPoolFactory;
import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MessageConstants;
import magoffin.matt.ma.NotAuthorizedException;
import magoffin.matt.ma.biz.AlbumBiz;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.FreeDataBiz;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.biz.ThemeBiz;
import magoffin.matt.ma.biz.UserBiz;
import magoffin.matt.ma.dao.AlbumCriteria;
import magoffin.matt.ma.dao.AlbumMediaCriteria;
import magoffin.matt.ma.dao.AlbumMediaPK;
import magoffin.matt.ma.dao.AlbumPK;
import magoffin.matt.ma.dao.AlbumPermissionsCriteria;
import magoffin.matt.ma.dao.AlbumPermissionsPK;
import magoffin.matt.ma.dao.FreeDataCriteria;
import magoffin.matt.ma.dao.MediaItemCriteria;
import magoffin.matt.ma.util.ComparatorUtil;
import magoffin.matt.ma.util.MediaUtil;
import magoffin.matt.ma.xsd.Album;
import magoffin.matt.ma.xsd.AlbumMedia;
import magoffin.matt.ma.xsd.AlbumPermissions;
import magoffin.matt.ma.xsd.AlbumTheme;
import magoffin.matt.ma.xsd.FreeData;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.ma.xsd.User;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.pool.ObjectPool;
import org.apache.log4j.Logger;

/**
 * Biz implementation for AlbumBiz.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class AlbumBizImpl extends AbstractBiz implements AlbumBiz 
{
	private static final Logger log = Logger.getLogger(AlbumBizImpl.class);

	private static final FreeData[] NO_FREE_DATA = new FreeData[0];
	
	private static final Comparator SORT_BY_DISPLAY_ORDER = new ComparatorUtil.AlbumMediaDisplayOrderSort();
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.AlbumBiz#getAlbumById(java.lang.Object)
 */
public Album getAlbumById(Integer id, User actingUser, boolean allowCached) 
throws MediaAlbumException, NotAuthorizedException
{
	if ( id == null ) {
		throw new MediaAlbumException("Null id passed to getAlbumById");
	}
	
	Album result = (Album)getCachedObject(
			allowCached,ApplicationConstants.CacheFactoryKeys.ALBUM,id);
	if ( result != null ) {
		if ( !canUserViewAlbum(actingUser,result) ) {
			throw new NotAuthorizedException(actingUser.getUsername(), 
					MessageConstants.ERR_AUTH_VIEW_ALBUM);
		}
		try {
			return (Album)BeanUtils.cloneBean(result);
		} catch ( Exception e ) {
			throw new MediaAlbumException("Unable to clone data", e);
		}
	}
	
	AlbumPK pk = null;
	ObjectPool pool = null;
	try {
		pool = PrimaryKeyObjectPoolFactory.getInstance().getPrimaryKeyObjectPool(
				Album.class);
		pk = (AlbumPK)borrowObjectFromPool(pool);
		
		pk.setId(id);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Album.class);
		
		result = (Album)dao.get(pk);
		
		if ( result == null ) {
			return null;
		}
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,pk);
	}
	
	AlbumPermissions[] perm = getAlbumPermissions(result.getAlbumId(),
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	if ( perm != null ) {
		result.setPermissions(perm);
	}
	
	cacheObject(ApplicationConstants.CacheFactoryKeys.ALBUM,id,result);
	
	if ( !canUserViewAlbum(actingUser,result) ) {
		throw new NotAuthorizedException(actingUser.getUsername(), 
				MessageConstants.ERR_AUTH_VIEW_ALBUM);
	}
	
	try {
		return (Album)BeanUtils.cloneBean(result);
	} catch ( Exception e ) {
		throw new MediaAlbumException("Unable to clone data", e);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.AlbumBiz#getAlbumByKey(java.lang.String, magoffin.matt.ma.xsd.User)
 */
public Album getAlbumByKey(String key, User actingUser) 
throws MediaAlbumException, NotAuthorizedException 
{
	if ( key == null ) {
		throw new MediaAlbumException("Null key passed to getAlbumByKey");
	}
	
	Album result = null;
	Integer albumId = (Integer)getCachedObject(
			ApplicationConstants.CACHED_OBJECT_ALLOWED,
			ApplicationConstants.CacheFactoryKeys.ALBUM_KEYS,
			key);
	if ( albumId != null ) {
		return getAlbumById(albumId,actingUser,
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
	}
	
	AlbumCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				Album.class);
		crit = (AlbumCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(AlbumCriteria.ALBUM_FOR_KEY_SEARCH);
		crit.setQuery(key);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Album.class);
		
		Album[] results = (Album[])dao.findByCriteria(crit);
		
		if ( results != null && results.length > 0 ) {
			result = results[0];
		} else {
			return null;
		}
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} catch ( Exception e ) {
		throw new MediaAlbumException("Unable to get data", e);
	} finally {
		returnObjectToPool(pool,crit);
	}
	
	cacheObject(ApplicationConstants.CacheFactoryKeys.ALBUM_KEYS,
			key,result.getAlbumId());
	
	return getAlbumById(result.getAlbumId(),actingUser,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.AlbumBiz#deleteAlbum(java.lang.Object, magoffin.matt.ma.xsd.User)
 */
public void deleteAlbum(Integer id, User actingUser) throws MediaAlbumException 
{
	Album album = getAlbumById(id,actingUser,ApplicationConstants.CACHED_OBJECT_ALLOWED);

	if ( !canUserDeleteAlbum(actingUser,album) ) {
		throw new NotAuthorizedException(actingUser.getUsername(),
				MessageConstants.ERR_AUTH_UPDATE_ALBUM);
	}
	
	AlbumPK pk = null;
	ObjectPool pool = null;
	try {
		pool = PrimaryKeyObjectPoolFactory.getInstance().getPrimaryKeyObjectPool(
				Album.class);
		pk = (AlbumPK)borrowObjectFromPool(pool);
		
		pk.setId(id);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Album.class);
		
		dao.remove(pk);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,pk);
	}
	
	updatedAlbum(album);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.AlbumBiz#getMediaAlbumForItemKey(java.lang.Object, java.lang.String)
 */
public Album getAlbumForItemKey(Integer itemId, String key)
throws MediaAlbumException 
{
	if ( itemId == null || key == null ) {
		throw new MediaAlbumException("Null itemId or key passed to getMediaAlbumForItemKey");
	}
	
	AlbumCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				Album.class);
		crit = (AlbumCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(AlbumCriteria.ALBUM_FOR_ITEM_KEY);
		crit.setQuery(new String[]{itemId.toString(),key});
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Album.class);
		
		Album[] results = (Album[])dao.findByCriteria(crit);
		
		if ( results == null || results.length < 1 ) {
			return null;
		}
		
		return results[0];
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} catch ( Exception e ) {
		throw new MediaAlbumException("Unable to get data", e);
	} finally {
		returnObjectToPool(pool,crit);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.AlbumBiz#getAlbumChildren(java.lang.Object)
 */
public Album[] getAlbumChildren(Integer albumId)
throws MediaAlbumException 
{
	if ( albumId == null ) {
		throw new MediaAlbumException("Null albumId passed to getAlbumChildren");
	}
	
	AlbumCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				Album.class);
		crit = (AlbumCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(AlbumCriteria.CHILDREN_OF_ALBUM_SEARCH);
		crit.setQuery(albumId);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Album.class);
		
		Album[] results = (Album[])dao.findByCriteria(crit);
		
		if ( results == null || results.length < 1 ) {
			return null;
		}
		
		return results;
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} catch ( Exception e ) {
		throw new MediaAlbumException("Unable to get data", e);
	} finally {
		returnObjectToPool(pool,crit);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.AlbumBiz#canUserDeleteAlbum(magoffin.matt.ma.xsd.User, java.lang.Object)
 */
public boolean canUserDeleteAlbum(User actingUser, Integer albumId)
throws MediaAlbumException 
{
	Album album = getAlbumById(albumId,null, ApplicationConstants.CACHED_OBJECT_ALLOWED);
	return canUserDeleteAlbum(actingUser,album);
}

private boolean canUserDeleteAlbum(User actingUser, Album album)
{
	if ( actingUser == null ) return true;
	
	// for now, we're only checking ownership
	if ( album != null && actingUser.getUserId().equals(album.getOwner()) ) {
		return true;
	}
	
	return false;
}


/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.AlbumBiz#canUserViewAlbum(magoffin.matt.ma.xsd.User, java.lang.String)
 */
public boolean canUserViewAlbum(User actingUser, Integer albumId)
throws MediaAlbumException 
{
	Album album = getAlbumById(albumId,null, ApplicationConstants.CACHED_OBJECT_ALLOWED);
	return canUserViewAlbum(actingUser,album);
}

private boolean canUserViewAlbum(User actingUser, Album album)
throws MediaAlbumException 
{
	if ( actingUser == null ) return true;
	
	Boolean allowAnon = album.getAllowAnonymous();
	
	if ( album == null ) {
		return false;
	}
	
	// check for anonymous first
	if ( allowAnon != null && allowAnon.booleanValue() ) {
		return true;
	}
	
	Integer userId = actingUser.getUserId();
	
	// check if owner
	if ( actingUser != null && userId.equals(album.getOwner()) ) {
		return true;
	}
	
	// check album permissions for view
	if ( album.getPermissionsCount() > 0 ) {
		int count = album.getPermissionsCount();
		UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
		for ( int i = 0; i < count; i++ ) {
			AlbumPermissions perm = album.getPermissions(i);
			if ( !perm.getView().booleanValue() ) {
				continue;
			}
			if ( perm.getUserId() != null ) {
				if ( userId.equals(perm.getUserId()) ) {
					return true;
				}
			} else if ( perm.getGroupId() != null ) {
				if ( userBiz.isUserMemberOfGroup(userId,perm.getGroupId()) ) {
					return true;
				}
			}
		}
	}
	
	if ( log.isDebugEnabled() ) {
		log.debug("User " +actingUser.getUsername() 
				+" does not have permission to view album " +album.getAlbumId());
	}
	
	return false;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.AlbumBiz#createAlbum(magoffin.matt.ma.xsd.Album)
 */
public Album createAlbum(Album album, User actingUser)
throws MediaAlbumException 
{
	if ( album == null ) {
		throw new MediaAlbumException("Null album passed to createAlbum");
	}
	
	if ( log.isDebugEnabled() ) {
		log.debug("Creating new album: " +album.getName());
	}
	
	try {
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Album.class);
		
		Album newAlbum = (Album)BeanUtils.cloneBean(album);
		newAlbum.setOwner(actingUser.getUserId());
		newAlbum.setCreationDate(new Date());
		
		// see if owner has default theme
		if ( actingUser.getDefaultThemeId() != null ) {
			newAlbum.setThemeId(actingUser.getDefaultThemeId());
		}
		
		dao.create(newAlbum);
		
		return newAlbum;

	} catch ( DAOException e ) {
		throw new MediaAlbumException("DAO exception",e);
	} catch ( Exception e ) {
		throw new MediaAlbumException("Unknown exception creating new album",e);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.AlbumBiz#canUserUpdateAlbum(magoffin.matt.ma.xsd.User, java.lang.Object)
 */
public boolean canUserUpdateAlbum(User actingUser, Integer albumId)
throws MediaAlbumException 
{
	Album album = getAlbumById(albumId, null, ApplicationConstants.CACHED_OBJECT_ALLOWED);
	return canUserUpdateAlbum(actingUser,album);
}

private boolean canUserUpdateAlbum(User actingUser, Album album)
throws MediaAlbumException 
{
	if ( actingUser == null ) return true;
	
	Integer userId = actingUser.getUserId();
	
	// check if owner
	if ( album != null && userId.equals(album.getOwner()) ) {
		return true;
	}

	// check album permissions for update
	if ( album.getPermissionsCount() > 0 ) {
		int count = album.getPermissionsCount();
		UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
		for ( int i = 0; i < count; i++ ) {
			AlbumPermissions perm = album.getPermissions(i);
			if ( !perm.getUpdate().booleanValue() ) {
				continue;
			}
			if ( perm.getUserId() != null ) {
				if ( userId.equals(perm.getUserId()) ) {
					return true;
				}
			} else if ( perm.getGroupId() != null ) {
				if ( userBiz.isUserMemberOfGroup(userId,perm.getGroupId()) ) {
					return true;
				}
			}
		}
	}
	
	return false;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.AlbumBiz#updateAlbum(magoffin.matt.ma.xsd.Album, magoffin.matt.ma.xsd.User)
 */
public Album updateAlbum(Album album, User actingUser)
throws MediaAlbumException, NotAuthorizedException
{
	if ( album == null ) {
		throw new MediaAlbumException("Null album passed to updateAlbum");
	}
	
	if ( !canUserUpdateAlbum(actingUser,album) ) {
		throw new NotAuthorizedException(actingUser.getUsername(),
				MessageConstants.ERR_AUTH_UPDATE_ALBUM);
	}
	
	album.setModificationDate(new Date());
	
	try {
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Album.class);
		dao.update(album);
	} catch ( DAOException e ) {
		throw new MediaAlbumException("DAO exception",e);
	}
	
	updatedAlbum(album);
	return album;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.AlbumBiz#getMediaItemsForAlbum(java.lang.Object, boolean)
 */
public MediaItem[] getMediaItemsForAlbum(
	Integer albumId,
	int itemPopulateMode, 
	boolean allowCached, User actingUser)
	throws MediaAlbumException 
{
	if ( albumId == null ) {
		throw new MediaAlbumException("Null id passed to getMediaItemsForAlbum");
	}
	
	MediaItem[] result = (MediaItem[])getCachedObject(allowCached,
			ApplicationConstants.CacheFactoryKeys.ALBUM_ITEMS,albumId);
	
	if ( result != null ) {
		return result;
	}
	
	MediaItemCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				MediaItem.class);
		crit = (MediaItemCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(MediaItemCriteria.ITEMS_FOR_ALBUM);
		crit.setQuery(albumId);
		
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
	Album album = getAlbumById(albumId,actingUser,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	User albumOwner = userBiz.getUserById(album.getOwner(),
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	MediaUtil.translateItemTime(albumOwner.getTzCode(),result);

	// populate item data
	MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);
	itemBiz.populateItems(result,itemPopulateMode,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
	cacheObject(ApplicationConstants.CacheFactoryKeys.ALBUM_ITEMS,albumId,
			result);

	return result;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.AlbumBiz#setAlbumMediaItems(java.lang.Object, magoffin.matt.ma.xsd.AlbumMedia[], magoffin.matt.ma.xsd.User)
 */
public void setAlbumMediaItems(
	Integer albumId,
	AlbumMedia[] items,
	User actingUser)
	throws MediaAlbumException, NotAuthorizedException 
{
	if ( albumId == null ) {
		throw new MediaAlbumException("Null albumId passed to setAlbumMediaItems");
	}
	
	Album album = getAlbumById(albumId,actingUser,ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
	if ( !canUserUpdateAlbum(actingUser,album) ) {
		throw new NotAuthorizedException(actingUser.getUsername(),
				MessageConstants.ERR_AUTH_UPDATE_ALBUM);
	}
	
	// first delete all media items for album
	AlbumMediaPK pk = null;
	ObjectPool pool = null;
	DAO dao = null;
	try {
		pool = PrimaryKeyObjectPoolFactory.getInstance().getPrimaryKeyObjectPool(
				AlbumMedia.class);
		pk = (AlbumMediaPK)borrowObjectFromPool(pool);
		
		pk.setId(albumId);
		
		dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				AlbumMedia.class);

		dao.remove(pk);
		
		// second sort items and add to the album
		if ( items != null && items.length > 0 ) {
			Arrays.sort(items,SORT_BY_DISPLAY_ORDER);
			for ( int i = 0; i < items.length; i++ ) {
				items[i].setDisplayOrder(new Integer(i+1));
			}
			dao.create(items);
		}
		
	} catch ( DAOException e ) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,pk);
	}
	
	updatedAlbum(album);
}

private void updatedAlbum(Album album) {
	removeObjectFromCache(ApplicationConstants.CacheFactoryKeys.ALBUM,
			album.getAlbumId());
	removeObjectFromCache(ApplicationConstants.CacheFactoryKeys.ALBUM_KEYS,
			album.getAnonymousKey());
	removeObjectFromCache(ApplicationConstants.CacheFactoryKeys.ALBUM_ITEMS,
			album.getAlbumId());	
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.AlbumBiz#getAlbumThemeForAlbum(java.lang.Object, boolean)
 */
public AlbumTheme getAlbumThemeForAlbum(Integer albumId, boolean allowCached)
throws MediaAlbumException 
{
	if ( albumId == null ) {
		throw new MediaAlbumException("Null id passed to getAlbumThemeForAlbum");
	}
	
	Album album = getAlbumById(albumId,null,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
	if ( album == null || album.getThemeId() == null ) {
		return null;
	}
	
	ThemeBiz themeBiz = (ThemeBiz)getBiz(BizConstants.THEME_BIZ);
	return themeBiz.getAlbumThemeById(album.getThemeId(),null,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.AlbumBiz#getAlbumPermissions(java.lang.Object, boolean)
 */
public AlbumPermissions[] getAlbumPermissions(
	Integer albumId,
	boolean allowCached)
	throws MediaAlbumException 
{
	if ( albumId == null ) {
		return null;
	}
	
	AlbumPermissions[] result = (AlbumPermissions[])getCachedObject(
			allowCached,ApplicationConstants.CacheFactoryKeys.ALBUM_PERMISSIONS,
			albumId);
	if ( result != null ) {
		return result;
	}
	
	AlbumPermissionsCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				AlbumPermissions.class);
		crit = (AlbumPermissionsCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(AlbumPermissionsCriteria.PERMISSIONS_FOR_ALBUM);
		crit.setQuery(albumId);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				AlbumPermissions.class);
		
		result = (AlbumPermissions[])dao.findByCriteria(crit);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} catch ( Exception e ) {
		throw new MediaAlbumException("Unable to get data", e);
	} finally {
		returnObjectToPool(pool,crit);
	}
	
	cacheObject(ApplicationConstants.CacheFactoryKeys.ALBUM_PERMISSIONS,albumId,
			result);
	
	return result;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.AlbumBiz#updateAlbumPermissions(java.lang.Object, magoffin.matt.ma.xsd.AlbumPermissions[], magoffin.matt.ma.xsd.User)
 */
public void updateAlbumPermissions(
	Integer albumId,
	AlbumPermissions[] permissions,
	User actingUser)
	throws MediaAlbumException, NotAuthorizedException 
{
	if ( albumId == null ) {
		throw new MediaAlbumException("Null albumId passed to updateAlbumPermissions");
	}
	
	Album album = getAlbumById(albumId,actingUser,ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
	// must be owner to update permissions
	if ( !album.getOwner().equals(actingUser.getUserId()) ) {
		throw new NotAuthorizedException(actingUser.getUsername(),
				MessageConstants.ERR_AUTH_UPDATE_ALBUM);
	}
	
	AlbumPermissionsPK pk = null;
	ObjectPool pool = null;
	try {
		// first delete
		pool = PrimaryKeyObjectPoolFactory.getInstance().getPrimaryKeyObjectPool(
				AlbumPermissions.class);
		pk = (AlbumPermissionsPK)borrowObjectFromPool(pool);
		
		pk.setAlbumId(albumId);

		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				AlbumPermissions.class);
		dao.remove(pk);
		
		// now insert new rows for each permissions
		if ( permissions != null && permissions.length > 0) {
			for ( int i = 0; i < permissions.length; i++ ) {
				permissions[i].setPermId(album.getAlbumId());
			}
			dao.create(permissions);
		}
	} catch ( DAOException e ) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,pk);
	}
	removeObjectFromCache(ApplicationConstants.CacheFactoryKeys.ALBUM_PERMISSIONS,
			albumId);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.AlbumBiz#fillInChildAlbums(magoffin.matt.ma.xsd.Album, java.lang.Integer, magoffin.matt.ma.xsd.User)
 */
public Album fillInChildAlbums(
	Album album,
	Integer fillInItemsAlbumId,
	int itemPopulateMode, 
	User actingUser, 
	int maxLevels)
	throws MediaAlbumException, NotAuthorizedException 
{
	Album populatedAlbum = null;
	
	// popluate items if item ID matches
	if ( fillInItemsAlbumId != null && fillInItemsAlbumId.equals(album.getAlbumId()) ) {
		MediaItem[] items = getMediaItemsForAlbum(album.getAlbumId(),
				itemPopulateMode, 
				ApplicationConstants.CACHED_OBJECT_ALLOWED, 
				actingUser);
		
		sortAlbumItems(album,items);
		
		album.setItem(items);
		populatedAlbum = album;
	}

	// get any albums with the parent ID as this album's ID
	Album[] children = getAlbumChildren(album.getAlbumId());
	
	if ( children == null || maxLevels == 0 ) {
		return populatedAlbum;
	}
	
	for ( int i = 0; i < children.length; i++ ) {
		if ( canUserViewAlbum(actingUser,children[i]) ) {
			Album popAlbum2 = fillInChildAlbums(
					children[i],fillInItemsAlbumId, 
					itemPopulateMode, actingUser, maxLevels - 1);
			if ( populatedAlbum == null && popAlbum2 != null ) {
				populatedAlbum = popAlbum2;
			}
			album.addAlbum(children[i]);
		}
	}
	return populatedAlbum;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.AlbumBiz#sortAlbumItems(magoffin.matt.ma.xsd.Album, magoffin.matt.ma.xsd.MediaItem[])
 */
public void sortAlbumItems(Album album, MediaItem[] items)
throws MediaAlbumException 
{
	if ( album == null || album.getSortMode() == null || album.getSortMode().intValue() < 1
			|| items == null || items.length < 1 ) {
		return;
	}
	
	if ( log.isDebugEnabled() ) {
		log.debug("Sorting media items according to album sort mode " +album.getSortMode() );
	}
	
	int sortMode = album.getSortMode().intValue();
	
	switch ( sortMode ) {
		case ApplicationConstants.SORT_MODE_NAME:
			// sort by name
			Arrays.sort(items,MediaUtil.MEDIA_ITEM_SORT_BY_NAME);
			if ( log.isDebugEnabled() ) {
				StringBuffer buf = new StringBuffer();
				buf.append("Sort by name results:\n");
				for ( int i = 0; i < items.length; i++ ) {
					String s = items[i].getName();
					if ( s == null ) {
						s = items[i].getPath();
					}
					buf.append(items[i].getItemId()).append(": ").append(s).append("\n");
				}
				log.debug(buf.toString());
			}
			break;
			
		case ApplicationConstants.SORT_MODE_CREATION_DATE:
			// sort by creation date
			Arrays.sort(items,MediaUtil.MEDIA_ITEM_SORT_BY_CREATION_DATE);
			if ( log.isDebugEnabled() ) {
				StringBuffer buf = new StringBuffer();
				buf.append("Sort by date results:\n");
				for ( int i = 0; i < items.length; i++ ) {
					buf.append(items[i].getItemId()).append(": ").append(items[i].getCreationDate()).append("\n");
				}
				log.debug(buf.toString());
			}
			break;
			
		default:
			// do nothing!
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.AlbumBiz#setAlbumTheme(java.lang.Integer, java.lang.Integer, magoffin.matt.ma.xsd.User)
 */
public void setAlbumTheme(Integer themeId, Integer albumId, User actingUser)
		throws MediaAlbumException, NotAuthorizedException 
{
	Album album = this.getAlbumById(albumId,actingUser,
			ApplicationConstants.CACHED_OBJECT_NOT_ALLOWED);
	
	// TODO verify user can use this theme
	
	ThemeBiz themeBiz = (ThemeBiz)getBiz(BizConstants.THEME_BIZ);
	AlbumTheme defaultTheme = themeBiz.getDefaultAlbumTheme();
	if ( themeId != null && themeId.equals(defaultTheme.getThemeId()) ) {
		themeId = null; // revert to default
	}
	
	if ( (themeId == null && album.getThemeId() == null ) || (themeId != null && themeId.equals(album.getThemeId())) ) {
		// nothing to change
		return;
	}
	
	if ( log.isDebugEnabled() ) {
		log.debug("Setting theme to " +themeId +" for album " +albumId);
	}
	
	album.setThemeId(themeId);
	updateAlbum(album,actingUser);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.AlbumBiz#populateAlbumFreeData(magoffin.matt.ma.xsd.Album[], boolean)
 */
public void populateAlbumFreeData(Album[] albums, boolean allowCached)
throws MediaAlbumException 
{
	Integer[] albumIds = null;
	
	if ( allowCached ) {
		List albumIdList = new ArrayList(albums.length);
		for ( int i = 0; i < albums.length; i++ ) {
			FreeData[] tmp = (FreeData[])getCachedObject(allowCached,
					ApplicationConstants.CacheFactoryKeys.ALBUM_FREE_DATA,
					albums[i].getAlbumId());
			if ( tmp == null ) {
				albumIdList.add(albums[i].getAlbumId());
			} else {
				albums[i].setData(tmp);
			}
		}
		if ( albumIdList.size() < 1 ) {
			// all done!
			return;
		}
		albumIds = (Integer[])albumIdList.toArray(new Integer[albumIdList.size()]);
	}
	
	if ( albumIds == null ) {
		// set to all ids
		albumIds = new Integer[albums.length];
		for ( int i = 0; i < albums.length; i++ ) {
			albumIds[i] = albums[i].getAlbumId();
		}
	}
	
	// sort ids
	Arrays.sort(albumIds);
	
	FreeData[] results = null;
	FreeDataCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				FreeData.class);
		crit = (FreeDataCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(FreeDataCriteria.FREE_DATA_FOR_ALBUMS);
		crit.setQuery(albumIds);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				FreeData.class);
		
		results = (FreeData[])dao.findByCriteria(crit);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
	
	FreeDataBiz fdBiz = (FreeDataBiz)getBiz(BizConstants.FREE_DATA_BIZ);
	fdBiz.populateFreeDataTypeNames(results,allowCached);
	
	int dataIdx = 0;
	for ( int i = 0; i < albums.length; i++ ) {
		Integer albumId = albums[i].getAlbumId();
		while ( dataIdx < results.length && !albumId.equals(results[dataIdx].getAlbumId()) ) {
			dataIdx++;
		}
		
		if ( dataIdx == results.length ) {
			dataIdx = 0;
			cacheObject(ApplicationConstants.CacheFactoryKeys.ALBUM_FREE_DATA,
					albums[i].getAlbumId(), NO_FREE_DATA);
			continue;
		}
		
		while ( dataIdx < results.length && albumId.equals(results[dataIdx].getAlbumId()) ) {
			albums[i].addData(results[dataIdx]);
			dataIdx++;
		}
		cacheObject(ApplicationConstants.CacheFactoryKeys.ALBUM_FREE_DATA,
				albums[i].getAlbumId(), albums[i].getData());
		dataIdx = 0;
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.AlbumBiz#getAlbumFreeData(java.lang.Integer, boolean)
 */
public FreeData[] getFreeData(Integer albumId, boolean allowCached)
		throws MediaAlbumException
{
	FreeData[] result = (FreeData[])getCachedObject(allowCached,
			ApplicationConstants.CacheFactoryKeys.ALBUM_FREE_DATA,
			albumId);
	if ( result != null ) {
		return result;
	}
	
	FreeDataCriteria crit = null;
	ObjectPool pool = null;
	
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				FreeData.class);
		crit = (FreeDataCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(FreeDataCriteria.FREE_DATA_FOR_ALBUM);
		crit.setQuery(albumId);
		
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
	
	cacheObject(ApplicationConstants.CacheFactoryKeys.ALBUM_FREE_DATA,
			albumId,
			result);
	
	return result;
}


/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.AlbumBiz#findAlbum(magoffin.matt.ma.xsd.Album[], java.lang.Integer)
 */
public Album findAlbum(Album[] albums, Integer albumId)
		throws MediaAlbumException
{
	if ( albums == null || albumId == null ) return null;
	for ( int i = 0; i < albums.length; i++ ) {
		if ( albumId.equals(albums[i].getAlbumId()) ) {
			return albums[i];
		}
		
		// recurse to check children
		if ( albums[i].getAlbumCount() > 0 ) {
			Album a = findAlbum(albums[i].getAlbum(),albumId);
			if ( a != null ) {
				return a;
			}
		}
	}
	return null;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.AlbumBiz#addMediaItemsToAlbum(java.lang.Integer, java.lang.Integer[], magoffin.matt.ma.xsd.User)
 */
public void addMediaItemsToAlbum(Integer albumId, Integer[] itemIds,
		User actingUser) throws MediaAlbumException 
{
	// verify can update album
	if ( !canUserUpdateAlbum(actingUser,albumId) ) {
		throw new NotAuthorizedException(actingUser.getUsername(),
				MessageConstants.ERR_AUTH_UPDATE_ALBUM);
	}
	
	// verify can view all media items
	MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);
	for ( int i = 0; i < itemIds.length; i++ ) {
		if ( !itemBiz.canUserViewMediaItem(itemIds[i],actingUser) ) {
			throw new NotAuthorizedException(actingUser.getUsername(),
					MessageConstants.ERR_AUTH_VIEW_MEDIA_ITEM);
		}
	}
	
	// get current album contents to verify not already in album
	AlbumMediaCriteria crit = null;
	ObjectPool pool = null;
	DAO dao = null;
	AlbumMedia[] media = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				AlbumMedia.class);
		crit = (AlbumMediaCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(AlbumMediaCriteria.ALBUM_MEDIA_FOR_ALBUM);
		crit.setQuery(albumId);
		
		dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				AlbumMedia.class);

		media = (AlbumMedia[])dao.findByCriteria(crit);
		
	} catch ( DAOException e ) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
	
	// remove any items already in album
	Map idMap = null;
	List mediaToAddList = null;
	AlbumMedia[] mediaToAdd = null;
	try {
		idMap = (Map)borrowObjectFromPool(hashMapPool);
		mediaToAddList = (List)borrowObjectFromPool(arrayListPool);
		for ( int i = 0; i < media.length; i++ ) {
			idMap.put(media[i].getMediaId(),media);
		}
		for ( int i = 0; i < itemIds.length; i++ ) {
			if ( !idMap.containsKey(itemIds[i]) ) {
				AlbumMedia am = new AlbumMedia();
				am.setAlbumId(albumId);
				am.setMediaId(itemIds[i]);
				am.setDisplayOrder(new Integer(media.length+mediaToAddList.size()+1));
				mediaToAddList.add(am);
			}
		}
		mediaToAdd = (AlbumMedia[])mediaToAddList.toArray(
				new AlbumMedia[mediaToAddList.size()]);
	} finally {
		returnObjectToPool(hashMapPool,idMap);
		returnObjectToPool(arrayListPool,mediaToAddList);
	}
	
	if ( mediaToAdd.length > 0 ) {
		try {
			dao.create(mediaToAdd);
		} catch ( DAOException e ) {
			throw new MediaAlbumException("DAO exception",e);
		}
		Album album = getAlbumById(albumId,actingUser,
				ApplicationConstants.CACHED_OBJECT_NOT_ALLOWED);
		updateAlbum(album,actingUser); // to update modified date
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.AlbumBiz#getAlbumsById(java.lang.Integer[], magoffin.matt.ma.xsd.User, boolean)
 */
public Album[] getAlbumsById(Integer[] albumIds, User actingUser,
		boolean allowCached) 
throws MediaAlbumException, NotAuthorizedException 
{
	if ( albumIds == null ) {
		throw new MediaAlbumException("Null IDs passed to getAlbumsById");
	}
	
	List results = null;
	try {
		results =  (List)arrayListPool.borrowObject();

		for ( int i = 0; i < albumIds.length; i++ ) {
			Album album = getAlbumById(albumIds[i], actingUser, allowCached);
			if ( album != null ) {
				results.add(album);
			}
		}
		
		return (Album[])results.toArray(new Album[results.size()]);
	} catch ( MediaAlbumException e ) {
		throw e;
	} catch ( Exception e ) {
		throw new MediaAlbumException("Unable to get albums by ID",e);
	} finally {
		returnObjectToPool(arrayListPool,results);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.AlbumBiz#removeMediaItemsFromAlbum(java.lang.Integer, java.lang.Integer[], magoffin.matt.ma.xsd.User)
 */
public int removeMediaItemsFromAlbum(Integer albumId, Integer[] itemIds,
		User actingUser) throws MediaAlbumException, NotAuthorizedException 
{
	// verify can update album
	if ( !canUserUpdateAlbum(actingUser,albumId) ) {
		throw new NotAuthorizedException(actingUser.getUsername(),
				MessageConstants.ERR_AUTH_UPDATE_ALBUM);
	}
	
	int result = 0;
	AlbumMediaCriteria crit = null;
	ObjectPool pool = null;
	DAO dao = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				AlbumMedia.class);
		crit = (AlbumMediaCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(AlbumMediaCriteria.ALBUM_MEDIA_SUBSET);
		
		Integer[] queryIds = new Integer[itemIds.length+1];
		queryIds[0] = albumId;
		System.arraycopy(itemIds,0,queryIds,1,itemIds.length);
		crit.setQuery(queryIds);
		
		dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				AlbumMedia.class);

		result = dao.remove(crit);
		
	} catch ( DAOException e ) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
	
	if ( result > 0 ) {
		Album album = getAlbumById(albumId,actingUser,
				ApplicationConstants.CACHED_OBJECT_NOT_ALLOWED);
		updateAlbum(album,actingUser); // to update modification date
	}
	
	return result;
}

}
