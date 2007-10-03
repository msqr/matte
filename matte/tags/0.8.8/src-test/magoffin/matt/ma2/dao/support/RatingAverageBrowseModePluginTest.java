/* ===================================================================
 * RatingAverageBrowseModePlugin.java
 * 
 * Created Sep 24, 2007 11:25:39 AM
 * 
 * Copyright (c) 2007 Matt Magoffin.
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
 * $Id: RatingAverageBrowseModePluginTest.java,v 1.2 2007/09/29 07:55:34 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.dao.support;

import java.util.Calendar;
import java.util.List;

import magoffin.matt.ma2.AbstractSpringEnabledTransactionalTest;
import magoffin.matt.ma2.TestConstants;
import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.dao.AlbumDao;
import magoffin.matt.ma2.dao.CollectionDao;
import magoffin.matt.ma2.dao.MediaItemDao;
import magoffin.matt.ma2.dao.TimeZoneDao;
import magoffin.matt.ma2.dao.UserDao;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.AlbumSearchResult;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.MediaItemRating;
import magoffin.matt.ma2.domain.PaginationCriteria;
import magoffin.matt.ma2.domain.PaginationIndex;
import magoffin.matt.ma2.domain.PaginationIndexSection;
import magoffin.matt.ma2.domain.PosterSearchResult;
import magoffin.matt.ma2.domain.SearchResults;
import magoffin.matt.ma2.domain.Theme;
import magoffin.matt.ma2.domain.TimeZone;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.support.BrowseAlbumsCommand;

/**
 * Test case for the {@link RatingAverageBrowseModePlugin} class.
 *
 * @author matt
 * @version $Revision: 1.2 $ $Date: 2007/09/29 07:55:34 $
 */
public class RatingAverageBrowseModePluginTest extends
		AbstractSpringEnabledTransactionalTest {
	
	/** The album DAO to test. */
	protected AlbumDao albumDao;
	
	/** The DomainObjectFactory instance. */
	protected DomainObjectFactory domainObjectFactory;
	
	/** The plugin to test. */
	protected RatingAverageBrowseModePlugin testPlugin;
	
	/** The UserDao to test with. */
	protected UserDao userDao;

	/** The TimeZoneDao to test with. */
	protected TimeZoneDao timeZoneDao;
	
	/** The CollectionDao to test with. */
	protected CollectionDao collectionDao;
	
	/** The MediaItemDao to test with. */
	protected MediaItemDao mediaItemDao;
	
	private int counter = 1;

	@Override
	protected void onSetUpInTransaction() throws Exception {
		super.onSetUpInTransaction();
		deleteFromTables(TestConstants.ALL_TABLES_FOR_CLEAR);
	}
	
	/**
	 * Test finding for a user by average rating.
	 */
	@SuppressWarnings("unchecked")
	public void testSearchForAlbumsForUserByAverageRating() {
		Long savedUserId = saveNewUser();
		User user = userDao.get(savedUserId);
		User user2 = userDao.get(saveNewUser());
		User user3 = userDao.get(saveNewUser());
		Collection c = saveNewCollection(user);	
		
		Long savedAlbumId1 = saveNewAlbum(null, user);
		Album album1 = albumDao.get(savedAlbumId1);
		try {
			Thread.sleep(1000); // sleep so creation dates apart
		} catch ( InterruptedException e ) {
			throw new RuntimeException(e);
		}
		Long savedAlbumId2 = saveNewAlbum(null, user);
		Album album2 = albumDao.get(savedAlbumId2);
		
		// add some shared items into the albums
		MediaItem item1 = saveNewMediaItem(c); // 4.0
		addUserRating(item1, user, 5);
		addUserRating(item1, user2, 3);
		mediaItemDao.store(item1);
		album1.getItem().add(item1);
		albumDao.store(album1);
		
		MediaItem item2 = saveNewMediaItem(c); // 2.5
		addUserRating(item2, user, 3);
		addUserRating(item2, user2, 2);
		mediaItemDao.store(item2);
		album1.getItem().add(item2);
		albumDao.store(album1);
		
		MediaItem item3 = saveNewMediaItem(c); // 2
		addUserRating(item3, user, 2);
		mediaItemDao.store(item3);
		album1.getItem().add(item3);
		albumDao.store(album1);
		
		MediaItem item4 = saveNewMediaItem(c); // 2
		addUserRating(item4, user2, 2);
		mediaItemDao.store(item4);
		album2.getItem().add(item4);
		albumDao.store(album2);
		
		MediaItem item5 = saveNewMediaItem(c); // 1.5
		addUserRating(item5, user, 2);
		addUserRating(item5, user2, 1);
		addUserRating(item5, user3, 1);
		mediaItemDao.store(item5);
		album2.getItem().add(item5);
		albumDao.store(album2);
		
		MediaItem item6 = saveNewMediaItem(c); // no ratings
		album2.getItem().add(item6);
		albumDao.store(album2);
		
		// execute Hibernate search to flush to DB
		albumDao.findAlbumsForUser(savedUserId);
		
		// test getting all
		BrowseAlbumsCommand command = new BrowseAlbumsCommand();
		command.setUserKey(user.getAnonymousKey());
		command.setMode(RatingAverageBrowseModePlugin.MODE_RATING_AVERAGE);
		SearchResults sr = testPlugin.find(command, null);
		
		// verify index
		PaginationIndex index = sr.getIndex();
		assertNotNull(index);
		List<PaginationIndexSection> sections = index.getIndexSection();
		assertNotNull(sections);
		assertEquals(4, sections.size());
		
		verifyIndexSection(sections.get(0), "4.0", 1);
		verifyIndexSection(sections.get(1), "2.5", 1);
		verifyIndexSection(sections.get(2), "2.0", 2);
		verifyIndexSection(sections.get(3), "1.5", 1);
		
		// first section should be populated with the 1 item
		assertNotNull(sr.getAlbum());
		assertEquals(1, sr.getAlbum().size());
		List<AlbumSearchResult> albumResults = sr.getAlbum();
		AlbumSearchResult asr = albumResults.get(0);
		assertEquals(Long.valueOf(1), asr.getItemCount());
		assertNotNull(asr.getSearchPoster());
		PosterSearchResult poster = asr.getSearchPoster();
		assertEquals(item1.getItemId(), poster.getItemId());
		
		// now request "2.0" section
		PaginationCriteria pageCriteria = domainObjectFactory.newPaginationCriteriaInstance();
		pageCriteria.setIndexKey("2.0");
		sr = testPlugin.find(command, pageCriteria);
		
		// verify index
		index = sr.getIndex();
		assertNotNull(index);
		sections = index.getIndexSection();
		assertNotNull(sections);
		assertEquals(4, sections.size());
		
		verifyIndexSection(sections.get(0), "4.0", 1);
		verifyIndexSection(sections.get(1), "2.5", 1);
		verifyIndexSection(sections.get(2), "2.0", 2);
		verifyIndexSection(sections.get(3), "1.5", 1);
		
		// first section should be populated with the 1 item
		assertNotNull(sr.getAlbum());
		assertEquals(1, sr.getAlbum().size());
		albumResults = sr.getAlbum();
		asr = albumResults.get(0);
		assertEquals(Long.valueOf(2), asr.getItemCount());
		assertNotNull(asr.getSearchPoster());
		poster = asr.getSearchPoster();
		assertEquals(item3.getItemId(), poster.getItemId());
		
	}

	private void verifyIndexSection(
			PaginationIndexSection paginationIndexSection, String key, int count) {
		assertNotNull(paginationIndexSection);
		assertEquals(key, paginationIndexSection.getIndexKey());
		assertEquals(count, paginationIndexSection.getCount());
	}

	// save a new album
	private Long saveNewUser() {
		User user = domainObjectFactory.newUserInstance();
		user.setAnonymousKey("123_" +(counter++));
		user.setEmail("foo@no.where_" +(counter++));
		user.setName("Foo Bar");
		user.setPassword("password");
		user.setLogin("foobar" +(counter++));
		user.setCreationDate(Calendar.getInstance());
		
		List<TimeZone> tzList = timeZoneDao.findAllTimeZones();
		user.setTz(tzList.get(0));
		
		return userDao.store(user);
	}
	
	// save a new album
	private Long saveNewAlbum(Theme theme, User owner) {
		Album album = newAlbumInstance(theme, owner);
		return albumDao.store(album);
	}
	
	private Album newAlbumInstance(Theme theme, User owner) {
		Album album = domainObjectFactory.newAlbumInstance();
		album.setComment("This is a new album.");
		album.setCreationDate(Calendar.getInstance());
		album.setName("My Test Album " +(counter++));
		if ( theme != null ) {
			album.setTheme(theme);
		}
		if ( owner != null ) {
			album.setOwner(owner);
		}
		album.setAnonymousKey("test.key."+counter);
		album.setAllowAnonymous(true);
		album.setAllowBrowse(true);
		album.setAllowFeed(true);
		return album;
	}

	private Collection saveNewCollection(User user) {
		Collection col = domainObjectFactory.newCollectionInstance();
		col.setComment("test");
		col.setCreationDate(Calendar.getInstance());
		col.setName("Test Col");
		col.setOwner(user);
		col.setPath("no/where/" +(counter++));
		Long cId = collectionDao.store(col);
		return collectionDao.get(cId);
	}
	
	@SuppressWarnings("unchecked")
	private MediaItem saveNewMediaItem(Collection col) {
		MediaItem item = domainObjectFactory.newMediaItemInstance();
		item.setCreationDate(Calendar.getInstance());
		item.setDescription("test");
		item.setFileSize(123l);
		item.setHeight(640);
		item.setWidth(480);
		item.setMime("image/jpeg");
		item.setName("test");
		item.setPath("test-" +(counter++) +".jpg");
		item.setTz(col.getOwner().getTz());
		item.setTzDisplay(col.getOwner().getTz());
		
		col.getItem().add(item);
		collectionDao.store(col);
		
		// find out what the saved MediaItem IDs are via their paths
		return mediaItemDao.getItemForPath(col.getCollectionId(), item.getPath());
	}

	@SuppressWarnings("unchecked")
	private void addUserRating(MediaItem item, User user, int rating) {
		MediaItemRating rating1_1 = domainObjectFactory.newMediaItemRatingInstance();
		rating1_1.setCreationDate(Calendar.getInstance());
		rating1_1.setRating((short)rating);
		rating1_1.setRatingUser(user);
		item.getUserRating().add(rating1_1);
	}
	
}
