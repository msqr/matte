/* ===================================================================
 * TestUserBizImpl.java
 * 
 * Created Dec 20, 2005 4:02:59 PM
 * 
 * Copyright (c) 2005 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: SystemBizImplTest.java,v 1.11 2006/12/17 07:37:40 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.biz.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import magoffin.matt.ma2.AbstractSpringEnabledTransactionalTest;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.dao.AlbumDao;
import magoffin.matt.ma2.dao.ThemeDao;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.Theme;
import magoffin.matt.ma2.domain.TimeZone;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.support.AddThemeCommand;
import magoffin.matt.util.TemporaryFile;

import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Test the {@link magoffin.matt.ma2.biz.impl.SystemBizImpl} class.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.11 $ $Date: 2006/12/17 07:37:40 $
 */
public class SystemBizImplTest extends AbstractSpringEnabledTransactionalTest {
	
	/** The SystemBiz. */
	protected SystemBizImpl testSystemBizImpl;
	
	/** Test UserBiz. */
	protected UserBiz testUserBiz;
	
	/** The domain object factory. */
	protected DomainObjectFactory domainObjectFactory;
	
	/** The ThemeDao. */
	protected ThemeDao themeDao;
	
	/** The AlbumDao. */
	protected AlbumDao albumDao;
	
	private final Logger log = Logger.getLogger(SystemBizImplTest.class);
	
	/**
	 * Test able to import a ThemePak.
	 * @throws Exception if an error occurs
	 */
	public void testAddTheme() throws Exception {
		Long themeId = addNewTheme();
		
		assertNotNull(themeId);
		Theme t = testSystemBizImpl.getThemeById(themeId);
		assertNotNull(t);
		
		// test files exist
		File testFile = new File(testSystemBizImpl.getExternalThemeDirectory()
				+t.getBasePath()+"/theme.xsl");
		assertTrue(testFile.exists());
	}

	private Long addNewTheme() {
		final Resource testThemePak = new ClassPathResource("test-theme.zip", 
				SystemBizImplTest.class);
		AddThemeCommand cmd = new AddThemeCommand();
		cmd.setTempFile(new TemporaryFile() {

			public String getContentType() {
				return "application/x-zip";
			}

			public InputStream getInputStream() throws IOException {
				return testThemePak.getInputStream();
			}

			public String getName() {
				return testThemePak.getFilename();
			}		
			
			public long getSize() {
				try {
					return testThemePak.getFile().length();
				} catch ( IOException e ) {
					throw new RuntimeException(e);
				}
			}
		});
		BizContext context = new TestBizContext(getContext(contextKey()),null);
		Long themeId = testSystemBizImpl.storeTheme(cmd, context);
		return themeId;
	}
	
	/**
	 * Test exporting a theme.
	 * @throws Exception if an error occurs
	 */
	public void testExportTheme() throws Exception {
		Long themeId = addNewTheme();
		
		// and now export this to a zip archive
		File tmpThemePakFile = File.createTempFile("ThemePak", ".zip");
		tmpThemePakFile.deleteOnExit();
		
		BizContext context = new TestBizContext(getContext(contextKey()),null);
		OutputStream out = new FileOutputStream(tmpThemePakFile);
		try {
			testSystemBizImpl.exportTheme(themeDao.get(themeId), out, null, context);
		} finally {
			out.close();
		}
		
		ZipFile zFile = new ZipFile(tmpThemePakFile);
		Enumeration<? extends ZipEntry> entries = zFile.entries();
		int count = 0;
		while ( entries.hasMoreElements() ) {
			ZipEntry entry = entries.nextElement();
			if ( log.isDebugEnabled() ) {
				log.debug("Got theme zip entry [" +entry.getName() +"]");
			}
			count++;
		}
		assertEquals(6, count);
	}
	
	/**
	 * Test able to delete an external theme.
	 * @throws Exception if an error occurs
	 */
	public void testDeleteExternalTheme() throws Exception {
		Long themeId = addNewTheme();
		Theme theme = testSystemBizImpl.getThemeById(themeId);
		assertNotNull(theme);

		File testFile = new File(testSystemBizImpl.getExternalThemeDirectory()
				+theme.getBasePath()+"/theme.xsl");
		assertTrue(testFile.exists());
		
		BizContext context = new TestBizContext(getContext(contextKey()),null);
		testSystemBizImpl.deleteTheme(theme, context);
		
		// test theme file now deleted
		assertFalse(testFile.exists());
	}
	
	/**
	 * Test able to delete an external theme that is currently being used
	 * by some albums.
	 * @throws Exception if an error occurs
	 */
	public void testDeleteExternalThemeInUse() throws Exception {
		Long themeId = addNewTheme();
		Theme theme = testSystemBizImpl.getThemeById(themeId);
		assertNotNull(theme);

		File testFile = new File(testSystemBizImpl.getExternalThemeDirectory()
				+theme.getBasePath()+"/theme.xsl");
		assertTrue(testFile.exists());
		
		// create an album and assign it this theme
		User testUser = getTestUser("testdeletetheme", "test@nowhere");
		Album newAlbum = domainObjectFactory.newAlbumInstance();
		newAlbum.setName("Test Album");
		newAlbum.setOwner(testUser);
		newAlbum.setTheme(theme);
		newAlbum.setCreationDate(Calendar.getInstance());
		newAlbum.setAllowAnonymous(false);
		newAlbum.setAllowOriginal(false);
		Long albumId = albumDao.store(newAlbum);
		
		BizContext context = new TestBizContext(getContext(contextKey()),null);
		testSystemBizImpl.deleteTheme(theme, context);
		
		// test theme file now deleted
		assertFalse(testFile.exists());
		
		// test that album is not assigned to this theme anymore
		Album savedAlbum = albumDao.get(albumId);
		assertFalse(savedAlbum.getTheme().getThemeId().equals(theme.getThemeId()));
	}
	
	/**
	 * Test that the time zones are available.
	 * @throws Exception if an error occurs
	 */
	public void testTimeZones() throws Exception {
		List<TimeZone> tzList = testSystemBizImpl.getAvailableTimeZones();
		
		assertNotNull("The TimeZone list should not be null", tzList);
		assertTrue("The TimeZone list should have more than 1 item in it", tzList.size() > 1 );
	}
	
	/**
	 * Test the default time zone.
	 * @throws Exception if an error occurs
	 */
	public void testDefaultTimeZone() throws Exception {
		TimeZone tz = testSystemBizImpl.getDefaultTimeZone();
		assertNotNull("The default time zone should not be null", tz);
	}
	
	/**
	 * Test the default theme.
	 * @throws Exception if an error occurs
	 */
	public void testDefaultTheme() throws Exception {
		Theme theme = testSystemBizImpl.getDefaultTheme();
		assertNotNull("The default theme should not be null", theme);
	}
	
	/**
	 * Test storing a new theme.
	 * @throws Exception if an error occurs
	 */
	public void testStoreTheme() throws Exception {
		User user = registerAndConfirmUser();
		BizContext context = new TestBizContext(getContext(contextKey()), user);
		
		Theme theme = domainObjectFactory.newThemeInstance();
		theme.setAuthor("Unit Test");
		theme.setBasePath("/test/new");
		theme.setDescription("This is a test theme.");
		theme.setName("Test");
		
		Long themeId = testSystemBizImpl.storeTheme(theme, context);
		
		Theme t = themeDao.get(themeId);
		assertNotNull(t);
		assertEquals(t.getAuthor(), theme.getAuthor());
		assertEquals(t.getName(), theme.getName());
		assertNotNull(t.getCreationDate());
		assertNotNull(t.getOwner());
		assertEquals(t.getOwner().getUserId(), user.getUserId());
	}
	
	/**
	 * Test able to generate shared album URL.
	 * @throws Exception if an error occurs
	 */
	public void testGetSharedAlbumUrl() throws Exception {
		Album album = domainObjectFactory.newAlbumInstance();
		album.setAnonymousKey("MY_ANONYMOUS_KEY");
		BizContext context = new TestBizContext(getContext(contextKey()),null);
		String url = testSystemBizImpl.getSharedAlbumUrl(album, context);
		assertNotNull(url);
		assertTrue(url.contains(album.getAnonymousKey()));
	}

	private User registerAndConfirmUser() throws Exception {
		return registerAndConfirmUser("test","nobody@loclhost");
	}

	private User registerAndConfirmUser(String login, String email) throws Exception {
		User newUser = getTestUser(login, email);
		BizContext context = new TestBizContext(getContext(contextKey()),null);		
		String confKey = testUserBiz.registerUser(newUser,context);
		User confirmedUser = testUserBiz.confirmRegisteredUser(newUser.getLogin(),
				confKey,context);
		return confirmedUser;
	}

	private User getTestUser(String login, String email) {
		User newUser = domainObjectFactory.newUserInstance();
		newUser.setEmail(email);
		newUser.setName("Test User");
		newUser.setPassword("test");
		newUser.setLogin(login);
		return newUser;
	}

}
