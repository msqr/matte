/* ===================================================================
 * MediaAlbumBizInitializer.java
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
 * $Id: MediaAlbumBizInitializer.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.biz;

import org.apache.commons.pool.ObjectPool;
import org.apache.log4j.Logger;

import magoffin.matt.biz.BizInitializer;
import magoffin.matt.dao.DAOFactory;
import magoffin.matt.mail.biz.MailBizInitializer;
import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.dao.MediaAlbumDAOInitializer;
import magoffin.matt.ma.util.PoolFactory;
import magoffin.matt.ma.xsd.MediaAlbumConfig;
import magoffin.matt.util.PoolUtil;
import magoffin.matt.util.cache.CacheFactory;
import magoffin.matt.util.cache.xsd.CacheFactoryConfig;
import magoffin.matt.util.config.Config;

/**
 * Initializer for Media Album Biz implementations.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public class MediaAlbumBizInitializer implements BizInitializer, MailBizInitializer {
	
	private static final Logger log = Logger.getLogger(MediaAlbumBizInitializer.class);
	
	private MediaAlbumConfig appConfig;
	private DAOFactory daoFactory;
	private CacheFactory cacheFactory;
	private MediaAlbumDAOInitializer daoInit;
	
	private ObjectPool pkPool;
	private ObjectPool critPool;
	private PoolFactory poolFactory;
	
public MediaAlbumBizInitializer(MediaAlbumConfig appConfig, DAOFactory daoFactory,
		PoolFactory poolFactory) 
{
	this.appConfig = appConfig;
	this.daoFactory = daoFactory;

	log.debug("Initializing DAO initializer");
	daoInit = new MediaAlbumDAOInitializer(appConfig);
	daoInit.setBizInitialzer(this);

	pkPool = PoolUtil.getObjectPool(daoInit.getPrimaryKeyPoolConfig());
	critPool = PoolUtil.getObjectPool(daoInit.getCriteriaPoolConfig());
	if ( log.isDebugEnabled() ) {
		log.debug("Primary key object pool initialized to " + pkPool);
		log.debug("Criteria object pool initialized to " + critPool);
	}

	CacheFactoryConfig cfc = appConfig.getCacheFactory();
	cacheFactory = new CacheFactory(cfc);
	if ( log.isDebugEnabled() ) {
		log.debug("CacheFactory initialized to " +cacheFactory);
	}
	
	this.poolFactory = poolFactory;
}

/**
 * @return Returns the MediaAlbumConfig.
 */
public MediaAlbumConfig getAppConfig() {
	return appConfig;
}

/**
 * @return Returns the MediaAlbumDAOInitializer.
 */
public MediaAlbumDAOInitializer getDAOInitializer() {
	return daoInit;
}

public ObjectPool getPrimaryKeyObjectPool() {
	return pkPool;
}

public ObjectPool getCriteriaObjectPool() {
	return critPool;
}

public DAOFactory getDAOFactory() {
	return daoFactory;
}

public CacheFactory getCacheFactory() {
	return cacheFactory;
}

public PoolFactory getPoolFactory() {
	return poolFactory;
}

/* (non-Javadoc)
 * @see magoffin.matt.mail.biz.MailBizInitializer#getDefaultFromAddress()
 */
public String getDefaultFromAddress() {
	return Config.get(
			ApplicationConstants.CONFIG_ENV,
			ApplicationConstants.ENV_MAIL_FROM);
}

/* (non-Javadoc)
 * @see magoffin.matt.mail.biz.MailBizInitializer#getJndiSessionPath()
 */
public String getJndiSessionPath() {
	return Config.get(
			ApplicationConstants.CONFIG_ENV,
			ApplicationConstants.ENV_MAIL_JNDI,
			true);
}

/* (non-Javadoc)
 * @see magoffin.matt.mail.biz.MailBizInitializer#getMailerName()
 */
public String getMailerName() {
	return Config.get(
			ApplicationConstants.CONFIG_ENV,
			ApplicationConstants.ENV_MAIL_MAILER,
			true);
}

/* (non-Javadoc)
 * @see magoffin.matt.mail.biz.MailBizInitializer#getSmtpServer()
 */
public String getSmtpServer() {
	return null; // not used
}

}
