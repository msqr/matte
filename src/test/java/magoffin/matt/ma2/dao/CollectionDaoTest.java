/* ===================================================================
 * CollectionDaoTest.java
 * 
 * Created Mar 17, 2006 9:56:29 PM
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
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.TimeZone;
import magoffin.matt.ma2.domain.User;

/**
 * Test case for the {@link magoffin.matt.ma2.dao.CollectionDao} class.
 * 
 * @author matt.magoffin
 * @version 1.0
 */
public class CollectionDaoTest extends AbstractSpringEnabledTransactionalTest {
	
	@javax.annotation.Resource private CollectionDao collectionDao;
	@javax.annotation.Resource private MediaItemDao mediaItemDao;
	@javax.annotation.Resource private UserDao userDao;
	@javax.annotation.Resource private TimeZoneDao timeZoneDao;
	@javax.annotation.Resource private DomainObjectFactory domainObjectFactory;
	
	private User testUser;
	private Collection testCollection;
	private MediaItem testItem;

	private User saveNewUser() {
		User user = domainObjectFactory.newUserInstance();
		user.setAnonymousKey("123");
		user.setEmail("foo@no.where");
		user.setName("Foo Bar");
		user.setPassword("password");
		user.setLogin("foobar");
		user.setCreationDate(Calendar.getInstance());
		
		List<TimeZone> tzList = timeZoneDao.findAllTimeZones();
		user.setTz(tzList.get(0));
		
		Long userId = userDao.store(user);
		return userDao.get(userId);
	}
	
	private Collection saveNewCollection(User user) {
		Collection col = domainObjectFactory.newCollectionInstance();
		col.setComment("test");
		col.setCreationDate(Calendar.getInstance());
		col.setName("Test Col");
		col.setOwner(user);
		col.setPath("no/where");
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
		item.setPath("test.jpg");
		item.setTz(this.testUser.getTz());
		item.setTzDisplay(this.testUser.getTz());
		
		col.getItem().add(item);
		collectionDao.store(col);
		
		// find out what the saved MediaItem IDs are via their paths
		return mediaItemDao.getItemForPath(col.getCollectionId(), item.getPath());
	}

	@Before
	@Override
	public void onSetUpInTransaction() {
		super.onSetUpInTransaction();
		deleteFromTables(TestConstants.ALL_TABLES_FOR_CLEAR);
		
		this.testUser = saveNewUser();
		this.testCollection = saveNewCollection(this.testUser);
		this.testItem = saveNewMediaItem(this.testCollection);
	}

	/**
	 * Test getting a collection by a media id.
	 * @throws Exception
	 */
	@Test
	public void testFindByMediaItemId() throws Exception {
		Collection result = collectionDao.getCollectionForMediaItem(this.testItem.getItemId());
		assertNotNull(result);
		if ( logger.isDebugEnabled() ) {
			logger.debug("Got Collection: " +result);
		}
	}

	/**
	 * Test getting a collection by a media id.
	 * @throws Exception
	 */
	@Test
	public void testFindCollectionsForUser() throws Exception {
		List<Collection> result = collectionDao.findCollectionsForUser(
				this.testUser.getUserId());
		assertNotNull(result);
		assertEquals(1, result.size());
		if ( logger.isDebugEnabled() ) {
			logger.debug("Got Collections: " +result);
		}
	}
}
