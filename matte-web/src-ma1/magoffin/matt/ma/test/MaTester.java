/* ===================================================================
 * MaTester.java
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
 * $Id: MaTester.java,v 1.1 2006/06/03 22:26:19 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.test;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import magoffin.matt.biz.BizFactory;
import magoffin.matt.dao.DAOFactory;
import magoffin.matt.dao.DAOInitializer;
import magoffin.matt.db.xsd.DataBaseConnect;
import magoffin.matt.db.xsd.DataBaseConnectJdbc;
import magoffin.matt.db.xsd.DataBaseConnectJndi;
import magoffin.matt.gerdal.dao.BaseRdbDAOXMLInitializer;
import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.biz.AlbumBiz;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.CollectionBiz;
import magoffin.matt.ma.biz.MediaAlbumBizInitializer;
import magoffin.matt.ma.biz.UserBiz;
import magoffin.matt.ma.util.MediaAlbumConfigUtil;
import magoffin.matt.ma.util.PoolFactory;
import magoffin.matt.ma.xsd.Album;
import magoffin.matt.ma.xsd.Collection;
import magoffin.matt.ma.xsd.Group;
import magoffin.matt.ma.xsd.MediaAlbumConfig;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.ma.xsd.User;
import magoffin.matt.util.ArrayUtil;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.exolab.castor.xml.CastorException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;


public class MaTester 
{

	private static final String VERSION = "0.1";
	private static final Logger LOG = Logger.getLogger(MaTester.class);

	private PrintStream out = System.out;

	private MediaAlbumConfig config = null;
	private boolean createAlbum = false;
	
	private BizFactory bizFactory = null;
	
	private Marshaller marshaller = null;
	private Integer[] albumIds = new Integer[0];
	private Integer[] userIdsFroGroup = new Integer[0];

private void init(MediaAlbumConfig config) throws IOException
{
	this.config = config;

	PoolFactory pf = MediaAlbumConfigUtil.getPoolFactory(config);

	DAOInitializer daoInit = new BaseRdbDAOXMLInitializer(config.getDao());
	
	DAOFactory daoFactory = DAOFactory.getInstance(daoInit);
	
	MediaAlbumBizInitializer bizInit = new MediaAlbumBizInitializer(config,
			daoFactory, pf);
	
	bizFactory = BizFactory.getInstance(bizInit);
	
	Writer writer = new BufferedWriter(new OutputStreamWriter(System.out));
	marshaller = new Marshaller(writer);
}

private Connection getConnection() throws Exception
{
	DataBaseConnect[] dbConfigs = config.getDao().getDbConfig().getDbConnect();
	for ( int i = 0; i < dbConfigs.length; i++ )
	{
		try {
			if ( dbConfigs[i] instanceof DataBaseConnectJndi )
			{
				DataBaseConnectJndi jndiConfig = (DataBaseConnectJndi)dbConfigs[i];
				LOG.debug("Got JNDI config: " +jndiConfig.getJndiPath());
				
				Context ctx = new InitialContext();
				if( ctx == null ) throw new Exception("Boom - No Context");
		
				Context envCtx = (Context)ctx.lookup("java:comp/env");
				DataSource ds = (DataSource)envCtx.lookup(jndiConfig.getJndiPath());
	
				if ( ds != null ) {
					LOG.debug("Got DataSource object: "
						+ds.getClass().getName());
					return ds.getConnection();
	              
	        	}
			
			} else if ( dbConfigs[i] instanceof DataBaseConnectJdbc ) {

				DataBaseConnectJdbc jdbcConfig =
					(DataBaseConnectJdbc)dbConfigs[i];
				LOG.debug("Got JDBC config: "
					+jdbcConfig.getJdbcUrl());

				Class.forName(jdbcConfig.getDriverClass());
				return DriverManager.getConnection(
					jdbcConfig.getJdbcUrl(),
					jdbcConfig.getCredentials().getUsername(),
					jdbcConfig.getCredentials().getPassword() );
				
			
			}
		
		} catch ( Exception e ) {
			LOG.error("Error getting connection: " +e.getMessage());
		}
	}

	throw new Exception("Unable to get connection.");
}

private void testDataSource() throws Exception
{
	Connection conn = null;
	try{
		conn = getConnection();
        if(conn != null)  {
            LOG.debug("Got Connection "+conn.toString());
            Statement stmt = conn.createStatement();
            ResultSet rst = 
                stmt.executeQuery(
                  "select key,data_value from test");
            if(rst.next()) {
				out.println(rst.getInt(1) +" = " +rst.getString(2));
            }
            conn.close();
        }
        
    } catch ( Exception e ) {
		try {
			conn.close();
		} catch ( Exception e2 ) {
			// ignore
		}
		throw e;
    }
}

private void testListDirsForUser(Integer userId) throws Exception
{
	UserBiz userBiz = (UserBiz)bizFactory.getBizInstance(BizConstants.USER_BIZ);
	
	Collection[] dirs = userBiz.getCollectionsForUser(userId);
	for ( int i = 0; i < dirs.length; i++ ) {
		testMediaList(dirs[i].getCollectionId());
	}
}

private void testCreateAlbum() throws Exception
{
	// test creating album
	AlbumBiz albumBiz = (AlbumBiz)bizFactory.getBizInstance(BizConstants.ALBUM_BIZ);
	User testUser = getTestUser();
	
	Album newAlbum = new Album();
	newAlbum.setName("My New Album");
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Creating new Album: " +newAlbum.getName() );
	}
	albumBiz.createAlbum(newAlbum,testUser);
	
	// now test updating the item
	newAlbum.setName("This Name Is New");
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Updating new Album to name: " +newAlbum.getName() );
	}
	albumBiz.updateAlbum(newAlbum, testUser);
}

private static final Integer TEST_USER_ID = new Integer(0);

private User getTestUser() throws Exception
{
	UserBiz userBiz = (UserBiz)bizFactory.getBizInstance(BizConstants.USER_BIZ);
	return userBiz.getUserById(TEST_USER_ID, ApplicationConstants.CACHED_OBJECT_ALLOWED);
}


private void testMediaList(Integer dirId ) 
throws Exception
{
	CollectionBiz collectionBiz = (CollectionBiz)bizFactory.getBizInstance(BizConstants.COLLECTION_BIZ);
	MediaItem[] results = collectionBiz.getMediaItemsForCollection(dirId,
			ApplicationConstants.CACHED_OBJECT_ALLOWED, null);
	if ( results.length < 1 ) {
		out.println("No media found for dirId " +dirId);
	} else {
		out.println("\nMEDIA ITEMS FOR DIR ID " +dirId +" ==========");
		for ( int i = 0; i < results.length; i++ ) {
			marshaller.marshal(results[i]);
			out.println("");
		}
	}
}


private void testGroupsForUser(Integer userId) throws Exception
{
	UserBiz userBiz = (UserBiz)bizFactory.getBizInstance(BizConstants.USER_BIZ);
	
	Group[] results = userBiz.getGroupsForUserId(userId);
	if ( results.length < 1 ) {
		out.println("\nNo groups found for user ID " +userId);
	} else {
		out.println("\nGROUPS FOR USER ID " +userId +" ==========");
		for ( int i = 0; i < results.length; i++ ) {
			out.println(results[i]);
		}
	}
}


private void testMediaItemsForAlbum(Integer albumId) throws Exception
{
	AlbumBiz albumBiz = (AlbumBiz)bizFactory.getBizInstance(BizConstants.ALBUM_BIZ);
	MediaItem[] results = albumBiz.getMediaItemsForAlbum(albumId,
			ApplicationConstants.POPULATE_MODE_NONE, ApplicationConstants.CACHED_OBJECT_ALLOWED, null);
	if ( results.length < 1 ) {
		out.println("\nNo media found for album ID " +albumId);
	} else {
		out.println("\nMEDIA ITEMS FOR ALBUM ID " +albumId +" ==========");
		for ( int i = 0; i < results.length; i++ ) {
			out.println(results[i]);
		}
	}
}

private static final boolean processCommandLine(String[] args, MaTester tester)
{
	LongOpt[] opts = new LongOpt[8];
	opts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
	opts[1] = new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'r');
	opts[2] = new LongOpt("verbose", LongOpt.NO_ARGUMENT, null, 'v');
	opts[3] = new LongOpt("appconfig", LongOpt.REQUIRED_ARGUMENT, null, 'a');
	opts[4] = new LongOpt("create-album", LongOpt.NO_ARGUMENT, null, 'c');
	opts[5] = new LongOpt("album", LongOpt.REQUIRED_ARGUMENT, null, 'b');
	opts[6] = new LongOpt("logconfig", LongOpt.REQUIRED_ARGUMENT, null, 'l');
	opts[7] = new LongOpt("group", LongOpt.REQUIRED_ARGUMENT, null, 'g');
	
	Getopt g = new Getopt("MaTester",args,"-a:b:cg:hl:rv",opts);
	g.setOpterr(false);
	int c = -1;
	String appConfigFilePath = null;
	String logConfigFilePath = null;

	boolean ok = true;
	
	while ( (c=g.getopt()) != -1 )
	{
		switch (c)
		{
			case 'a':
				appConfigFilePath = g.getOptarg();
				break;
			case 'b':
				try {
					Integer id = Integer.valueOf(g.getOptarg());
					if ( id != null ) {
						tester.albumIds = ArrayUtil.setItem(
							tester.albumIds,tester.albumIds.length,id);
					}
				} catch ( Exception e ) {
					System.err.println("Can't use album ID: " +g.getOptarg());
				}
				break;
			case 'c':
				tester.createAlbum = true;
				break;
			case 'g':
				try {
					Integer id = Integer.valueOf(g.getOptarg());
					if ( id != null ) {
						tester.userIdsFroGroup = ArrayUtil.setItem(
							tester.userIdsFroGroup,tester.userIdsFroGroup.length,id);
					}
				} catch ( Exception e ) {
					System.err.println("Can't use user ID for group: " +g.getOptarg());
				}
				break;
			case 'h':
				printUsage();
				ok = false;
				break;
			case 'l':
				logConfigFilePath = g.getOptarg();
				break;
			case 'r':
				printVersion();
				ok = false;
				break;
			case 'v':
				//tester.verbose = true;
				break;
			case ':':
				System.err.println("The option '" +g.getOptopt() 
					+"' requires an argument.");
				ok = false;
				break;
			case '?':
				System.err.println("Unknown option: " +g.getOptopt());
				ok = false;
				break;
		}
	}

	if ( appConfigFilePath == null ) {
		System.err.println("Must provide application configuration XML: --appconfig");
		return false;
	}

	if ( !ok ) {
		return false;
	}
	
	if ( logConfigFilePath != null ) {
		DOMConfigurator.configureAndWatch( logConfigFilePath );
	} else {
		BasicConfigurator.configure();
	}

	try {
		MediaAlbumConfig appConfig = 
			(MediaAlbumConfig)Unmarshaller.unmarshal(
				MediaAlbumConfig.class, new BufferedReader(
					new FileReader( appConfigFilePath ) ) );
		
		tester.init(appConfig);
		
		if ( LOG.isDebugEnabled() ) {
			StringWriter strWriter = new StringWriter();
			appConfig.marshal(strWriter);
			LOG.debug(strWriter.toString());
		}

	} catch ( IOException e ) {
		LOG.error("Can't read application config file "
			+appConfigFilePath +": " +e.getMessage());
		return false;
	} catch ( CastorException e ) {
		LOG.error("Error parsing application config file "
			+appConfigFilePath +": " +e.getMessage());
		return false;
	}
			
	LOG.debug("\nReturning " +ok);
	return ok;
}

public static void main(String[] args)
{
	MaTester tester = new MaTester();
	if ( !processCommandLine(args,tester) ) {
		System.exit(0);
	}
	try {
		tester.testDataSource();
		tester.out.println("TESTING DAO ==========");
		tester.testListDirsForUser(new Integer(0));
		for ( int i = 0; i < tester.albumIds.length; i++ ) {
			tester.testMediaItemsForAlbum(tester.albumIds[i]);
		}
		for ( int i = 0; i < tester.userIdsFroGroup.length; i++ ) {
			tester.testGroupsForUser(tester.userIdsFroGroup[i]);
		}
		if ( tester.createAlbum ) {
			tester.testCreateAlbum();
		}
	} catch (Exception e) {
		e.printStackTrace();
	}
}

private static final void printVersion()
{
	System.out.println("MaTester version " +VERSION);
}

private static final void printUsage()
{
	System.out.println("MaTester [options]");
}

} // class MaTester

