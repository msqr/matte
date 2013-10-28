/* ===================================================================
 * BaseAppTester.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Mar 30, 2004 12:07:42 PM.
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
 * $Id: BaseAppTester.java,v 1.1 2006/06/03 22:26:19 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import magoffin.matt.biz.Biz;
import magoffin.matt.biz.BizFactory;
import magoffin.matt.dao.DAOFactory;
import magoffin.matt.ma.MediaAlbumRuntimeException;
import magoffin.matt.ma.biz.MediaAlbumBizInitializer;
import magoffin.matt.ma.dao.MediaAlbumDAOInitializer;
import magoffin.matt.ma.util.MediaAlbumConfigUtil;
import magoffin.matt.ma.util.PoolFactory;
import magoffin.matt.ma.xsd.MediaAlbumConfig;

import org.apache.log4j.Logger;
import org.exolab.castor.xml.CastorException;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Abstract class to help writing application test classes.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:19 $
 */
public abstract class BaseAppTester {
	
	private static final Logger LOG = Logger.getLogger(BaseAppTester.class);
	
	protected boolean initialized = false;
	protected MediaAlbumConfig config = null;
	protected BizFactory bizFactory = null;

protected void init(MediaAlbumConfig config)
{
	this.config = config;

	PoolFactory pf = MediaAlbumConfigUtil.getPoolFactory(config);

	MediaAlbumDAOInitializer daoInit =  new MediaAlbumDAOInitializer(config);
	
	LOG.debug("Obtaining DataAccessObjectFactory instance");
	DAOFactory daoFactory = DAOFactory.getInstance(daoInit);
	
	LOG.debug("Obtaining BizIntfFactory instance");
	MediaAlbumBizInitializer bizInit = new MediaAlbumBizInitializer(config,
			daoFactory, pf);
	bizFactory = BizFactory.getInstance(bizInit);
	
	// set the BizInitializer on the DAO initializer
	daoInit.setBizInitialzer(bizInit);
	
	initialized = true;
}

protected void init(String configPath)
{
	try {
		MediaAlbumConfig appConfig = 
			(MediaAlbumConfig)Unmarshaller.unmarshal(
				MediaAlbumConfig.class, new BufferedReader(
					new FileReader( configPath ) ) );
		
		init(appConfig);
	
	} catch ( IOException e ) {
		LOG.error("IOException reading application config file "
				+configPath +": " +e.getMessage());
		throw new MediaAlbumRuntimeException("Unable to initialize",e);
	} catch ( CastorException e ) {
		LOG.error("Error parsing application config file "
			+configPath +": " +e.getMessage());
		throw new MediaAlbumRuntimeException("Unable to initialize",e);
	}
	
}

protected Biz getBiz(String name) {
	if ( bizFactory == null ) {
		throw new MediaAlbumRuntimeException("No BizFactory configured");
	}
	return bizFactory.getBizInstance(name);
}

	
}
