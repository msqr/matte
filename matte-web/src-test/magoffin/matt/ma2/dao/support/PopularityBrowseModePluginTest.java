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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2.dao.support;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

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
 * Test case for the {@link PopularityBrowseModePlugin} class.
 *
 * @author matt
 * @version $Revision$ $Date$
 */
@ContextConfiguration
public class PopularityBrowseModePluginTest
extends AbstractSpringEnabledTransactionalTest {
	
	@javax.annotation.Resource private AlbumDao albumDao;
	@javax.annotation.Resource private DomainObjectFactory domainObjectFactory;
	@javax.annotation.Resource private PopularityBrowseModePlugin testPlugin;
	@javax.annotation.Resource private UserDao userDao;
	@javax.annotation.Resource private TimeZoneDao timeZoneDao;
	@javax.annotation.Resource private CollectionDao collectionDao;
	@javax.annotation.Resource private MediaItemDao mediaItemDao;
	
	private int counter = 1;

	@Before
	@Override
	public void onSetUpInTransaction() {
		super.onSetUpInTransaction();
		deleteFromTables(TestConstants.ALL_TABLES_FOR_CLEAR);
	}
	
	/**
	 * Test finding for a user by popularity.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testSearchForAlbumsForUserByPopularity() {
		Long savedUserId = saveNewUser();
		User user = userDao.get(savedUserId);
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
		MediaItem item1 = saveNewMediaItem(c); // 63 -> 60
		item1.setHits(63);
		mediaItemDao.store(item1);
		album1.getItem().add(item1);
		albumDao.store(album1);
		
		MediaItem item2 = saveNewMediaItem(c); // 35 -> 40
		item2.setHits(35);
		mediaItemDao.store(item2);
		album1.getItem().add(item2);
		albumDao.store(album1);
		
		MediaItem item3 = saveNewMediaItem(c); // 12 -> 20
		item3.setHits(12);
		mediaItemDao.store(item3);
		album1.getItem().add(item3);
		albumDao.store(album1);
		
		MediaItem item4 = saveNewMediaItem(c); // 8 -> 0
		item4.setHits(8);
		mediaItemDao.store(item4);
		album2.getItem().add(item4);
		albumDao.store(album2);
		
		MediaItem item5 = saveNewMediaItem(c); // 3 -> 0
		item5.setHits(3);
		mediaItemDao.store(item5);
		album2.getItem().add(item5);
		albumDao.store(album2);
		
		MediaItem item6 = saveNewMediaItem(c); // 0 -> excluded
		album2.getItem().add(item6);
		albumDao.store(album2);
		
		// execute Hibernate search to flush to DB
		albumDao.findAlbumsForUser(savedUserId);
		mediaItemDao.findItemsForCollection(c.getCollectionId());
		
		// test getting all
		BrowseAlbumsCommand command = new BrowseAlbumsCommand();
		command.setUserKey(user.getAnonymousKey());
		command.setMode(PopularityBrowseModePlugin.MODE_POPULARITY);
		SearchResults sr = testPlugin.find(command, null);
		
		// verify index
		PaginationIndex index = sr.getIndex();
		assertNotNull(index);
		List<PaginationIndexSection> sections = index.getIndexSection();
		assertNotNull(sections);
		assertEquals(4, sections.size());
		
		verifyIndexSection(sections.get(0), "60 - 79", 1);
		verifyIndexSection(sections.get(1), "40 - 59", 1);
		verifyIndexSection(sections.get(2), "20 - 39", 1);
		verifyIndexSection(sections.get(3), "0 - 19", 2);
		
		// first section should be populated with the 1 item
		assertNotNull(sr.getAlbum());
		assertEquals(1, sr.getAlbum().size());
		List<AlbumSearchResult> albumResults = sr.getAlbum();
		AlbumSearchResult asr = albumResults.get(0);
		assertEquals(Long.valueOf(1), asr.getItemCount());
		assertNotNull(asr.getSearchPoster());
		PosterSearchResult poster = asr.getSearchPoster();
		assertEquals(item1.getItemId(), poster.getItemId());
		assertEquals("60 - 79 hits: item 1", asr.getName());
		
		// now request "0" section
		PaginationCriteria pageCriteria = domainObjectFactory.newPaginationCriteriaInstance();
		pageCriteria.setIndexKey("0 - 19");
		sr = testPlugin.find(command, pageCriteria);
		
		// verify index
		index = sr.getIndex();
		assertNotNull(index);
		sections = index.getIndexSection();
		assertNotNull(sections);
		assertEquals(4, sections.size());
		
		verifyIndexSection(sections.get(0), "60 - 79", 1);
		verifyIndexSection(sections.get(1), "40 - 59", 1);
		verifyIndexSection(sections.get(2), "20 - 39", 1);
		verifyIndexSection(sections.get(3), "0 - 19", 2);
		
		// first section should be populated with the 1 album with 2 items
		assertNotNull(sr.getAlbum());
		assertEquals(1, sr.getAlbum().size());
		albumResults = sr.getAlbum();
		asr = albumResults.get(0);
		assertEquals(Long.valueOf(2), asr.getItemCount());
		assertEquals("0 - 19 hits: items 1 - 2", asr.getName());
		assertNotNull(asr.getSearchPoster());
		poster = asr.getSearchPoster();
		assertEquals(item4.getItemId(), poster.getItemId());
		
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

}
