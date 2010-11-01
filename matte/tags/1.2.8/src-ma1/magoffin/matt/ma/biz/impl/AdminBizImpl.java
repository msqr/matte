/* ===================================================================
 * AdminBizImpl.java
 * 
 * Created Jan 8, 2004 5:30:19 PM
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
 * $Id: AdminBizImpl.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.biz.impl;

import java.util.ArrayList;
import java.util.List;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.biz.AdminBiz;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.CollectionBiz;
import magoffin.matt.ma.biz.WorkBiz;
import magoffin.matt.ma.scan.MediaScanWorkRequest;
import magoffin.matt.ma.search.RecreateIndexWorkRequest;
import magoffin.matt.ma.xsd.Collection;
import magoffin.matt.util.ConfigObjectPoolFactory;
import magoffin.matt.util.PoolUtil;
import magoffin.matt.util.cache.CacheUtil;
import magoffin.matt.util.cache.SimpleCache;
import magoffin.matt.util.cache.xsd.CacheStatus;
import magoffin.matt.xsd.ObjectPoolStatus;

import org.apache.commons.pool.ObjectPool;

/**
 * Biz implementation for AdminBiz.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class AdminBizImpl extends AbstractBiz implements AdminBiz {

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.AdminBiz#getCacheStatuses(java.lang.String)
 */
public CacheStatus[] getCacheStatuses(String populateBucketsKey)
throws MediaAlbumException 
{
	List l = new ArrayList(CACHE_KEYS.length);
	
	// get all cache statuses
	for (int i = 0; i < CACHE_KEYS.length; i++ ) {
		SimpleCache cache = cacheFactory.getCacheInstance(CACHE_KEYS[i]);
		CacheStatus status = CacheUtil.getCacheStatus(cache,CACHE_KEYS[i],
				(CACHE_KEYS[i].equals(populateBucketsKey)) );
		if ( status == null ) continue;
		l.add(status);
	}
	
	if ( l.size() < 1 ) {
		return null;
	}
	
	return (CacheStatus[])l.toArray(new CacheStatus[l.size()]);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.AdminBiz#getObjectPoolStatuses()
 */
public ObjectPoolStatus[] getObjectPoolStatuses()
throws MediaAlbumException 
{
	List l = new ArrayList(POOL_CLASSES.length);
	
	// hard-coded object pools
	ConfigObjectPoolFactory pf = ConfigObjectPoolFactory.getInstance(
			ApplicationConstants.CONFIG_ENV);
	for ( int i = 0; i < POOL_CLASSES.length; i++ ) {
		ObjectPool pool = pf.getObjectPool(POOL_CLASSES[i]);
		if ( pool != null ) {
			ObjectPoolStatus status = PoolUtil.getStatus(pool);
			status.setName(POOL_CLASSES[i].getName());
			l.add(status);
		}
	}
	
	// configured object pools
	Class[] clzz = poolFactory.getRegisteredObjectPools();
	for ( int i = 0; i < clzz.length; i++ ) {
		ObjectPool pool = this.poolFactory.getPoolInstance(clzz[i]);
		if ( pool != null ) {
			ObjectPoolStatus status = PoolUtil.getStatus(pool);
			status.setName(clzz[i].getName());
			l.add(status);
		}
	}
	
	if ( l.size() < 1 ) return null;
	
	return (ObjectPoolStatus[])l.toArray(new ObjectPoolStatus[l.size()]);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.AdminBiz#scan(java.lang.Integer[], boolean)
 */
public void scan(Integer[] collectionIds, boolean forceRescan)
throws MediaAlbumException 
{
	CollectionBiz collectionBiz = (CollectionBiz)getBiz(BizConstants.COLLECTION_BIZ);
	Collection[] collections = new Collection[collectionIds.length];
	for ( int i = 0; i < collectionIds.length; i++ ) {
		collections[i] = collectionBiz.getCollectionById(collectionIds[i],
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
	}

	MediaScanWorkRequest work = new MediaScanWorkRequest(collections,forceRescan,
			this.bizFactory);
	
	WorkBiz workBiz = (WorkBiz)getBiz(BizConstants.WORK_BIZ);
	workBiz.queue(work);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.AdminBiz#recreateSearchIndicies()
 */
public void recreateSearchIndicies() throws MediaAlbumException 
{
	RecreateIndexWorkRequest work = new RecreateIndexWorkRequest(bizFactory);
	WorkBiz workBiz = (WorkBiz)getBiz(BizConstants.WORK_BIZ);
	workBiz.queue(work);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.AdminBiz#resetCache(java.lang.String)
 */
public boolean resetCache(String cacheKey) throws MediaAlbumException {
	return clearCache(cacheKey);
}

}
