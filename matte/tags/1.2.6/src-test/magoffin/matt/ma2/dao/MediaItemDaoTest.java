/* ===================================================================
 * MediaItemDaoTest.java
 * 
 * Created Oct 7, 2006 4:47:56 PM
 * 
 * Copyright (c) 2006 Matt Magoffin (spamsqr@msqr.us)
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

import java.util.BitSet;
import java.util.Calendar;
import java.util.List;

import magoffin.matt.dao.BasicBatchOptions;
import magoffin.matt.dao.BatchableDao.BatchCallback;
import magoffin.matt.dao.BatchableDao.BatchCallbackResult;
import magoffin.matt.dao.BatchableDao.BatchResult;
import magoffin.matt.ma2.AbstractSpringEnabledTransactionalTest;
import magoffin.matt.ma2.TestConstants;
import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.TimeZone;
import magoffin.matt.ma2.domain.User;

import org.apache.commons.lang.mutable.MutableInt;
import org.apache.log4j.Logger;

/**
 * Unit test for the {@link MediaItemDao} class.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class MediaItemDaoTest extends AbstractSpringEnabledTransactionalTest {

	/** The Collection DAO to test. */
	protected CollectionDao collectionDao;
	
	/** The MediaItemDao to test. */
	protected MediaItemDao mediaItemDao;
	
	/** The DomainObjectFactory instance. */
	protected DomainObjectFactory domainObjectFactory;
	
	/** The ThemeDao to test with. */
	protected ThemeDao themeDao;

	/** The TimeZoneDao to test with. */
	protected TimeZoneDao timeZoneDao;
	
	/** The UserDao to test with. */
	protected UserDao userDao;
	
	private final Logger log = Logger.getLogger(MediaItemDaoTest.class);
	
	private int counter = 1;
	private User testUser = null;

	/**
	 * Default constructor.
	 */
	public MediaItemDaoTest() {
		setPopulateProtectedVariables(true);
	}

	@Override
	protected void onSetUpInTransaction() throws Exception {
		super.onSetUpInTransaction();
		deleteFromTables(TestConstants.ALL_TABLES_FOR_CLEAR);
		saveTestUser();
	}

	/**
	 * Test getting items for a set of IDs.
	 */
	public void testGetItemsForIds() {
		BatchCallback<MediaItem> callback = new BatchCallback<MediaItem>() {
			public BatchCallbackResult handle(MediaItem domainObject) {
				if ( log.isDebugEnabled() ) {
					log.debug("Got item " +domainObject.getItemId() +": "
							+domainObject);
				}
				return BatchCallbackResult.CONTINUE;
			}
		};
		Collection col = saveNewCollection();
		MediaItem item1 = saveNewMediaItem(col);
		MediaItem item2 = saveNewMediaItem(col);
		
		Long[] itemIds = new Long[] {
			item1.getItemId(),
			item2.getItemId(),
		};
		
		BasicBatchOptions batchOptions = new BasicBatchOptions(
				MediaItemDao.BATCH_NAME_PROCESS_MEDIA_IDS);
		batchOptions.getParameters().put(MediaItemDao.BATCH_PROCESS_PARAM_MEDIA_IDS_LIST, 
				itemIds);
		BatchResult result = mediaItemDao.batchProcess(callback, batchOptions);
		
		assertNotNull(result);
		assertEquals(2, result.numProcessed());
	}
	
	/**
	 * Test can index MediaItem data.
	 */
	public void testIndexAllMediaItemData() {
		Collection col = saveNewCollection();
		final MediaItem item1 = saveNewMediaItem(col);
		final MediaItem item2 = saveNewMediaItem(col);
		final BitSet itemSet = new BitSet(2);
		final MutableInt numIndexed = new MutableInt(0);
		
		BasicBatchOptions batchOptions = new BasicBatchOptions(
				MediaItemDao.BATCH_NAME_INDEX);
		BatchResult results = mediaItemDao.batchProcess(
				new magoffin.matt.dao.BatchableDao.BatchCallback<MediaItem>() {
			
			public BatchCallbackResult handle(MediaItem domainObject) {
				numIndexed.setValue(numIndexed.intValue()+1);
				if ( logger.isDebugEnabled() ) {
					logger.debug("Got item: " +domainObject.getItemId());
				}
				assertNotNull(domainObject.getItemId());
				if ( domainObject.getItemId().equals(item1.getItemId())) {
					itemSet.set(0);
				} else if ( domainObject.getItemId().equals(item2.getItemId())) {
					itemSet.set(1);
				}
				return BatchCallbackResult.CONTINUE;
			}
			
		}, batchOptions);
		assertNotNull(results);
		assertEquals(2, numIndexed.intValue());
		assertEquals(2, itemSet.cardinality());
		assertEquals(2, results.numProcessed());
	}
	
	// save a new album
	private void saveTestUser() {
		User user = domainObjectFactory.newUserInstance();
		user.setAnonymousKey("123");
		user.setEmail("foo@no.where" +(counter++));
		user.setName("Foo Bar");
		user.setPassword("password");
		user.setLogin("foobar" +(counter++));
		user.setCreationDate(Calendar.getInstance());
		
		List<TimeZone> tzList = timeZoneDao.findAllTimeZones();
		user.setTz(tzList.get(0));
		
		Long userId = userDao.store(user);
		this.testUser = userDao.get(userId);
	}

	private Collection saveNewCollection() {
		Collection col = domainObjectFactory.newCollectionInstance();
		col.setComment("test");
		col.setCreationDate(Calendar.getInstance());
		col.setName("Test Col");
		col.setOwner(this.testUser);
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
		item.setTz(this.testUser.getTz());
		item.setTzDisplay(this.testUser.getTz());
		
		col.getItem().add(item);
		collectionDao.store(col);
		
		// find out what the saved MediaItem IDs are via their paths
		return mediaItemDao.getItemForPath(col.getCollectionId(), item.getPath());
	}

}
