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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;
import org.springframework.util.StringUtils;

/**
 * Extension of Spring's AbstractTransactionalDataSourceSpringContextTests 
 * with helper methods for the Media Album project.
 * 
 * @author matt.magoffin
 * @version $Revision$ $Date$
 */
public abstract class AbstractSpringEnabledTransactionalTest extends
		AbstractTransactionalDataSourceSpringContextTests {

	/** The base Spring context. */
	private static ConfigurableApplicationContext baseContext = null;

	/**
	 * A cache of ApplicationContext objects created from individual URLs, used
	 * to keep from having to recreate ApplicationContext objects between test
	 * case invocations.
	 */
	private static final Map<URL, ConfigurableApplicationContext> APP_CONTEXT_CACHE =
		new HashMap<URL, ConfigurableApplicationContext>();

	/**
	 * Default constructor.
	 */
	public AbstractSpringEnabledTransactionalTest() {
		setPopulateProtectedVariables(true);
		if ( logger.isInfoEnabled() ) {
			logger.info("\n*** Constructing test class [" +getClass().getName() +"] ***");
		}
	}
	
	/**
	 * A shutdown hook.
	 */
	public static void shutdown() {
		for ( ConfigurableApplicationContext context : APP_CONTEXT_CACHE.values() ) {
			try {
				context.close();
			} catch ( Exception e ) {
				System.err.println("Error closing base context: " +e);
			}
		}
		try {
			baseContext.close();
		} catch ( Exception e ) {
			System.err.println("Error closing base context: " +e);
		}
	}
	
	@Override
	protected void onSetUpInTransaction() throws Exception {
		super.onSetUpInTransaction();
		
		// just in case any themes owned by a user... update themes now
		int rowCount = this.jdbcTemplate.update("UPDATE " +TestConstants.TABLE_THEMES
				+" SET owner_ = NULL");
		if (logger.isInfoEnabled()) {
			logger.info("Updated " + rowCount + " rows from table " 
					+TestConstants.TABLE_THEMES);
		}
		
		// and force application to be "configured" with unit test settings
		deleteFromTables(new String[] {TestConstants.TABLE_SETTINGS});
		this.jdbcTemplate.update("INSERT INTO " +TestConstants.TABLE_SETTINGS
				+" (skey,svalue) VALUES ('app.setup.complete','true')");
	}

	@Override
	public boolean isPopulateProtectedVariables() {
		return true;
	}

	@Override
	protected final String[] getConfigLocations() {
		return TestConstants.DEFAULT_APP_CONTEXT_PATHS;
	}

	@Override
	protected Object contextKey() {
		return getClass();
	}

	@Override
	protected ConfigurableApplicationContext loadContext(Object key) {
		ConfigurableApplicationContext context = getBaseContext(getConfigLocations());

		List<Class<?>> classHierarchy = getClassHierarchy();
		for (int i = 0; i < classHierarchy.size(); i++) {
			Class<?> clazz = classHierarchy.get(i);
			context = getApplicationContext(clazz,
					getClassName(clazz) + "Context.xml", context);
		}
		
		return context;
	}

	private List<Class<?>> getClassHierarchy() {
		List<Class<?>> result = new ArrayList<Class<?>>();
		Class<?> superclass = getClass();
		do {
			result.add(superclass);
		} while ((superclass = superclass.getSuperclass()) != null
				&& superclass != AbstractSpringEnabledTransactionalTest.class);
		return result;
	}

	private String getClassName(Class<?> clazz) {
		String fullClassName = clazz.getName();
		String result = fullClassName
				.substring(fullClassName.lastIndexOf(".") + 1);
		return result;
	}

	private ConfigurableApplicationContext getApplicationContext(Class<?> clazz,
			String resourceName, ConfigurableApplicationContext parentContext) {
		logger.debug("Attempting to locate " + resourceName);
		URL url = clazz.getResource(resourceName);

		if (url == null) {
			return parentContext;
		}

		if (!APP_CONTEXT_CACHE.containsKey(url)) {
			logger.info("Loading " + url);
			APP_CONTEXT_CACHE.put(url,
					new ClassPathXmlApplicationContext(new String[] { url
							.toString() }, parentContext));
		}
		return APP_CONTEXT_CACHE.get(url);
	}

	@SuppressWarnings("unchecked")
	private synchronized ConfigurableApplicationContext getBaseContext(String[] configLocations) {
		if ( baseContext == null ) {
			baseContext = getContext(configLocations);
		}
		Map<String,JdbcTemplate> map = baseContext.getBeansOfType(
				JdbcTemplate.class,false,false);
		if ( map.size() > 0 ) {
			this.jdbcTemplate = map.values().iterator().next();
		}
		return baseContext;
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
		
		BizContext context = new TestBizContext(getContext(contextKey()), user);
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
