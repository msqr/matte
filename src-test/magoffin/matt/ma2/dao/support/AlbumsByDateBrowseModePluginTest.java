/* ===================================================================
 * TestAlbumsByDateBrowseModePlugin.java
 * 
 * Created Sep 20, 2007 12:34:27 PM
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
import magoffin.matt.ma2.dao.TimeZoneDao;
import magoffin.matt.ma2.dao.UserDao;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.AlbumSearchResult;
import magoffin.matt.ma2.domain.SearchResults;
import magoffin.matt.ma2.domain.Theme;
import magoffin.matt.ma2.domain.TimeZone;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.support.BrowseAlbumsCommand;

/**
 * Test case for the {@link AlbumsByDateBrowseModePlugin} class.
 *
 * @author matt
 * @version $Revision$ $Date$
 */
@ContextConfiguration
public class AlbumsByDateBrowseModePluginTest
extends AbstractSpringEnabledTransactionalTest {

	@javax.annotation.Resource private AlbumDao albumDao;
	@javax.annotation.Resource private DomainObjectFactory domainObjectFactory;
	@javax.annotation.Resource private AlbumsByDateBrowseModePlugin testPlugin;
	@javax.annotation.Resource private UserDao userDao;
	@javax.annotation.Resource private TimeZoneDao timeZoneDao;
	
	private int albumCounter = 1;

	@Before
	@Override
	public void onSetUpInTransaction() {
		super.onSetUpInTransaction();
		deleteFromTables(TestConstants.ALL_TABLES_FOR_CLEAR);
	}
	
	/**
	 * Test finding for a user by date.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testSearchForAlbumsForUserByDate() {
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
		
		// execute Hibernate search to flush
		albumDao.findAlbumsForUser(savedUserId);
		
		// test getting all
		BrowseAlbumsCommand command = new BrowseAlbumsCommand();
		command.setUserKey(user.getAnonymousKey());
		command.setMode(BrowseAlbumsCommand.MODE_ALBUMS);
		SearchResults sr = testPlugin.find(command, null);
		List<AlbumSearchResult> albums = sr.getAlbum();
		assertNotNull(albums);

		// now verify order... should be album2, album1
		assertEquals(2, albums.size());
		assertEquals(album2.getAlbumId(), albums.get(0).getAlbumId());
		assertEquals(album1.getAlbumId(), albums.get(1).getAlbumId());
		
		// test getting limit
		command.setMaxEntries(1);
		sr = testPlugin.find(command, null);
		albums = sr.getAlbum();
		assertEquals(1, albums.size());
		assertEquals(album2.getAlbumId(), albums.get(0).getAlbumId());
		
		// now add child album to album1
		Long savedAlbumId3 = saveNewAlbum(null, user);
		Album album3 = albumDao.get(savedAlbumId3);
		album2.getAlbum().add(album3);
		albumDao.store(album2);
		albumDao.store(album3);
		
		// execute search again to flush
		albumDao.findAlbumsForUser(savedUserId);
		
		command.setMaxEntries(-1);
		sr = testPlugin.find(command, null);
		albums = sr.getAlbum();
		assertEquals(2, albums.size());
		assertEquals(album2.getAlbumId(), albums.get(0).getAlbumId());
		assertEquals(album1.getAlbumId(), albums.get(1).getAlbumId());
		assertEquals(1, albums.get(0).getSearchAlbum().size());
		assertEquals(savedAlbumId3, ((AlbumSearchResult)
				albums.get(0).getSearchAlbum().get(0)).getAlbumId());
		
		// test getting limit
		command.setMaxEntries(1);
		sr = testPlugin.find(command, null);
		albums = sr.getAlbum();
		assertEquals(1, albums.size());
		assertEquals(album2.getAlbumId(), albums.get(0).getAlbumId());
		assertEquals(1, albums.get(0).getSearchAlbum().size());
		assertEquals(savedAlbumId3, ((AlbumSearchResult)
				albums.get(0).getSearchAlbum().get(0)).getAlbumId());
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
		album.setAllowBrowse(true);
		album.setAllowFeed(true);
		return album;
	}
	

}
