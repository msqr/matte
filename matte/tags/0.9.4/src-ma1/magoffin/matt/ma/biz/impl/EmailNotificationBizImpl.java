/* ===================================================================
 * EmailNotificationBizImpl.java
 * 
 * Created Apr 28, 2004 8:57:54 AM
 * 
 * Copyright (c) 2004 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: EmailNotificationBizImpl.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.biz.impl;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import magoffin.matt.biz.BizInitializer;
import magoffin.matt.dao.CriteriaObjectPoolFactory;
import magoffin.matt.dao.DAO;
import magoffin.matt.dao.DAOException;
import magoffin.matt.dao.PrimaryKeyObjectPoolFactory;
import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.biz.AlbumBiz;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.EmailNotificationBiz;
import magoffin.matt.ma.biz.UserBiz;
import magoffin.matt.ma.biz.WorkBiz;
import magoffin.matt.ma.dao.FreeDataCriteria;
import magoffin.matt.ma.dao.FreeDataPK;
import magoffin.matt.ma.util.WorkQueue;
import magoffin.matt.ma.xsd.Album;
import magoffin.matt.ma.xsd.FreeData;
import magoffin.matt.ma.xsd.User;
import magoffin.matt.mail.MailProcessingException;
import magoffin.matt.mail.biz.MailBiz;
import magoffin.matt.util.config.Config;

import org.apache.commons.pool.ObjectPool;
import org.apache.log4j.Logger;

/**
 * Implementation of the EmailNotificationBiz interface.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class EmailNotificationBizImpl extends AbstractBiz
implements EmailNotificationBiz
{
	private static final Logger LOG = Logger.getLogger(EmailNotificationBizImpl.class);
	
	/**
	 * The mail merge template resource property for the album update
	 * notification event, from {@link ApplicationConstants#CONFIG_ENV}.
	 */
	public static final String ENV_MAIL_TEMPLATES_ALBUM_UPDATE_NOTIFICATION = 
			"mail.template.alert.album.updated";
	
	/**
	 * The mail merge subject property for the album update
	 * notification event, from {@link ApplicationConstants#CONFIG_ENV}.
	 */
	public static final String ENV_MAIL_SUBJECT_ALBUM_UPDATE_NOTIFICATION = 
			"mail.subject.alert.album.updated";
	
	/**
	 * The mail merge template resource property for the user update
	 * notification event, from {@link ApplicationConstants#CONFIG_ENV}.
	 */
	public static final String ENV_MAIL_TEMPLATES_USER_UPDATE_NOTIFICATION = 
			"mail.template.alert.user.updated";
	
	/**
	 * The mail merge subject property for the user update
	 * notification event, from {@link ApplicationConstants#CONFIG_ENV}.
	 */
	public static final String ENV_MAIL_SUBJECT_USER_UPDATE_NOTIFICATION = 
			"mail.subject.alert.user.updated";
	
	private String[] mailTemplateAlbumUpdateNotification = null;
	private String[] mailTemplateUserUpdateNotification = null;
	
/* (non-Javadoc)
 * @see magoffin.matt.biz.Biz#init(magoffin.matt.biz.BizInitializer)
 */
public void init(BizInitializer initializer)
{
	super.init(initializer);
	mailTemplateAlbumUpdateNotification = getMailTemplateResourcePaths(
			Config.getStrings(
			ApplicationConstants.CONFIG_ENV, 
			ENV_MAIL_TEMPLATES_ALBUM_UPDATE_NOTIFICATION));
	mailTemplateUserUpdateNotification = getMailTemplateResourcePaths(
			Config.getStrings(
			ApplicationConstants.CONFIG_ENV, 
			ENV_MAIL_TEMPLATES_USER_UPDATE_NOTIFICATION));
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.EmailNotificationBiz#watchForNewItems(java.lang.Integer, magoffin.matt.ma.xsd.User)
 */
public void watchForNewItems(Integer userId, User actingUser)
throws MediaAlbumException
{
	// get current user free data
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	FreeData[] fdata = userBiz.getFreeData(actingUser,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
	// verify not already watching
	for ( int i = 0; i < fdata.length; i++ ) {
		if ( ApplicationConstants.FREE_DATA_TYPE_EMAIL_NOTIFICATION.equals(
				fdata[i].getDataTypeId()) && userId.equals(
				fdata[i].getUserId()) ) {
			// already watching, so just return now
			return;
		}
	}
	
	// ok, create email alert now
	FreeData alert = new FreeData();
	alert.setDataTypeId(ApplicationConstants.FREE_DATA_TYPE_EMAIL_NOTIFICATION);
	alert.setUserId(userId);
	alert.setOwner(actingUser.getUserId());
	alert.setDataValue(String.valueOf(System.currentTimeMillis()));
	
	DAO dao = null;
	
	try {
		dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				FreeData.class);
		
		dao.create(alert);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	}
	
	removeObjectFromCache(ApplicationConstants.CacheFactoryKeys.USER_FREE_DATA,
			actingUser.getUserId());
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.EmailNotificationBiz#stopWatchingForNewItems(java.lang.Integer, magoffin.matt.ma.xsd.User)
 */
public void stopWatchingForNewItems(Integer userId, User actingUser)
throws MediaAlbumException
{
	// get current user free data
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	FreeData[] fdata = userBiz.getFreeData(actingUser,
			ApplicationConstants.CACHED_OBJECT_NOT_ALLOWED);
	
	// verify already watching
	for ( int i = 0; i < fdata.length; i++ ) {
		if ( ApplicationConstants.FREE_DATA_TYPE_EMAIL_NOTIFICATION.equals(
				fdata[i].getDataTypeId()) && userId.equals(
				fdata[i].getUserId()) ) {
			DAO dao = null;
			FreeDataPK pk = null;
			ObjectPool pool = null;
			try {
				pool = PrimaryKeyObjectPoolFactory.getInstance().getPrimaryKeyObjectPool(
						FreeData.class);
				pk = (FreeDataPK)borrowObjectFromPool(pool);
				dao = initializer.getDAOFactory().getDataAccessObjectInstance(
						FreeData.class);
				
				pk.setDataId(fdata[i].getDataId());
				dao.remove(pk);
				
			} catch (DAOException e) {
				throw new MediaAlbumException("DAO exception",e);
			} finally {
				returnObjectToPool(pool,pk);
			}
			
			removeObjectFromCache(ApplicationConstants.CacheFactoryKeys.USER_FREE_DATA,
					actingUser.getUserId());
		}
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.EmailNotificationBiz#watchForNewItemsInAlbum(java.lang.Integer, magoffin.matt.ma.xsd.User)
 */
public void watchForNewItemsInAlbum(Integer albumId, User actingUser)
throws MediaAlbumException
{
	// get current album free data
	AlbumBiz albumBiz = (AlbumBiz)getBiz(BizConstants.ALBUM_BIZ);
	FreeData[] fdata = albumBiz.getFreeData(albumId,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
	// verify not already watching
	for ( int i = 0; i < fdata.length; i++ ) {
		if ( ApplicationConstants.FREE_DATA_TYPE_EMAIL_NOTIFICATION.equals(
				fdata[i].getDataTypeId()) && actingUser.getUserId().equals(
				fdata[i].getOwner()) ) {
			// already watching, so just return now
			return;
		}
	}
	
	// ok, create email alert now
	FreeData alert = new FreeData();
	alert.setDataTypeId(ApplicationConstants.FREE_DATA_TYPE_EMAIL_NOTIFICATION);
	alert.setAlbumId(albumId);
	alert.setOwner(actingUser.getUserId());
	alert.setDataValue(String.valueOf(System.currentTimeMillis()));
	
	DAO dao = null;
	
	try {
		dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				FreeData.class);
		
		dao.create(alert);
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	}
	
	removeObjectFromCache(ApplicationConstants.CacheFactoryKeys.ALBUM_FREE_DATA,
			albumId);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.EmailNotificationBiz#stopWatchingForNewItemsInAlbum(java.lang.Integer, magoffin.matt.ma.xsd.User)
 */
public void stopWatchingForNewItemsInAlbum(Integer albumId, User actingUser)
throws MediaAlbumException
{
	// get current album free data
	AlbumBiz albumBiz = (AlbumBiz)getBiz(BizConstants.ALBUM_BIZ);
	FreeData[] fdata = albumBiz.getFreeData(albumId,
			ApplicationConstants.CACHED_OBJECT_NOT_ALLOWED);
	
	// verify not already watching
	for ( int i = 0; i < fdata.length; i++ ) {
		if ( ApplicationConstants.FREE_DATA_TYPE_EMAIL_NOTIFICATION.equals(
				fdata[i].getDataTypeId()) && actingUser.getUserId().equals(
				fdata[i].getOwner()) ) {
			DAO dao = null;
			FreeDataPK pk = null;
			ObjectPool pool = null;
			try {
				pool = PrimaryKeyObjectPoolFactory.getInstance().getPrimaryKeyObjectPool(
						FreeData.class);
				pk = (FreeDataPK)borrowObjectFromPool(pool);
				dao = initializer.getDAOFactory().getDataAccessObjectInstance(
						FreeData.class);
				
				pk.setDataId(fdata[i].getDataId());
				dao.remove(pk);
				
			} catch (DAOException e) {
				throw new MediaAlbumException("DAO exception",e);
			} finally {
				returnObjectToPool(pool,pk);
			}
			
			removeObjectFromCache(ApplicationConstants.CacheFactoryKeys.ALBUM_FREE_DATA,
					albumId);
		}
	}
	
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.EmailNotificationBiz#processUpdatedAlbumNotifications(magoffin.matt.ma.xsd.Album[], java.lang.String, java.net.URL)
 */
public void processUpdatedAlbumNotifications(Album[] albums,
		String message, URL viewAlbumUrl, URL browseUserUrl) throws MediaAlbumException
{
	EmailNotificationWorkRequest work = new EmailNotificationWorkRequest(
			this,albums,message,viewAlbumUrl,browseUserUrl);
	
	WorkBiz workBiz = (WorkBiz)getBiz(BizConstants.WORK_BIZ);
	workBiz.queue(work);	
}

private static class EmailNotificationWorkRequest implements WorkQueue.WorkRequest
{
	private EmailNotificationBizImpl myBiz;
	private Album[] albums;
	private String message;
	private URL viewAlbumUrl;
	private URL browseUserUrl;
	
	public EmailNotificationWorkRequest(EmailNotificationBizImpl myBiz, Album[] albums,
			String message, URL viewAlbumUrl, URL browseUserUrl) {
		this.myBiz = myBiz;
		this.albums = albums;
		this.message = message;
		this.viewAlbumUrl = viewAlbumUrl;
		this.browseUserUrl = browseUserUrl;
	}
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma.util.WorkQueue.WorkRequest#startWork()
	 */
	public void startWork() throws Exception
	{
		// compile all results into a single list
		Map completedMap = new HashMap();
		Map userMap = new HashMap();
		Map mergeData = new HashMap();
		
		// for each album find all users to notify
		AlbumBiz albumBiz = (AlbumBiz)myBiz.getBiz(BizConstants.ALBUM_BIZ);
		UserBiz userBiz = (UserBiz)myBiz.getBiz(BizConstants.USER_BIZ);
		MailBiz mailBiz = (MailBiz)myBiz.getBiz(BizConstants.MAIL_BIZ);
		for ( int i = 0; i < albums.length; i++ ) {
			FreeData[] fdata = albumBiz.getFreeData(albums[i].getAlbumId(),
					ApplicationConstants.CACHED_OBJECT_ALLOWED);
			for ( int j = 0; j < fdata.length; j++ ) {
				if ( ApplicationConstants.FREE_DATA_TYPE_EMAIL_NOTIFICATION.equals(
						fdata[i].getDataTypeId()) ) {
					if ( !userMap.containsKey(fdata[i].getOwner()) ) {
						User user = userBiz.getUserById(fdata[i].getOwner(),
								ApplicationConstants.CACHED_OBJECT_ALLOWED);
						userMap.put(user.getUserId(),user);
					}
				}
			}
			if ( userMap.size() < 1 ) {
				continue;
			}
			
			if ( LOG.isDebugEnabled() ) {
				LOG.debug("Sending email alert for updated album " +albums[i].getAlbumId()
						+" to " +userMap.size() +" users");
			}
			
			for ( Iterator itr = userMap.values().iterator(); itr.hasNext(); ) {
				User user = (User)itr.next();
				if ( ! albumBiz.canUserViewAlbum(user,albums[i].getAlbumId()) ) {
					if ( LOG.isDebugEnabled() ) {
						LOG.debug("User " +user.getUserId() +" is not allowed to view album "
								+albums[i].getAlbumId() +" so not sending notificaiton alert");
						continue;
					}
				}
				String url = viewAlbumUrl.toString() +"?key=" +albums[i].getAnonymousKey();
				mergeData.clear();
				mergeData.put("message",message);
				mergeData.put("user",user);
				mergeData.put("album",albums[i]);
				mergeData.put("url_view",url);
				try {
					String mailAddress = mailBiz.formatMailAddress(user.getName(),
							user.getEmail());
					Object[] subjectParams = new Object[] {
							user.getName(),
							albums[i].getName() };
					String subject = Config.get(
							ApplicationConstants.CONFIG_ENV,
							ENV_MAIL_SUBJECT_ALBUM_UPDATE_NOTIFICATION,
							subjectParams);
					mailBiz.sendResourceMailMerge(null,mailAddress,null,null,
							subject, myBiz.mailTemplateAlbumUpdateNotification,mergeData);
				} catch ( MailProcessingException e ) {
					LOG.error("Unable to send album update notification email: " 
							+e.toString());
				}
			}
			
			// save users in completed map
			completedMap.putAll(userMap);
			
			// reset map for next album
			userMap.clear();
		}
		
		// ok, now look for anyone watching for user changes in general
		for ( int i = 0; i < albums.length; i++ ) {
			User owner = userBiz.getUserById(albums[i].getOwner(),
					ApplicationConstants.CACHED_OBJECT_ALLOWED);
		
			FreeDataCriteria crit = null;
			ObjectPool pool = null;
			FreeData[] fdata = null;
			try {
				pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
						FreeData.class);
				crit = (FreeDataCriteria)myBiz.borrowObjectFromPool(pool);
				
				crit.setSearchType(FreeDataCriteria.FREE_DATA_FOR_WATCHING_USER);
				crit.setQuery(owner.getUserId());
				
				DAO dao = myBiz.initializer.getDAOFactory().getDataAccessObjectInstance(
						FreeData.class);
				
				fdata = (FreeData[])dao.findByCriteria(crit);
				
			} catch (DAOException e) {
				throw new MediaAlbumException("DAO exception",e);
			} finally {
				myBiz.returnObjectToPool(pool,crit);
			}
			
			if ( fdata == null || fdata.length < 1 ) continue;
			
			for ( int j = 0; j < fdata.length; j++ ) {
				Integer watchingUserId = fdata[j].getUserId();
				if ( completedMap.containsKey(watchingUserId) ) continue; // don't email again
				
				User watchingUser = userBiz.getUserById(watchingUserId,
						ApplicationConstants.CACHED_OBJECT_ALLOWED);
				Album[] browsable = userBiz.getAlbumsViewableForUser(owner,
						AlbumBiz.SORT_ALBUM_BY_DATE,watchingUser);
				if ( browsable == null || browsable.length < 1 ) continue;
				
				Album updatedAlbum = albumBiz.findAlbum(browsable,albums[i].getAlbumId());
				
				if ( updatedAlbum == null ) continue; // not browsable

				// calculate the album heirarchy for the email link
				StringBuffer linkUrl = new StringBuffer(browseUserUrl.toString());
				linkUrl.append("?key=").append(owner.getAnonymousKey());
				
				// finally! we have the necessary data
				mergeData.clear();
				mergeData.put("user",owner);
				mergeData.put("message",message);
				mergeData.put("browse_url",linkUrl.toString());
				linkUrl.append("&album=").append(updatedAlbum.getAlbumId());
				mergeData.put("view_url",linkUrl.toString());
				try {
					String mailAddress = mailBiz.formatMailAddress(
							watchingUser.getName(),watchingUser.getEmail());
					Object[] subjectParams = new Object[] { owner.getName() };
					String subject = Config.get(
							ApplicationConstants.CONFIG_ENV,
							ENV_MAIL_SUBJECT_USER_UPDATE_NOTIFICATION,
							subjectParams);
					mailBiz.sendResourceMailMerge(null,mailAddress,null,null,
							subject, myBiz.mailTemplateUserUpdateNotification,mergeData);
				} catch ( MailProcessingException e ) {
					LOG.error("Unable to send album update notification email: " 
							+e.toString());
				}
				
				// stash in map so don't email again
				completedMap.put(watchingUserId,watchingUser);
			}
		}
	}
}

}
