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
 */

package magoffin.matt.ma2.biz.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import magoffin.matt.ma2.AbstractSpringEnabledTransactionalTest;
import magoffin.matt.ma2.AuthorizationException;
import magoffin.matt.ma2.TestConstants;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.biz.IOBiz;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.biz.WorkBiz.WorkInfo;
import magoffin.matt.ma2.dao.AlbumDao;
import magoffin.matt.ma2.dao.TimeZoneDao;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.support.AddMediaCommand;
import magoffin.matt.ma2.support.PreferencesCommand;
import magoffin.matt.util.TemporaryFile;

/**
 * Test the {@link magoffin.matt.ma2.biz.impl.UserBizImpl} class.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
@ContextConfiguration
public class UserBizImplTest extends AbstractSpringEnabledTransactionalTest {

	@javax.annotation.Resource
	private DomainObjectFactory domainObjectFactory;
	@javax.annotation.Resource
	private UserBizImpl testUserBizImpl;
	@javax.annotation.Resource
	private IOBiz testIOBiz;
	//	@javax.annotation.Resource private SystemBiz testSystemBiz;
	//	@javax.annotation.Resource private MediaBiz testMediaBiz;
	@javax.annotation.Resource
	private AlbumDao albumDao;
	//	@javax.annotation.Resource private CollectionDao collectionDao;
	@javax.annotation.Resource
	private TimeZoneDao timeZoneDao;

	private User testUser;

	@Before
	@Override
	public void onSetUpInTransaction() {
		super.onSetUpInTransaction();
		deleteFromTables(TestConstants.ALL_TABLES_FOR_CLEAR);
		testUser = null;
	}

	/**
	 * Test registering a new user.
	 * 
	 * @throws Exception
	 *         if an error occurs
	 */
	@Test
	public void testRegisterUser() throws Exception {
		User newUser = getTestUser();
		BizContext context = new TestBizContext(applicationContext, null);
		String confKey = testUserBizImpl.registerUser(newUser, context);

		assertNotNull("The returned confirmation key must not be null", confKey);
	}

	/**
	 * Test registering a new user and then confirming them.
	 * 
	 * @throws Exception
	 *         if an error occurs
	 */
	@Test
	public void testRegisterAndConfirmUser() throws Exception {
		User newUser = getTestUser();
		BizContext context = new TestBizContext(applicationContext, null);
		String confKey = testUserBizImpl.registerUser(newUser, context);

		assertNotNull("The returned confirmation key must not be null", confKey);

		User confirmedUser = testUserBizImpl.confirmRegisteredUser(newUser.getLogin(), confKey, context);

		assertNotNull("The confirmed user must not be null", confirmedUser);
		assertNotNull("The confirmed user must have an ID", confirmedUser.getUserId());
		assertNotNull("The confirmed user must have a TimeZone", confirmedUser.getTz());
		assertNotNull("The confirmed user must have a thumbnail spec",
				confirmedUser.getThumbnailSetting());
		assertNotNull("The confirmed user must have a view spec", confirmedUser.getViewSetting());

		List<Collection> collections = testUserBizImpl.getCollectionsForUser(confirmedUser, context);

		assertNotNull("The confirmed user must have a collection", collections);
		assertEquals("The confirmed user must have a collection", 1, collections.size());

		testUser = confirmedUser;
	}

	/**
	 * Test registering a new user, then confirming with a bad confirmation
	 * code.
	 * 
	 * @throws Exception
	 *         if an error occurs
	 */
	@Test
	public void testRegisterBadConfirmation() throws Exception {
		User newUser = getTestUser();
		BizContext context = new TestBizContext(applicationContext, null);
		@SuppressWarnings("unused")
		String confKey = testUserBizImpl.registerUser(newUser, context);

		try {
			@SuppressWarnings("unused")
			User confirmedUser = testUserBizImpl.confirmRegisteredUser(newUser.getLogin(),
					"this is not the confirmation code", context);
			fail("AuthorizationException should have been thrown");
		} catch ( AuthorizationException e ) {
			assertTrue(
					"AuthroizationException with reason ["
							+ AuthorizationException.Reason.REGISTRATION_NOT_CONFIRMED
							+ "] should have been thrown",
					AuthorizationException.Reason.REGISTRATION_NOT_CONFIRMED.equals(e.getReason()));
		}
	}

	/**
	 * Test registering a new user whose login is already taken throws proper
	 * exception.
	 * 
	 * @throws Exception
	 *         if an error occurs
	 */
	@Test
	public void testRegisterDuplicateUserLogin() throws Exception {
		User newUser = getTestUser();
		BizContext context = new TestBizContext(applicationContext, null);
		testUserBizImpl.registerUser(newUser, context);

		User newUser2 = getTestUser();
		newUser2.setEmail("somebody@localhost");
		try {
			testUserBizImpl.registerUser(newUser2, context);
			fail("AuthorizationException should have been thrown");
		} catch ( AuthorizationException e ) {
			assertTrue("AuthroizationException with reason ["
					+ AuthorizationException.Reason.DUPLICATE_LOGIN + "] should have been thrown",
					AuthorizationException.Reason.DUPLICATE_LOGIN.equals(e.getReason()));
		}
	}

	/**
	 * Test registering a new user whose email is already taken throws proper
	 * exception.
	 * 
	 * @throws Exception
	 *         if an error occurs
	 */
	@Test
	public void testRegisterDuplicateUserEmail() throws Exception {
		User newUser = getTestUser();
		BizContext context = new TestBizContext(applicationContext, null);
		testUserBizImpl.registerUser(newUser, context);

		User newUser2 = getTestUser();
		newUser2.setLogin("somebody");
		try {
			testUserBizImpl.registerUser(newUser2, context);
			fail("AuthorizationException should have been thrown");
		} catch ( AuthorizationException e ) {
			assertTrue("AuthroizationException with reason ["
					+ AuthorizationException.Reason.DUPLICATE_EMAIL + "] should have been thrown",
					AuthorizationException.Reason.DUPLICATE_EMAIL.equals(e.getReason()));
		}
	}

	/**
	 * Test get a user by ID.
	 * 
	 * @throws Exception
	 *         if an error occurs
	 */
	@Test
	public void testGetUser() throws Exception {
		User newUser = getTestUser();
		BizContext context = new TestBizContext(applicationContext, null);
		Long savedUserId = testUserBizImpl.storeUser(newUser, context);

		User foundUser = testUserBizImpl.getUserById(savedUserId, context);
		assertNotNull("The user should be found", foundUser);
		assertEquals(savedUserId, foundUser.getUserId());
	}

	/**
	 * Test saving a user.
	 * 
	 * @throws Exception
	 *         if an error occurs
	 */
	@Test
	public void testStoreUser() throws Exception {
		User newUser = getTestUser();
		BizContext context = new TestBizContext(applicationContext, null);
		Long savedUserId = testUserBizImpl.storeUser(newUser, context);
		assertNotNull("Saved user's ID should not be null", savedUserId);

		User savedUser = testUserBizImpl.getUserById(savedUserId, context);
		assertEquals(newUser.getLogin(), savedUser.getLogin());
		assertEquals(newUser.getEmail(), savedUser.getEmail());
		assertNotNull("Stored user's TimeZone should not be null", savedUser.getTz());
	}

	/**
	 * Test deleting a user.
	 * 
	 * @throws Exception
	 *         if an error occurs
	 */
	@Test
	public void testRemoveUser() throws Exception {
		User newUser = getTestUser();
		BizContext context = new TestBizContext(applicationContext, null);
		Long savedUserId = testUserBizImpl.storeUser(newUser, context);

		testUserBizImpl.removeUser(savedUserId, context);

		// verify can't find now
		User foundUser = testUserBizImpl.getUserById(savedUserId, context);
		assertNull("The deleted user should not be found", foundUser);
	}

	/**
	 * Test that new users are not created with ADMIN access level.
	 * 
	 * @throws Exception
	 *         if an error occurs
	 */
	@Test
	public void testNewUserAccessLevel() throws Exception {
		User newUser = getTestUser();
		BizContext context = new TestBizContext(applicationContext, null);
		Long savedUserId = testUserBizImpl.storeUser(newUser, context);
		User savedUser = testUserBizImpl.getUserById(savedUserId, context);

		assertTrue("New user should not have ADMIN access",
				!testUserBizImpl.hasAccessLevel(savedUser, UserBiz.ACCESS_ADMIN));
	}

	/**
	 * Test that new users are not created with ADMIN access level.
	 * 
	 * @throws Exception
	 *         if an error occurs
	 */
	@Test
	public void testAdminAccessLevel() throws Exception {
		User newUser = getTestUser();
		newUser.setAccessLevel(UserBiz.ACCESS_ADMIN);

		assertTrue("User should have ADMIN access",
				testUserBizImpl.hasAccessLevel(newUser, UserBiz.ACCESS_ADMIN));
	}

	/**
	 * Test that users are saved with the DO_NOT_CHANGE password, the password
	 * does not change.
	 * 
	 * @throws Exception
	 *         if an error occurs
	 */
	@Test
	public void testDoNotChangePassword() throws Exception {
		User newUser = getTestUser();
		BizContext context = new TestBizContext(applicationContext, null);
		Long savedUserId = testUserBizImpl.storeUser(newUser, context);
		User savedUser = testUserBizImpl.getUserById(savedUserId, context);
		String originalPassword = savedUser.getPassword();

		// now change the name, leave the password
		newUser = (User) domainObjectFactory.clone(savedUser);
		assertNotNull(newUser);

		assertNotSame(newUser.getName(), "new name");
		newUser.setName("new name");
		newUser.setPassword(UserBiz.DO_NOT_CHANGE_VALUE);
		Long resavedUserId = testUserBizImpl.storeUser(newUser, context);
		assertEquals(savedUserId, resavedUserId);

		savedUser = testUserBizImpl.getUserById(resavedUserId, context);
		assertEquals("The resaved user's password should not be changed", originalPassword,
				savedUser.getPassword());
		assertEquals("The resaved user's name should be updated", "new name", savedUser.getName());
	}

	/**
	 * Test logging a user on.
	 * 
	 * @throws Exception
	 *         if an error occurs
	 */
	@Test
	public void testLogonUser() throws Exception {
		User newUser = getTestUser();
		BizContext context = new TestBizContext(applicationContext, null);
		testUserBizImpl.storeUser(newUser, context);
		User savedUser = testUserBizImpl.logonUser(newUser.getLogin(), "test");
		assertNotNull(savedUser);
	}

	/**
	 * Test logging a user on where the login doesn't exist.
	 * 
	 * @throws Exception
	 *         if an error occurs
	 */
	@Test
	public void testLogonUserBadLogin() throws Exception {
		User newUser = getTestUser();
		BizContext context = new TestBizContext(applicationContext, null);
		testUserBizImpl.storeUser(newUser, context);
		try {
			testUserBizImpl.logonUser("i dont exist", "test");
			fail("An AuthorizationException should have been thrown.");
		} catch ( AuthorizationException e ) {
			if ( !AuthorizationException.Reason.UNKNOWN_LOGIN.equals(e.getReason()) ) {
				fail("An AuthorizationException with reason UNKNOWN_LOGIN should have been thrown.");
			}
		}
	}

	/**
	 * Test logging a user on where the login doesn't exist.
	 * 
	 * @throws Exception
	 *         if an error occurs
	 */
	@Test
	public void testLogonUserBadPassword() throws Exception {
		User newUser = getTestUser();
		BizContext context = new TestBizContext(applicationContext, null);
		testUserBizImpl.storeUser(newUser, context);
		try {
			testUserBizImpl.logonUser(newUser.getLogin(), "this is not my password");
			fail("An AuthorizationException should have been thrown.");
		} catch ( AuthorizationException e ) {
			if ( !AuthorizationException.Reason.BAD_PASSWORD.equals(e.getReason()) ) {
				fail("An AuthorizationException with reason BAD_PASSWORD should have been thrown.");
			}
		}
	}

	/**
	 * Test able to execute "forgot password" function.
	 * 
	 * @throws Exception
	 *         if an error occurs
	 */
	@Test
	public void testForgotPassword() throws Exception {
		User newUser = getTestUser();
		BizContext context = new TestBizContext(applicationContext, null);
		testUserBizImpl.storeUser(newUser, context);
		String confCode = testUserBizImpl.forgotPassword(newUser.getLogin(), context);
		assertNotNull(confCode);
	}

	/**
	 * Test able to execute "forgot password" function, and confirm.
	 * 
	 * @throws Exception
	 *         if an error occurs
	 */
	@Test
	public void testForgotPasswordAndConfirm() throws Exception {
		User newUser = getTestUser();
		BizContext context = new TestBizContext(applicationContext, null);
		newUser = testUserBizImpl.getUserById(testUserBizImpl.storeUser(newUser, context), context);
		String confCode = testUserBizImpl.forgotPassword(newUser.getLogin(), context);
		assertNotNull(confCode);

		User confirmedUser = testUserBizImpl.confirmForgotPassword(newUser.getLogin(), confCode,
				"this.is.a.new.password", context);
		assertNotNull(confirmedUser);
		assertNotNull(confirmedUser.getUserId());
		assertEquals(newUser.getUserId(), confirmedUser.getUserId());
	}

	/**
	 * Test gettting the collections for a user.
	 * 
	 * @throws Exception
	 *         if an error occurs
	 */
	@Test
	public void testGetCollectionsForUser() throws Exception {
		BizContext context = new TestBizContext(applicationContext, null);
		testRegisterAndConfirmUser();
		List<Collection> collections = testUserBizImpl.getCollectionsForUser(testUser, context);
		assertNotNull(collections);
		assertTrue("Should have at least 1 collection", collections.size() > 0);

		// verify each collection does not have any items (should not as just created user)
		for ( Collection c : collections ) {
			assertEquals(0, c.getItem().size());
		}

		// now add something to collection
		Collection theCollection = collections.get(0);
		importImage("magoffin/matt/ma2/image/bee-action.jpg", theCollection, context);

		collections = testUserBizImpl.getCollectionsForUser(testUser, context);
		assertNotNull(collections);
		assertTrue("Should have at least 1 collection", collections.size() > 0);

		// verify each collection does not have any items (even though did add an item just now)
		for ( Collection c : collections ) {
			assertEquals(0, c.getItem().size());
		}
	}

	/**
	 * Test creating a new collections for a user.
	 * 
	 * @throws Exception
	 *         if an error occurs
	 */
	@Test
	public void testNewCollectionForUser() throws Exception {
		BizContext context = new TestBizContext(applicationContext, null);
		testRegisterAndConfirmUser();

		Collection newCollection = domainObjectFactory.newCollectionInstance();
		newCollection.setName("This is a new collection");
		newCollection.setComment("This is a new collection's comments.");

		Collection c = testUserBizImpl.newCollectionForUser(newCollection, testUser, context);

		// verify collection does not have any items (should not as just created)
		assertEquals(0, c.getItem().size());
		assertEquals(newCollection.getName(), c.getName());
		assertEquals(newCollection.getComment(), c.getComment());

		assertNotNull(c.getCollectionId());
		assertNotNull(c.getPath());
		assertNotNull(c.getCreationDate());
		assertNotNull(c.getOwner());
		assertEquals(testUser.getUserId(), c.getOwner().getUserId());
	}

	/**
	 * Test gettting the albums for a user.
	 * 
	 * @throws Exception
	 *         if an error occurs
	 */
	@Test
	public void testGetAlbumsForUser() throws Exception {
		BizContext context = new TestBizContext(applicationContext, null);
		testRegisterAndConfirmUser();
		List<Collection> collections = testUserBizImpl.getCollectionsForUser(testUser, context);
		Collection theCollection = collections.get(0);
		importImage("magoffin/matt/ma2/image/bee-action.jpg", theCollection, context);

		List<Album> albums = testUserBizImpl.getAlbumsForUser(testUser, context);
		assertNotNull(albums);
		assertEquals(0, albums.size());

		// now create an album
		saveNewAlbum(testUser);
		albums = testUserBizImpl.getAlbumsForUser(testUser, context);
		assertEquals(1, albums.size());

		// now create another album
		saveNewAlbum(testUser);
		albums = testUserBizImpl.getAlbumsForUser(testUser, context);
		assertEquals(2, albums.size());

	}

	/**
	 * Test storing a watermark image.
	 * 
	 * @throws Exception
	 *         if any error occurs
	 */
	@Test
	public void testStoreWatermark() throws Exception {
		BizContext context = new TestBizContext(applicationContext, null);
		testRegisterAndConfirmUser();
		Resource testWatermarkResource = new ClassPathResource("test-watermark.png", getClass());
		final File testWatermarkFile = testWatermarkResource.getFile();
		TemporaryFile watermarkTempFile = new TemporaryFile() {

			public InputStream getInputStream() throws IOException {
				return new FileInputStream(testWatermarkFile);
			}

			public String getName() {
				return testWatermarkFile.getName();
			}

			public String getContentType() {
				return "image/png";
			}

			public long getSize() {
				return testWatermarkFile.length();
			}
		};
		PreferencesCommand cmd = new PreferencesCommand();
		cmd.setWatermarkFile(watermarkTempFile);
		cmd.setUserId(testUser.getUserId());
		testUserBizImpl.storeUserPreferences(cmd, context);

		Resource watermark = testUserBizImpl.getUserWatermark(testUser.getUserId());
		assertNotNull(watermark);
		assertTrue(watermark.exists());

		// now request to delete
		cmd.setDeleteWatermark(true);
		cmd.setWatermarkFile(null);
		testUserBizImpl.storeUserPreferences(cmd, context);

		Resource watermark2 = testUserBizImpl.getUserWatermark(testUser.getUserId());
		assertNull(watermark2);
		assertFalse(watermark.exists());
	}

	// save a new album
	private Album saveNewAlbum(User user) {
		Album album = domainObjectFactory.newAlbumInstance();
		album.setComment("This is a new album.");
		album.setCreationDate(Calendar.getInstance());
		album.setName("My Test Album");
		album.setOwner(user);

		Long albumId = albumDao.store(album);
		return albumDao.get(albumId);
	}

	private void importImage(String path, Collection c, BizContext context) throws Exception {
		AddMediaCommand addCmd = new AddMediaCommand();
		addCmd.setAutoAlbum(false);
		addCmd.setCollectionId(c.getCollectionId());
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

		WorkInfo info = testIOBiz.importMedia(addCmd, context);

		assertNotNull("Returned WorkInfo must not be null", info);

		// wait at most 10 minutes for job to complete
		info.get(600, TimeUnit.SECONDS);
		assertTrue(info.isDone());
		assertNull(info.getException());
	}

	private User getTestUser() {
		User newUser = domainObjectFactory.newUserInstance();
		newUser.setEmail("nobody@localhost");
		newUser.setName("Test User");
		newUser.setPassword("test");
		newUser.setLogin("nobody");
		newUser.setTz(timeZoneDao.get("UTC"));
		return newUser;
	}

}
