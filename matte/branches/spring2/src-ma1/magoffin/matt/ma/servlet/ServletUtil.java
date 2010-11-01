/* ===================================================================
 * ServletUtil.java
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
 * $Id: ServletUtil.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.biz.BizFactory;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.NotInitializedException;
import magoffin.matt.ma.util.PoolFactory;
import magoffin.matt.ma.xsd.AlbumTheme;
import magoffin.matt.ma.xsd.MediaAlbumConfig;
import magoffin.matt.util.cache.CacheFactory;
import magoffin.matt.util.cache.SimpleCache;

import org.apache.commons.pool.ObjectPool;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * Static helper methods for servlet functions.
 * 
 * <p> Created on Nov 12, 2002 6:03:35 PM.</p>
 *
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public final class ServletUtil 
{
	private static final Logger log = Logger.getLogger(ServletUtil.class);


/**
 * Get the configured MediaAlbumConfig.
 *  * @param ctx the current servlet context * @return MediaAlbumConfig * @throws MediaAlbumException */
public static MediaAlbumConfig getApplicationConfig(ServletContext ctx) 
throws MediaAlbumException
{
	try {
		Object o = ctx.getAttribute(ServletConstants.APP_KEY_CONFIG);
		if (!(o instanceof MediaAlbumConfig) ) {
			throw new NotInitializedException("MediaAlbumConfig not configured properly: " +o);
		}
		return (MediaAlbumConfig)o;
	} catch ( NotInitializedException e ) {
		throw e;
	} catch ( Exception e ) {
		log.error("Unable to get MediaAlbumConfig: " +e.getMessage());
		throw new MediaAlbumException("Unable to get MediaAlbumConfig.",e);
	}
}


/**
 * Get an ObjectPool instance from the application PoolFactory.
 * 
 * @param ctx the servlet context
 * @param forClass the class of the object to return a pool for
 * @return an object pool for <var>class</var> objects
 * @throws NotInitializedException if the CacheFactory is not available
 * @throws MediaAlbumException if an error occurs
 */
public static ObjectPool getMediaRequestObjectPool(ServletContext ctx, Class forClass)
throws MediaAlbumException, NotInitializedException
{
	try {
		Object o = ctx.getAttribute(ServletConstants.APP_KEY_MEDIA_REQUEST_POOL_FACTORY);
		if (!(o instanceof PoolFactory) ) {
			throw new NotInitializedException("Media request PoolFactory not configured properly: " +o);
		}
		return ((PoolFactory)o).getPoolInstance(forClass);
	} catch ( NotInitializedException e ) {
		throw e;
	} catch ( Exception e ) {
		log.error("Unable to get media request object pool: " +e.getMessage());
		throw new MediaAlbumException("Unable to get media request object pool.",e);
	}
}


/**
 * Get a SimpleCache instance from the application CacheFactory.
 * 
 * @param ctx the servlet context
 * @param cacheKey the key of the cache to get
 * @return SimpleCache
 * @throws NotInitializedException if the CacheFactory is not available
 * @throws MediaAlbumException if an error occurs
 */
public static SimpleCache getCacheFactoryCache(ServletContext ctx, Object cacheKey)
throws MediaAlbumException, NotInitializedException
{
	CacheFactory cf = getCacheFactory(ctx);
	return cf.getCacheInstance(cacheKey);
}

/**
 * Get the configured cache factory.
 * 
 * @param ctx the servlet context
 * @return CacheFactory
 * @throws NotInitializedException if the CacheFactory is not available
 * @throws MediaAlbumException if an error occurs
 */
public static CacheFactory getCacheFactory(ServletContext ctx)
throws MediaAlbumException, NotInitializedException
{
	try {
		Object o = ctx.getAttribute(ServletConstants.APP_KEY_CACHE_FACTORY);
		if (!(o instanceof CacheFactory) ) {
			throw new NotInitializedException("Media request CacheFactory not configured properly: " +o);
		}
		return (CacheFactory)o;
	} catch ( NotInitializedException e ) {
		throw e;
	} catch ( Exception e ) {
		log.error("Unable to get cache factory: " +e.getMessage());
		throw new MediaAlbumException("Unable to get cache factory",e);
	}
}

/**
 * Get the path to an album theme XSL.
 * @param theme
 * @return String
 */
public static String getAlbumThemePath(AlbumTheme theme)
{
	return ServletConstants.THEME_XSL_PATH_PREFIX+theme.getBaseDir()+theme.getXsl();
}

public static BizFactory getBizIntfFactory(ServletContext ctx) 
throws MediaAlbumException
{
	Object o = ctx.getAttribute(ServletConstants.APP_KEY_BIZ_INTF_FACTORY);
	if ( !(o instanceof BizFactory) ) {
		throw new NotInitializedException("BizIntfFactory not configured properly: " +o);
	}
	return (BizFactory)o;
}


/**
 * Save the current URL to session.
 * 
 * <p>The saved URL will contain any query string passed along with the request.
 * Note that a subsequent call to any action extending this action will clear the 
 * saved URL from the request after the 
 * {@link #go(ActionMapping, ActionForm, HttpServletRequest, HttpServletResponse, ActionResult)} method completes. If you want
 * to save the URL again, call this method again.</p>
 * 
 * @param request the current request
 */
public static final void saveRequestURL(HttpServletRequest request)
{
	StringBuffer buf = request.getRequestURL();
	String queryString = request.getQueryString();
	if ( queryString != null ) {
		buf.append('?');
		buf.append(queryString);
	}
	request.getSession().setAttribute(ServletConstants.SES_KEY_SAVED_URL,buf.toString());
	request.setAttribute(ServletConstants.REQ_ATTR_SAVED_URL,"t");
}


/**
 * Get the saved URL.
 * 
 * @param request the current request
 * @return the saved URL, or <em>null</em> if none saved
 */
public static final String getSavedRequestURL(HttpServletRequest request)
{
	return (String)request.getSession().getAttribute(
			ServletConstants.SES_KEY_SAVED_URL);
}

}
