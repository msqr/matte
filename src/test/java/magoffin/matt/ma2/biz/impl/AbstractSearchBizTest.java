/* ===================================================================
 * AbstractSearchBizTest.java
 * 
 * Created Jul 7, 2007 1:10:51 PM
 * 
 * Copyright (c) 2007 Matt Magoffin (spamsqr@msqr.us)
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

package magoffin.matt.ma2.biz.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import magoffin.matt.ma2.AbstractSpringEnabledTransactionalTest;
import magoffin.matt.ma2.TestConstants;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.biz.IOBiz;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.biz.SearchBiz;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.biz.WorkBiz.WorkInfo;
import magoffin.matt.ma2.dao.AlbumDao;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.AlbumSearchResult;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.PosterSearchResult;
import magoffin.matt.ma2.domain.SearchResults;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.support.AddMediaCommand;
import magoffin.matt.ma2.support.BasicAlbumSearchCriteria;
import magoffin.matt.util.TemporaryFile;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;

/**
 * Test case for the {@link AbstractSearchBiz} class.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
@ContextConfiguration
public class AbstractSearchBizTest extends
		AbstractSpringEnabledTransactionalTest {

	@javax.annotation.Resource private SearchBiz testAbstractSearchBiz;
	@javax.annotation.Resource private DomainObjectFactory domainObjectFactory;
	@javax.annotation.Resource private MediaBiz testMediaBiz;
	@javax.annotation.Resource private UserBiz testUserBiz;
	@javax.annotation.Resource private IOBiz testIOBiz;
	@javax.annotation.Resource private AlbumDao albumDao;
	
	private int counter = 0;
	
	/**
	 * Clean up DB before test.
	 */
	@Before
	@Override
	public void onSetUpInTransaction() {
		super.onSetUpInTransaction();
		deleteFromTables(TestConstants.ALL_TABLES_FOR_CLEAR);
	}

	/**
	 * Test able to perform album search.
	 * 
	 * @throws Exception if any error occurs
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testSearchForAlbumById() throws Exception {
		User user = registerAndConfirmUser();
		BizContext context = new TestBizContext(applicationContext, null);
		List<Collection> collections = testUserBiz.getCollectionsForUser(user,context);
		Album album = saveNewAlbum(user);

		importImage("magoffin/matt/ma2/image/bee-action.jpg",collections.get(0),context);
		List<MediaItem> items = testMediaBiz.getMediaItemsForCollection(
				collections.get(0), context);
		MediaItem item = items.get(0);
		testMediaBiz.addMediaItemsToAlbum(album,
				new Long[]{item.getItemId()},context);
		
		// execute search based on album ID
		BasicAlbumSearchCriteria criteria = new BasicAlbumSearchCriteria(
				album.getAlbumId());
		SearchResults sr = testAbstractSearchBiz.findAlbums(criteria, null, context);
		assertNotNull(sr);
		assertNotNull(sr.getSearchTime());
		assertEquals(new Long(1), sr.getTotalResults());
		assertNotNull(sr.getAlbum());
		List<AlbumSearchResult> albumResults = sr.getAlbum();
		assertEquals(1, albumResults.size());
		AlbumSearchResult albumResult = albumResults.get(0);
		verifyAlbumResultAndPoster(album, item, albumResult, 1);
	}

	private void verifyAlbumResultAndPoster(Album album, MediaItem item, 
			AlbumSearchResult albumResult, int expectedTotalItems) {
		assertEquals(album.getAlbumId(), albumResult.getAlbumId());
		assertEquals(album.getName(), albumResult.getName());
		assertEquals(new Long(expectedTotalItems), albumResult.getItemCount());
		
		Calendar itemDate = item.getItemDate();
		if ( itemDate == null ) {
			itemDate = item.getCreationDate();
		}
		assertEquals(itemDate, albumResult.getItemMaxDate());
		assertEquals(itemDate, albumResult.getItemMinDate());
		
		assertNotNull(albumResult.getSearchPoster());
		PosterSearchResult psr = albumResult.getSearchPoster();
		assertEquals(psr.getItemId(), item.getItemId());
		assertEquals(psr.getName(), item.getName());
	}
	
	/**
	 * Search for an album with nested albums.
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void testSearchForAlbumByIdWithChildren() throws Exception {
		User user = registerAndConfirmUser();
		BizContext context = new TestBizContext(applicationContext ,null);
		List<Collection> collections = testUserBiz.getCollectionsForUser(user,context);
		Album album = saveNewAlbum(user);
		Album album2 = saveNewAlbum(user);
		Album album3 = saveNewAlbum(user);
		album.getAlbum().add(album2);
		album.getAlbum().add(album3);
		albumDao.store(album);

		importImage("magoffin/matt/ma2/image/bee-action.jpg",collections.get(0),context);
		List<MediaItem> items = testMediaBiz.getMediaItemsForCollection(
				collections.get(0), context);
		MediaItem item = items.get(0);
		testMediaBiz.addMediaItemsToAlbum(album,
				new Long[]{item.getItemId()},context);
		testMediaBiz.addMediaItemsToAlbum(album2,
				new Long[]{item.getItemId()},context);
		testMediaBiz.addMediaItemsToAlbum(album3,
				new Long[]{item.getItemId()},context);
		
		// execute search based on album ID
		BasicAlbumSearchCriteria criteria = new BasicAlbumSearchCriteria(
				album.getAlbumId());
		SearchResults sr = testAbstractSearchBiz.findAlbums(criteria, null, context);
		assertNotNull(sr);
		assertNotNull(sr.getSearchTime());
		assertEquals(new Long(1), sr.getTotalResults());
		assertNotNull(sr.getAlbum());
		List<AlbumSearchResult> albumResults = sr.getAlbum();
		assertEquals(1, albumResults.size());
		AlbumSearchResult albumResult = albumResults.get(0);
		verifyAlbumResultAndPoster(album, item, albumResult, 1);
		
		// check children
		assertNotNull(albumResult.getSearchAlbum());
		assertEquals(2, albumResult.getSearchAlbum().size());
		List<AlbumSearchResult> children = albumResult.getSearchAlbum();
		verifyAlbumResultAndPoster(album2, item, children.get(0), 1);
		verifyAlbumResultAndPoster(album3, item, children.get(1), 1);
	}
	
	// save a new album
	private Album saveNewAlbum(User user) {
		int count = ++counter;
		Album album = domainObjectFactory.newAlbumInstance();
		album.setComment("This is a new album.");
		album.setCreationDate(Calendar.getInstance());
		album.setName("My Test Album " +count);
		album.setOwner(user);
		
		Long albumId = albumDao.store(album);
		return albumDao.get(albumId);
	}
	
	private User registerAndConfirmUser() throws Exception {
		return registerAndConfirmUser("test","nobody@loclhost");
	}

	private User registerAndConfirmUser(String login, String email) throws Exception {
		User newUser = getTestUser(login, email);
		BizContext context = new TestBizContext(applicationContext, null);		
		String confKey = testUserBiz.registerUser(newUser,context);
		User confirmedUser = testUserBiz.confirmRegisteredUser(newUser.getLogin(),
				confKey,context);
		return confirmedUser;
	}
	
	private void importImage(String path, Collection c, BizContext context, 
			AddMediaCommand addCmd) throws Exception {
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
		info.get(600,TimeUnit.SECONDS);	
		assertTrue(info.isDone());
		assertNull(info.getException());
	}

	private void importImage(String path, Collection c, BizContext context) 
	throws Exception {
		AddMediaCommand addCmd = new AddMediaCommand();
		importImage(path, c, context, addCmd);
	}

	private User getTestUser(String login, String email) {
		User newUser = domainObjectFactory.newUserInstance();
		newUser.setEmail(email);
		newUser.setName("Test User");
		newUser.setPassword("test");
		newUser.setLogin(login);
		magoffin.matt.ma2.domain.TimeZone tz = domainObjectFactory.newTimeZoneInstance();
		TimeZone defaultTz = TimeZone.getDefault();
		tz.setCode(defaultTz.getID());
		tz.setName(defaultTz.getDisplayName());
		tz.setOffset(defaultTz.getRawOffset());
		newUser.setTz(tz);
		return newUser;
	}
	
}
