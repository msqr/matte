/* ===================================================================
 * AbstractBiz.java
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
 * $Id: AbstractBiz.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.biz.impl;

import java.util.ArrayList;
import java.util.HashMap;

import magoffin.matt.biz.Biz;
import magoffin.matt.biz.BizFactory;
import magoffin.matt.biz.BizInitializer;
import magoffin.matt.biz.BizRuntimeException;
import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumRuntimeException;
import magoffin.matt.ma.biz.MediaAlbumBizInitializer;
import magoffin.matt.ma.util.PoolFactory;
import magoffin.matt.util.cache.CacheFactory;
import magoffin.matt.util.cache.SimpleCache;
import magoffin.matt.util.config.Config;

import org.apache.commons.pool.ObjectPool;
import org.apache.log4j.Logger;

/**
 * Base Biz implementation for Media Album.
 * 
 * <p>This implementation only accepts 
 * {@link magoffin.matt.ma.biz.MediaAlbumBizInitializer} instances in the
 * {@link #init(BizInitializer)} method. When this method is called, the 
 * {@link #cacheFactory} instance will be configured from the settings
 * defined in the application configuration.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public abstract class AbstractBiz implements Biz {
	
	private static final Logger log = Logger.getLogger(AbstractBiz.class);
	
	private static final String MAILMERGE_ROOT_PATH = 
			Config.get(ApplicationConstants.CONFIG_ENV,
			ApplicationConstants.ENV_MAILMERGE_ROOT_PATH);
	
	protected MediaAlbumBizInitializer initializer = null;
	protected CacheFactory cacheFactory = null;
	protected BizFactory bizFactory = null;
	protected PoolFactory poolFactory = null;
	
	// some helper object pools for HashMap and ArrayList
	protected ObjectPool hashMapPool = null;
	protected ObjectPool arrayListPool = null;


/* (non-Javadoc)
 * @see magoffin.matt.biz.Biz#getInitializer()
 */
public BizInitializer getInitializer() {
	return initializer;
}

/* (non-Javadoc)
 * @see magoffin.matt.biz.Biz#init(magoffin.matt.biz.BizInitializer)
 */
public void init(BizInitializer initializer) {
	if ( initializer != null && !(initializer instanceof MediaAlbumBizInitializer) ) {
		throw new BizRuntimeException(
			"Only " +MediaAlbumBizInitializer.class +" supported.");
	}
	this.initializer = (MediaAlbumBizInitializer)initializer;
	this.cacheFactory = this.initializer.getCacheFactory();
	this.bizFactory = BizFactory.getInstance(this.initializer);
	this.poolFactory = this.initializer.getPoolFactory();
	this.hashMapPool = poolFactory.getPoolInstance(HashMap.class);
	this.arrayListPool = poolFactory.getPoolInstance(ArrayList.class);
}

/**
 * Return an object to an object pool, logging warning message if fails.
 * 
 * <p>If either <var>pool</var> or <var>o</var> are <em>null</em> then 
 * this method returns immediately.</p>
 * 
 * @param pool the object pool
 * @param o the object
 */
protected void returnObjectToPool(ObjectPool pool, Object o) {
	if ( pool == null || o == null ) return;
	try {
		pool.returnObject(o);
	} catch ( Exception e ) {
		log.warn("Unable to return object to pool, object: " +o 
				+"; pool: " +pool +": exception: " +e);
	}
}

/**
 * Borrow an object from an object pool, throwing runtime exception if fails.
 * 
 * @param pool the object pool
 * @return the borrowed object
 * @throws MediaAlbumRuntimeException if unable to borrow an object
 */
protected Object borrowObjectFromPool(ObjectPool pool) {
	try {
		return pool.borrowObject();
	} catch ( Exception e ) {
		throw new MediaAlbumRuntimeException(
				"Unable to borrow object from pool", e);
	}
}

/**
 * Get a cached object.
 * 
 * <p>This method will use the configured {@link CacheFactory} instance
 * (if available) to attempt to return a cached object.If either the 
 * CacheFactory is not configured or the CacheFactory does not have a 
 * cache configured for <var>cacheName</var> this method will return
 * <em>null</em>.</p>
 * 
 * @param cacheName the name of the cache configured with the CacheFactory
 * @param key the cached object's key in the cache
 * @return the cached object, or <em>null</em> if not available
 * @see #cacheObject(String, Object, Object)
 */
protected Object getCachedObject(String cacheName, Object key) {
	if ( cacheFactory == null ) {
		return null;
	}
	SimpleCache cache = cacheFactory.getCacheInstance(cacheName);
	if ( cache == null ) {
		return null;
	}
	return cache.get(key);
}

/**
 * Get a cached object if allowed.
 * 
 * <p>This method will use the configured {@link CacheFactory} instance
 * (if available) to attempt to return a cached object.If either the 
 * CacheFactory is not configured or the CacheFactory does not have a 
 * cache configured for <var>cacheName</var> this method will return
 * <em>null</em>.</p>
 * 
 * @param allowCached if <em>true</em> then call {@link #getCachedObject(String, Object)}
 * @param cacheName the name of the cache configured with the CacheFactory
 * @param key the cached object's key in the cache
 * @return the cached object, or <em>null</em> if not available
 * @see #getCachedObject(String, Object)
 */
protected Object getCachedObject(boolean allowCached, String cacheName, Object key) {
	if ( allowCached ) {
		return getCachedObject(cacheName,key);
	}
	return null;
}

/**
 * Delete an object from cache.
 * 
 * @param cacheName the name of the cache configured with the CacheFactory
 * @param key the cached object's key in the cache
 */
protected void removeObjectFromCache(String cacheName, Object key) {
	if ( cacheFactory == null ) {
		return;
	}
	SimpleCache cache = cacheFactory.getCacheInstance(cacheName);
	if ( cache == null ) {
		return;
	}
	cache.remove(key);
}


/**
 * Clear a cache.
 * 
 * @param cacheName the name of the cache configured with the CacheFactory
 * @return <em>true</em> if the cache was cleared
 */
protected boolean clearCache(String cacheName) {
	if ( cacheFactory == null ) {
		return false;
	}
	SimpleCache cache = cacheFactory.getCacheInstance(cacheName);
	if ( cache == null ) {
		return false;
	}
	cache.clear();
	return true;
}


/**
 * Cache an object.
 * 
 * <p>This method will use the configured {@link CacheFactory} instance
 * (if available) to cache an object. If either the CacheFactory is not
 * configured or the CacheFactory does not have a cache configured for
 * <var>cacheName</var> calling this method will have no effect.</p>
 * 
 * 
 * @param cacheName the name of the cache configured with the CacheFactory
 * @param key the key to store the object at in the cache 
 * @param o the object to cache
 * @see #getCachedObject(String, Object)
 */
protected void cacheObject(String cacheName, Object key, Object o) {
	if ( cacheFactory == null ) {
		return;
	}
	SimpleCache cache = cacheFactory.getCacheInstance(cacheName);
	if ( cache == null ) {
		return;
	}
	cache.put(key,o);
}

/**
 * Get a Biz implementation.
 * 
 * @param bizName the name of the biz to get
 * @return the Biz
 */
protected Biz getBiz(String bizName) {
	return bizFactory.getBizInstance(bizName);
}

/**
 * Get a mail template resource path.
 * 
 * @param resource the relative path
 * @return full resource path
 */
protected String getMailTemplateResourcePath(String resource) {
	return MAILMERGE_ROOT_PATH+resource;
}

/**
 * Get mail template resource paths.
 * 
 * @param resources the relative paths
 * @return full resource paths
 */
protected String[] getMailTemplateResourcePaths(String[] resources) {
	String[] result = new String[resources.length];
	for ( int i = 0; i < resources.length; i++ ) {
		result[i] = MAILMERGE_ROOT_PATH+resources[i];
	}
	return result;
}

/* (non-Javadoc)
 * @see magoffin.matt.biz.Biz#finish()
 */
public void finish() {
	// don't do anything
}

}
