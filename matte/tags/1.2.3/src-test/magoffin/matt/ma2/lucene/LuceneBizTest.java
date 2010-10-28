/* ===================================================================
 * LuceneBizTest.java
 * 
 * Created May 29, 2006 6:05:52 PM
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

package magoffin.matt.ma2.lucene;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import magoffin.matt.lucene.IndexEvent;
import magoffin.matt.lucene.IndexListener;
import magoffin.matt.lucene.IndexUpdateTracker;
import magoffin.matt.lucene.IndexEvent.EventType;
import magoffin.matt.ma2.AbstractSpringEnabledTransactionalTest;
import magoffin.matt.ma2.AuthorizationException;
import magoffin.matt.ma2.TestConstants;
import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.biz.WorkBiz.WorkInfo;
import magoffin.matt.ma2.biz.impl.IOBizImpl;
import magoffin.matt.ma2.biz.impl.TestBizContext;
import magoffin.matt.ma2.dao.CollectionDao;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.MediaItemSearchResult;
import magoffin.matt.ma2.domain.PaginationCriteria;
import magoffin.matt.ma2.domain.PaginationIndexSection;
import magoffin.matt.ma2.domain.SearchResults;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.domain.UserSearchResult;
import magoffin.matt.ma2.domain.UserTag;
import magoffin.matt.ma2.support.BasicMediaItemSearchCriteria;
import magoffin.matt.ma2.support.MediaInfoCommand;

/**
 * Test case for the {@link LuceneBiz} class.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class LuceneBizTest extends AbstractSpringEnabledTransactionalTest {
	
	/** The LuceneBiz to test. */
	protected LuceneBiz testLuceneBiz;
	
	/** The UserBiz to help with testing. */
	protected UserBiz testUserBiz;
	
	/** The MediaBiz to help with testing. */
	protected MediaBiz testMediaBiz;
	
	/** The IOBiz. */
	protected IOBizImpl testIOBizImpl;

	/** The DomainObjectFactory to help with testing. */
	protected DomainObjectFactory domainObjectFactory;
	
	/** The CollectionDao. */
	protected CollectionDao collectionDao;
	
	private TestBizContext myBizContext = new TestBizContext(getContext(contextKey()),null);
	private IndexUpdateTracker updateTracker = new IndexUpdateTracker();

	@Override
	public boolean isPopulateProtectedVariables() {
		return true;
	}

	@Override
	protected void onSetUpInTransaction() throws Exception {
		super.onSetUpInTransaction();
		deleteFromTables(TestConstants.ALL_TABLES_FOR_CLEAR);
		testLuceneBiz.getLucene().addIndexEventListener(updateTracker);
	}

	@Override
	protected void onTearDownAfterTransaction() throws Exception {
		super.onTearDownAfterTransaction();
		updateTracker.cleanUp(testLuceneBiz.getLucene());
	}

	/**
	 * Test can index all users.
	 * @throws Exception if an error occurs
	 */
	public void testIndexAllUsers() throws Exception {
		User user1 = registerAndConfirmUser("test1", "test1@localhost.localdomain");
		User user2 = registerAndConfirmUser("test2", "test2@localhost.localdomain");
		User user3 = registerAndConfirmUser("test3", "test3@localhost.localdomain");
		
		final Set<Long> indexedUserIds = new LinkedHashSet<Long>();	
		IndexListener listener = new IndexListener() {
			public void onIndexEvent(IndexEvent event) {
				if ( event.getType() == EventType.UPDATE  && IndexType.USER.name().equals(
						event.getIndexType())) {
					indexedUserIds.add((Long)event.getSource());
				}
			}
		};
		testLuceneBiz.getLucene().addIndexEventListener(listener);
		try {
			WorkInfo workInfo = testLuceneBiz.recreateUserIndex(myBizContext);
			
			assertNotNull(workInfo);
			if ( !workInfo.isDone() ) {
				workInfo.get();
			}
			
			assertTrue(workInfo.isDone());
			assertNotNull(workInfo.getObjectIds());
			assertEquals(3, workInfo.getObjectIds().size());
			assertTrue(workInfo.getObjectIds().contains(user1.getUserId()));
			assertTrue(workInfo.getObjectIds().contains(user2.getUserId()));
			assertTrue(workInfo.getObjectIds().contains(user3.getUserId()));
			
			assertEquals(3, indexedUserIds.size());
		} finally {
			testLuceneBiz.getLucene().removeIndexEventListener(listener);
		}
	}
	
	/**
	 * Test finding users for the user index.
	 * @throws Exception if an error occurs
	 */
	public void testFindUsersForIndex() throws Exception {
		setupTestUsers();
		
		PaginationCriteria criteria = domainObjectFactory.newPaginationCriteriaInstance();
		SearchResults results = testLuceneBiz.findUsersForIndex(criteria, myBizContext);
		assertNotNull(results);
		assertNotNull(results.getIndex());
		assertEquals(2, results.getIndex().getIndexSection().size());

		PaginationIndexSection indexA = (PaginationIndexSection)
			results.getIndex().getIndexSection().get(0);
		assertEquals("a", indexA.getIndexKey());
		assertEquals(2, indexA.getCount());
		
		PaginationIndexSection indexZ = (PaginationIndexSection)
			results.getIndex().getIndexSection().get(1);
		assertEquals("z", indexZ.getIndexKey());
		assertEquals(1, indexZ.getCount());
	}
	
	/**
	 * Test finding users for the user index.
	 * @throws Exception if an error occurs
	 */
	public void testFindUsersForIndexWithSelection() throws Exception {
		setupTestUsers();
		
		PaginationCriteria criteria = domainObjectFactory.newPaginationCriteriaInstance();
		criteria.setIndexKey("a");
		
		SearchResults results = testLuceneBiz.findUsersForIndex(criteria, myBizContext);
		assertNotNull(results);
		assertNotNull(results.getIndex());
		assertEquals(2, results.getIndex().getIndexSection().size());

		PaginationIndexSection indexA = (PaginationIndexSection)
			results.getIndex().getIndexSection().get(0);
		assertEquals("a", indexA.getIndexKey());
		assertEquals(2, indexA.getCount());
		assertEquals(2, results.getUser().size());
		assertTrue(results.getUser().get(0) instanceof UserSearchResult);
		assertEquals("atest1", ((UserSearchResult)results.getUser().get(0)).getLogin());
		
		PaginationIndexSection indexZ = (PaginationIndexSection)
			results.getIndex().getIndexSection().get(1);
		assertEquals("z", indexZ.getIndexKey());
		assertEquals(1, indexZ.getCount());
	}
	
	/**
	 * Test can index all users.
	 * @throws Exception if an error occurs
	 */
	@SuppressWarnings("unchecked")
	public void testIndexAllMediaItems() throws Exception {
		Collection testCollection = setupTestCollection();
		importImage("magoffin/matt/ma2/image/bee-action.jpg", collectionDao, 
				testIOBizImpl, testCollection, testCollection.getOwner());
		importImage("magoffin/matt/ma2/image/dylan2.jpg", collectionDao, 
				testIOBizImpl, testCollection, testCollection.getOwner());
		importImage("magoffin/matt/ma2/image/IMG_4215.jpg", collectionDao, 
				testIOBizImpl, testCollection, testCollection.getOwner());
		testCollection = collectionDao.get(testCollection.getCollectionId());
		List<MediaItem> items = testCollection.getItem();
		assertEquals(3, items.size());
		
		final Set<Long> indexedIds = new LinkedHashSet<Long>();	
		IndexListener listener = new IndexListener() {
			public void onIndexEvent(IndexEvent event) {
				if ( event.getType() == EventType.UPDATE && IndexType.MEDIA_ITEM.name().equals(
						event.getIndexType()) ) {
					if ( event.getSource() instanceof Long ) {
						indexedIds.add((Long)event.getSource());
					}
				}
			}
		};
		testLuceneBiz.getLucene().addIndexEventListener(listener);
		try {
			WorkInfo workInfo = testLuceneBiz.recreateMediaItemIndex(myBizContext);
			
			assertNotNull(workInfo);
			if ( !workInfo.isDone() ) {
				workInfo.get();
			}
			
			assertTrue(workInfo.isDone());
			assertNotNull(workInfo.getObjectIds());
			assertEquals(3, workInfo.getObjectIds().size());
			assertTrue(workInfo.getObjectIds().contains(items.get(0).getItemId()));
			assertTrue(workInfo.getObjectIds().contains(items.get(1).getItemId()));
			assertTrue(workInfo.getObjectIds().contains(items.get(2).getItemId()));
			
			assertEquals(3, indexedIds.size());
		} finally {
			testLuceneBiz.getLucene().removeIndexEventListener(listener);
		}
	}
	
	/**
	 * Test able to find a MediaItem by it's ID.
	 * @throws Exception if an error occurs
	 */
	public void testFindMediaItemById() throws Exception {
		Collection testCollection = setupTestCollection();
		importImage("magoffin/matt/ma2/image/bee-action.jpg", collectionDao, 
				testIOBizImpl, testCollection, testCollection.getOwner());
		testCollection = collectionDao.get(testCollection.getCollectionId());
		MediaItem testItem = (MediaItem)testCollection.getItem().get(0);
		testLuceneBiz.indexMediaItem(testItem.getItemId());
		
		BasicMediaItemSearchCriteria criteria = new BasicMediaItemSearchCriteria();
		criteria.setMediaItemTemplate(testItem);
		SearchResults results = testLuceneBiz.findMediaItems(criteria, null, 
				myBizContext);
		assertNotNull(results);
		assertEquals(1, results.getReturnedResults().longValue());
		assertEquals(1, results.getTotalResults().longValue());
		assertEquals(1, results.getItem().size());
		
		MediaItemSearchResult resultItem = (MediaItemSearchResult)results.getItem().get(0);
		assertEquals(testItem.getItemId(), resultItem.getItemId());
	}
	
	/**
	 * Test able to find a MediaItem by one or more of it's tags.
	 * @throws Exception if an error occurs
	 */
	@SuppressWarnings("unchecked")
	public void testFindMediaItemByTag() throws Exception {
		Collection testCollection = setupTestCollection();
		importImage("magoffin/matt/ma2/image/bee-action.jpg", collectionDao, 
				testIOBizImpl, testCollection, testCollection.getOwner());
		importImage("magoffin/matt/ma2/image/dylan2.jpg", collectionDao, 
				testIOBizImpl, testCollection, testCollection.getOwner());
		testCollection = collectionDao.get(testCollection.getCollectionId());
		MediaItem testItem = (MediaItem)testCollection.getItem().get(0);
		MediaItem testItem2 = (MediaItem)testCollection.getItem().get(1);
		
		// add tags
		MediaInfoCommand cmd = new MediaInfoCommand();
		cmd.setItemIds(new Long[] {testItem.getItemId(), testItem2.getItemId()});
		cmd.setTags("test_tag1, test_tag2");
		testMediaBiz.storeMediaItemInfo(cmd, myBizContext);
		
		// add one more tag to just one item
		cmd.setItemIds(new Long[] {testItem.getItemId()});
		cmd.setTags("test_tag1, test_tag2, test_tag3");
		testMediaBiz.storeMediaItemInfo(cmd, myBizContext);
		
		// now index items
		testLuceneBiz.indexMediaItem(testItem.getItemId());
		testLuceneBiz.indexMediaItem(testItem2.getItemId());
		
		// search for single tag
		BasicMediaItemSearchCriteria criteria = new BasicMediaItemSearchCriteria();
		MediaItem searchTemplate = domainObjectFactory.newMediaItemInstance();
		UserTag searchTag = domainObjectFactory.newUserTagInstance();
		searchTag.setTag("test_tag1");
		searchTemplate.getUserTag().add(searchTag);
		criteria.setMediaItemTemplate(searchTemplate);
		
		SearchResults results = testLuceneBiz.findMediaItems(criteria, null, 
				myBizContext);
		assertNotNull(results);
		assertTrue(results.getReturnedResults().longValue() >= 2);
		assertTrue(results.getTotalResults().longValue() >= 2);
		assertTrue(results.getItem().size() >= 2);
		
		boolean foundItem1 = false;
		boolean foundItem2 = false;
		for ( MediaItemSearchResult sr : (List<MediaItemSearchResult>)results.getItem() ) {
			if ( sr.getItemId().equals(testItem.getItemId()) ) {
				foundItem1 = true;
			} else if ( sr.getItemId().equals(testItem2.getItemId()) ) {
				foundItem2 = true;
			}
		}
		assertTrue(foundItem1 && foundItem2);
		
		// now search for tag3, should only find testItem
		searchTag.setTag("test_tag3");
		results = testLuceneBiz.findMediaItems(criteria, null, myBizContext);
		assertNotNull(results);
		assertTrue(results.getReturnedResults().longValue() >= 1);
		assertTrue(results.getTotalResults().longValue() >= 1);
		assertTrue(results.getItem().size() >= 1);
		foundItem1 = false;
		foundItem2 = false;
		for ( MediaItemSearchResult sr : (List<MediaItemSearchResult>)results.getItem() ) {
			if ( sr.getItemId().equals(testItem.getItemId()) ) {
				foundItem1 = true;
			} else if ( sr.getItemId().equals(testItem2.getItemId()) ) {
				foundItem2 = true;
			}
		}
		assertTrue(foundItem1);
		assertFalse(foundItem2);
		
		// now search for tag3 for our user only
		searchTag.setTaggingUser(myBizContext.getActingUser());
		results = testLuceneBiz.findMediaItems(criteria, null, myBizContext);
		assertNotNull(results);
		assertEquals(1, results.getReturnedResults().longValue());
		assertEquals(1, results.getTotalResults().longValue());
		assertEquals(1, results.getItem().size());
		MediaItemSearchResult found = (MediaItemSearchResult)results.getItem().get(0);
		assertEquals(testItem.getItemId(), found.getItemId());
		assertEquals(1, found.getUserTag().size());
		UserTag foundTag = (UserTag)found.getUserTag().get(0);
		assertNotNull(foundTag.getTaggingUser());
		assertEquals(myBizContext.getActingUser().getUserId(), 
				foundTag.getTaggingUser().getUserId());
		assertEquals("test_tag1, test_tag2, test_tag3", foundTag.getTag());
		
		// search for multiple tags at once... item 1 should be first in results since has both tags
		searchTag.setTag("test_tag1, test_tag3");
		results = testLuceneBiz.findMediaItems(criteria, null, myBizContext);
		assertNotNull(results);
		assertEquals(2, results.getReturnedResults().longValue());
		assertEquals(2, results.getTotalResults().longValue());
		assertEquals(2, results.getItem().size());
		found = (MediaItemSearchResult)results.getItem().get(0);
		assertEquals(testItem.getItemId(), found.getItemId());
		MediaItemSearchResult found2 = (MediaItemSearchResult)results.getItem().get(1);
		assertEquals(testItem2.getItemId(), found2.getItemId());
	}
	
	/**
	 * Test able to find a MediaItem by one or more of it's tags.
	 * @throws Exception if an error occurs
	 */
	@SuppressWarnings("unchecked")
	public void testFindMediaCount() throws Exception {
		Collection testCollection = setupTestCollection();
		importImage("magoffin/matt/ma2/image/bee-action.jpg", collectionDao, 
				testIOBizImpl, testCollection, testCollection.getOwner());
		importImage("magoffin/matt/ma2/image/dylan2.jpg", collectionDao, 
				testIOBizImpl, testCollection, testCollection.getOwner());
		testCollection = collectionDao.get(testCollection.getCollectionId());
		MediaItem testItem = (MediaItem)testCollection.getItem().get(0);
		MediaItem testItem2 = (MediaItem)testCollection.getItem().get(1);
		
		// add tags
		MediaInfoCommand cmd = new MediaInfoCommand();
		cmd.setItemIds(new Long[] {testItem.getItemId(), testItem2.getItemId()});
		cmd.setTags("test_tag1, test_tag2");
		testMediaBiz.storeMediaItemInfo(cmd, myBizContext);
		
		// add one more tag to just one item
		cmd.setItemIds(new Long[] {testItem.getItemId()});
		cmd.setTags("test_tag1, test_tag2, test_tag3");
		testMediaBiz.storeMediaItemInfo(cmd, myBizContext);
		
		// now index items
		testLuceneBiz.indexMediaItem(testItem.getItemId());
		testLuceneBiz.indexMediaItem(testItem2.getItemId());
		
		// search for single tag, count only
		BasicMediaItemSearchCriteria criteria = new BasicMediaItemSearchCriteria();
		MediaItem searchTemplate = domainObjectFactory.newMediaItemInstance();
		UserTag searchTag = domainObjectFactory.newUserTagInstance();
		searchTag.setTag("test_tag1");
		searchTemplate.getUserTag().add(searchTag);
		searchTag.setTaggingUser(myBizContext.getActingUser());
		criteria.setMediaItemTemplate(searchTemplate);
		criteria.setCountOnly(true);
		
		SearchResults results = testLuceneBiz.findMediaItems(criteria, null, 
				myBizContext);
		assertNotNull(results);
		assertEquals(2, results.getTotalResults().longValue());
		assertEquals(0, results.getItem().size());
	}

	private void setupTestUsers() {
		try {
			registerAndConfirmUser("atest1", "test1@localhost.localdomain");
			registerAndConfirmUser("atest2", "test2@localhost.localdomain");
			registerAndConfirmUser("ztest3", "test3@localhost.localdomain");
			WorkInfo workInfo = testLuceneBiz.recreateUserIndex(myBizContext);
			if ( !workInfo.isDone() ) {
				workInfo.get();
			}
		} catch ( Exception e ) {
			throw new RuntimeException(e);
		}
	}
	
	private Collection setupTestCollection() {
		User testUser = registerAndConfirmUser("test.user", "test@localhost");
		myBizContext.setActingUser(testUser);
		List<Collection> collections = testUserBiz.getCollectionsForUser(
				testUser, myBizContext);
		return collections.get(0);
	}

	private User registerAndConfirmUser(String login, String email) throws AuthorizationException {
		User newUser = getTestUser(login, email);
		String confKey = testUserBiz.registerUser(newUser, myBizContext);
		User confirmedUser = testUserBiz.confirmRegisteredUser(newUser.getLogin(),
				confKey, myBizContext);
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
