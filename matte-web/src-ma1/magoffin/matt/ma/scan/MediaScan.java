/* ===================================================================
 * MediaScan.java
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
 * $Id: MediaScan.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.scan;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import magoffin.matt.biz.BizFactory;
import magoffin.matt.dao.DAOException;
import magoffin.matt.dao.DuplicateKeyException;
import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MediaAlbumRuntimeException;
import magoffin.matt.ma.MediaMetadata;
import magoffin.matt.ma.MediaRequestHandler;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.CollectionBiz;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.biz.UserBiz;
import magoffin.matt.ma.util.MediaUtil;
import magoffin.matt.ma.xsd.Collection;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.ma.xsd.User;
import magoffin.matt.util.ArrayUtil;
import magoffin.matt.util.StringUtil;

import org.apache.log4j.Logger;

/**
 * Helper class for scanning media source directories and updating 
 * the database with new/changed media.
 * 
 * <p>Created on Sep 27, 2002 5:06:37 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class MediaScan 
{
	private static final Logger LOG = Logger.getLogger(MediaScan.class);
	
	private static final FileFilter DIR_FILTER = new MediaScan.MediaDirFilter();
	
	private boolean test = false;
	private MediaFileFilter filterTemplate = null;
	private Map errors = null;
	
	private CollectionBiz collectionBiz = null;
	private MediaItemBiz itemBiz = null;
	private UserBiz userBiz = null;
	
/**
 * MediaScan constructor.
 *  * @param bizFactory
 * @param test */
public MediaScan(
	BizFactory bizFactory,
	boolean test)
{
	this.test = test;
	
	this.collectionBiz = (CollectionBiz)bizFactory.getBizInstance(BizConstants.COLLECTION_BIZ);
	this.itemBiz = (MediaItemBiz)bizFactory.getBizInstance(BizConstants.MEDIA_ITEM_BIZ);
	this.userBiz = (UserBiz)bizFactory.getBizInstance(BizConstants.USER_BIZ);
	
	// create our filter template
	try {
		filterTemplate = new MediaScan.MediaFileFilter();
		for ( Iterator itr = itemBiz.getSupportedFileTypes().iterator(); itr.hasNext(); ) {
			String ext = (String)itr.next();
			filterTemplate .addExtension(ext);
		}
	} catch ( MediaAlbumException e ) {
		throw new MediaAlbumRuntimeException("Unable to configure filter template",e);
	}
	
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("FileFilter tempalte = " +filterTemplate);
	}
}


/**
 * Perform a media scan on all media sources.
 *  * @return the number of media items added * @throws MediaAlbumException */
public int doScan() throws MediaAlbumException
{
	// clear out any errors
	this.errors = null;
	
	Collection[] dirs = collectionBiz.getAllCollections();
	int count = 0;
	for ( int i = 0; i < dirs.length; i++ ) {
		count += this.doScan(dirs[i],false);
	}
	return count;
}


/**
 * Perform a media scan on a single media source.
 *  * @param dir * @return the number of media items added
 * @throws MediaAlbumException */
public int doScan(Collection dir) throws MediaAlbumException
{
	return this.doScan(dir,true);
}


private int doScan(Collection collection, boolean clearErorrs) throws MediaAlbumException
{
	File root = collectionBiz.getBaseCollectionDirectory(collection);
	
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Scanning dir " +root.getAbsolutePath());
	}
	
	if ( clearErorrs ) {
		this.errors = null;
	}
	
	MediaScan.MediaFileFilter filter = (MediaScan.MediaFileFilter)this.filterTemplate.clone();
	if ( collection.getScandate() != null ) {
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Setting newerThanDate to " +collection.getScandate());
		}
		filter.newerThanDate = collection.getScandate().getTime();
	}
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("File filter = " +filter);
	}
	
	
	try {
		// perform the recursive scan
		Date scanDate = new Date();
		int count = this.scanDir(collection,root,filter);
		collection.setScandate(scanDate);
		collectionBiz.updateCollection(collection);
		return count;
	} catch ( Exception e ) {
		LOG.error("Exception scanning collection " +root.getAbsolutePath() +": " +e.getMessage(),e);
		if ( e instanceof MediaAlbumException ) {
			throw (MediaAlbumException)e;
		}
		throw new MediaAlbumException("Exception scanning collection " +collection.getPath() +": "
			+ e.getMessage(),e);
	}
}


/**
 * Return <em>true</em> if there are any errors stored in this object.
 *  * @return boolean */
public boolean hasErrors()
{
	return this.errors != null && this.errors.size() > 0 ? true : false;
}


/**
 * Get a map of errors, where the file name is the key and the 
 * erorr message is the value.
 *  * @return Map */
public Map getErrors()
{
	return this.errors;
}

/**
 * Method scanDir.
 * 
 * @param dir
 * @param root
 * @param filter
 * @return
 * @throws IOException
 * @throws DAOException
 * @throws MediaAlbumException
 */
private int scanDir(
	Collection dir,
	File root,
	FileFilter filter) 
throws IOException, DAOException, MediaAlbumException
{
	if ( ! root.isDirectory() ) {
		return 0;
	}
	
	int count = 0;
	
	File[] files = root.listFiles(filter);
	if ( files != null && files.length > 0 ) {
		for ( int i = 0; i < files.length; i++ ) {
			if ( !files[i].canRead() ) {
				LOG.warn("Can't read file " +files[i].getPath());
				this.addError(dir,files[i],"can't read file");
				continue;
			}
			String name = files[i].getName();
			int extIdx = name.lastIndexOf('.');
			if ( extIdx > 0 && extIdx < name.length() ) { // don't allow files starting/ending with .
				String ext = name.substring(extIdx+1).toLowerCase();
				MediaRequestHandler handler = itemBiz.getHandlerForExtension(ext);
				if ( handler == null ) {
					LOG.warn("No handler defined for file " +files[i].getPath());
					this.addError(dir,files[i],"no handler defined for file type");
					continue;
				}
				
				MediaItem item = new MediaItem();
				
				//item.setHits(null);
				item.setName(null);
//				item.setDir(dir.getId());
				
				// set the MIME type of the item
				String mime = itemBiz.getMIMEforExtension(ext);
				if ( mime == null ) {
					LOG.warn("No MIME mapping for extension " +ext +", skipping file " +files[i].getPath());
					this.addError(dir,files[i],"no MIME mapping for file type");
					continue;
				}
				item.setMime(mime);
				
				// set the relative path of the item
				String relPath = files[i].getAbsoluteFile().toURL().toString().substring(
						collectionBiz.getBaseCollectionDirectory(dir).getAbsoluteFile().toURL().toString().length());
				item.setPath(relPath);
				
				// set the file size
				item.setFileSize(new Integer((int)files[i].length()));
				
				MediaMetadata meta = null;
				
				try {
					// get the item parameters (size, width, etc)
					meta = handler.setMediaItemParameters(files[i], item);
				} catch (Exception e) {
					LOG.warn("Exception reading image " +files[i].getPath() +": " +e.getMessage(),e);
					this.addError(dir,files[i],"exception decoding image: " +e.getMessage());
					continue;
				} catch (Error e) {
					LOG.warn("Error reading image " +files[i].getPath() +": " +e,e);
					Runtime runtime = Runtime.getRuntime();
					runtime.runFinalization();
					runtime.gc();
					this.addError(dir,files[i],"error decoding image: " +e.getMessage());
					continue;
				}
				
				if ( item.getWidth() == null || item.getHeight() == null ) {
					LOG.warn("Width/Height not set for image " +files[i].getPath());
					this.addError(dir,files[i],"could not get width/height");
					continue;
				}
				
				if ( item.getCreationDate() == null ) {
					// set to file date
					item.setCreationDate(new Date(files[i].lastModified()));
				}
				
				if ( meta != null ) {
					item.setMeta(meta.serializeToString());
				} else {
					item.setMeta(null);
				}
				
				if ( LOG.isDebugEnabled() ) {
					LOG.debug("Got media item " +item.getPath() 
						+", collection=" +dir.getCollectionId()
						+", mime=" +item.getMime()
						+", width=" +item.getWidth()
						+", height=" +item.getHeight()
						+", fileSize=" +item.getFileSize()
						+", meta=" +item.getMeta() );
				}
				if ( !test ) {
					try {
						item = itemBiz.createMediaItem(item,dir.getCollectionId(),null); // make scan user instead of null?
					} catch (MediaAlbumException e) {
						if ( MediaItemBiz.ERROR_QUOTA_EXCEEDED.equals(
								e.getErrorCode()) ) {
							// FIXME use message
							this.addError(dir,files[i], "Quota exceeded");
						} else {
							Throwable nested = e.getNestedException();
							if ( nested != null && nested instanceof DuplicateKeyException ) {
								if ( LOG.isDebugEnabled() ) {
									LOG.debug("Duplicate media item: " 
											+dir.getCollectionId() 
											+ ":" +item.getPath());
								}
								try {
									MediaItem item2 = itemBiz.getMediaItemByPath(dir.getCollectionId(),item.getPath(),
											ApplicationConstants.CACHED_OBJECT_NOT_ALLOWED);
									if ( item2 == null ) {
										this.addError(dir,files[i],"Unable to update item (collection/path not found)");
									} else {
										item.setHits(item2.getHits());
										item.setItemId(item2.getItemId());
										if ( item2.getName() != null ) {
											item.setName(item2.getName());
										}
										item.setCollection(item2.getCollection());
										item.setComment(item2.getComment());
										item.setTzCode(item2.getTzCode());
										
										// do not override create date of custom date flag set
										if ( item2.getCustomDate().booleanValue() 
												&& item2.getCreationDate() != null ) {
											item.setCreationDate(item2.getCreationDate());
										} else {
											// translate time, because update will untranslate
											User owner = userBiz.getUserById(
													collectionBiz.getCollectionById(
															item2.getCollection(),
															ApplicationConstants.CACHED_OBJECT_ALLOWED).getOwner(),
													ApplicationConstants.CACHED_OBJECT_ALLOWED);
											MediaUtil.translateItemTime(owner.getTzCode(),item);
										}
										
										itemBiz.updateMediaItem(item,null); // make scan user?
										itemBiz.deleteMediaItemCacheFiles(item,null);
									}
								} catch ( MediaAlbumException e2 ) {
									this.addError(dir,files[i],"Unable to update item: " +e2.getMessage());
								}
							} else {
								throw e;
							}
						}
					}
				}
				count++;
				
				if ( count % 25 == 0 ) {
					Runtime runtime = Runtime.getRuntime();
					runtime.runFinalization();
					runtime.gc();
				}
				
			} else {
				if ( LOG.isDebugEnabled() ) {
					LOG.debug("Skipping file " +files[i].getPath());
				}
			}
		}
	}
	
	File[] dirs = root.listFiles(DIR_FILTER);
	if ( dirs != null && dirs.length > 0 ) {
		for ( int i = 0; i < dirs.length; i++ ) {
			count += scanDir(dir,dirs[i],filter);
		}
	}
	
	Runtime runtime = Runtime.getRuntime();
	runtime.runFinalization();
	runtime.gc();
	return count;
}


/**
 * Add an error to the error map.
 *  * @param collection
 * @param file
 * @param msg */
private void addError(Collection collection, File file, Object msg)
throws MediaAlbumException
{
	if ( file == null || msg == null ) {
		return;
	}
	File collectionDir = collectionBiz.getBaseCollectionDirectory(collection);
	String relPath = file.getAbsolutePath().substring(collectionDir.getAbsolutePath().length()+1);
	if ( this.errors == null ) {
		this.errors = new HashMap();
	}
	this.errors.put(relPath,msg);
}


/**
 * Initiate scan on all directories.
 * 
 * @param bizFactory
 * @param test if <em>true</em> do not actually make any changes
 * in the application
 * @return
 * @throws MediaAlbumException
 */
public static int doScan(
	BizFactory bizFactory, 
	boolean test)
throws MediaAlbumException
{
	if ( bizFactory == null ) {
		LOG.error("Null config or factory passed to doScan()");
		return 0;
	}
	
	MediaScan scanner = new MediaScan(bizFactory,test);
	LOG.debug("Performing media scan on ALL source directories");
	int count = scanner.doScan();
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Media scan on ALL source directories updated " +count +" media items");
	}
	return count;
}

/**
 * Initiate scan on one directory.
 *  * @param bizFactory
 * @param collection * @param test * @return int * @throws MediaAlbumException */
public static int doScan(
	BizFactory bizFactory,
	Collection collection,
	boolean test)
throws MediaAlbumException
{
	if ( bizFactory == null  ) {
		LOG.error("Null config or factory passed to doScan()");
		return 0;
	}
	
	MediaScan scanner = new MediaScan(bizFactory,test);
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Performing media scan on source directory " +collection.getPath());
	}
	int count = scanner.doScan(collection);
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Media scan on ALL source directories updated " +count +" media items");
	}
	return count;
}

	/**
	 * File filter for supported media file types and modified date.
	 * 
	 * <p>Created Oct 7, 2002 9:32:47 AM.</p>
	 * 
	 * @author Matt Magoffin (spamsqr@msqr.us)
	 */
	private static class MediaFileFilter implements FileFilter, Cloneable
	{
		
		private String[] extensions = new String[0];
		private long newerThanDate = -1;
		
	public void addExtension(String ext) 
	{
		extensions = ArrayUtil.setItem(extensions,extensions.length,ext.toLowerCase());
		Arrays.sort(extensions);
	}
	
	public void addExtensions(String[] exts) 
	{
		for ( int i=0; i<exts.length; i++ ) {
			exts[i] = exts[i].toLowerCase();
		}
		extensions = (String[])ArrayUtil.merge(extensions,exts);
		Arrays.sort(extensions);
	}
	
	/**
	 * Returns <em>true</em> for any supported media extension.
	 * 	 * @param file
	 * @see java.io.FilenameFilter#accept(File, String)	 * @return
	 */
	public boolean accept(File file)
	{
		if ( file.lastModified() < newerThanDate ) return false;
		if ( extensions.length < 1 ) return true;
		
		String name = file.getName();
		int extIdx = name.lastIndexOf('.');
		String ext = name.substring(extIdx+1);
		if ( ext != null ) {
			ext = ext.toLowerCase();
		}
		if ( Arrays.binarySearch(extensions,ext) >= 0 ) {
			return true;
		}
		return false;
	}
	
	public Object clone()
	{
		MediaFileFilter filter = new MediaFileFilter();
		filter.extensions = this.extensions;
		filter.newerThanDate = this.newerThanDate;
		return filter;
	}
	
	public String toString()
	{
		return "MediaScanner.MediaFileFilter{newerThanDate="
			+newerThanDate +",extensions="
			+StringUtil.valueOf(extensions) +"}";
	}
	
	} // class MediaFilenameFilter

	/**
	 * File filter for directories.
	 * 
	 * <p>Created Oct 7, 2002 9:33:34 AM.</p>
	 * 
	 * @author Matt Magoffin (spamsqr@msqr.us)
	 */
	private static class MediaDirFilter implements FileFilter
	{
		
	/**
	 * Returns <em>true</em> for directories.
	 * 	 * @param file
	 * @see java.io.FileFilter#accept(File)	 * @return
	 */
	public boolean accept(File file)
	{
		
		if ( file.isDirectory() ) {
			return true;
		} else {
			return false;
		}
	}
	
	} // class MediaDirFilter


} // class MediaScanner
