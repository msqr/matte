/* ===================================================================
 * UploadMediaZipWorkRequest.java
 *
 * Copyright (c) 2002 Matt Magoffin.
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
 * $Id: UploadMediaZipWorkRequest.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import magoffin.matt.biz.BizFactory;
import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.biz.AlbumBiz;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.CollectionBiz;
import magoffin.matt.ma.biz.EmailNotificationBiz;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.biz.UserBiz;
import magoffin.matt.ma.scan.MediaScan;
import magoffin.matt.ma.util.MediaUtil;
import magoffin.matt.ma.util.WorkQueue.WorkRequest;
import magoffin.matt.ma.xsd.Album;
import magoffin.matt.ma.xsd.AlbumMedia;
import magoffin.matt.ma.xsd.Collection;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.ma.xsd.User;
import magoffin.matt.mail.biz.MailBiz;
import magoffin.matt.util.ArrayUtil;
import magoffin.matt.util.FileUtil;
import magoffin.matt.util.StringUtil;
import magoffin.matt.util.config.Config;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.util.MessageResources;

/**
 * WorkRequest implementation for adding new media compressed within a Zip
 * archive.
 * 
 * <p> Created on Dec 18, 2002 3:16:54 PM.</p>
 *
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class UploadMediaZipWorkRequest implements WorkRequest 
{
	private static Logger LOG = Logger.getLogger(UploadMediaZipWorkRequest.class);
	
	private static final String MAILMERGE_ROOT_PATH = 
		Config.get(ApplicationConstants.CONFIG_ENV,
				ApplicationConstants.ENV_MAILMERGE_ROOT_PATH);
	
	public static final String[] MAIL_TEMPLATES_UPLOAD_COMPLETE = 
		Config.getStrings(ApplicationConstants.CONFIG_ENV,
				"mail.template.upload.complete");
	
	public static final String MAIL_SUBJECT_OK = 
		Config.get(ApplicationConstants.CONFIG_ENV,
		"mail.subject.upload.complete.ok");
	
	public static final String MAIL_SUBJECT_NO_MEDIA = 
		Config.get(ApplicationConstants.CONFIG_ENV,
		"mail.subject.upload.complete.none");
	
	public static final String MAIL_SUBJECT_ERROR = 
		Config.get(ApplicationConstants.CONFIG_ENV,
		"mail.subject.upload.complete.err");
	
	private UploadMediaZipWorkRequest.ZipUploadRequestParams params;
	
	private List errors;
	private List processedFiles;
	private List changedAlbums;
	private int count;
	private Map autoCollectionCache = new HashMap();
	private Map autoAlbumCache = new HashMap();
	
	
public UploadMediaZipWorkRequest(UploadMediaZipWorkRequest.ZipUploadRequestParams params)
{
	this.errors = new LinkedList();
	this.processedFiles = new LinkedList();
	this.changedAlbums = new LinkedList();
	this.count = 0;
	
	this.params = params;
}

/*
 * @see magoffin.matt.ma.util.WorkQueue.WorkRequest#startWork()
 */
public void startWork() throws Exception 
{
	try {
		doWork();
	} catch ( Exception e ) {
		LOG.error("Exception in zip work request: " +e.getMessage(),e);
		errors.add(
			new ActionMessage("upload.error.general", e.getMessage()));
	}

	// send user status email
	if ( params.userEmail != null && params.userEmail.length() > 0 ) {
		sendEmail();
	}

	// process album notifications
	if ( changedAlbums.size() > 0 ) {
		EmailNotificationBiz alertBiz = (EmailNotificationBiz)params.bizFactory
				.getBizInstance(BizConstants.EMAIL_NOTIFICATIONS_BIZ);
		Album[] updatedAlbums = (Album[])changedAlbums.toArray(
				new Album[changedAlbums.size()]);
		alertBiz.processUpdatedAlbumNotifications(updatedAlbums,null,
				params.viewAlbumUrl,params.browseUserUrl);
	}
}

private void doWork() throws Exception 
{
	ZipFile zipFile = new ZipFile(params.zippedMediaFile);
	try {
		Enumeration enum = zipFile.entries();
		CollectionBiz collectionBIz = (CollectionBiz)params.bizFactory
			.getBizInstance(BizConstants.COLLECTION_BIZ);
		MediaItemBiz itemBiz = (MediaItemBiz)params.bizFactory.getBizInstance(
				BizConstants.MEDIA_ITEM_BIZ);
		while ( enum.hasMoreElements() ) {
			ZipEntry entry = (ZipEntry)enum.nextElement();
			String fileName = entry.getName();

			if ( entry.isDirectory() ) {
				continue;
			}
			
			Collection collection = null;
			if ( params.autoCollection ) {
				collection = getAutoCollection(fileName);
				String fName = StringUtil.substringAfterFirst(fileName,'/');
				if ( fName != null ) {
					fileName = fName;
				}
			} else {
				collection = params.collection;
			}
			

			File oneFile = new File(collectionBIz.getBaseCollectionDirectory(
					collection),fileName);
			
			// verify configured to handle file type
			if ( !itemBiz.isFileTypeSupported(oneFile.getName()) ) {
				errors.add(
					new ActionMessage("upload.error.file.unsupported",fileName));
				continue;
			}

			// verify dirs exists for file
			File parentDir = oneFile.getParentFile();
			if ( !parentDir.exists() ) {
				parentDir.mkdirs();
			}

			InputStream oneIn = new BufferedInputStream(
				zipFile.getInputStream(entry) );

			if ( LOG.isDebugEnabled() ) {
				LOG.debug("Unzipping media file " +oneFile);
			}
			
			FileUtil.save(oneIn,oneFile,true);

			oneFile.setLastModified(System.currentTimeMillis());

			count++;
			processedFiles.add(fileName);
			
			if ( params.autoAlbum ) {
				saveAutoAlbum(collection,fileName);
			}
			
		}

	} finally {
		// delete original zip file
		try {
			if ( LOG.isDebugEnabled() ) {
				LOG.debug("Deleting uploaded Zip file " +params.zippedMediaFile);
			}
			params.zippedMediaFile.delete();
		} catch ( Exception e ) {
			LOG.warn("Could not delete uploaded Zip file: " +e.getMessage() );
		}
	}

	if ( count > 0 ) {
		// run the media scanner for this source dir
		MediaScan scanner = new MediaScan(
			params.bizFactory,
			false);
		try {
			if ( params.autoCollection && autoCollectionCache.size() > 0 ) {
				for ( Iterator itr = autoCollectionCache.values().iterator(); itr.hasNext(); ) {
					Collection oneCollection = (Collection)itr.next();
					scanner.doScan(oneCollection);
				}
			} else {
				scanner.doScan(params.collection);
			}
		} catch ( MediaAlbumException e ) {
			LOG.error("Exception in MediaScan: " +e.getMessage());
			errors.add(
				new ActionMessage("upload.error.general", e.getMessage()));
		} finally {
			if ( scanner.hasErrors() ) {
				Map errMap = scanner.getErrors();
				for ( Iterator itr = errMap.entrySet().iterator(); itr.hasNext(); ) {
					Map.Entry me = (Map.Entry)itr.next();
					errors.add(new ActionMessage("upload.error.general.file", me.getKey(), me.getValue()));
				}
			}
		}		
	}
	
	// save auto albums to back end
	if ( autoAlbumCache.size() > 0 ) {
		AlbumBiz albumBiz = (AlbumBiz)params.bizFactory.getBizInstance(BizConstants.ALBUM_BIZ);
		MediaItemBiz itemBiz = (MediaItemBiz)params.bizFactory.getBizInstance(BizConstants.MEDIA_ITEM_BIZ);
		for ( Iterator itr = autoAlbumCache.values().iterator(); itr.hasNext(); ) {
			Album album = (Album)itr.next();
			// get existing album media data
			MediaItem[] items = albumBiz.getMediaItemsForAlbum(album.getAlbumId(),
					ApplicationConstants.POPULATE_MODE_NONE, ApplicationConstants.CACHED_OBJECT_ALLOWED, params.getUser());
			Arrays.sort(items,MediaUtil.MEDIA_ITEM_SORT_BY_PATH);
			MediaItem[] newItems = album.getItem();
			List newItemList = new ArrayList(newItems.length);
			for ( int i = 0; i < newItems.length; i++ ) {
				int matchIdx = Arrays.binarySearch(items,newItems[i],MediaUtil.MEDIA_ITEM_SORT_BY_PATH);
				if ( matchIdx < 0 ) {
					newItemList.add(newItems[i]);
				}
			}
			// now need to look up each new item from the backend for it's ID
			if ( newItemList.size() > 0 ) {
				for ( Iterator itr2 = newItemList.iterator(); itr2.hasNext(); ) {
					MediaItem item = (MediaItem)itr2.next();
					MediaItem item2 = itemBiz.getMediaItemByPath(item.getCollection(),item.getPath(),
							ApplicationConstants.CACHED_OBJECT_NOT_ALLOWED);
					if ( item2 == null ) {
						// didn't find item!!?
						itr2.remove();
					} else {
						item.setItemId(item2.getItemId());
					}
				}
				
				newItems = (MediaItem[])newItemList.toArray(
					new MediaItem[newItemList.size()]);
				
				items = (MediaItem[])ArrayUtil.merge(items,newItems);
				List mediaItemsList = new ArrayList(items.length);
				for ( int i = 0; i < items.length; i++ ) {
					AlbumMedia albumItem = new AlbumMedia();
					albumItem.setAlbumId(album.getAlbumId());
					albumItem.setMediaId(items[i].getItemId());
					albumItem.setDisplayOrder(new Integer(i+1));
					mediaItemsList.add(albumItem);							
				}
				
				AlbumMedia[] albumItems = (AlbumMedia[])mediaItemsList.toArray(
						new AlbumMedia[mediaItemsList.size()]);
				albumBiz.setAlbumMediaItems(album.getAlbumId(),albumItems,null);
			}
		}
	}
}

private void saveAutoAlbum(Collection collection, String fileName) 
throws MediaAlbumException
{
	String albumPath = StringUtil.substringBeforeLast(fileName,'/');
	if ( albumPath == null ) {
		return;
	}
	
	Album album = null;
	if ( autoAlbumCache.containsKey(albumPath) ) {
		album = (Album)autoAlbumCache.get(albumPath);
	} else {
		album = getSavedAlbum(albumPath);
	}
	MediaItem fakeItem = new MediaItem();
	fakeItem.setPath(fileName);
	fakeItem.setCollection(collection.getCollectionId());
	album.addItem(fakeItem);
}

private Album getSavedAlbum(String path)  
throws MediaAlbumException
{
	if ( !(path.indexOf('/') > 0) ) {
		Album album = getAlbumByName(path,null);
		autoAlbumCache.put(path,album);
		return album;
	}
	String subPath = StringUtil.substringBeforeLast(path,'/');
	Album parent = getSavedAlbum(subPath);
	String name = StringUtil.substringAfter(path,'/');
	Album album = getAlbumByName(name,parent.getAlbumId());
	album.setParentId(parent.getAlbumId());
	autoAlbumCache.put(path,album);
	return album;
}

private Album getAlbumByName(String name,Integer parentId) 
throws MediaAlbumException
{
	
	// first check cache
	if ( autoAlbumCache.containsKey(name) ) {
		Album album = MediaUtil.findAlbumByName(
			(Album)autoAlbumCache.get(name),name,parentId);
		if ( album != null ) {
			return album;
		}
	}
	
	UserBiz userBiz = (UserBiz)params.bizFactory.getBizInstance(BizConstants.USER_BIZ);
	Album[] albums = userBiz.getAlbumsOwnedByUser(params.user.getUserId());
	
	Album album = null;
	if ( albums != null ) {
		for ( int i = 0; i < albums.length && album == null; i++ ) {
			album = MediaUtil.findAlbumByName(albums[i],name,parentId);
		}
	}
	if ( album != null ) {
		changedAlbums.add(album);
		return album;
	}
	// create new album
	album = new Album();
	album.setName(name);
	album.setParentId(parentId);
	
	AlbumBiz albumBiz = (AlbumBiz)params.bizFactory.getBizInstance(BizConstants.ALBUM_BIZ);
	
	album = albumBiz.createAlbum(album,params.user);
	return album;
}


private Collection getAutoCollection(String fileName) throws MediaAlbumException
{
	String collectionName = StringUtil.substringBefore(fileName,'/');
	
	if ( collectionName == null ) {
		return params.collection;
	}
	
	if ( autoCollectionCache.containsKey(collectionName) ) {
		return (Collection)autoCollectionCache.get(collectionName);
	}
	
	CollectionBiz collectionBiz = (CollectionBiz)params.bizFactory.getBizInstance(
		BizConstants.COLLECTION_BIZ);
	UserBiz userBiz = (UserBiz)params.bizFactory.getBizInstance(
		BizConstants.USER_BIZ);
	
	if ( autoCollectionCache.size() < 1 ) {
		Collection[] userDirs = userBiz.getCollectionsForUser(params.user.getUserId());
		if ( userDirs != null ) {
			for ( int i = 0; i < userDirs.length; i++ ) {
				autoCollectionCache.put(userDirs[i].getName(),userDirs[i]);
			}
		}
	}
	
	if ( autoCollectionCache.containsKey(collectionName) ) {
		return (Collection)autoCollectionCache.get(collectionName);
	}
	
	// create the new collection now
	String autoCollectionName = String.valueOf(System.currentTimeMillis());
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Creatig new collection '" +autoCollectionName +"' for user ID " +params.user.getUserId());
	}
	Collection collection = collectionBiz.createCollection(collectionName,params.user);
	
	autoCollectionCache.put(collectionName,collection);
	return collection;
}

private void sendEmail()
{
	StringBuffer buf = new StringBuffer();
	String subject = null;
	Map mergeMap = new HashMap(10);

	mergeMap.put("file_name",params.zippedMediaFile.getName());
	mergeMap.put("file_count",new Integer(count));
	mergeMap.put("collection",params.collection);
	
	if ( errors.size() < 1 && count < 1 ) {
		subject = MAIL_SUBJECT_NO_MEDIA;
	} else if ( errors.size() < 1 ) {
		// no errors
		subject = MAIL_SUBJECT_OK;
	} else {
		// errors!
		subject = MAIL_SUBJECT_ERROR;
		buf.append("The following errors were encountered:\n\n"); 
		int errCount = 1;
		for ( Iterator itr = errors.iterator(); itr.hasNext(); errCount++) {
			ActionMessage msg = (ActionMessage)itr.next();
			buf.append(errCount).append(". ")
				.append(params.resources.getMessage(msg.getKey(),msg.getValues()))
				.append("\n\n");
		}
	}
	
	if ( this.count > 0 ) {
		int i = 1;
		buf.append("Processed files:\n\n");
		for ( Iterator itr = processedFiles.iterator(); itr.hasNext(); i++ ) {
			buf.append(i).append(". ").append(itr.next()).append("\n");
		}
	}
	
	mergeMap.put("msg_body",buf.toString());
	
	String[] templatePaths = new String[MAIL_TEMPLATES_UPLOAD_COMPLETE.length];
	for (int i = 0; i < templatePaths.length; i++ ) {
		templatePaths[i] = MAILMERGE_ROOT_PATH+MAIL_TEMPLATES_UPLOAD_COMPLETE[i];
	}
	
	try {
		MailBiz mailBiz = (MailBiz)params.bizFactory.getBizInstance(
				BizConstants.MAIL_BIZ);
		mailBiz.sendResourceMailMerge(null,params.userEmail,null,null,subject,
				templatePaths,mergeMap);
	} catch ( Exception e ) {
		LOG.error("Unable to send user email message",e);
		LOG.error(buf.toString());
	}
}

/**
 * Class to encapsulate parameters for a zip upload work request.
 * 
 * @author Matt Magofin (spamsqr@msqr.us)
 */
public static final class ZipUploadRequestParams 
{
	private BizFactory bizFactory;
	private File zippedMediaFile;
	private Collection collection;
	private String userEmail;
	private String[] ccEmail = null;
	private String[] bccEmail = null;
	private MessageResources resources;
	private boolean autoCollection;
	private boolean autoAlbum;
	private boolean overwrite;
	private User user;
	private URL viewAlbumUrl; // for email notifications
	private URL browseUserUrl; // for email notifications
	/**
	 * @return boolean
	 */
	public boolean isAutoAlbum() {
		return autoAlbum;
	}

	/**
	 * @return boolean
	 */
	public boolean isAutoCollection() {
		return autoCollection;
	}

	/**
	 * @return String[]
	 */
	public String[] getBccEmail() {
		return bccEmail;
	}

	/**
	 * @return String[]
	 */
	public String[] getCcEmail() {
		return ccEmail;
	}

	/**
	 * @return Collection
	 */
	public Collection getCollection() {
		return collection;
	}

	/**
	 * @return MessageResources
	 */
	public MessageResources getResources() {
		return resources;
	}

	/**
	 * @return String
	 */
	public String getUserEmail() {
		return userEmail;
	}

	/**
	 * @return File
	 */
	public File getZippedMediaFile() {
		return zippedMediaFile;
	}

	/**
	 * Sets the autoAlbum.
	 * @param autoAlbum The autoAlbum to set
	 */
	public void setAutoAlbum(boolean autoAlbum) {
		this.autoAlbum = autoAlbum;
	}

	/**
	 * Sets the autoCollection.
	 * @param autoCollection The autoCollection to set
	 */
	public void setAutoCollection(boolean autoCollection) {
		this.autoCollection = autoCollection;
	}

	/**
	 * Sets the bccEmail.
	 * @param bccEmail The bccEmail to set
	 */
	public void setBccEmail(String[] bccEmail) {
		this.bccEmail = bccEmail;
	}

	/**
	 * Sets the ccEmail.
	 * @param ccEmail The ccEmail to set
	 */
	public void setCcEmail(String[] ccEmail) {
		this.ccEmail = ccEmail;
	}

	/**
	 * Sets the dir.
	 * @param dir The dir to set
	 */
	public void setCollection(Collection dir) {
		this.collection = dir;
	}

	/**
	 * Sets the resources.
	 * @param resources The resources to set
	 */
	public void setResources(MessageResources resources) {
		this.resources = resources;
	}

	/**
	 * Sets the userEmail.
	 * @param userEmail The userEmail to set
	 */
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	/**
	 * Sets the zippedMediaFile.
	 * @param zippedMediaFile The zippedMediaFile to set
	 */
	public void setZippedMediaFile(File zippedMediaFile) {
		this.zippedMediaFile = zippedMediaFile;
	}

	/**
	 * @return boolean
	 */
	public boolean isOverwrite() {
		return overwrite;
	}

	/**
	 * Sets the overwrite.
	 * @param overwrite The overwrite to set
	 */
	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

	/**
	 * @return User
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Sets the user.
	 * @param user The user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @return Returns the bizFactory.
	 */
	public BizFactory getBizFactory() {
		return bizFactory;
	}

	/**
	 * @param bizFactory The bizFactory to set.
	 */
	public void setBizFactory(BizFactory bizFactory) {
		this.bizFactory = bizFactory;
	}

	/**
	 * @return Returns the browseUserUrl.
	 */
	public URL getBrowseUserUrl()
	{
		return browseUserUrl;
	}
	/**
	 * @param browseUserUrl The browseUserUrl to set.
	 */
	public void setBrowseUserUrl(URL browseUserUrl)
	{
		this.browseUserUrl = browseUserUrl;
	}
	/**
	 * @return Returns the viewAlbumUrl.
	 */
	public URL getViewAlbumUrl()
	{
		return viewAlbumUrl;
	}
	/**
	 * @param viewAlbumUrl The viewAlbumUrl to set.
	 */
	public void setViewAlbumUrl(URL viewAlbumUrl)
	{
		this.viewAlbumUrl = viewAlbumUrl;
	}
} // class ZipUploadRequestParams

} // UploadMediaZipWorkRequest
