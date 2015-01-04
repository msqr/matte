/* ===================================================================
 * AbstractSpringEnabledTransactionalTest.java
 * 
 * Created Jan 25, 2006 9:57:58 PM
 * 
 * Copyright (c) 2006 Matt Magoffin.
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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.IOBiz;
import magoffin.matt.ma2.biz.WorkBiz.WorkInfo;
import magoffin.matt.ma2.biz.impl.TestBizContext;
import magoffin.matt.ma2.dao.CollectionDao;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.Metadata;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.support.AddMediaCommand;
import magoffin.matt.util.TemporaryFile;

import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.oracle.jrockit.jfr.ValueDefinition;

/**
 * Extension of Spring's AbstractTransactionalDataSourceSpringContextTests 
 * with helper methods for the Media Album project.
 * 
 * @author matt.magoffin
 * @version $Revision$ $Date$
 */
@ContextConfiguration(locations={
		"classpath:magoffin/matt/ma2/TestContext.xml",
		"file:web/WEB-INF/applicationContext.xml",
		"file:web/WEB-INF/dataAccessContext.xml",
		"classpath:testContext.xml",
})
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
@TestExecutionListeners(DependencyInjectionTestExecutionListener.class)
public abstract class AbstractSpringEnabledTransactionalTest
extends AbstractTransactionalJUnit4SpringContextTests {
	
	/**
	 * Default constructor.
	 */
	public AbstractSpringEnabledTransactionalTest() {
		if ( logger.isInfoEnabled() ) {
			logger.info("\n*** Constructing test class [" +getClass().getName() +"] ***");
		}
	}
	
	/**
	 * Execute some pre-test tasks.
	 */
	@Before
	public void onSetUpInTransaction() {
		// just in case any themes owned by a user... update themes now
		int rowCount = this.simpleJdbcTemplate.update("UPDATE " +TestConstants.TABLE_THEMES
				+" SET owner_ = NULL");
		if (logger.isInfoEnabled()) {
			logger.info("Updated " + rowCount + " rows from table " 
					+TestConstants.TABLE_THEMES);
		}

		executeSqlScript("file:defs/sql/derby/create-system.sql", true);

		// and force application to be "configured" with unit test settings
		deleteFromTables(new String[] {TestConstants.TABLE_SETTINGS});
		this.simpleJdbcTemplate.update("INSERT INTO " +TestConstants.TABLE_SETTINGS
				+" (skey,svalue) VALUES ('app.setup.complete','true')");
	}

	/**
	 * Utility method for importing an image for testing.
	 * 
	 * @param path classpath to the test image
	 * @param collectionDao the CollectionDao
	 * @param ioBiz the IOBiz
	 * @param collection the Collection
	 * @param user the User
	 * @throws Exception if an error occurs
	 */
	@SuppressWarnings("unchecked")
	protected void importImage(String path, CollectionDao collectionDao, IOBiz ioBiz, 
			Collection collection, User user) throws Exception {
		AddMediaCommand addCmd = new AddMediaCommand();
		addCmd.setAutoAlbum(false);
		addCmd.setCollectionId(collection.getCollectionId());
		final Resource testJpegImage = new ClassPathResource(path);
		addCmd.setTempFile(new TemporaryFile() {
	
			public InputStream getInputStream() throws IOException {
				return testJpegImage.getInputStream();
			}
	
			public String getName() {
				return testJpegImage.getFilename();
			}
	
			public String getContentType() {
				return "image/jpeg";
			}
	
			public long getSize() {
				try {
					return testJpegImage.getFile().length();
				} catch ( IOException e ) {
					throw new RuntimeException(e);
				}
			}
			
		});
		
		BizContext context = new TestBizContext(applicationContext, user);
		WorkInfo info = ioBiz.importMedia(addCmd, context);
		
		assertNotNull("Returned WorkInfo must not be null", info);
		
		// wait at most 10 minutes for job to complete
		info.get(600,TimeUnit.SECONDS);	
		assertTrue(info.isDone());
		assertNull(info.getException());
		
		// now verify that Collection actually has item in it
		Collection c = collectionDao.get(collection.getCollectionId());
		assertNotNull(c);
		boolean found = false;
		String fileName = StringUtils.getFilename(path);
		for ( MediaItem item : (List<MediaItem>)c.getItem() ) {
			if ( fileName.equals(StringUtils.getFilename(item.getPath())) ) {
				found = true;
				break;
			}
		}
		assertTrue(found);
	}

	/**
	 * Spit out a bunch of debug log lines for information about a MediaItem.
	 * @param item the item
	 */
	@SuppressWarnings("unchecked")
	protected void debugLog(MediaItem item) {
		if ( logger.isDebugEnabled() ) {
			logger.debug("Got WxH: " +item.getWidth() +"x" +item.getHeight());
		}
		
		List<Metadata> metaList = item.getMetadata();
		for ( Metadata meta : metaList ) {
			logger.debug("Got metadata [" +meta.getKey() +"]: " +meta.getValue());
		}
	}
	
}
