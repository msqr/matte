/* ===================================================================
 * AdminBiz.java
 * 
 * Created Jan 8, 2004 8:09:00 AM
 * 
 * Copyright (c) 2004 Matt Magoffin.
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
 * $Id: AdminBiz.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.biz;

import magoffin.matt.biz.Biz;
import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.xsd.AdminData;
import magoffin.matt.ma.xsd.BrowseData;
import magoffin.matt.ma.xsd.MediaAlbumData;
import magoffin.matt.ma.xsd.MediaAlbumSettings;
import magoffin.matt.util.cache.xsd.CacheStatus;
import magoffin.matt.xsd.ObjectPoolStatus;

/**
 * Biz interface for Media Album system maintenance.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public interface AdminBiz extends Biz {

	/** An array of all supported cache keys for {@link #getCacheStatuses(String)}. */
	public static final String[] CACHE_KEYS = new String[] {
			ApplicationConstants.CacheFactoryKeys.ALBUM,	
			ApplicationConstants.CacheFactoryKeys.ALBUM_PERMISSIONS,	
			ApplicationConstants.CacheFactoryKeys.COLLECTION,	
			ApplicationConstants.CacheFactoryKeys.COLLECTION_ITEMS,	
			ApplicationConstants.CacheFactoryKeys.ITEM,	
			ApplicationConstants.CacheFactoryKeys.ITEM_COMMENTS,	
			ApplicationConstants.CacheFactoryKeys.ITEM_FREE_DATA,	
			ApplicationConstants.CacheFactoryKeys.ITEM_HITS,	
			ApplicationConstants.CacheFactoryKeys.ITEM_RATINGS,	
			ApplicationConstants.CacheFactoryKeys.THEME,	
			ApplicationConstants.CacheFactoryKeys.THEME_FOR_USER,
			ApplicationConstants.CacheFactoryKeys.THEME_FOR_USER_GLOBALS,
			ApplicationConstants.CacheFactoryKeys.THEME_FOR_USER_OWNER,
			ApplicationConstants.CacheFactoryKeys.USER,
			ApplicationConstants.CacheFactoryKeys.USER_FREE_DATA,
	};

	/** An array of all supported object pool classes. */
	public static final Class[] POOL_CLASSES = new Class[] {
			AdminData.class,
			MediaAlbumData.class,	
			MediaAlbumSettings.class,	
			BrowseData.class	
	};

/**
 * Return array of cache status data.
 * 
 * @param populateBucketsKey if matches any supported cache key, then
 * bucket status data will be populated for that cache
 * @return array of cache statuses, or <em>null</em> if none available
 * @throws MediaAlbumException if an error occurs
 * @see #CACHE_KEYS
 */
public  CacheStatus[] getCacheStatuses(String populateBucketsKey)
throws MediaAlbumException;

/**
 * Return array of object poool status data.
 * 
 * @return array of ObjectPoolStatus objects
 * @throws MediaAlbumException if an error occurs
 */
public ObjectPoolStatus[] getObjectPoolStatuses() throws MediaAlbumException;

/**
 * Scan one or more collections.
 * 
 * @param collectionIds the collections to scan
 * @param forceRescan if <em>true</em> then clear the last-scanned date
 * from the collection and scan all items in the collections
 * @throws MediaAlbumException if an error occurs
 */
public void scan(Integer[] collectionIds, boolean forceRescan) 
throws MediaAlbumException;

/**
 * Recreate the application's search indicies.
 * @throws MediaAlbumException if an error occurs
 */
public void recreateSearchIndicies() throws MediaAlbumException;

/**
 * Clear out a cache.
 * 
 * @param cacheKey the cache key
 * @return <em>true</em> if the cache was reset
 * @throws MediaAlbumException if an error occurs
 */
public boolean resetCache(String cacheKey) throws MediaAlbumException;

}
