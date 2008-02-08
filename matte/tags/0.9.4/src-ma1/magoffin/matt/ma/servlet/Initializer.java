/* ===================================================================
 * Initializer.java
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
 * $Id: Initializer.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet;

import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import magoffin.matt.biz.BizFactory;
import magoffin.matt.dao.DAOFactory;
import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.MediaAlbumBizInitializer;
import magoffin.matt.ma.dao.MediaAlbumDAOInitializer;
import magoffin.matt.ma.util.MediaAlbumConfigUtil;
import magoffin.matt.ma.util.PoolFactory;
import magoffin.matt.ma.xsd.MediaAlbumConfig;
import magoffin.matt.util.config.Config;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Servlet to initialize application resources.
 * 
 * <p>Created on Sep 30, 2002 3:37:18 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class Initializer extends HttpServlet {
	
	private static final Logger LOG = Logger.getLogger(Initializer.class);
	
	private BizFactory bizFactory = null;

public void init(ServletConfig config)
throws ServletException
{
	super.init(config);
	
	String log4jConfigFilePath = Config.get(
		ApplicationConstants.CONFIG_ENV,ApplicationConstants.ENV_LOG4J_CONFIG);
		
	long logWatchDelay = Config.getLong(
		ApplicationConstants.CONFIG_ENV,ApplicationConstants.ENV_LOG4J_WATCH,-1);
	
	URL log4jConfig = getClass().getClassLoader().getResource(log4jConfigFilePath);
	
	if ( logWatchDelay < 1 ) {
		DOMConfigurator.configure(log4jConfig);
	} else {
		String log4jConfigFile = log4jConfig.getFile();
		
		DOMConfigurator.configureAndWatch( 
			log4jConfigFile, 
			logWatchDelay );
	}

	if ( LOG.isInfoEnabled() ) {
		LOG.info("Initialized logging with " 
			+log4jConfigFilePath +" and watch " +logWatchDelay);
	}
	
	String appConfigFilePath = Config.get(
		ApplicationConstants.CONFIG_ENV,ApplicationConstants.ENV_APP_CONFIG);

	if ( appConfigFilePath == null || appConfigFilePath.length() < 1 ) {
		LOG.fatal("Missing environment property " +ApplicationConstants.ENV_APP_CONFIG);
		throw new ServletException("Required environment property " 
			+ApplicationConstants.ENV_APP_CONFIG +" not provided.");
	}
	
	ServletContext sc = config.getServletContext();
	
	try {
		
		if ( LOG.isInfoEnabled() ) {
			LOG.info("Reading application configuration from " +appConfigFilePath);
		}
		
		Unmarshaller unmarshaller = new Unmarshaller(MediaAlbumConfig.class);
		//unmarshaller.setDebug(true);
		MediaAlbumConfig appConfig = 
			(MediaAlbumConfig)unmarshaller.unmarshal(new InputStreamReader(
					sc.getResourceAsStream(appConfigFilePath)));
					
		// get the media request pool factory
		PoolFactory pf = MediaAlbumConfigUtil.getPoolFactory(appConfig);
		sc.setAttribute(ServletConstants.APP_KEY_MEDIA_REQUEST_POOL_FACTORY,pf);
		if ( LOG.isInfoEnabled() ) {
			LOG.info("Storing PoolFactory " +pf +" at application key " 
				+ServletConstants.APP_KEY_MEDIA_REQUEST_POOL_FACTORY);
		}
		
		LOG.debug("Initializing DAO initializer");
		MediaAlbumDAOInitializer daoInit = 
			new MediaAlbumDAOInitializer(appConfig);
		
		LOG.debug("Obtaining DataAccessObjectFactory instance");
		DAOFactory daoFactory = DAOFactory.getInstance(daoInit);
		
		LOG.debug("Obtaining BizIntfFactory instance");
		MediaAlbumBizInitializer bizInit = new MediaAlbumBizInitializer(
				appConfig,daoFactory,pf);
		bizFactory = BizFactory.getInstance(bizInit);
		
		// set the BizInitializer on the DAO initializer
		daoInit.setBizInitialzer(bizInit);
		
		// get the cache factory
		if ( bizInit.getCacheFactory() != null ) {
			sc.setAttribute(ServletConstants.APP_KEY_CACHE_FACTORY,bizInit.getCacheFactory());
			if ( LOG.isInfoEnabled() ) {
				LOG.info("Storing CacheFactory " +bizInit.getCacheFactory() 
						+" at application key " 
						+ServletConstants.APP_KEY_CACHE_FACTORY);
			}
		}
		
		// stash application objects
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Storing BizIntfFactory at application key "
					+ServletConstants.APP_KEY_BIZ_INTF_FACTORY);
		}
		config.getServletContext().setAttribute(
				ServletConstants.APP_KEY_BIZ_INTF_FACTORY,bizFactory);
	
		// stash the config object
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Storing config at application key " 
					+ServletConstants.APP_KEY_CONFIG);
		}
		config.getServletContext().setAttribute(
				ServletConstants.APP_KEY_CONFIG,appConfig);
		
	} catch ( Exception e ) {
		LOG.fatal("Can't configure Album: "
			+appConfigFilePath +": " +e.getMessage(),e);
		throw new ServletException("Error configuring application, see error log for details");
	}
	
	// get www-base-file-path
	File f = MediaAlbumConfigUtil.getBaseWWWDir();
	if ( f == null ) {
		throw new ServletException("Error configuring application, see error log for details.");
	}
	
	if ( LOG.isInfoEnabled() ) {
		LOG.info("WWW base dir is " +f.getAbsolutePath());
	}
	config.getServletContext().setAttribute(ServletConstants.APP_KEY_WWW_BASE_FILE_PATH,f);
	
	// get app-base-file-path
	String appBaseFilePath = Config.get(ApplicationConstants.CONFIG_ENV,
		ApplicationConstants.ENV_BASE_FILE_PATH_APP);
	if ( appBaseFilePath == null ) {
		LOG.fatal("Required environment property '" +ApplicationConstants.ENV_BASE_FILE_PATH_APP 
			+"' not provided.");
		throw new ServletException("Error configuring application, see error log for details.");
	}
	f = new File(appBaseFilePath);
	if ( !f.exists() || !f.isDirectory() ) {
		LOG.fatal("App base dir '" +ApplicationConstants.ENV_BASE_FILE_PATH_APP 
			+"' defined, but path not accessible: " +appBaseFilePath);
			throw new ServletException("Error configuring application, see error log for details.");
	}
	if ( LOG.isInfoEnabled() ) {
		LOG.info("APP base dir is " +f.getAbsolutePath());
	}
	config.getServletContext().setAttribute(ServletConstants.APP_KEY_APP_BASE_FILE_PATH,f);
	
	// get collection-base-file-path
	String collectionBaseFilePath = Config.get(ApplicationConstants.CONFIG_ENV,
		ApplicationConstants.ENV_BASE_FILE_PATH_COLLECTION);
	if ( collectionBaseFilePath == null ) {
		LOG.fatal("Required environment property '" +ApplicationConstants.ENV_BASE_FILE_PATH_COLLECTION
			+"' not provided.");
		throw new ServletException("Error configuring application, see error log for details.");
	}
	f = new File(collectionBaseFilePath);
	if ( !f.exists() || !f.isDirectory() ) {
		LOG.fatal("Collection base dir " +collectionBaseFilePath +" not accessible.");
			throw new ServletException("Error configuring application, see error log for details.");
	}
	if ( LOG.isInfoEnabled() ) {
		LOG.info("Collection base dir is " +f.getAbsolutePath());
	}
	config.getServletContext().setAttribute(ServletConstants.APP_KEY_COLLECTION_BASE_FILE_PATH,f);

	// init search biz in case needs to reindex
	bizFactory.getBizInstance(BizConstants.SEARCH_BIZ);
}

/* (non-Javadoc)
 * @see javax.servlet.Servlet#destroy()
 */
public void destroy() {
	super.destroy();
	bizFactory.finish();
}

} 
