/* ===================================================================
 * MediaScanner.java
 * 
 * Copyright (c) 2002-2003 Matt Magoffin.
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
 * $Id: MediaScanner.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import magoffin.matt.biz.BizFactory;
import magoffin.matt.dao.DAOFactory;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.CollectionBiz;
import magoffin.matt.ma.biz.MediaAlbumBizInitializer;
import magoffin.matt.ma.dao.MediaAlbumDAOInitializer;
import magoffin.matt.ma.scan.MediaScan;
import magoffin.matt.ma.util.MediaAlbumConfigUtil;
import magoffin.matt.ma.util.PoolFactory;
import magoffin.matt.ma.xsd.Collection;
import magoffin.matt.ma.xsd.MediaAlbumConfig;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.exolab.castor.xml.CastorException;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Command-line application for running the Media Album Scanner.
 * 
 * <p>Created on Sep 27, 2002 5:25:10 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class MediaScanner 
{
	
	public static final String VERSION = "${revision}";
	public static final String DATE = "${date}";
	
	private static final Logger LOG = Logger.getLogger(MediaScanner.class);

	private boolean test = false;
	private Integer collectionId = null;
	
	private BizFactory bizFactory = null;

private static final boolean processCommandLine(String[] args, MediaScanner scanner)
{
	LongOpt[] opts = new LongOpt[7];
	opts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
	opts[1] = new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'r');
	opts[2] = new LongOpt("verbose", LongOpt.NO_ARGUMENT, null, 'v');
	opts[3] = new LongOpt("appconfig", LongOpt.REQUIRED_ARGUMENT, null, 'a');
	opts[4] = new LongOpt("test", LongOpt.NO_ARGUMENT, null, 't');
	opts[5] = new LongOpt("logconfig", LongOpt.REQUIRED_ARGUMENT, null, 'l');
	opts[6] = new LongOpt("collection", LongOpt.REQUIRED_ARGUMENT, null, 'g');

	Getopt g = new Getopt(MediaScanner.class.getName(),args,"-a:g:hl:rtv",opts);
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
			case 'g':
				scanner.collectionId = Integer.valueOf(g.getOptarg());
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
				//scanner.verbose = true;
				break;
			case 't':
				scanner.test = true;
				break;
			case ':':
				System.err.println("The option '" +g.getOptopt() 
					+"' requires an argument.");
				ok = false;
				break;
			case '?':
				System.err.println("Unknown option: " +g.getOptopt());
				break;
		}
	}

	if ( appConfigFilePath == null ) {
		System.err.println("Must provide application configuration XML: --appconfig");
		ok = false;
	}
	
	if ( !ok ) {
		return false;
	}
	
	if ( logConfigFilePath != null ) {
		DOMConfigurator.configure(logConfigFilePath);
	} else {
		BasicConfigurator.configure();
	}
	
	try {
		MediaAlbumConfig appConfig = 
			(MediaAlbumConfig)Unmarshaller.unmarshal(
				MediaAlbumConfig.class, new BufferedReader(
					new FileReader( appConfigFilePath ) ) );
		
		//appConfig.marshal(new BufferedWriter(new OutputStreamWriter(System.out)));

		scanner.init(appConfig);

	} catch ( IOException e ) {
		LOG.error("Can't read application config file "
			+appConfigFilePath +": " +e.getMessage());
		ok = false;
	} catch ( CastorException e ) {
		LOG.error("Error parsing application config file "
			+appConfigFilePath +": " +e.getMessage());
		ok = false;
	} catch ( Exception e ) {
		LOG.error("Exception: " +e);
		ok = false;
	}
			
	LOG.debug("\nReturning " +ok);
	return ok;
}


/**
 * Initialize the Media Album Scanner.
 * 
 * @param config
 * @throws Exception
 */
private void init(MediaAlbumConfig config) throws Exception
{
	LOG.debug("Initializing DAO initializer");
	PoolFactory pf = MediaAlbumConfigUtil.getPoolFactory(config);

	MediaAlbumDAOInitializer daoInit = 
		new MediaAlbumDAOInitializer(config);
	
	LOG.debug("Obtaining DataAccessObjectFactory instance");
	DAOFactory daoFactory = DAOFactory.getInstance(daoInit);
	
	LOG.debug("Obtaining BizIntfFactory instance");
	MediaAlbumBizInitializer bizInit = new MediaAlbumBizInitializer(config,
			daoFactory, pf);
	bizFactory = BizFactory.getInstance(bizInit);
	
	// set the BizInitializer on the DAO initializer
	daoInit.setBizInitialzer(bizInit);
}


/**
 * Method printVersion.
 */
private static void printVersion() 
{
	System.out.println(MediaScanner.class.getName() +" version " 
		+VERSION +" " +DATE);
}


/**
 * Method printUsage.
 */
private static void printUsage() 
{
	System.out.println(MediaScanner.class.getName() + " [options]" );
}

/**
 * Method go.
 */
private void go() 
{
	try {
		if ( collectionId != null ) {
			CollectionBiz collectionBiz = (CollectionBiz)bizFactory.getBizInstance(
				BizConstants.COLLECTION_BIZ);
			Collection collection = collectionBiz.getCollectionById(collectionId, ApplicationConstants.CACHED_OBJECT_ALLOWED);
			MediaScan.doScan(bizFactory,collection,this.test);
		} else {
			MediaScan.doScan(bizFactory,this.test);
		}
	} catch (MediaAlbumException e) {
		LOG.error("Exception performing scan: " +e);
	} catch ( Exception e ) {
		LOG.error("Exception performing scan: " +e);
	}
}


public static void main(String[] args) 
{
	MediaScanner scanner = new MediaScanner();
	if ( processCommandLine(args,scanner) ) {
		try {
			scanner.go();
		} catch ( Exception e ) {
			System.out.println(e);
		}
	}
}

} // class MediaScanner
