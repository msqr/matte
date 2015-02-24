/* ===================================================================
 * AlbumDaoTest.java
 * 
 * Created Sep 19, 2005 2:41:35 PM
 * 
 * Copyright (c) 2005 Matt Magoffin.
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

package magoffin.matt.ma2.dao;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import magoffin.matt.ma2.AbstractSpringEnabledTransactionalTest;
import magoffin.matt.ma2.TestConstants;
import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.Theme;
import magoffin.matt.ma2.domain.TimeZone;
import magoffin.matt.ma2.domain.User;

/**
 * Test case for AlbumDao.
 * 
 * @author matt.magoffin
 * @version $Revision$ $Date$
 */
public class AlbumDaoTest extends AbstractSpringEnabledTransactionalTest {

	@javax.annotation.Resource private AlbumDao albumDao;
	@javax.annotation.Resource private DomainObjectFactory domainObjectFactory;
	@javax.annotation.Resource private ThemeDao themeDao;
	@javax.annotation.Resource private TimeZoneDao timeZoneDao;
	@javax.annotation.Resource private UserDao userDao;
	@javax.annotation.Resource private CollectionDao collectionDao;
	@javax.annotation.Resource private MediaItemDao mediaItemDao;
	
	private int themeCounter = 1;
	private int albumCounter = 1;
	private int colCounter = 1;
	private int itemCounter = 1;

	@Before
	@Override
	public void onSetUpInTransaction() {
		super.onSetUpInTransaction();
		deleteFromTables(TestConstants.ALL_TABLES_FOR_CLEAR);
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
		album.setName("My Test Album " +(albumCounter++));
		if ( theme != null ) {
			album.setTheme(theme);
		}
		if ( owner != null ) {
			album.setOwner(owner);
		}
		album.setAnonymousKey("test.key."+albumCounter);
		album.setAllowAnonymous(true);
		return album;
	}
	
	/**
	 * Test persisting a new Album instance.
	 */
	@Test
	public void testCreate() {
		Long savedAlbumId = saveNewAlbum(null, null);
		assertNotNull("Returned album ID should not be null",savedAlbumId);
	}
	
	/**
	 * Test getting an album by it's primary key.
	 */
	@Test
	public void testGetByPrimaryKey() {
		Long savedAlbumId = saveNewAlbum(null, null);
		
		Album album = albumDao.get(savedAlbumId);
		assertNotNull("Returned album should not be null",album);
		assertEquals("Returned album ID should be the same as the saved album ID",
				savedAlbumId, album.getAlbumId());
	}

	/**
	 * Test finding for a user by date.
	 */
	@Test
	public void testFindForUserByDate() {
		Long savedUserId = saveNewUser();
		User user = userDao.get(savedUserId);
		
		Long savedAlbumId1 = saveNewAlbum(null, user);
		Album album1 = albumDao.get(savedAlbumId1);
		try {
			Thread.sleep(1000); // sleep so creation dates apart
		} catch ( InterruptedException e ) {
			throw new RuntimeException(e);
		}
		Long savedAlbumId2 = saveNewAlbum(null, user);
		Album album2 = albumDao.get(savedAlbumId2);
		
		// test getting all
		List<Album> albums = albumDao.findAlbumsForUserByDate(savedUserId, -1, 
				false, false, false);
		assertNotNull(albums);

		// now verify order... should be album2, album1
		assertEquals(2, albums.size());
		assertEquals(album2.getAlbumId(), albums.get(0).getAlbumId());
		assertEquals(album1.getAlbumId(), albums.get(1).getAlbumId());
		
		// test getting limit
		albums = albumDao.findAlbumsForUserByDate(savedUserId, 1, 
				false, false, false);
		assertEquals(1, albums.size());
		assertEquals(album2.getAlbumId(), albums.get(0).getAlbumId());
		
	}

	/**
	 * Test finding for a user by date since.
	 */
	public void testFindForUserByDateSince() {
		Long savedUserId = saveNewUser();
		User user = userDao.get(savedUserId);
		
		Calendar beforeDate = Calendar.getInstance();
		try {
			Thread.sleep(1000); // sleep so creation dates apart
		} catch ( InterruptedException e ) {
			throw new RuntimeException(e);
		}
		Long savedAlbumId1 = saveNewAlbum(null, user);
		Album album1 = albumDao.get(savedAlbumId1);
		try {
			Thread.sleep(1000); // sleep so creation dates apart
		} catch ( InterruptedException e ) {
			throw new RuntimeException(e);
		}
		Calendar inbetweenDate = Calendar.getInstance();
		Long savedAlbumId2 = saveNewAlbum(null, user);
		Album album2 = albumDao.get(savedAlbumId2);
		try {
			Thread.sleep(1000); // sleep so creation dates apart
		} catch ( InterruptedException e ) {
			throw new RuntimeException(e);
		}
		Calendar afterDate = Calendar.getInstance();
		
		// test getting with date before
		List<Album> albums = albumDao.findAlbumsForUserByDate(savedUserId, 
				beforeDate, false, false, false);
		assertNotNull(albums);

		// now verify order... should be album2, album1
		assertEquals(2, albums.size());
		assertEquals(album2.getAlbumId(), albums.get(0).getAlbumId());
		assertEquals(album1.getAlbumId(), albums.get(1).getAlbumId());
		
		// test getting with inbetween date
		albums = albumDao.findAlbumsForUserByDate(savedUserId, inbetweenDate, 
				false, false, false);
		assertEquals(1, albums.size());
		assertEquals(album2.getAlbumId(), albums.get(0).getAlbumId());
		
		// test getting with exact date
		albums = albumDao.findAlbumsForUserByDate(savedUserId, 
				album1.getCreationDate(), false, false, false);
		assertEquals(2, albums.size());
		assertEquals(album2.getAlbumId(), albums.get(0).getAlbumId());
		assertEquals(album1.getAlbumId(), albums.get(1).getAlbumId());
		
		// test getting with after date
		albums = albumDao.findAlbumsForUserByDate(savedUserId, afterDate, 
				false, false, false);
		assertEquals(0, albums.size());
	}

	/**
	 * Test persisting an existing Album instance.
	 */
	@Test
	public void testSave() {
		Long savedAlbumId = saveNewAlbum(null, null);
		
		Album savedAlbum = albumDao.get(savedAlbumId);
		savedAlbum.setName("My Updated Album");
		savedAlbum.setComment("This is an updated album.");
		
		Long updatedAlbumId = albumDao.store(savedAlbum);
		assertNotNull("Updated album ID should not be null",updatedAlbumId);
		assertEquals(savedAlbumId, updatedAlbumId);
	}
	
	/**
	 * Test persisting an existing Album instance.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testSaveNested() {
		User owner = userDao.get(saveNewUser());
		Long savedAlbumId = saveNewAlbum(null, owner);
		Album rootAlbum = albumDao.get(savedAlbumId);
		Album child1 = albumDao.get(saveNewAlbum(null, owner));
		Album child2 = albumDao.get(saveNewAlbum(null, owner));
		
		rootAlbum.getAlbum().add(child1);
		rootAlbum.getAlbum().add(child2);
		
		albumDao.store(rootAlbum);
		
		// now find
		List<Album> results = albumDao.findAlbumsForUser(owner.getUserId());
		assertNotNull(results);
		assertEquals(1, results.size());
	}
	
	/**
	 * Test able to reassign themes.
	 */
	@Test
	public void testReassignTheme() {
		Long savedUserId = saveNewUser();
		User user = userDao.get(savedUserId);

		Theme theme1 = saveNewTheme(user);
		Long albumId = saveNewAlbum(theme1, user);
		
		Theme theme2 = saveNewTheme(user);
		int count = albumDao.reassignAlbumsUsingTheme(theme1, theme2);
		assertEquals(1, count);
		
		Album savedAlbum = albumDao.get(albumId);
		assertEquals(theme2.getThemeId(), savedAlbum.getTheme().getThemeId());
	}
	
	/**
	 * Test finding for shared MediaItem.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testFindSharedAlbumsContainingMediaItem() {
		User user = userDao.get(saveNewUser());
		Collection c = saveNewCollection(user);
		Album savedAlbum1 = albumDao.get(saveNewAlbum(null, user));
		Album savedAlbum2 = albumDao.get(saveNewAlbum(null, user));
		savedAlbum1.setAllowAnonymous(true);
		savedAlbum2.setAllowAnonymous(false);
		MediaItem item1 = saveNewMediaItem(c, user);
		MediaItem item2 = saveNewMediaItem(c, user);
		savedAlbum1.getItem().add(item1);
		savedAlbum2.getItem().add(item2);
		albumDao.store(savedAlbum1);
		albumDao.store(savedAlbum2);
		
		List<Album> found = albumDao.findSharedAlbumsContainingItem(item1);
		assertNotNull(found);
		assertEquals(1, found.size());
		assertEquals(item1.getItemId(), 
				((MediaItem)found.get(0).getItem().get(0)).getItemId());

		found = albumDao.findSharedAlbumsContainingItem(item2);
		assertNotNull(found);
		assertEquals(0, found.size());
	}
	
	/**
	 * Test able to find a parent album from a child album.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testFindParentAlbum() {
		User owner = userDao.get(saveNewUser());
		Long savedAlbumId = saveNewAlbum(null, owner);
		Album rootAlbum = albumDao.get(savedAlbumId);
		Album child1 = albumDao.get(saveNewAlbum(null, owner));
		Album child2 = albumDao.get(saveNewAlbum(null, owner));
		
		rootAlbum.getAlbum().add(child1);
		rootAlbum.getAlbum().add(child2);
		
		albumDao.store(rootAlbum);
		
		// now find rootAlbum as parent of child1 and child2
		Album a1 = albumDao.getParentAlbum(child1.getAlbumId());
		assertNotNull(a1);
		assertEquals(rootAlbum.getAlbumId(), a1.getAlbumId());
		
		Album a2 = albumDao.getParentAlbum(child2.getAlbumId());
		assertNotNull(a2);
		assertEquals(rootAlbum.getAlbumId(), a2.getAlbumId());

		Album a3 = albumDao.getParentAlbum(rootAlbum.getAlbumId());
		assertNull("RootAlbum should not have a parent", a3);
	}
	
	// save a new album
	private Long saveNewUser() {
		User user = domainObjectFactory.newUserInstance();
		user.setAnonymousKey("123");
		user.setEmail("foo@no.where");
		user.setName("Foo Bar");
		user.setPassword("password");
		user.setLogin("foobar");
		user.setCreationDate(Calendar.getInstance());
		
		List<TimeZone> tzList = timeZoneDao.findAllTimeZones();
		user.setTz(tzList.get(0));
		
		return userDao.store(user);
	}
	
	private Theme saveNewTheme(User owner) {
		Theme theme = domainObjectFactory.newThemeInstance();
		theme.setAuthor("Test Author");
		theme.setAuthorEmail("Test Author Email");
		theme.setBasePath("base/path");
		theme.setCreationDate(Calendar.getInstance());
		theme.setDescription("Description here.");
		theme.setModifyDate(Calendar.getInstance());
		theme.setName("Theme Name " +(themeCounter++));
		theme.setOwner(owner);
		Long savedThemeId = themeDao.store(theme);
		return themeDao.get(savedThemeId);
	}
	
	private Collection saveNewCollection(User owner) {
		Collection col = domainObjectFactory.newCollectionInstance();
		col.setComment("test");
		col.setCreationDate(Calendar.getInstance());
		col.setName("Test Col");
		col.setOwner(owner);
		col.setPath("no/where/" +(colCounter++));
		Long cId = collectionDao.store(col);
		return collectionDao.get(cId);
	}
	
	@SuppressWarnings("unchecked")
	private MediaItem saveNewMediaItem(Collection col, User owner) {
		MediaItem item = domainObjectFactory.newMediaItemInstance();
		item.setCreationDate(Calendar.getInstance());
		item.setDescription("test");
		item.setFileSize(123l);
		item.setHeight(640);
		item.setWidth(480);
		item.setMime("image/jpeg");
		item.setName("test");
		item.setPath("test-" +(itemCounter++) +".jpg");
		item.setTz(owner.getTz());
		item.setTzDisplay(owner.getTz());
		
		col.getItem().add(item);
		collectionDao.store(col);
		
		// find out what the saved MediaItem IDs are via their paths
		return mediaItemDao.getItemForPath(col.getCollectionId(), item.getPath());
	}

}
