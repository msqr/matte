/* ===================================================================
 * UserBiz.java
 * 
 * Created Nov 30, 2003.
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
 * $Id: UserBizImpl.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.biz.impl;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import magoffin.matt.biz.BizInitializer;
import magoffin.matt.dao.CriteriaObjectPoolFactory;
import magoffin.matt.dao.DAO;
import magoffin.matt.dao.DAOException;
import magoffin.matt.dao.DAOSearchCallback;
import magoffin.matt.dao.DataObject;
import magoffin.matt.dao.PrimaryKeyObjectPoolFactory;
import magoffin.matt.gerdal.dataobjects.CountData;
import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MediaAlbumValidationException;
import magoffin.matt.ma.MessageConstants;
import magoffin.matt.ma.NotAuthorizedException;
import magoffin.matt.ma.NotInitializedException;
import magoffin.matt.ma.biz.AlbumBiz;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.CollectionBiz;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.biz.UserBiz;
import magoffin.matt.ma.dao.AlbumCriteria;
import magoffin.matt.ma.dao.CollectionCriteria;
import magoffin.matt.ma.dao.DAOConstants;
import magoffin.matt.ma.dao.FreeDataCriteria;
import magoffin.matt.ma.dao.FreeDataPK;
import magoffin.matt.ma.dao.GroupCriteria;
import magoffin.matt.ma.dao.GroupPK;
import magoffin.matt.ma.dao.InvitationCriteria;
import magoffin.matt.ma.dao.InvitationPK;
import magoffin.matt.ma.dao.LongNumCallback;
import magoffin.matt.ma.dao.MediaItemCriteria;
import magoffin.matt.ma.dao.MemberPK;
import magoffin.matt.ma.dao.PermissionsPK;
import magoffin.matt.ma.dao.RegistrationCriteria;
import magoffin.matt.ma.dao.RegistrationPK;
import magoffin.matt.ma.dao.UserCriteria;
import magoffin.matt.ma.dao.UserPK;
import magoffin.matt.ma.util.ComparatorUtil;
import magoffin.matt.ma.util.MediaSpecUtil;
import magoffin.matt.ma.util.MediaUtil;
import magoffin.matt.ma.xsd.Album;
import magoffin.matt.ma.xsd.AlbumPermissions;
import magoffin.matt.ma.xsd.AlbumTheme;
import magoffin.matt.ma.xsd.Collection;
import magoffin.matt.ma.xsd.FreeData;
import magoffin.matt.ma.xsd.Friend;
import magoffin.matt.ma.xsd.Group;
import magoffin.matt.ma.xsd.Invitation;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.ma.xsd.Member;
import magoffin.matt.ma.xsd.Permissions;
import magoffin.matt.ma.xsd.Registration;
import magoffin.matt.ma.xsd.User;
import magoffin.matt.ma.xsd.UserSearchData;
import magoffin.matt.ma.xsd.UserSearchResults;
import magoffin.matt.mail.MailProcessingException;
import magoffin.matt.mail.biz.MailBiz;
import magoffin.matt.util.HashMapPoolableFactory;
import magoffin.matt.util.MessageDigester;
import magoffin.matt.util.StringUtil;
import magoffin.matt.util.config.Config;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.ArrayStack;
import org.apache.commons.collections.Buffer;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.StackObjectPool;
import org.apache.log4j.Logger;

/**
 * Biz implementation for UserBiz.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class UserBizImpl extends AbstractBiz implements UserBiz {
	
	private static final Logger log = Logger.getLogger(UserBizImpl.class);
	
	private static final ObjectPool MAP_POOL = 
		new StackObjectPool(new HashMapPoolableFactory(),5,10);
	
	private static final SimpleDateFormat MONTH_NAME = new SimpleDateFormat("MMMM");

	/** The salt used to encrypt passwords. */
	private static final byte[] SALT = new byte[0];
	
	/** The default permissions for a user if nothing assigned. */
	private static final Permissions DEFAULT_PERMISSIONS = new Permissions();
	
	static {
		DEFAULT_PERMISSIONS.setAssignCreateUser(Boolean.FALSE);
		DEFAULT_PERMISSIONS.setAssignSuperUser(Boolean.FALSE);
		DEFAULT_PERMISSIONS.setCreateUser(Boolean.FALSE);
		DEFAULT_PERMISSIONS.setSuperUser(Boolean.FALSE);
	}
	
	private static final User INTERNAL_USER = new User();
	
	private static final Comparator REVERSE_COMPARABLE_SORT = 
			new ComparatorUtil.ComparableReverseSort();
	
	private String newUserCollectionName = null;

/* (non-Javadoc)
 * @see magoffin.matt.biz.Biz#init(magoffin.matt.biz.BizInitializer)
 */
public void init(BizInitializer initializer) 
{
	super.init(initializer);
	newUserCollectionName = Config.getNotEmpty(
			ApplicationConstants.CONFIG_ENV,
			ApplicationConstants.ENV_NEW_USER_COLLECTION_NAME);
}
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBizIntf#getUserByUsername(java.lang.String)
 */
public User getUserByUsername(String username) throws MediaAlbumException 
{
	if ( username == null ) {
		throw new MediaAlbumException("Null username passed to getUserByUsername");
	}

	UserCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(User.class);
		crit = (UserCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(UserCriteria.USERNAME_SEARCH);
		crit.setQuery(username);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				User.class);
		
		return (User)(dao.findByCriteria(crit)[0]);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} catch ( IndexOutOfBoundsException e ) {
		return null;
	} finally {
		returnObjectToPool(pool,crit);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBizIntf#getUserById(java.lang.Object)
 */
public User getUserById(Object id, boolean allowCached) throws MediaAlbumException {
	if ( id == null ) {
		throw new MediaAlbumException("Null id passed to getUserById");
	}

	User result = (User)getCachedObject(allowCached,
			ApplicationConstants.CacheFactoryKeys.USER,id.toString());
	if ( result != null ) {
		return result;
	}
	
	UserPK pk = null;
	ObjectPool pool = null;
	try {
		pool = PrimaryKeyObjectPoolFactory.getInstance().getPrimaryKeyObjectPool(User.class);
		pk = (UserPK)borrowObjectFromPool(pool);
		
		pk.setKey(id);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				User.class);
		
		Object o = dao.get(pk);
		if ( o == null ) {
			throw new MediaAlbumException("ID not found",ERROR_USER_NOT_FOUND);
		}
		result = (User)o;
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,pk);
	}
	
	cacheObject(ApplicationConstants.CacheFactoryKeys.USER,id.toString(),result);
	
	return result;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBizIntf#getUserByAnonymousKey(java.lang.String)
 */
public User getUserByAnonymousKey(String key) throws MediaAlbumException {
	if ( key == null ) {
		throw new MediaAlbumException("Null key passed to getUserByAnonymousKey");
	}

	UserCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(User.class);
		crit = (UserCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(UserCriteria.ANONYMOUS_KEY_SEARCH);
		crit.setQuery(key);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				User.class);
		
		Object[] o = dao.findByCriteria(crit);
		if ( o == null || o.length < 1 ) {
			throw new MediaAlbumException("Key not found",ERROR_USER_NOT_FOUND);
		}
		return (User)o[0];
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} catch ( IndexOutOfBoundsException e ) {
		return null;
	} finally {
		returnObjectToPool(pool,crit);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBizIntf#getAllUsers()
 */
public User[] getAllUsers() throws MediaAlbumException {
	throw new UnsupportedOperationException();
	/*
	try {
		return (User[])this.getAll(this.tableAlias);
	} catch ( ClassCastException e ) {
		log.error("Data not of expected type (User[]): " + e.getMessage());
		throw new DAOException("Data not of expected type",e);
	} catch ( IndexOutOfBoundsException e ) {
		return null;
	}
	*/
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#getAllGroups()
 */
public Group[] getAllGroups() throws MediaAlbumException {
	throw new UnsupportedOperationException();
	/*
	try {
		return (Group[])this.getAll(this.tableAlias);
	} catch ( ClassCastException e ) {
		log.error("Data not of expected type (Group[]): " + e.getMessage());
		throw new DAOException("Data not of expected type",e);
	} catch ( IndexOutOfBoundsException e ) {
		return null;
	}
	*/
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#getGroupById(java.lang.Object)
 */
public Group getGroupById(Object id) throws MediaAlbumException {
	if ( id == null ) {
		throw new MediaAlbumException("Null groupId passed to getGroupById");
	}

	GroupPK pk = null;
	ObjectPool pool = null;
	try {
		pool = PrimaryKeyObjectPoolFactory.getInstance().getPrimaryKeyObjectPool(Group.class);
		pk = (GroupPK)borrowObjectFromPool(pool);
		
		pk.setKey(id);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Group.class);
		
		return (Group)dao.get(pk);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,pk);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#getGroupsForUserId(java.lang.Object)
 */
public Group[] getGroupsForUserId(Object userId)
throws MediaAlbumException 
{
	if ( userId == null ) {
		throw new MediaAlbumException("Null userId passed to getGroupsForUserId");
	}
	
	GroupCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(Group.class);
		crit = (GroupCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(GroupCriteria.GROUPS_FOR_USER_SEARCH);
		crit.setQuery(userId);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Group.class);
		
		return ((Group[])dao.findByCriteria(crit));
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#setMembersForGroup(java.lang.Object, magoffin.matt.ma.xsd.Member[])
 */
public void setMembersForGroup(Integer groupId, Integer[] memberIds)
throws MediaAlbumException 
{
	if ( groupId == null ) {
		throw new MediaAlbumException("Null groupId passed to setMembersForGroup");
	}
	
	MemberPK pk = null;
	ObjectPool pool = null;
	try {
		pool = PrimaryKeyObjectPoolFactory.getInstance().getPrimaryKeyObjectPool(
				Member.class);
		pk = (MemberPK)borrowObjectFromPool(pool);

		pk.setGroup(groupId);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Member.class);
		
		// delete all rows with groupId
		dao.remove(pk);
		
		if ( memberIds != null && memberIds.length > 0 ) {
			// add all new rows
			Member[] members = new Member[memberIds.length];
			for ( int i = 0; i < memberIds.length; i++ ) {
				Member m = (Member)dao.getNewInstance();
				m.setGroupId(groupId);
				m.setUserId(memberIds[i]);
				members[i] = m;
			}
			dao.create(members);
		}
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,pk);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#getUsersForGroup(java.lang.Object)
 */
public User[] getUsersForGroup(Object groupId) throws MediaAlbumException {
	if ( groupId == null ) {
		throw new MediaAlbumException("Null groupId passed to getUsersForGroup");
	}

	UserCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(User.class);
		crit = (UserCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(UserCriteria.USERS_IN_GROUP_SEARCH);
		crit.setQuery(groupId);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				User.class);
		
		return ((User[])dao.findByCriteria(crit));
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#getFriendsForUser(java.lang.Object)
 */
public User[] getFriendsForUser(Object userId) throws MediaAlbumException 
{
	if ( userId == null ) {
		throw new MediaAlbumException("Null userId passed to getFriendsForUser");
	}
	
	UserCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(User.class);
		crit = (UserCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(UserCriteria.FRIENDS_OF_USER_SEARCH);
		crit.setQuery(userId);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				User.class);
		
		return ((User[])dao.findByCriteria(crit));
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#getCollectionsForUser(java.lang.Object)
 */
public Collection[] getCollectionsForUser(Object userId)
throws MediaAlbumException 
{
	if ( userId == null ) {
		throw new MediaAlbumException("Null userId passed to getCollectionsForUser");
	}
	Collection[] results = null;
	CollectionCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(Collection.class);
		crit = (CollectionCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(CollectionCriteria.COLLECTIONS_FOR_USER_SEARCH);
		crit.setQuery(userId);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Collection.class);
		
		results = (Collection[])dao.findByCriteria(crit);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
	
	Arrays.sort(results,CollectionBiz.SORT_COLLECTION_BY_NAME);
	return results;
}

private Album[] doGetAlbumsOwnedByUser(Object userId)
throws MediaAlbumException
{
	AlbumCriteria crit = null;
	ObjectPool pool = null;
	
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				Album.class);
		crit = (AlbumCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(AlbumCriteria.ALBUMS_FOR_OWNER);
		crit.setQuery(userId);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Album.class);
		
		return (Album[])dao.findByCriteria(crit);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
}

private void doPopulateAlbumPermissions(Album[] albums)
throws MediaAlbumException
{
	if ( albums == null || albums.length < 1 ) return;
	
	AlbumBiz albumBiz = (AlbumBiz)getBiz(BizConstants.ALBUM_BIZ);
	
	// get permissions for albums
	for ( int i = 0; i < albums.length; i++ ) {
		AlbumPermissions[] perm = albumBiz.getAlbumPermissions(albums[i].getAlbumId(),
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
		if ( perm == null ) continue;
		albums[i].setPermissions(perm);
	}

}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#getMediaAlbumsOwnedByUser(java.lang.Object)
 */
public Album[] getAlbumsOwnedByUser(Object userId)
throws MediaAlbumException 
{
	if ( userId == null ) {
		throw new MediaAlbumException("Null userId passed to getMediaAlbumsOwnedByUser");
	}

	Album[] results = doGetAlbumsOwnedByUser(userId);
	
	if ( results == null || results.length < 1 ) {
		return results;
	}
	
	doPopulateAlbumPermissions(results);
	
	return doArrangeAlbumNesting(results, null);

}

private Album[] doArrangeAlbumNesting(Album[] albums, Comparator sort) 
{
	if (  albums.length < 2 ) {
		// no nesting possible
		return albums;
	}
	
	if ( sort != null ) {
		Arrays.sort(albums,sort);
	}
	
	// check for nested albums, and if found re-arrange albums with nesting
	boolean hasNested = false;
	for ( int i = 0; i < albums.length; i++ ) {
		if ( albums[i].getParentId() != null ) {
			hasNested = true;
			break;
		}
	}
	if ( !hasNested ) {
		return albums;
	}
	
	List resultList = new ArrayList(albums.length);
	Map parentMap = null;
	try {
		parentMap = (Map)borrowObjectFromPool(MAP_POOL);
		for ( int i = 0; i < albums.length; i++ ) {
			Integer parentId = albums[i].getParentId();
			Integer id = albums[i].getAlbumId();
			parentMap.put(id,albums[i]);
			if ( parentId == null ) {
				resultList.add(albums[i]);
			}
		}
		for ( int i = 0; i < albums.length; i++ ) {
			Integer parentId = albums[i].getParentId();
			Integer id = albums[i].getAlbumId();
			if ( parentId == null ) {
				continue;
			}
			if ( !parentMap.containsKey(parentId) ) {
				log.warn("Media album " +id +" is orphaned from parent: "
						+parentId);
				continue;
			}
			Album album = (Album)parentMap.get(parentId);
			album.addAlbum(albums[i]);
		}
		
	} finally {
		returnObjectToPool(MAP_POOL,parentMap);
	}
	
	return (Album[])resultList.toArray(new Album[resultList.size()]);
}

private Album[] doGetAlbumsViewableByUser(Object userId)
throws MediaAlbumException
{
	AlbumCriteria crit = null;
	ObjectPool pool = null;
	
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				Album.class);
		crit = (AlbumCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(AlbumCriteria.VIEWABLE_BY_USER);
		crit.setQuery(userId);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Album.class);
		
		return (Album[])dao.findByCriteria(crit);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
	
}

private Album[] doGetAlbumsViewableByGroup(Object groupId)
throws MediaAlbumException
{
	AlbumCriteria crit = null;
	ObjectPool pool = null;
	
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				Album.class);
		crit = (AlbumCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(AlbumCriteria.VIEWABLE_BY_GROUP);
		crit.setQuery(groupId);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Album.class);
		
		return (Album[])dao.findByCriteria(crit);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
	
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#getAlbumsViewableByUser(java.lang.Object)
 */
public Album[] getAlbumsViewableByUser(Object userId)
throws MediaAlbumException 
{
	List albums = new ArrayList(20);
	
	// get albums owned by user first
	Album[] owned = doGetAlbumsOwnedByUser(userId);
	for ( int i = 0; i < owned.length; i++ ) {
		albums.add(owned[i]);
	}
	
	// add in albums viewable by user, excluding any already owned
	Album[] results = doGetAlbumsViewableByUser(userId);
	for ( int i = 0; i < results.length; i++ ) {
		if ( !results[i].getOwner().equals(userId) ) {
			albums.add(results[i]);
		}
	}
	
	// finally add in any albums viewable by groups user is member of
	Group[] groups = getGroupsForUserId(userId);
	if ( groups != null ) {
		for ( int i = 0; i < groups.length; i++ ) {
			results = doGetAlbumsViewableByGroup(groups[i].getGroupId());
			for ( int j = 0; j < results.length; j++ ) {
				if ( !results[j].getOwner().equals(userId) ) {
					albums.add(results[j]);
				}
			}
		}
	}
	
	results = (Album[])albums.toArray(new Album[albums.size()]);
	
	doPopulateAlbumPermissions(results);
	
	return doArrangeAlbumNesting(results, null);
}

private void addAnonymousMediaAlbum(List l, Album album, Album parentAlbum) 
throws MediaAlbumException 
{
	if ( album.getAllowAnonymous() != null && 
			album.getAllowAnonymous().booleanValue() && 
			(parentAlbum == null || parentAlbum.getAllowAnonymous() == null ||
					!parentAlbum.getAllowAnonymous().booleanValue()) ) {
		// clone it if has kids, and remove kids
		if ( album.getAlbumCount() > 0 ) {
			try {
				Album clonedAlbum = (Album)BeanUtils.cloneBean(album);
				clonedAlbum.clearAlbum();
				l.add(clonedAlbum);
			} catch ( Exception e ) {
				throw new MediaAlbumException("Exception cloning album",e);
			}
		} else {
			l.add(album);
		}
		// get the last album off our list and add the poster media item to it
		Album theAlbum = (Album)l.get(l.size()-1);
		if ( theAlbum.getPosterId() != null ) {
			MediaItemBiz itemBiz = (MediaItemBiz)bizFactory.getBizInstance(
					BizConstants.MEDIA_ITEM_BIZ);
			MediaItem item = itemBiz.getMediaItemById(theAlbum.getPosterId(),
					ApplicationConstants.CACHED_OBJECT_ALLOWED);
			if ( item != null ) {
				theAlbum.addItem(item);
			} else {
				log.warn("Poster not found for album " +theAlbum.getAlbumId()
						+" (" +theAlbum.getName() +"): " +theAlbum.getPosterId());
			}
		}
	}
	
	// recurse with nested albums, if have any
	if ( album.getAlbumCount() > 0 ) {
		int count = album.getAlbumCount();
		for ( int i = 0; i < count; i++ ) {
			addAnonymousMediaAlbum(l,album.getAlbum(i),album);
		}
	}
}
/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#getAnonymousAlbumsForUser(java.lang.String)
 */
public Album[] getAnonymousAlbumsForUser(String userKey)
throws MediaAlbumException 
{
	User user = getUserByAnonymousKey(userKey);
	
	// get all albums for user, then filter for only anonymous ones
	List l = null;
	Album[] albums = getAlbumsOwnedByUser(user.getUserId());
	l = new ArrayList(albums.length);
	for ( int i = 0; i < albums.length; i++ ) {
		addAnonymousMediaAlbum(l,albums[i],null);
	}
	if ( l == null || l.size() < 1 ) {
		return null;
	}
	Collections.sort(l,MediaUtil.MEDIA_ALBUM_SORT_BY_DATE);
	return (Album[])l.toArray(new Album[l.size()]);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#getAlbumThemesOwnedByUser(java.lang.Object)
 */
public AlbumTheme[] getAlbumThemesOwnedByUser(Object userId)
throws MediaAlbumException 
{
	// TODO Auto-generated method stub
	return null;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#logonUser(java.lang.String, java.lang.String)
 */
public User logonUser(String username, String password)
throws MediaAlbumException, NotAuthorizedException 
{
	String attemptedPass = MessageDigester.generateDigest(password,SALT);
	if ( attemptedPass == null ) {
		throw new NotInitializedException("Unable to encrypt password.");
	}
	
	if ( log.isDebugEnabled() ) {
		log.debug("Attempting login: user=" +username 
				+", attemptedPass=" +attemptedPass);
	}
	
	User user = getUserByUsername(username);
	if ( user == null ) {
		throw new NotAuthorizedException(username,"logon.error.invalid");
	}
	if ( !attemptedPass.equals(user.getPassword()) ) {
		throw new NotAuthorizedException(username,"logon.error.invalid");
	}
	
	// re-get user with full data
	return getFullUser(user.getUserId());
	
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#getFullUser(java.lang.Integer)
 */
public User getFullUser(Integer userId) throws MediaAlbumException
{
	User user = getUserById(userId,ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
	// clone data
	try {
		user = (User)BeanUtils.cloneBean(user);
	} catch ( Exception e ) {
		throw new MediaAlbumException("Unable to clone data",e);
	}
	
	// get the permissions for the user
	Permissions perm = getUserPermissions(user.getUserId());
	user.setPermissions(perm);
	
	// get the user's free data
	FreeData[] fdata = getFreeData(user,ApplicationConstants.CACHED_OBJECT_ALLOWED);
	user.setData(fdata);
	
	return user;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#canUserSearchUsers(magoffin.matt.ma.xsd.User)
 */
public boolean canUserSearchUsers(User actingUser)
throws MediaAlbumException 
{
	if ( actingUser == null ) return false;
	
	if ( actingUser == INTERNAL_USER ) return true;
	
	Permissions perm = actingUser.getPermissions();
	
	if ( perm == null ) return false;
	
	if ( perm.getSuperUser() != null && perm.getSuperUser().booleanValue() ) {
		return true;
	}
	
	if ( perm.getCreateUser() != null ) {
		return perm.getCreateUser().booleanValue();
	}
	
	return false;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#searchForUsers(magoffin.matt.ma.xsd.UserSearchData, magoffin.matt.ma.xsd.User)
 */
public UserSearchResults searchForUsers(
	UserSearchData searchData,
	User actingUser)
	throws MediaAlbumException, NotAuthorizedException 
{
	if ( searchData == null ) {
		throw new MediaAlbumException("Null searchData passed to searchForUsers");
	}
	
	if ( !canUserSearchUsers(actingUser) ) {
		throw new NotAuthorizedException(
				actingUser == null ? "" : actingUser.getUsername(),
				ERROR_AUTH_SEARCH_USERS);
	}

	UserCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(User.class);
		crit = (UserCriteria)borrowObjectFromPool(pool);
		
		switch ( searchData.getMode() ) {
			
			case USER_SEARCH_EMAIL:
				crit.setSearchType(UserCriteria.EMAIL_SEARCH);
				crit.setQuery(searchData.getEmail());
				break;
				
			case USER_SEARCH_NAME:
				crit.setSearchType(UserCriteria.NAME_SEARCH);
				crit.setQuery(searchData.getName());
				break;
				
			case USER_SEARCH_USERNAME:
				crit.setSearchType(UserCriteria.USERNAME_SUB_SEARCH);
				crit.setQuery(searchData.getUsername());
				break;
				
			default:
				throw new MediaAlbumException("Unknown search type",
						ERROR_UNKNOWN_SEARCH_TYPE);
		
		}
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				User.class);
		
		long searchTime = System.currentTimeMillis();
		
		User[] users = (User[])dao.findByCriteria(crit);
		
		searchTime = System.currentTimeMillis() - searchTime;
		
		UserSearchResults results = new UserSearchResults();
		
		results.setUser(users);
		results.setIsPartialResult(false);
		results.setReturnedResults(users == null ? 0 : users.length);
		results.setSearchTime(searchTime);
		results.setStartingOffset(0);
		results.setTotalResults(users == null ? 0 : users.length);
		
		return results;
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} catch ( IndexOutOfBoundsException e ) {
		return null;
	} finally {
		returnObjectToPool(pool,crit);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#getNewUserInstance(magoffin.matt.ma.xsd.User)
 */
public User getNewUserInstance(User actingUser)
throws MediaAlbumException 
{
	User user = new User();
	// TODO add 'createdBy' attribute to user
	
	Permissions perm = new Permissions();
	perm.setCreateUser(Boolean.FALSE);
	perm.setAssignCreateUser(Boolean.FALSE);
	perm.setSuperUser(Boolean.FALSE);
	perm.setAssignSuperUser(Boolean.FALSE);
	user.setPermissions(perm);
	
	user.setSingleCompress(MediaSpecUtil.COMPRESS_NORMAL);
	user.setSingleSize(MediaSpecUtil.SIZE_NORMAL);
	user.setThumbCompress(MediaSpecUtil.COMPRESS_NORMAL);
	user.setThumbSize(MediaSpecUtil.SIZE_NORMAL);
	
	return user;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#canUserCreateUsers(magoffin.matt.ma.xsd.User)
 */
public boolean canUserCreateUsers(User actingUser)
throws MediaAlbumException 
{
	if ( actingUser == null ) return true;
	
	if ( actingUser == INTERNAL_USER ) return true;
	
	if ( actingUser.getPermissions() == null 
			|| actingUser.getPermissions().getCreateUser() == null ) return false;

	return actingUser.getPermissions().getCreateUser().booleanValue();
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#canUserUpdateUser(magoffin.matt.ma.xsd.User, magoffin.matt.ma.xsd.User)
 */
public boolean canUserUpdateUser(User user, User actingUser)
throws MediaAlbumException 
{
	if ( user.getUserId().equals(actingUser.getUserId()) ) return true;
	
	// TODO use user 'createdBy' attribute in combination with super-user
	// permission to tell if user can update other users... i.e. users 
	// can only upate users they've created, unless they are a super user
	// in which case they can update anyone
	return canUserCreateUsers(actingUser);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#createUser(magoffin.matt.ma.xsd.User, magoffin.matt.ma.xsd.User)
 */
public User createUser(User user, User actingUser)
throws MediaAlbumException, NotAuthorizedException 
{
	if ( user == null ) {
		throw new MediaAlbumException("Null user passed to createUser");
	}
	
	if ( !canUserCreateUsers(actingUser) ) {
		throw new NotAuthorizedException(actingUser.getUsername(),ERROR_AUTH_CREATE_USER);
	}
	
	if ( log.isDebugEnabled() ) {
		log.debug("Creating new user: " +user.getUsername());
	}
	
	try {
		User newUser = (User)BeanUtils.cloneBean(user);

		verifyUser(newUser,actingUser,true);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				User.class);
		
		// TODO have createdBy and creationDate attributes
		
		dao.create(newUser);
		
		if ( newUser.getPermissions() != null ) {		
			DAO permDao = initializer.getDAOFactory().getDataAccessObjectInstance(
					Permissions.class);	
			Permissions perm = newUser.getPermissions();
			perm.setPermId(newUser.getUserId());
			permDao.create(perm);
		}
		
		// create a new collection for the user
		CollectionBiz collectionBiz = (CollectionBiz)getBiz(BizConstants.COLLECTION_BIZ);
		collectionBiz.createCollection(newUserCollectionName, newUser);
		
		return newUser;
		
	} catch ( MediaAlbumException e ) {
		throw e;
	} catch ( DAOException e ) {
		throw new MediaAlbumException("DAO exception",e);
	} catch ( Exception e ) {
		throw new MediaAlbumException("Unknown exception creating new user",e);
	}
}

/**
 * Validate user data.
 * 
 * @param user the user
 * @param forCreate <em>true</em> if creating this user, <em>false</em> if updating
 */
private void verifyUser(User user, User actingUser, boolean forCreate) 
throws MediaAlbumException
{
	// make sure password encrypted
	if ( user.getPassword() != null && !user.getPassword().startsWith("{SHA}") ) {
		// encrypt the password now
		String encryptedPass = MessageDigester.generateDigest(user.getPassword(),SALT);
		user.setPassword(encryptedPass);
	}
	
	// verify username not taken
	User otherUser = getUserByUsername(user.getUsername());
	if ( otherUser != null && ( (user.getUserId() != null && 
			!otherUser.getUserId().equals(user.getUserId())) || user.getUserId() == null) ) {
		throw new MediaAlbumValidationException(
				ERROR_USERNAME_TAKEN,
				new Object[] {user.getUsername()} );
	}
	
	// verify email not taken
	UserSearchData search = new UserSearchData();
	search.setEmail(user.getEmail());
	search.setMode(USER_SEARCH_EMAIL);
	UserSearchResults sresults = searchForUsers(search,INTERNAL_USER);
	otherUser = sresults.getUserCount() > 0 ? sresults.getUser(0) : null;
	if ( otherUser != null && ((user.getUserId() != null && 
			!otherUser.getUserId().equals(user.getUserId())) || user.getUserId() == null) ) {
		throw new MediaAlbumValidationException(
				ERROR_EMAIL_TAKEN,
				new Object[] {user.getEmail()} );
	}
	
	Registration reg = getRegistrationByUsername(user.getUsername());
	if ( reg != null ) {
		throw new MediaAlbumValidationException(
				ERROR_USERNAME_TAKEN,
				new Object[] {user.getUsername()} );
	}
	
	// verify time zone set
	if ( user.getTzCode() == null ) {
		// set to server tz
		TimeZone tz = TimeZone.getDefault();
		user.setTzCode(tz.getID());
	}
	
	// verify quota not 0
	if ( user.getQuota() != null && user.getQuota().intValue() == 0 ) {
		user.setQuota(null);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#updateUser(magoffin.matt.ma.xsd.User, magoffin.matt.ma.xsd.User)
 */
public User updateUser(User user, User actingUser)
throws MediaAlbumException, NotAuthorizedException 
{
	if ( user == null ) {
		throw new MediaAlbumException("Null user passed to updateUser");
	}
	
	if ( !canUserUpdateUser(user,actingUser) ) {
		throw new NotAuthorizedException(actingUser.getUsername(),ERROR_AUTH_UPDATE_USER);
	}
	
	if ( log.isDebugEnabled() ) {
		log.debug("Updating user: " +user.getUsername());
	}
	
	try {
		User newUser = (User)BeanUtils.cloneBean(user);

		verifyUser(newUser,actingUser,false);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Album.class);
		
		// TODO have modifiedBy and modificationDate attributes
		
		dao.update(newUser);
		
		removeObjectFromCache(ApplicationConstants.CacheFactoryKeys.USER,
				user.getUserId().toString());
		if ( user.getThemeId() != null ) {
			removeObjectFromCache(ApplicationConstants.CacheFactoryKeys.THEME_META_DATA,
					user.getThemeId());
		}
		if ( user.getDefaultThemeId() != null ) {
			removeObjectFromCache(ApplicationConstants.CacheFactoryKeys.THEME_META_DATA,
					user.getDefaultThemeId());
		}
		return newUser;
	
	} catch ( MediaAlbumException e ) {
		throw e;
	} catch ( DAOException e ) {
		throw new MediaAlbumException("DAO exception",e);
	} catch ( Exception e ) {
		throw new MediaAlbumException("Unknown exception creating new user",e);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#getUserPermissions(java.lang.Integer)
 */
public Permissions getUserPermissions(Integer userId)
throws MediaAlbumException 
{
	// get the permissions for the user
	PermissionsPK pk = null;
	ObjectPool pool = null;
	try {
		pool = PrimaryKeyObjectPoolFactory.getInstance().getPrimaryKeyObjectPool(
				Permissions.class);
		pk = (PermissionsPK)borrowObjectFromPool(pool);
		
		pk.setUserId(userId);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Permissions.class);
		
		Permissions perm = null;		
		Object o = dao.get(pk);
		if ( o != null ) {
			perm = (Permissions)o;
		} else {
			try {
				perm = (Permissions)BeanUtils.cloneBean(DEFAULT_PERMISSIONS);
			} catch ( Exception e ) {
				throw new MediaAlbumException("Unable to clone data", e);
			}
			perm.setPermId(userId);
		}
		
		return perm;
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,pk);
	}
	
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#canUserDeleteUser(magoffin.matt.ma.xsd.User, magoffin.matt.ma.xsd.User)
 */
public boolean canUserDeleteUser(User user, User actingUser)
throws MediaAlbumException {
	// TODO user user 'createdBy' attribute in combination with super-user
	// permission to tell if user can delete other users... i.e. users 
	// can only delete users they've created, unless they are a super user
	// in which case they can delete anyone
	return canUserCreateUsers(actingUser);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#deleteUser(java.lang.Object, magoffin.matt.ma.xsd.User)
 */
public void deleteUser(Object id, User actingUser)
throws MediaAlbumException, NotAuthorizedException 
{
	User user = getUserById(id, ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
	if ( !canUserDeleteUser(user,actingUser) ) {
		throw new NotAuthorizedException(actingUser.getUsername(),
				ERROR_AUTH_DELETE_USER);
	}
	
	// 1: delete user's cache files
	MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);
	itemBiz.deleteUserCacheFiles(user,actingUser);
	
	// 2: delete collections user owns
	Collection[] collections = getCollectionsForUser(id);
	if ( collections != null ) {
		CollectionBiz collectionBiz = (CollectionBiz)getBiz(BizConstants.COLLECTION_BIZ);
		for ( int i = 0; i < collections.length; i++ ) {
			collectionBiz.deleteCollection(collections[i].getCollectionId(),false,null,actingUser);
		}
	}
	
	// 3: delete user
	UserPK pk = null;
	ObjectPool pool = null;
	try {
		pool = PrimaryKeyObjectPoolFactory.getInstance().getPrimaryKeyObjectPool(
				User.class);
		pk = (UserPK)borrowObjectFromPool(pool);
		
		pk.setKey(id);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				User.class);
		
		dao.remove(pk);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,pk);
	}
	
	removeObjectFromCache(ApplicationConstants.CacheFactoryKeys.USER,id.toString());
}

/**
 * Find a user by their email address.
 * 
 * @param email the email address
 * @return the user, or <em>null</em> if not found
 * @throws MediaAlbumException if an error occurs
 */
private User getUserByEmail(String email) throws MediaAlbumException {
	if ( email == null ) {
		throw new MediaAlbumException("Null email passed to getUserByEmail");
	}

	UserCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(User.class);
		crit = (UserCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(UserCriteria.EMAIL_SEARCH_EXACT);
		crit.setQuery(email);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				User.class);
		
		return (User)(dao.findByCriteria(crit)[0]);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} catch ( IndexOutOfBoundsException e ) {
		return null;
	} finally {
		returnObjectToPool(pool,crit);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#inviteFriend(java.lang.String, magoffin.matt.ma.xsd.User, java.net.URL, java.net.URL)
 */
public void inviteFriend(String email, User actingUser, URL accept, URL decline)
throws MediaAlbumException 
{
	// check if inviting self
	if ( actingUser.getEmail().equals(email) ) {
		throw new MediaAlbumValidationException(ERROR_INVITE_SELF);
	}
	
	// check if email exists on system already
	User user = getUserByEmail(email);
	if ( user != null ) {
		// verify user not already a friend
		User[] friends = getFriendsForUser(actingUser.getUserId());
		if ( friends != null ) {
			for ( int i = 0; i < friends.length; i++ ) {
				if ( friends[i].getUserId().equals(user.getUserId()) ) {
					throw new MediaAlbumValidationException(ERROR_USER_ALREADY_FRIEND,
							new Object[] {user.getName(),user.getEmail()});
				}
			}
		}
	}
	
	DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
			Invitation.class);

	// check if invitation already pending
	InvitationCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				Invitation.class);
		crit = (InvitationCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(InvitationCriteria.PENDING_INVITATION_SEARCH);
		crit.setQuery(new Object[] {actingUser.getUserId(),email});
		
		Invitation[] invites = (Invitation[])dao.findByCriteria(crit);
		
		if ( invites != null && invites.length > 0 ) {
			throw new MediaAlbumValidationException(ERROR_USER_ALREADY_INVITED,
					new Object[] {email});
		}
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
	
	// insert new invitation
	Invitation invite = new Invitation();
	invite.setEmail(email);
	invite.setInviteDate(new Date());
	invite.setUserId(actingUser.getUserId());
	
	try {
		dao.create(invite);
	} catch ( DAOException e ) {
		throw new MediaAlbumException("Unable to create invitation",e);
	}
	
	String key = "?"+KEY_URL_PARAM+"="+invite.getKey();
	
	// email user invitation
	
	MailBiz mailBiz = (MailBiz)getBiz(BizConstants.MAIL_BIZ);
	
	Map mergeData = new HashMap(10);
	mergeData.put("user",actingUser);
	mergeData.put("url_accept",accept.toString()+key);
	mergeData.put("url_decline",decline.toString()+key);
	
	String[] templatePaths = getMailTemplateResourcePaths(
			MAIL_TEMPLATE_INVITE_FRIEND);
	try {
		mailBiz.sendResourceMailMerge(actingUser.getEmail(),email,null,null,
				MAIL_SUBJECT_INVITE_FRIEND,templatePaths,mergeData);
	} catch ( MailProcessingException e ) {
		
		log.warn("Unable to send invitation email: " +e.toString());
		
		// delete invitation, notifiy user of error
		try {
			deleteInvitation(invite.getKey());
		} catch ( MediaAlbumException e2 ) {
			log.error("Unable to delete invitation after email exception: " +e2.toString());
		}
		throw new MediaAlbumValidationException(ERROR_INVITE_EMAIL_FAILURE,
				new Object[] {invite.getEmail()});
	}
}

private void deleteInvitation(String key) 
throws MediaAlbumException 
{
	InvitationPK pk = null;
	ObjectPool pool = null;
	try {
		pool = PrimaryKeyObjectPoolFactory.getInstance().getPrimaryKeyObjectPool(
				Invitation.class);
		pk = (InvitationPK)borrowObjectFromPool(pool);
		
		pk.setKey(key);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Invitation.class);
		
		dao.remove(pk);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,pk);
	}
}

private Registration getRegistrationByKey(String key) 
throws MediaAlbumException 
{
	RegistrationPK pk = null;
	ObjectPool pool = null;
	try {
		pool = PrimaryKeyObjectPoolFactory.getInstance().getPrimaryKeyObjectPool(
				Registration.class);
		pk = (RegistrationPK)borrowObjectFromPool(pool);
		
		pk.setKey(key);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Registration.class);
		
		Object o = dao.get(pk);
		
		if ( o instanceof Registration ) {
			return (Registration)o;
		}
		
		return null;
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,pk);
	}
}

private Invitation getInvitationByKey(String key) 
throws MediaAlbumException 
{
	InvitationPK pk = null;
	ObjectPool pool = null;
	try {
		pool = PrimaryKeyObjectPoolFactory.getInstance().getPrimaryKeyObjectPool(
				Invitation.class);
		pk = (InvitationPK)borrowObjectFromPool(pool);
		
		pk.setKey(key);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Invitation.class);
		
		Object o = dao.get(pk);
		
		if ( o instanceof Invitation ) {
			return (Invitation)o;
		}
		
		return null;
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,pk);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#getRegistrationByUsername(java.lang.String)
 */
public Registration getRegistrationByUsername(String username) 
throws MediaAlbumException 
{
	RegistrationCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				Registration.class);
		crit = (RegistrationCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(RegistrationCriteria.SEARCH_USERNAME);
		crit.setQuery(username);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Registration.class);
		
		DataObject[] o = dao.findByCriteria(crit);
		
		if ( o instanceof Registration[] && o.length > 0 ) {
			return (Registration)o[0];
		}
		
		return null;
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
}

private Registration getRegistrationByEmail(String email) 
throws MediaAlbumException 
{
	RegistrationCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				Registration.class);
		crit = (RegistrationCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(RegistrationCriteria.SEARCH_EMAIL);
		crit.setQuery(email);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Registration.class);
		
		DataObject[] o = dao.findByCriteria(crit);
		
		if ( o instanceof Registration[] && o.length > 0 ) {
			return (Registration)o[0];
		}
		
		return null;
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#createRegistration(magoffin.matt.ma.xsd.Registration, java.net.URL, java.net.URL)
 */
public Registration register(Registration reg, URL accept, URL decline)
throws MediaAlbumException 
{
	if ( reg == null ) {
		throw new MediaAlbumException("Null registration passed to createRegistration");
	}
	
	if ( log.isDebugEnabled() ) {
		log.debug("Creating new registration: " +reg.getUsername());
	}
	
	verifyRegistration(reg,true);
	
	Registration result = null;
	
	// make sure email address not already a user
	
	User user = getUserByEmail(reg.getEmail());
	if ( user != null ) {
		throw new MediaAlbumValidationException(ERROR_ALREADY_USER,
				new Object[]{reg.getEmail()});
	}
	
	// make sure email address not already pending
	
	result = getRegistrationByEmail(reg.getEmail());
	if ( result != null ) {
		throw new MediaAlbumValidationException(ERROR_PENDING_REGISTRATION,
				new Object[]{reg.getEmail()});
	}
	
	// make sure username not already taken
	
	user = getUserByUsername(reg.getUsername());
	if ( user != null ) {
		throw new MediaAlbumValidationException(ERROR_USERNAME_TAKEN,
				new Object[]{reg.getUsername()});
	}

	try {
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Registration.class);
		
		Registration newReg = (Registration)BeanUtils.cloneBean(reg);
		newReg.setRegisterDate(new Date());
		
		if ( !newReg.getPassword().startsWith("{SHA}") ) {
			// encrypt the password now
			String encryptedPass = MessageDigester.generateDigest(newReg.getPassword(),SALT);
			newReg.setPassword(encryptedPass);
		}
		
		dao.create(newReg);
		
		result = newReg;

	} catch ( DAOException e ) {
		throw new MediaAlbumException("DAO exception",e);
	} catch ( Exception e ) {
		throw new MediaAlbumException("Unknown exception creating new user",e);
	}
	
	String key = "?"+KEY_URL_PARAM+"="+result.getKey();
	
	// email user invitation
	
	MailBiz mailBiz = (MailBiz)getBiz(BizConstants.MAIL_BIZ);
	
	Map mergeData = new HashMap(10);
	mergeData.put("reg",result);
	mergeData.put("url_accept",accept.toString()+key);
	mergeData.put("url_decline",decline.toString()+key);
	
	String[] templatePaths = getMailTemplateResourcePaths(
			MAIL_TEMPLATE_CONFIRM_REGISTRATION);
	try {
		
		String mailAddress = mailBiz.formatMailAddress(reg.getName(),reg.getEmail());
		
		mailBiz.sendResourceMailMerge(null,mailAddress,null,null,
				MAIL_SUBJECT_CONFIRM_REGISTRATION,templatePaths,mergeData);
	} catch ( MailProcessingException e ) {
		
		log.warn("Unable to send invitation email: " +e.toString());
		
		// delete invitation, notifiy user of error
		try {
			deleteRegistration(result.getKey());
		} catch ( MediaAlbumException e2 ) {
			log.error("Unable to delete invitation after email exception: " +e2.toString());
		}
		throw new MediaAlbumValidationException(ERROR_REGISTRATION_EMAIL_FAILURE,
				new Object[] {reg.getEmail()});
	}
	
	return result;
}

/**
 * Verify registration data.
 * 
 * @param reg the registration
 * @param forCreate <em>true</em> if creating, <em>false</em> if updating
 */
private void verifyRegistration(Registration reg, boolean forCreate) 
throws MediaAlbumException
{
	// verify data not null
	if ( reg == null ) return;
	
	String email = StringUtil.trimToNull(reg.getEmail());
	if ( email == null ) {
		throw new MediaAlbumValidationException(ERROR_INVALID_DATA,
				new String[] {MessageConstants.USER_EMAIL});
	}
	reg.setEmail(email);
	
	String name = StringUtil.trimToNull(reg.getName());
	if ( name == null ) {
		throw new MediaAlbumValidationException(ERROR_INVALID_DATA,
				new String[] {MessageConstants.USER_NAME});
	}
	reg.setName(name);
	
	String password = StringUtil.trimToNull(reg.getPassword());
	if ( password == null ) {
		throw new MediaAlbumValidationException(ERROR_INVALID_DATA,
				new String[] {MessageConstants.USER_PASSWORD});
	}
	reg.setPassword(password);
	
	String username = StringUtil.trimToNull(reg.getUsername());
	if ( username == null ) {
		throw new MediaAlbumValidationException(ERROR_INVALID_DATA,
				new String[] {MessageConstants.USER_USERNAME});
	}
	reg.setUsername(username);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#deleteRegistration(java.lang.String)
 */
public void deleteRegistration(String key)
throws MediaAlbumException 
{
	RegistrationPK pk = null;
	ObjectPool pool = null;
	try {
		pool = PrimaryKeyObjectPoolFactory.getInstance().getPrimaryKeyObjectPool(
				Registration.class);
		pk = (RegistrationPK)borrowObjectFromPool(pool);
		
		pk.setKey(key);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Registration.class);
		
		dao.remove(pk);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,pk);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#getNewRegistrationForInvitation(java.lang.String)
 */
public Registration getNewRegistrationForInvitation(String inviteKey)
throws MediaAlbumException 
{
	Registration reg = new Registration();
	Invitation invite = getInvitationByKey(inviteKey);
	if ( invite != null ) {
		reg.setEmail(invite.getEmail());
	}
	return reg;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#confirmRegistration(java.lang.String, java.net.URL)
 */
public User confirmRegistration(String regKey, URL access) throws MediaAlbumException 
{
	Registration reg = getRegistrationByKey(regKey);
	if ( reg == null ) {
		throw new MediaAlbumException("Registration for key " +regKey +" not found");
	}
	
	// copy reg to user
	User user = getNewUserInstance(null);
	user.setEmail(reg.getEmail());
	user.setName(reg.getName());
	user.setPassword(reg.getPassword());
	user.setUsername(reg.getUsername());
	
	// delete pending reg
	deleteRegistration(regKey);
	
	// create user
	user = createUser(user,null);
	
	// send welcome email
	MailBiz mailBiz = (MailBiz)getBiz(BizConstants.MAIL_BIZ);
	
	Map mergeData = new HashMap(10);
	mergeData.put("user",user);
	mergeData.put("url_access",access.toString());
	
	String[] templatePaths = getMailTemplateResourcePaths(
			MAIL_TEMPLATE_WELCOME);
	try {
		
		String mailAddress = mailBiz.formatMailAddress(user.getName(),user.getEmail());
		
		mailBiz.sendResourceMailMerge(null,mailAddress,null,null,
				MAIL_SUBJECT_WELCOME,templatePaths,mergeData);
		
	} catch ( MailProcessingException e ) {
		log.warn("Unable to send invitation email: " +e.toString());
	}
	
	return user;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#declineRegistration(java.lang.String)
 */
public void declineRegistration(String regKey) throws MediaAlbumException {
	try {
		deleteRegistration(regKey);
	} catch ( MediaAlbumException e ) {
		// ignore error
		log.warn("Unable to delete registration for decline: " +e.getMessage());
	}
}

private void addUserAsFriend(User user, User friend)
throws MediaAlbumException
{
	try {
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Friend.class);
		
		Friend f = new Friend();
		f.setUserId(user.getUserId());
		f.setFriendId(friend.getUserId());
		
		dao.create(f);
		
	} catch ( DAOException e ) {
		throw new MediaAlbumException("DAO exception",e);
	} catch ( Exception e ) {
		throw new MediaAlbumException("Unknown exception creating new user",e);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#confirmInvitation(java.lang.String)
 */
public User confirmInvitation(String key) throws MediaAlbumException 
{
	Invitation invite = getInvitationByKey(key);
	if ( invite == null ) {
		throw new MediaAlbumException("Invitation key " +key +" not available");
	}
	
	// get inviter
	User inviter = getUserById(invite.getUserId(), ApplicationConstants.CACHED_OBJECT_ALLOWED);
	if ( inviter == null ) {
		throw new MediaAlbumException("Invitee " +invite.getUserId() +" does not exist");
	}
	
	// get invitee
	User invitee = getUserByEmail(invite.getEmail());
	if ( invitee == null ) {
		throw new MediaAlbumException("Invited user " +invite.getEmail() +" does not exist");
	}
	
	// all going well now, so add each other as friends
	addUserAsFriend(inviter,invitee);
	addUserAsFriend(invitee,inviter);
	
	MailBiz mailBiz = (MailBiz)getBiz(BizConstants.MAIL_BIZ);
	
	Map mergeData = new HashMap(10);
	mergeData.put("user",invitee);
	
	String[] templatePaths = getMailTemplateResourcePaths(
			MAIL_TEMPLATE_INVITE_FRIEND_ACCPETED);
	try {
		
		String mailAddress = mailBiz.formatMailAddress(inviter.getName(),inviter.getEmail());
		String mailSubject = invitee.getName() +" " + MAIL_SUBJECT_INVITE_FRIEND_ACCPETED;
		mailBiz.sendResourceMailMerge(null,mailAddress,null,null,
				mailSubject, templatePaths, mergeData);
		
	} catch ( MailProcessingException e ) {
		log.warn("Unable to send invitation email: " +e.toString());
	}
	
	
	// delete invitation
	deleteInvitation(key);
	
	return inviter;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#declineInvitation(java.lang.String)
 */
public User declineInvitation(String key) throws MediaAlbumException 
{
	Invitation invite = getInvitationByKey(key);
	if ( invite == null ) {
		log.warn("Invitation for key '" +key +"' not found for decline");
		return null;
	}
	
	// delete invitation
	deleteInvitation(key);
	
	User inviter = getUserById(invite.getUserId(), ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
	// email iviter invitation was declined
	MailBiz mailBiz = (MailBiz)getBiz(BizConstants.MAIL_BIZ);
	
	Map mergeData = new HashMap(10);
	mergeData.put("email",invite.getEmail());
	
	String[] templatePaths = getMailTemplateResourcePaths(
			MAIL_TEMPLATE_INVITE_FRIEND_DECLINED);
	try {
		
		String mailAddress = mailBiz.formatMailAddress(inviter.getName(),inviter.getEmail());
		String mailSubject = invite.getEmail() +" " +MAIL_SUBJECT_INVITE_FRIEND_DECLINED;
		mailBiz.sendResourceMailMerge(null,mailAddress,null,null,
				mailSubject, templatePaths, mergeData);
		
	} catch ( MailProcessingException e ) {
		log.warn("Unable to send invitation email: " +e.toString());
	}
	
	return inviter;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#createGroup(java.lang.String, magoffin.matt.ma.xsd.User)
 */
public Group createGroup(String name, User actingUser)
throws MediaAlbumException 
{
	if ( actingUser == null ) {
		throw new MediaAlbumException("Null user passed to createGroup");
	}
	
	if ( log.isDebugEnabled() ) {
		log.debug("Creating new group '" +name +"' for user " +actingUser.getUsername());
	}
	Group g = new Group();
	g.setName(name);
	g.setOwner(actingUser.getUserId());
	
	// create group object
	
	try {
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Group.class);
		
		dao.create(g);
		
	} catch ( DAOException e ) {
		throw new MediaAlbumException("DAO exception",e);
	} catch ( Exception e ) {
		throw new MediaAlbumException("Unknown exception creating new group",e);
	}
	
	// add user as member to group
	
	Member m = new Member();
	m.setGroupId(g.getGroupId());
	m.setUserId(actingUser.getUserId());
	
	try {
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Member.class);
		
		dao.create(m);
		
	} catch ( DAOException e ) {
		throw new MediaAlbumException("DAO exception",e);
	} catch ( Exception e ) {
		throw new MediaAlbumException("Unknown exception creating new group member",e);
	}
	
	return g;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#canUserDeleteGroup(java.lang.Integer, magoffin.matt.ma.xsd.User)
 */
public boolean canUserDeleteGroup(Integer groupId, User actingUser)
throws MediaAlbumException 
{
	Group group = getGroupById(groupId);
	return canUserDeleteGroup(group,actingUser);
}

private boolean canUserDeleteGroup(Group group, User actingUser) 
{
	if ( actingUser == null ) return true;
	
	// only allow if user is owner of group
	return group != null && group.getGroupId() != null && group.getOwner().equals(actingUser.getUserId());
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#deleteGroup(java.lang.Integer, magoffin.matt.ma.xsd.User)
 */
public Group deleteGroup(Integer groupId, User actingUser)
throws MediaAlbumException, NotAuthorizedException 
{
	Group group = getGroupById(groupId);
	
	if ( !canUserDeleteGroup(group,actingUser) ) {
		throw new NotAuthorizedException(actingUser.getUsername(),ERROR_AUTH_DELETE_GROUP);
	}

	GroupPK pk = null;
	ObjectPool pool = null;
	try {
		pool = PrimaryKeyObjectPoolFactory.getInstance().getPrimaryKeyObjectPool(
				Group.class);
		pk = (GroupPK)borrowObjectFromPool(pool);
		
		pk.setKey(groupId);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Group.class);
		
		dao.remove(pk);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,pk);
	}
	
	return group;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#canUserUpdateGroup(java.lang.Integer, magoffin.matt.ma.xsd.User)
 */
public boolean canUserUpdateGroup(Integer groupId, User actingUser)
throws MediaAlbumException 
{
	Group group = getGroupById(groupId);
	return canUserUpdateGroup(group,actingUser);
}

private boolean canUserUpdateGroup(Group group, User actingUser)
{
	return canUserDeleteGroup(group,actingUser);
}

private boolean canUserUpdateFreeData(User user, User actingUser)
throws MediaAlbumException
{
	if ( user.getUserId().equals(actingUser.getUserId()) ) {
		return true;
	}
	return isUserSuperUser(actingUser.getUserId());
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#updateGroup(magoffin.matt.ma.xsd.Group, magoffin.matt.ma.xsd.User)
 */
public Group updateGroup(Group group, User actingUser)
throws MediaAlbumException, NotAuthorizedException 
{
	if ( !canUserUpdateGroup(group,actingUser) ) {
		throw new NotAuthorizedException(actingUser.getUsername(),ERROR_AUTH_UPDATE_GROUP);
	}

	try {
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Group.class);
		
		dao.update(group);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	}
	
	return group;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#isUserMemberOfGroup(java.lang.Integer, java.lang.Integer)
 */
public boolean isUserMemberOfGroup(Integer userId, Integer groupId)
throws MediaAlbumException 
{
	// this is quick and dirty hack to check :-/
	// TODO turn into search instead of get all users
	
	User[] members = getUsersForGroup(groupId);
	if ( members == null ) return false;
	
	for ( int i = 0; i < members.length; i++ ) {
		if ( userId.equals(members[i].getUserId()) ) {
			return true;
		}
	}
	
	return false;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#getTotalItemsViewableForUser(magoffin.matt.ma.xsd.User, magoffin.matt.ma.xsd.User)
 */
public int getTotalAlbumItemsViewableForUser(User user, User actingUser)
throws MediaAlbumException 
{
	AlbumCriteria crit = null;
	ObjectPool pool = null;
	
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				Album.class);
		crit = (AlbumCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(AlbumCriteria.ITEM_COUNT_FOR_USER);
		Integer[] query = new Integer[] {
				user.getUserId(),
				actingUser == null 
					? ApplicationConstants.ANONYMOUS_USER_ID 
					: actingUser.getUserId()};
		crit.setQuery(query);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Album.class);
		
		DataObject[] result = dao.findByCriteria(crit);
		
		if ( !(result instanceof CountData[]) ) {
			throw new MediaAlbumException("DAO not configured properly: " +result);
		}
		
		int count = ((CountData[])result)[0].getCount();
		
		return count;
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#getAlbumsViewableForUser(magoffin.matt.ma.xsd.User, java.util.Comparator, magoffin.matt.ma.xsd.User)
 */
public Album[] getAlbumsViewableForUser(User user, Comparator sort, User actingUser)
throws MediaAlbumException 
{
	AlbumCriteria crit = null;
	ObjectPool pool = null;
	
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				Album.class);
		crit = (AlbumCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(AlbumCriteria.ALBUMS_FOR_USER);
		Integer[] query = new Integer[] {
				user.getUserId(),
				actingUser == null 
				? ApplicationConstants.ANONYMOUS_USER_ID 
				: actingUser.getUserId()};
		crit.setQuery(query);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				Album.class);
		
		Album[] result = (Album[])dao.findByCriteria(crit);
		
		// populate free data
		AlbumBiz albumBiz = (AlbumBiz)getBiz(BizConstants.ALBUM_BIZ);
		albumBiz.populateAlbumFreeData(result,
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
		
		return doArrangeAlbumNesting(result, sort);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#getVirtualAlbumsViewableForUser(magoffin.matt.ma.xsd.User, int, magoffin.matt.ma.xsd.User)
 */
public Album[] getVirtualAlbumsViewableForUser(
	User user,
	int mode,
	User actingUser)
	throws MediaAlbumException 
{
	switch ( mode ) {
		case VIRTUAL_VIEW_MODE_DATE:
			return doGetVirtualAlbumsByDate(user,actingUser);
			
		case VIRTUAL_VIEW_MODE_AVERAGE_RATING:
			return doGetVirtualAlbumsByAverageRating(user,actingUser);
			
		case VIRTUAL_VIEW_MODE_POPULARITY:
			return doGetVirtualAlbumsByHits(user,actingUser);
			
		case VIRTUAL_VIEW_MODE_MY_RATING:
			return doGetVirtualAlbumsByUserRating(user,actingUser,actingUser);
		
		case VIRTUAL_VIEW_MODE_OWNER_RATING:
			return doGetVirtualAlbumsByUserRating(user,actingUser,user);
		
		default:
			throw new MediaAlbumException("Unknown virtual view mode: " +mode);
	}
}

private Album[] doGetVirtualAlbumsByUserRating(User user, User actingUser,
		User ratingUser) 
throws MediaAlbumException 
{
	MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);
	MediaItem[] result = itemBiz.getMediaItemsViewableForUser(user,actingUser);
	
	if ( result == null || result.length < 1 ) {
		return null;
	}
	
	// populate ratings
	itemBiz.populateItemRatings(result,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);

	// sort by rating
	Map ratingMap = new TreeMap(REVERSE_COMPARABLE_SORT);
	
	// arrange items into "virtual" albums by ratings (9-10, 8-9, etc)
	for ( int i = 0; i < result.length; i++ ) {
		Short key = itemBiz.getOwnerRating(result[i],ratingUser);
		if ( key == null ) {
			key = NO_RATING_VALUE;
		}
		
		Album album = null;
		if ( !ratingMap.containsKey(key) ) {
			album = new Album();
			album.setAlbumId(new Integer(key.intValue()));
			String name = key.shortValue() < 1 
					? NO_RATING_ALBUM_NAME 
					: key.toString();
			album.setName(name);
			album.setOwner(user.getUserId());
			album.setPosterId(result[i].getItemId());
			ratingMap.put(key,album);
		} else {
			album = (Album)ratingMap.get(key);
		}
		album.addItem(result[i]);
	}
	
	Album[] ratingAlbums = (Album[])ratingMap.values().toArray(
			new Album[ratingMap.size()]);
	
	return ratingAlbums;
}


/**
 * Get virtual albums viewable by date.
 * @param user the user to find items for
 * @param actingUser the acting user
 * @return albums
 */
private Album[] doGetVirtualAlbumsByDate(User user, User actingUser) 
throws MediaAlbumException
{
	MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);
	MediaItem[] result = itemBiz.getMediaItemsViewableForUser(user,actingUser);
	
	if ( result == null || result.length < 1 ) {
		return null;
	}
	
	// sort by date first
	Arrays.sort(result,MediaItemBiz.SORT_ITEM_BY_DATE);
	
	int year = -1;
	int month = -1;
	List years = new ArrayList(10);
	Calendar cal = Calendar.getInstance();
	Album currAlbum = null;
	
	// arrange items into "virtual" albums by year -> month
	for ( int i = 0; i < result.length; i++ ) {
		Date d = result[i].getCreationDate();
		if ( d == null ) {
			String noYearName = UNKNOWN_DATE_ALBUM_NAME;
			if ( currAlbum == null || !currAlbum.getName().equals(noYearName) ) {
				currAlbum = new Album();
				currAlbum.setAlbumId(UNKNOWN_DATE_ALBUM_ID);
				currAlbum.setName(noYearName);
				currAlbum.setOwner(user.getUserId());
				currAlbum.setPosterId(result[i].getItemId());
				years.add(currAlbum);
			}
		} else {
			cal.setTime(d);
			int currYear = cal.get(Calendar.YEAR);
			int currMonth = cal.get(Calendar.MONTH);
			if ( currYear != year ) {
				String yearStr = String.valueOf(currYear);
				currAlbum = new Album();
				currAlbum.setAlbumId(new Integer(currYear));
				currAlbum.setName(yearStr);
				currAlbum.setOwner(user.getUserId());
				currAlbum.setPosterId(result[i].getItemId());
				currAlbum.addItem(result[i]);
				years.add(currAlbum);
				year = currYear;
				month = -1;
			}
			if ( currMonth != month ) {
				String monthStr = MONTH_NAME.format(d);
				Album monthAlbum = new Album();
				monthAlbum.setAlbumId(Integer.valueOf(
						String.valueOf(currYear)+currMonth));
				monthAlbum.setName(monthStr);
				monthAlbum.setParentId(new Integer(currYear));
				monthAlbum.setOwner(user.getUserId());
				monthAlbum.setPosterId(result[i].getItemId());
				Album yearAlbum = (Album)years.get(years.size()-1);
				yearAlbum.addAlbum(monthAlbum);
				currAlbum = monthAlbum;
				month = currMonth;
			}
		}
		currAlbum.addItem(result[i]);
	}
	
	Album[] yearAlbums = (Album[])years.toArray(new Album[years.size()]);
	
	return yearAlbums;
}

/**
 * Get virtual albums viewable by date.
 * @param user the user to find items for
 * @param actingUser the acting user
 * @return albums
 */
private Album[] doGetVirtualAlbumsByHits(User user, User actingUser) 
throws MediaAlbumException
{
	MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);
	MediaItem[] result = itemBiz.getMediaItemsViewableForUser(user,actingUser);
	
	if ( result == null || result.length < 1 ) {
		return null;
	}
	
	// sort by hits first
	Arrays.sort(result,MediaItemBiz.SORT_ITEM_BY_HITS);
	
	// divide range of hits into decent sized steps
	int min = result[result.length-1].getHits() != null
			? result[result.length-1].getHits().intValue()
			: 0;
	int max = result[0].getHits() != null
			? result[0].getHits().intValue()
			: 0;
	int range = max - min;
	int step = (int)Math.round(Math.pow(range,0.5));
			
	Buffer steps = new ArrayStack((range/step)+1 +(min==0?1:0));
	int currStep = min-step;
	int top = currStep+step-1;
	Album currAlbum = null;
	Buffer tmpItemBuffer = new ArrayStack(result.length);
	
	// arrange items into "virtual" albums by hits (9-10, 8-9, etc)
	for ( int i = result.length-1; i >= 0; i-- ) {
		int currHit = result[i].getHits() != null
			? result[i].getHits().intValue()
			: 0;
		if ( currHit > top ) {
			while ( currHit >= (currStep+step) ) {
				currStep += step;
			}
			top = currStep+step-1;
			if ( currHit == 0 && top > 0 ) {
				top = 0;
				currStep -= step;
			}
			Album album = new Album();
			album.setAlbumId(new Integer(currStep));
			String name;
			if ( currHit == 0 ) {
				name = NO_HITS_ALBUM_NAME;
			} else if ( top > max ) {
				name = String.valueOf(currHit);
			} else {
				name = (currStep == 0 ? 1 : currStep) + " - " + top;
			}
			album.setName(name);
			album.setOwner(user.getUserId());
			album.setPosterId(result[i].getItemId());
			steps.add(album);
			int size = tmpItemBuffer.size();
			for ( int j = 0; j < size; j++ ) {
				currAlbum.addItem((MediaItem)tmpItemBuffer.remove());
			}
			currAlbum = album;
		}
		tmpItemBuffer.add(result[i]);
		//currAlbum.addItem(result[i]);
	}
	int size = tmpItemBuffer.size();
	for ( int j = 0; j < size; j++ ) {
		currAlbum.addItem((MediaItem)tmpItemBuffer.remove());
	}	
	
	// turn to array (since stack, items removed in reverse order)
	Album[] albums = new Album[steps.size()];
	for ( int j = 0; j < albums.length; j++ ) {
		albums[j] = (Album)steps.remove();
	}
	
	return albums;
}

/**
 * Get virtual albums viewable by rating.
 * 
 * @param user the user to find items for
 * @param actingUser the acting user
 * @return albums arranged by rating
 */
private Album[] doGetVirtualAlbumsByAverageRating(User user, User actingUser) 
throws MediaAlbumException
{
	MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);
	MediaItem[] result = itemBiz.getMediaItemsViewableForUser(user,actingUser);
	
	if ( result == null || result.length < 1 ) {
		return null;
	}
	
	// populate ratings
	itemBiz.populateItemRatings(result,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);

	// sort by rating
	Map ratingMap = new TreeMap(REVERSE_COMPARABLE_SORT);
	
	// arrange items into "virtual" albums by ratings (9-10, 8-9, etc)
	for ( int i = 0; i < result.length; i++ ) {
		float a = itemBiz.getAverageUserRating(result[i],actingUser);
		Integer key = new Integer((int)Math.floor(a));
		
		Album album = null;
		if ( !ratingMap.containsKey(key) ) {
			album = new Album();
			album.setAlbumId(key);
			String name = a < 1 
					? NO_RATING_ALBUM_NAME 
					: key.toString();
			album.setName(name);
			album.setOwner(user.getUserId());
			album.setPosterId(result[i].getItemId());
			ratingMap.put(key,album);
		} else {
			album = (Album)ratingMap.get(key);
		}
		album.addItem(result[i]);
	}
	
	Album[] ratingAlbums = (Album[])ratingMap.values().toArray(
			new Album[ratingMap.size()]);
	
	return ratingAlbums;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#getTotalItemsViewableForUser(magoffin.matt.ma.xsd.User, magoffin.matt.ma.xsd.User)
 */
public int getTotalItemsViewableForUser(User user, User actingUser)
throws MediaAlbumException 
{
	MediaItemCriteria crit = null;
	ObjectPool pool = null;
	
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				MediaItem.class);
		crit = (MediaItemCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(MediaItemCriteria.ITEMS_COUNT_VIEWABLE_FOR_USER);
		Integer[] query = new Integer[] {
				user.getUserId(),
				actingUser == null 
				? ApplicationConstants.ANONYMOUS_USER_ID 
				: actingUser.getUserId()};
		crit.setQuery(query);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				MediaItem.class);
		
		DataObject[] result = dao.findByCriteria(crit);
		
		if ( !(result instanceof CountData[]) ) {
			throw new MediaAlbumException("DAO not configured properly: " +result);
		}
		
		int count = ((CountData[])result)[0].getCount();
		
		return count;
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#isUserSuperUser(java.lang.Integer)
 */
public boolean isUserSuperUser(Integer userId) throws MediaAlbumException {
	Permissions perm = getUserPermissions(userId);
	if ( perm != null && perm.getSuperUser() != null && 
				perm.getSuperUser().booleanValue() ) {
		return true;
	}
	return false;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#getFreeData(magoffin.matt.ma.xsd.User, boolean)
 */
public FreeData[] getFreeData(User user, boolean allowCached) 
throws MediaAlbumException 
{
	FreeData[] result = (FreeData[])getCachedObject(allowCached,
			ApplicationConstants.CacheFactoryKeys.USER_FREE_DATA,
			user.getUserId());
	if ( result != null ) {
		return result;
	}
	
	FreeDataCriteria crit = null;
	ObjectPool pool = null;
	
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				FreeData.class);
		crit = (FreeDataCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(FreeDataCriteria.FREE_DATA_FOR_OWNER);
		crit.setQuery(user.getUserId());
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				FreeData.class);
		
		FreeData[] data = (FreeData[])dao.findByCriteria(crit);
		
		if ( data == null ) {
			result = new FreeData[0];
		} else {
			result = data;
		}
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
	
	cacheObject(ApplicationConstants.CacheFactoryKeys.USER_FREE_DATA,
			user.getUserId(),
			result);
	
	return result;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#getOwnersWithFreeData(magoffin.matt.ma.xsd.Album[], boolean)
 */
public User[] getOwnersWithFreeData(Album[] albums, boolean allowCached)
throws MediaAlbumException 
{
	Integer[] ownerIds = MediaUtil.getOwners(albums);
	if ( ownerIds == null ) {
		return new User[0];
	}
	User[] owners = new User[ownerIds.length];
		for ( int i = 0; i < ownerIds.length; i++ ) {
			User owner = getUserById(ownerIds[i],allowCached);
			/*try {
				owner = (User)BeanUtils.cloneBean(owner);
			} catch ( Exception e ) {
				throw new MediaAlbumException("Unable to clone data",e);
			}*/
			FreeData[] fdata = getFreeData(owner,allowCached);
			owner.setData(fdata);
			
			owners[i] = owner;
		}
	return owners;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#setFreeData(magoffin.matt.ma.xsd.User, magoffin.matt.ma.xsd.FreeData[], magoffin.matt.ma.xsd.User)
 */
public void setFreeData(User user, FreeData[] data, User actingUser)
throws MediaAlbumException, NotAuthorizedException 
{
	if ( !canUserUpdateFreeData(user,actingUser) ) {
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
		
		pk.setUserId(user.getUserId());
		
		dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				FreeData.class);
		
		dao.remove(pk);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,pk);
	}
	
	// now create new user free data
	FreeData[] newFd = new FreeData[data.length];
	for ( int i = 0; i < data.length; i++ ) {
		try {
			newFd[i] = (FreeData)BeanUtils.cloneBean(data[i]);
		} catch ( Exception e ) {
			throw new MediaAlbumException("Unable to clone data",e);
		}
		newFd[i].setOwner(user.getUserId());
		newFd[i].setAlbumId(null);
		newFd[i].setCollectionId(null);
		newFd[i].setItemId(null);
		newFd[i].setDataId(null);
	}
	
	try {
		dao.create(newFd);
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	}
	
	removeObjectFromCache(
			ApplicationConstants.CacheFactoryKeys.USER_FREE_DATA,
			user.getUserId());
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.UserBiz#getDiskUsage(java.lang.Integer)
 */
public long getDiskUsage(Integer userId) throws MediaAlbumException 
{
	MediaItemCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				MediaItem.class);
		crit = (MediaItemCriteria)borrowObjectFromPool(pool);
		crit.setSearchType(MediaItemCriteria.TOTAL_SIZE_FOR_OWNER);
		crit.setQuery(userId);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				MediaItem.class);
		DAOSearchCallback callback = dao.getCallbackInstance(
				DAOConstants.SEARCH_CALLBACK_SIZE);
		
		if ( !(callback instanceof LongNumCallback) ) {
			throw new MediaAlbumException("LongNum callback not configured properly");
		}
		
		LongNumCallback mCallback = (LongNumCallback)callback;
		mCallback.setCriteria(crit);
		dao.find(mCallback);
		
		return mCallback.getLongNum();
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}
}

}
