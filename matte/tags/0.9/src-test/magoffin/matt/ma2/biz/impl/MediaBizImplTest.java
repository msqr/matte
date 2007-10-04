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
 * $Id: MediaBizImplTest.java,v 1.35 2007/09/09 01:58:47 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.biz.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import magoffin.matt.ma2.AbstractSpringEnabledTransactionalTest;
import magoffin.matt.ma2.MediaQuality;
import magoffin.matt.ma2.MediaSize;
import magoffin.matt.ma2.TestConstants;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.biz.IOBiz;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.biz.WorkBiz.WorkInfo;
import magoffin.matt.ma2.dao.AlbumDao;
import magoffin.matt.ma2.dao.CollectionDao;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.KeyNameType;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.MediaItemRating;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.domain.UserComment;
import magoffin.matt.ma2.domain.UserTag;
import magoffin.matt.ma2.support.AddMediaCommand;
import magoffin.matt.ma2.support.Geometry;
import magoffin.matt.ma2.support.MediaInfoCommand;
import magoffin.matt.ma2.support.MoveItemsCommand;
import magoffin.matt.ma2.support.ShareAlbumCommand;
import magoffin.matt.ma2.support.SortAlbumsCommand;
import magoffin.matt.ma2.support.UserCommentCommand;
import magoffin.matt.util.TemporaryFile;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Test the {@link magoffin.matt.ma2.biz.impl.MediaBizImpl} class.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.35 $ $Date: 2007/09/09 01:58:47 $
 */
public class MediaBizImplTest extends AbstractSpringEnabledTransactionalTest {
	
	/** The MediaBiz. */
	protected MediaBizImpl testMediaBizImpl;

	/** The DomainObjectFactory instance. */
	protected DomainObjectFactory domainObjectFactory;

	/** The UserBiz. */
	protected UserBiz testUserBiz;
	
	/** The IOBiz. */
	protected IOBiz testIOBiz;
	
	/** Test AlbumDao. */
	protected AlbumDao albumDao;
	
	/** Test CollectionDao. */
	protected CollectionDao collectionDao;
	
	/** A MediaBiz configured for JPEG. */
	protected MediaBizImpl testJpegMediaBizImpl;
	
	private Album albumWithItems = null;
	private Collection collectionWithItems = null;
	private int counter = 0;
	
	@Override
	public boolean isPopulateProtectedVariables() {
		return true;
	}

	@Override
	protected void onSetUpInTransaction() throws Exception {
		super.onSetUpInTransaction();
		deleteFromTables(TestConstants.ALL_TABLES_FOR_CLEAR);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onTearDownAfterTransaction() throws Exception {
		if ( albumWithItems != null ) {
			// verify can access album items
			for ( MediaItem item : (List<MediaItem>)albumWithItems.getItem() ) {
				if ( item == null ) throw new RuntimeException("Item should not be null");
			}
		}
		if ( collectionWithItems != null ) {
			// verify can access collection items
			for ( MediaItem item : (List<MediaItem>)collectionWithItems.getItem() ) {
				if ( item == null ) throw new RuntimeException("Item should not be null");
			}
		}
	}
	
	/**
	 * Test able to add info for media items.
	 * 
	 * @throws Exception if an error occurs
	 */
	@SuppressWarnings("unchecked")
	public void testStoreNewMediaItemInfo() throws Exception {
		User user = registerAndConfirmUser();
		BizContext context = new TestBizContext(getContext(contextKey()), user);
		
		Collection newCollection = domainObjectFactory.newCollectionInstance();
		newCollection.setName("This is a new collection");
		newCollection.setComment("This is a new collection's comments.");
		
		Collection c = testUserBiz.newCollectionForUser(
				newCollection,user,context);
		
		// import a couple of items
		importImage("magoffin/matt/ma2/image/bee-action.jpg",c,context);
		importImage("magoffin/matt/ma2/image/dylan2.jpg",c,context);
		this.collectionWithItems = collectionDao.getCollectionWithItems(
				c.getCollectionId());
		assertNotNull(this.collectionWithItems);
		
		Long[] itemIds = new Long[2];
		itemIds[0] = ((MediaItem)this.collectionWithItems.getItem().get(0)).getItemId();
		itemIds[1] = ((MediaItem)this.collectionWithItems.getItem().get(1)).getItemId();
		
		MediaInfoCommand cmd = new MediaInfoCommand();
		cmd.setItemIds(itemIds);
		cmd.setComments("Ye haw!");
		cmd.setTags("tag1,tag2,tag3 here");
		this.testMediaBizImpl.storeMediaItemInfo(cmd, context);
		
		for ( MediaItem item : (List<MediaItem>)this.collectionWithItems.getItem() ) {
			boolean foundTags = false;
			assertEquals(cmd.getComments(), item.getDescription());
			for ( UserTag tag : (List<UserTag>)item.getUserTag() ) {
				assertEquals(cmd.getTags(), tag.getTag());
				assertEquals(user.getUserId(), tag.getTaggingUser().getUserId());
				foundTags = true;
				break;
			}
			assertTrue(foundTags);
		}
	}
	
	/**
	 * Test able to update info for media items.
	 * 
	 * @throws Exception if an error occurs
	 */
	@SuppressWarnings("unchecked")
	public void testStoreUpdatedMediaItemInfo() throws Exception {
		User user = registerAndConfirmUser();
		BizContext context = new TestBizContext(getContext(contextKey()), user);
		
		Collection newCollection = domainObjectFactory.newCollectionInstance();
		newCollection.setName("This is a new collection");
		newCollection.setComment("This is a new collection's comments.");
		
		Collection c = testUserBiz.newCollectionForUser(
				newCollection,user,context);
		
		// import a couple of items
		importImage("magoffin/matt/ma2/image/bee-action.jpg",c,context);
		importImage("magoffin/matt/ma2/image/dylan2.jpg",c,context);
		this.collectionWithItems = collectionDao.getCollectionWithItems(
				c.getCollectionId());
		assertNotNull(this.collectionWithItems);
		
		Long[] itemIds = new Long[2];
		itemIds[0] = ((MediaItem)this.collectionWithItems.getItem().get(0)).getItemId();
		itemIds[1] = ((MediaItem)this.collectionWithItems.getItem().get(1)).getItemId();
		
		MediaInfoCommand cmd = new MediaInfoCommand();
		cmd.setItemIds(itemIds);
		cmd.setComments("Ye haw!");
		cmd.setTags("tag1,tag2,tag3 here");
		this.testMediaBizImpl.storeMediaItemInfo(cmd, context);
		
		// now update to new values
		cmd.setComments("New comments");
		cmd.setTags("tag3,tag3 here");
		
		this.testMediaBizImpl.storeMediaItemInfo(cmd, context);
		this.collectionWithItems = collectionDao.getCollectionWithItems(
				c.getCollectionId());

		for ( MediaItem item : (List<MediaItem>)this.collectionWithItems.getItem() ) {
			assertEquals(cmd.getComments(), item.getDescription());
			boolean foundTags = false;
			for ( UserTag tag : (List<UserTag>)item.getUserTag() ) {
				assertEquals(cmd.getTags(), tag.getTag());
				assertEquals(user.getUserId(), tag.getTaggingUser().getUserId());
				foundTags = true;
				break;
			}
			assertTrue(foundTags);
		}
	}
	
	/**
	 * Test able to apply album sort to album with nested child albums.
	 * @throws Exception if any error occurs
	 */
	@SuppressWarnings({ "unchecked", "cast" })
	public void testStoreChildAlbumOrdering() throws Exception {
		User user = registerAndConfirmUser();
		Album album = saveNewAlbum(user);
		BizContext context = new TestBizContext(getContext(contextKey()),user);
		Album child1 = saveNewAlbum(user);
		Album child2 = saveNewAlbum(user);
		Album child3 = saveNewAlbum(user);
		album.getAlbum().add(child1);
		album.getAlbum().add(child2);
		album.getAlbum().add(child3);
		
		Collection c = testUserBiz.getCollectionsForUser(user,context).get(0);
		importImage("magoffin/matt/ma2/image/bee-action.jpg",c,context);
		importImage("magoffin/matt/ma2/image/dylan2.jpg",c,context);
		List<MediaItem> items = testMediaBizImpl.getMediaItemsForCollection(c, context);
		assertEquals(2, items.size());
		testMediaBizImpl.addMediaItemsToAlbum(
				child1,new Long[]{items.get(0).getItemId()},context);
		testMediaBizImpl.addMediaItemsToAlbum(
				child2,new Long[]{items.get(0).getItemId()},context);
		testMediaBizImpl.addMediaItemsToAlbum(
				child3,new Long[]{items.get(1).getItemId()},context);
		
		// now re-sort, so order = child3, child1, child2
		Long[] newOrder = new Long[] {
				child3.getAlbumId(), child1.getAlbumId(), child2.getAlbumId()};
		SortAlbumsCommand sortCmd = new SortAlbumsCommand();
		sortCmd.setAlbumId(album.getAlbumId());
		sortCmd.setChildAlbumIds(newOrder);
		testMediaBizImpl.storeAlbumOrdering(sortCmd, context);
		
		// now get album again, to pick up new order
		Album orderedAlbum = testMediaBizImpl.getAlbum(album.getAlbumId(), context);
		assertNotNull(orderedAlbum);
		assertNotNull(orderedAlbum.getAlbum());
		assertEquals(3, orderedAlbum.getAlbum().size());
		List<Album> children = (List<Album>)orderedAlbum.getAlbum();
		assertEquals(child3.getAlbumId(), children.get(0).getAlbumId());
		assertEquals(child1.getAlbumId(), children.get(1).getAlbumId());
		assertEquals(child2.getAlbumId(), children.get(2).getAlbumId());
	}
	
	/**
	 * Test sharing an album.
	 * @throws Exception if an error occurs
	 */
	public void testShareAlbum() throws Exception {
		User user = registerAndConfirmUser();
		Album album = saveNewAlbum(user);
		BizContext context = new TestBizContext(getContext(contextKey()),user);
		ShareAlbumCommand shareAlbumCmd = new ShareAlbumCommand();
		shareAlbumCmd.setAlbumId(album.getAlbumId());
		shareAlbumCmd.setFeed(true);
		shareAlbumCmd.setShared(true);
		String key = testMediaBizImpl.shareAlbum(shareAlbumCmd, context);
		assertNotNull(key);
		
		Album sharedAlbum = testMediaBizImpl.getSharedAlbum(key, context);
		assertNotNull(sharedAlbum);
		assertEquals(key, sharedAlbum.getAnonymousKey());
		assertTrue(sharedAlbum.isAllowAnonymous());
	}
	
	/**
	 * Test unsharing an album.
	 * @throws Exception if an error occurs
	 */
	public void testUnShareAlbum() throws Exception {
		User user = registerAndConfirmUser();
		Album album = saveNewAlbum(user);
		BizContext context = new TestBizContext(getContext(contextKey()),user);
		ShareAlbumCommand shareAlbumCmd = new ShareAlbumCommand();
		shareAlbumCmd.setAlbumId(album.getAlbumId());
		shareAlbumCmd.setFeed(true);
		shareAlbumCmd.setShared(true);
		String key = testMediaBizImpl.shareAlbum(shareAlbumCmd, context);
		Album sharedAlbum = testMediaBizImpl.getSharedAlbum(key, context);
		
		testMediaBizImpl.unShareAlbum(sharedAlbum.getAlbumId(), context);
		sharedAlbum = testMediaBizImpl.getSharedAlbum(key, context);
		assertNull(sharedAlbum);
		
		sharedAlbum = albumDao.get(album.getAlbumId());
		assertFalse(sharedAlbum.isAllowAnonymous());
		assertFalse(sharedAlbum.isAllowOriginal());
	}

	/**
	 * Test the file extension handling.
	 * @throws Exception if an error occurs
	 */
	public void testMediaHandlerFileExtensionMapping() throws Exception {
		File testFile = new File("/this/is/a/test/file.test");
		File unsupportedFile = new File("/unsupported/file.type");
		assertTrue(testMediaBizImpl.isFileSupported(testFile));
		assertFalse(testMediaBizImpl.isFileSupported(unsupportedFile));
		assertNotNull(testMediaBizImpl.getMediaHandler(testFile));
		assertNull(testMediaBizImpl.getMediaHandler(unsupportedFile));
	}
	
	/**
	 * Test the MIME handling.
	 * @throws Exception if an error occurs
	 */
	public void testMediaHandlerMimeMapping() throws Exception {
		String testMime = "test/test";
		String unsupportedMime = "unsupported/mime";
		assertNotNull(testMediaBizImpl.getMediaHandler(testMime));
		assertNull(testMediaBizImpl.getMediaHandler(unsupportedMime));
	}
	
	/**
	 * Test all MediaSize constants have a mapped Geometry.
	 * @throws Exception if an error occurs
	 */
	public void testGeometryMapping() throws Exception {
		EnumSet<MediaSize> set = EnumSet.allOf(MediaSize.class);
		for ( MediaSize size : set ) {
			Geometry g = testMediaBizImpl.getGeometry(size);
			assertNotNull("The geometry for size [" +size +"] must not be null", g);
			if ( logger.isDebugEnabled() ) {
				logger.debug("Size " +size +" geometry set to [" +g +"]");
			}
		}
		
		// now change the EnumMap to make sure that works
		EnumMap<MediaSize,Geometry> gMap = new EnumMap<MediaSize,Geometry>(MediaSize.class);
		gMap.put(MediaSize.NORMAL,new Geometry(64,32));
		testMediaBizImpl.setGeometryMap(gMap);
		Geometry g = testMediaBizImpl.getGeometry(MediaSize.NORMAL);
		assertEquals(64,g.getWidth());
		assertEquals(32,g.getHeight());
	}
	
	/**
	 * Test all MediaQuality constants have a mapped Float.
	 * @throws Exception if an error occurs
	 */
	public void testQualityMapping() throws Exception {
		EnumSet<MediaQuality> set = EnumSet.allOf(MediaQuality.class);
		for ( MediaQuality q : set ) {
			float qv = testMediaBizImpl.getQualityValue(q);
			assertTrue("The quality value for quality [" +q +"] must be > 0", qv > 0f);
			assertTrue("The quality value for quality [" +q +"] must be <= 1", qv <= 1.0f);
			if ( logger.isDebugEnabled() ) {
				logger.debug("Quality " +q +" quality value set to [" +qv +"]");
			}
		}

		// now change the EnumMap to make sure that works
		float testQ = 0.3333f;
		EnumMap<MediaQuality,Float> qMap = new EnumMap<MediaQuality,Float>(MediaQuality.class);
		qMap.put(MediaQuality.GOOD,testQ);
		testMediaBizImpl.setQualityMap(qMap);
		float qv = testMediaBizImpl.getQualityValue(MediaQuality.GOOD);
		assertEquals(testQ,qv);
	}
	
	/**
	 * Test getting media items for a Collection.
	 * @throws Exception if an error occurs
	 */
	public void testGetMediaItemsForCollection() throws Exception {
		User user = registerAndConfirmUser();
		BizContext context = new TestBizContext(getContext(contextKey()),null);
		List<Collection> collections = testUserBiz.getCollectionsForUser(user,context);
		List<MediaItem> items = testMediaBizImpl.getMediaItemsForCollection(
				collections.get(0), context);
		assertNotNull(items);
		assertEquals(0,items.size());
		
		// now add an item to collection and verify query still works
		importImage("magoffin/matt/ma2/image/bee-action.jpg",collections.get(0),
				context);
		
		items = testMediaBizImpl.getMediaItemsForCollection(collections.get(0), 
				context);
		assertNotNull(items);
		assertEquals(1,items.size());
		for ( MediaItem item : items ) {
			// really just to verify it's a MediaItem
			logger.debug("Got MediaItem: " +item.getItemId());
		}
	}
	
	/**
	 * Test adding an item to an Album.
	 * @throws Exception if an error occurs
	 */
	public void testAddToAlbum() throws Exception {
		User user = registerAndConfirmUser();
		BizContext context = new TestBizContext(getContext(contextKey()),null);
		List<Collection> collections = testUserBiz.getCollectionsForUser(user,context);
		importImage("magoffin/matt/ma2/image/bee-action.jpg",collections.get(0),context);
		List<MediaItem> items = testMediaBizImpl.getMediaItemsForCollection(
				collections.get(0), context);
		Album album = saveNewAlbum(user);
		testMediaBizImpl.addMediaItemsToAlbum(album,new Long[]{items.get(0).getItemId()},context);
		
		// verify item in the album now
		album = albumDao.get(album.getAlbumId());
		assertEquals(1,album.getItem().size());
	}

	/**
	 * Test getting an Album with items populated.
	 * 
	 * @throws Exception if an error occurs
	 * @see #onTearDownAfterTransaction()
	 */
	public void testGetAlbumWithItems() throws Exception {
		User user = registerAndConfirmUser();
		BizContext context = new TestBizContext(getContext(contextKey()),null);
		List<Collection> collections = testUserBiz.getCollectionsForUser(user,context);
		importImage("magoffin/matt/ma2/image/bee-action.jpg",collections.get(0),context);
		List<MediaItem> items = testMediaBizImpl.getMediaItemsForCollection(
				collections.get(0), context);
		Album album = saveNewAlbum(user);
		testMediaBizImpl.addMediaItemsToAlbum(album,new Long[]{items.get(0).getItemId()},context);
		
		// verify item in the album now
		this.albumWithItems = albumDao.getAlbumWithItems(album.getAlbumId());
		assertNotNull(this.albumWithItems);
	}

	/**
	 * Test getting a Collection with items populated.
	 * 
	 * @throws Exception if an error occurs
	 * @see #onTearDownAfterTransaction()
	 */
	public void testGetCollectionWithItems() throws Exception {
		User user = registerAndConfirmUser();
		BizContext context = new TestBizContext(getContext(contextKey()),null);

		Collection newCollection = domainObjectFactory.newCollectionInstance();
		newCollection.setName("This is a new collection");
		newCollection.setComment("This is a new collection's comments.");
		Collection c = testUserBiz.newCollectionForUser(
				newCollection,user,context);
		importImage("magoffin/matt/ma2/image/bee-action.jpg",c,context);
		
		// verify item in the collection now
		this.collectionWithItems = collectionDao.getCollectionWithItems(
				c.getCollectionId());
		assertNotNull(this.collectionWithItems);
	}

	/**
	 * Test deleting a collection that does not have any items in it.
	 * 
	 * @throws Exception if an error occurs
	 */
	public void testDeleteCollectionWithoutItems() throws Exception {
		BizContext context = new TestBizContext(getContext(contextKey()),null);
		User user = registerAndConfirmUser();
		
		Collection newCollection = domainObjectFactory.newCollectionInstance();
		newCollection.setName("This is a new collection");
		newCollection.setComment("This is a new collection's comments.");
		
		Collection c = testUserBiz.newCollectionForUser(
				newCollection,user,context);
		
		// now delete the collection 
		int numDeleted = testMediaBizImpl.deleteCollectionAndItems(
				c.getCollectionId(), context).size();
		assertEquals(0, numDeleted);
	}

	/**
	 * Test deleting a collection that has some items in it.
	 * 
	 * @throws Exception if an error occurs
	 */
	public void testDeleteCollectionWithItems() throws Exception {
		BizContext context = new TestBizContext(getContext(contextKey()),null);
		User user = registerAndConfirmUser();
		
		Collection newCollection = domainObjectFactory.newCollectionInstance();
		newCollection.setName("This is a new collection");
		newCollection.setComment("This is a new collection's comments.");
		
		Collection c = testUserBiz.newCollectionForUser(
				newCollection,user,context);
		
		// import an item
		importImage("magoffin/matt/ma2/image/bee-action.jpg",c,context);
		
		// now delete the collection 
		int numDeleted = testMediaBizImpl.deleteCollectionAndItems(
				c.getCollectionId(), context).size();
		assertEquals(1, numDeleted);
	}
	
	/**
	 * Test moving items from one collection to another.
	 * @throws Exception if an error occurs
	 */
	@SuppressWarnings("unchecked")
	public void testMoveItemsToNewCollection() throws Exception {
		BizContext context = new TestBizContext(getContext(contextKey()),null);
		User user = registerAndConfirmUser();
		
		Collection newCollection = domainObjectFactory.newCollectionInstance();
		newCollection.setName("This is a new collection");
		newCollection.setComment("This is a new collection's comments.");
		
		Collection c = testUserBiz.newCollectionForUser(
				newCollection,user,context);
		
		// import an item
		importImage("magoffin/matt/ma2/image/bee-action.jpg", c, context);
		importImage("magoffin/matt/ma2/image/IMG_896.JPG", c, context);
		c = collectionDao.get(c.getCollectionId());
		
		newCollection = domainObjectFactory.newCollectionInstance();
		newCollection.setName("This is the second collection");
		newCollection.setComment("This is the second collection's comments.");
		Collection c2 = testUserBiz.newCollectionForUser(
				newCollection, user, context);
		
		List<MediaItem> cItems = c.getItem();
		assertNotNull(cItems);
		assertEquals(2, cItems.size());
		Long[] itemIds = new Long[] {
				cItems.get(0).getItemId(),
				cItems.get(1).getItemId(),
		};
		MoveItemsCommand cmd = new MoveItemsCommand(c2.getCollectionId(), itemIds);
		testMediaBizImpl.moveMediaItems(cmd, context);
		c = collectionDao.get(c.getCollectionId());
		c2 = collectionDao.get(c2.getCollectionId());
		assertEquals(0, c.getItem().size());
		assertEquals(2, c2.getItem().size());
	}

	/**
	 * Test deleting a collection that has some items in it tjat 
	 * are also in an album.
	 * 
	 * @throws Exception if an error occurs
	 */
	public void testDeleteCollectionWithItemsInAlbum() throws Exception {
		BizContext context = new TestBizContext(getContext(contextKey()),null);
		User user = registerAndConfirmUser();
		
		Collection newCollection = domainObjectFactory.newCollectionInstance();
		newCollection.setName("This is a new collection");
		newCollection.setComment("This is a new collection's comments.");
		
		Collection c = testUserBiz.newCollectionForUser(
				newCollection,user,context);
		
		// import an item
		importImage("magoffin/matt/ma2/image/bee-action.jpg",c,context);
		
		// now create an album
		Album album = saveNewAlbum(user);	
		
		// add the item from the collection into the album
		List<MediaItem> collectionItems = testMediaBizImpl.getMediaItemsForCollection(
				c, context);
		Long[] itemIds = new Long[collectionItems.size()];
		for ( int i = 0; i < itemIds.length; i++ ) {
			itemIds[i] = collectionItems.get(i).getItemId();
		}
		testMediaBizImpl.addMediaItemsToAlbum(album, itemIds, context);

		// now delete the collection 
		int numDeleted = testMediaBizImpl.deleteCollectionAndItems(
				c.getCollectionId(), context).size();
		assertEquals(1, numDeleted);
		
		// and verify can't find collection anymore
		Collection notFound = collectionDao.get(c.getCollectionId());
		assertNull(notFound);
	}
	
	/**
	 * Test deleting a set of items that are also in albums.
	 * 
	 * @throws Exception if an error occurs
	 */
	@SuppressWarnings("unchecked")
	public void testDeleteItemsWithItemsInAlbum() throws Exception {
		BizContext context = new TestBizContext(getContext(contextKey()),null);
		User user = registerAndConfirmUser();
		
		Collection newCollection = domainObjectFactory.newCollectionInstance();
		newCollection.setName("This is a new collection");
		newCollection.setComment("This is a new collection's comments.");
		
		Collection c = testUserBiz.newCollectionForUser(
				newCollection,user,context);
		
		// import an item
		importImage("magoffin/matt/ma2/image/bee-action.jpg",c,context);
		importImage("magoffin/matt/ma2/image/dylan2.jpg",c,context);
		
		// now create an album
		Album album = saveNewAlbum(user);	
		
		// add the item from the collection into the album
		List<MediaItem> collectionItems = testMediaBizImpl.getMediaItemsForCollection(
				c, context);
		Long[] itemIds = new Long[collectionItems.size()];
		for ( int i = 0; i < itemIds.length; i++ ) {
			itemIds[i] = collectionItems.get(i).getItemId();
		}
		testMediaBizImpl.addMediaItemsToAlbum(album, itemIds, context);

		int numDeleted = testMediaBizImpl.deleteMediaItems(itemIds, context);
		assertEquals(2, numDeleted);
		
		// and verify not in album any more
		album = albumDao.get(album.getAlbumId());
		for ( MediaItem item : (List<MediaItem>)album.getItem() ) {
			for ( Long mediaId : itemIds ) {
				assertNotSame("Item should not be in album anymore", mediaId, item.getItemId());
			}
		}
		
		// and verify not in collection any more
		c = collectionDao.get(c.getCollectionId());
		for ( MediaItem item : (List<MediaItem>)c.getItem() ) {
			for ( Long mediaId : itemIds ) {
				assertNotSame("Item should not be in collection anymore", mediaId, 
						item.getItemId());
			}
		}
		
		// import another item
		importImage("magoffin/matt/ma2/image/bee-action.jpg",c,context);
		c = collectionDao.get(c.getCollectionId());
		assertEquals(1, c.getItem().size());
	}
	
	/**
	 * Test removing media items from an album.
	 * @throws Exception if an error occurs
	 */
	public void testRemoveFromAlbum() throws Exception {
		BizContext context = new TestBizContext(getContext(contextKey()),null);
		User user = registerAndConfirmUser();
		
		Collection newCollection = domainObjectFactory.newCollectionInstance();
		newCollection.setName("This is a new collection");
		newCollection.setComment("This is a new collection's comments.");
		
		Collection c = testUserBiz.newCollectionForUser(
				newCollection,user,context);
		
		// import some items
		importImage("magoffin/matt/ma2/image/bee-action.jpg",c,context);
		importImage("magoffin/matt/ma2/image/dylan2.jpg",c,context);
		
		// now create an album
		Album album = saveNewAlbum(user);	
		
		// add the items from the collection into the album
		List<MediaItem> collectionItems = testMediaBizImpl.getMediaItemsForCollection(
				c, context);
		Long[] itemIds = new Long[collectionItems.size()];
		for ( int i = 0; i < itemIds.length; i++ ) {
			itemIds[i] = collectionItems.get(i).getItemId();
		}
		testMediaBizImpl.addMediaItemsToAlbum(album, itemIds, context);
		
		album = albumDao.get(album.getAlbumId());
		
		assertEquals(2, album.getItem().size());
		
		// now delete one from the album
		int numRemoved = testMediaBizImpl.removeMediaItemsFromAlbum(album.getAlbumId(), 
				new Long[] {itemIds[0]}, context);
		assertEquals(1, numRemoved);
		
		// now get the album again and verify only 1 item
		album = albumDao.get(album.getAlbumId());
		assertEquals(1, album.getItem().size());
		
		// now delete a false item
		numRemoved = testMediaBizImpl.removeMediaItemsFromAlbum(album.getAlbumId(), 
				new Long[] {-1L, -2L, -3L}, context);
		assertEquals(0, numRemoved);
		
		// now get album and verify still 1 item
		album = albumDao.get(album.getAlbumId());
		assertEquals(1, album.getItem().size());

		// now delete final item
		numRemoved = testMediaBizImpl.removeMediaItemsFromAlbum(album.getAlbumId(), 
				itemIds, context);
		assertEquals(1, numRemoved);
		
		// now get album and verify 0 items
		album = albumDao.get(album.getAlbumId());
		assertEquals(0, album.getItem().size());
	}

	/**
	 * Get getting a single item with full info.
	 * @throws Exception if an error occurs
	 */
	public void testGetMediaItemWithInfo() throws Exception {
		BizContext context = new TestBizContext(getContext(contextKey()),null);
		User user = registerAndConfirmUser();
		
		Collection newCollection = domainObjectFactory.newCollectionInstance();
		newCollection.setName("This is a new collection");
		newCollection.setComment("This is a new collection's comments.");
		
		Collection c = testUserBiz.newCollectionForUser(
				newCollection,user,context);
		
		// import an item (with metadata)
		importImage("magoffin/matt/ma2/image/bee-action.jpg",c,context);
		
		List<MediaItem> collectionItems = testMediaBizImpl.getMediaItemsForCollection(
				c, context);
		Long itemId = collectionItems.get(0).getItemId();
		
		MediaItem item = testMediaBizImpl.getMediaItemWithInfo(itemId, context);
		assertNotNull(item);
		assertTrue(item.getMetadata().size() > 0);
	}
	
	/**
	 * Test storing media item ratings.
	 * @throws Exception if an error occurs
	 */
	public void testStoreMediaItemRating() throws Exception {
		User user = registerAndConfirmUser();
		BizContext context = new TestBizContext(getContext(contextKey()),user);
		
		Collection newCollection = domainObjectFactory.newCollectionInstance();
		newCollection.setName("This is a new collection");
		newCollection.setComment("This is a new collection's comments.");
		
		Collection c = testUserBiz.newCollectionForUser(
				newCollection,user,context);
		
		// import an item (with metadata)
		importImage("magoffin/matt/ma2/image/bee-action.jpg",c,context);
		
		List<MediaItem> collectionItems = testMediaBizImpl.getMediaItemsForCollection(
				c, context);
		Long itemId = collectionItems.get(0).getItemId();
		Long[] itemIds = new Long[] {itemId};
		
		// create first rating
		testMediaBizImpl.storeMediaItemRating(itemIds, (short)2, context);
		
		// verify rating available
		MediaItem item = testMediaBizImpl.getMediaItemWithInfo(itemId, context);
		assertNotNull(item.getUserRating());
		assertEquals(1, item.getUserRating().size());
		MediaItemRating rating = (MediaItemRating)item.getUserRating().get(0);
		assertEquals(2, rating.getRating());
		assertEquals(user.getUserId(), rating.getRatingUser().getUserId());
		
		// verify "adding" rating by same user resets rating
		testMediaBizImpl.storeMediaItemRating(itemIds, (short)3, context);
		item = testMediaBizImpl.getMediaItemWithInfo(itemId, context);
		assertNotNull(item.getUserRating());
		assertEquals(1, item.getUserRating().size());
		rating = (MediaItemRating)item.getUserRating().get(0);
		assertEquals(3, rating.getRating());
		assertEquals(user.getUserId(), rating.getRatingUser().getUserId());
		
		// test setting two ratings at once
		importImage("magoffin/matt/ma2/image/dylan2.jpg",c,context);
		collectionItems = testMediaBizImpl.getMediaItemsForCollection(c, context);
		Long[] twoItemIds = new Long[] {itemId, collectionItems.get(0).getItemId()};
		testMediaBizImpl.storeMediaItemRating(twoItemIds, (short)10, context);
		item = testMediaBizImpl.getMediaItemWithInfo(twoItemIds[0], context);
		assertNotNull(item.getUserRating());
		assertEquals(1, item.getUserRating().size());
		rating = (MediaItemRating)item.getUserRating().get(0);
		assertEquals(10, rating.getRating());
		assertEquals(user.getUserId(), rating.getRatingUser().getUserId());
		item = testMediaBizImpl.getMediaItemWithInfo(twoItemIds[1], context);
		assertNotNull(item.getUserRating());
		assertEquals(1, item.getUserRating().size());
		rating = (MediaItemRating)item.getUserRating().get(0);
		assertEquals(10, rating.getRating());
		assertEquals(user.getUserId(), rating.getRatingUser().getUserId());
		
		// now store second user's rating
		User user2 = registerAndConfirmUser("test2","nobody2@localhost");
		BizContext context2 = new TestBizContext(getContext(contextKey()),user2);
		testMediaBizImpl.storeMediaItemRating(itemIds, (short)4, context2);
		item = testMediaBizImpl.getMediaItemWithInfo(itemId, context);
		assertNotNull(item.getUserRating());
		assertEquals(2, item.getUserRating().size());
		rating = (MediaItemRating)item.getUserRating().get(1);
		assertEquals(4, rating.getRating());
		assertEquals(user2.getUserId(), rating.getRatingUser().getUserId());
	}
	
	/**
	 * Test able to increment hit counter.
	 * @throws Exception if an error occurs
	 */
	public void testIncrementHitCounter() throws Exception {
		User user = registerAndConfirmUser();
		BizContext context = new TestBizContext(getContext(contextKey()),user);
		
		Collection newCollection = domainObjectFactory.newCollectionInstance();
		newCollection.setName("This is a new collection");
		newCollection.setComment("This is a new collection's comments.");
		
		Collection c = testUserBiz.newCollectionForUser(
				newCollection,user,context);
		
		// import an item (with metadata)
		importImage("magoffin/matt/ma2/image/bee-action.jpg",c,context);
		
		List<MediaItem> collectionItems = testMediaBizImpl.getMediaItemsForCollection(
				c, context);
		MediaItem item = collectionItems.get(0);
		Long itemId = item.getItemId();

		assertEquals(0, item.getHits());
		
		int hits = testMediaBizImpl.incrementMediaItemHits(itemId);
		assertEquals(1, hits);
		
		item = testMediaBizImpl.getMediaItemWithInfo(itemId, context);
		assertNotNull(item);
		assertEquals(1, item.getHits());
	}
	
	/**
	 * Test able to get the list of sort modes.
	 * @throws Exception if an error occurs
	 */
	public void testGetAlbumSortModes() throws Exception {
		User user = registerAndConfirmUser();
		BizContext context = new TestBizContext(getContext(contextKey()),user);
		List<KeyNameType> sortModes = testMediaBizImpl.getAlbumSortTypes(context);
		assertNotNull(sortModes);
		assertTrue(sortModes.size() > 0);
	}
	
	/**
	 * Test able to store an anonymous user comment.
	 * @throws Exception if an error occurs
	 */
	public void testStoreAnonymousUserComment() throws Exception {
		User user = registerAndConfirmUser();
		BizContext context = new TestBizContext(getContext(contextKey()),user);

		Collection newCollection = domainObjectFactory.newCollectionInstance();
		newCollection.setName("This is a new collection");
		newCollection.setComment("This is a new collection's comments.");
		
		Collection c = testUserBiz.newCollectionForUser(
				newCollection,user,context);
		
		// import an item (with metadata)
		importImage("magoffin/matt/ma2/image/bee-action.jpg",c,context);
		
		List<MediaItem> collectionItems = testMediaBizImpl.getMediaItemsForCollection(
				c, context);
		Long itemId = collectionItems.get(0).getItemId();
		
		// now store some comments anonymously, without any name/email
		BizContext anonContext = new TestBizContext(getContext(contextKey()), 
				testUserBiz.getAnonymousUser());
		UserCommentCommand cmd = new UserCommentCommand();
		cmd.setComment("This is a test comment.");
		cmd.setItemId(itemId);
		testMediaBizImpl.storeMediaItemUserComment(cmd, anonContext);
		
		// now get the item, test has comment
		MediaItem item = testMediaBizImpl.getMediaItemWithInfo(itemId, context);
		assertNotNull(item.getUserComment());
		assertEquals(1, item.getUserComment().size());
		UserComment comment = (UserComment)item.getUserComment().get(0);
		assertNull(comment.getCommenter());
		assertNull(comment.getCommentingUser());
		assertEquals(cmd.getComment(), comment.getComment());
		
		// now add another comment, with a name set
		cmd.setName("Test Name");
		testMediaBizImpl.storeMediaItemUserComment(cmd, anonContext);
		item = testMediaBizImpl.getMediaItemWithInfo(itemId, context);
		assertNotNull(item.getUserComment());
		assertEquals(2, item.getUserComment().size());
		comment = (UserComment)item.getUserComment().get(1);
		assertNull(comment.getCommentingUser());
		assertEquals(cmd.getComment(), comment.getComment());
		assertEquals(cmd.getName(), comment.getCommenter());
		
		// finally add another comment, with name + email set
		cmd.setEmail("test@email");
		testMediaBizImpl.storeMediaItemUserComment(cmd, anonContext);
		item = testMediaBizImpl.getMediaItemWithInfo(itemId, context);
		assertNotNull(item.getUserComment());
		assertEquals(3, item.getUserComment().size());
		comment = (UserComment)item.getUserComment().get(2);
		assertNull(comment.getCommentingUser());
		assertEquals(cmd.getComment(), comment.getComment());
		assertNotNull(comment.getCommenter());
		assertTrue(comment.getCommenter().contains(cmd.getName()));
		assertTrue(comment.getCommenter().contains(cmd.getEmail()));
	}
	
	/*
	 * Test able to format an item's date correct.
	 * @throws Exception if an error occurs
	 *
	public void testFormatItemDate() throws Exception {
		User user = registerAndConfirmUser();
		BizContext context = new TestBizContext(getContext(contextKey()),user);
		
		Collection newCollection = domainObjectFactory.newCollectionInstance();
		newCollection.setName("This is a new collection");
		newCollection.setComment("This is a new collection's comments.");
		
		Collection c = testUserBiz.newCollectionForUser(
				newCollection,user,context);
		
		// import an item (with metadata)
		AddMediaCommand cmd = new AddMediaCommand();
		cmd.setMediaTz("Etc/GMT+8");
		cmd.setLocalTz("Pacific/Honolulu");
		importImage("magoffin/matt/ma2/image/IMG_896.JPG", c, context, cmd);
		
		cmd.setMediaTz("Etc/GMT+8");
		cmd.setLocalTz("America/Los_Angeles");
		importImage("magoffin/matt/ma2/image/IMG_4215.jpg", c, context, cmd);
		
		List<MediaItem> collectionItems = testMediaBizImpl.getMediaItemsForCollection(
				c, context);
		MediaItem item1 = collectionItems.get(0);
		MediaItem item2 = collectionItems.get(1);
		
		assertEquals("Etc/GMT+8", item1.getTz().getCode());
		assertEquals("Pacific/Honolulu", item1.getTzDisplay().getCode());

		assertEquals("Etc/GMT+8", item2.getTz().getCode());
		assertEquals("America/Los_Angeles", item2.getTzDisplay().getCode());
		
		SimpleThreadSafeDateFormat sdf = new SimpleThreadSafeDateFormat(
				"yyyy-MM-dd HH:mm:ss zzz");
		logger.debug("Input = " +sdf.format(item1.getItemDate(), 
				TimeZone.getTimeZone(item1.getTz().getCode()))
				+", " +sdf.format(item2.getItemDate(), 
				TimeZone.getTimeZone(item2.getTz().getCode())));
		
		
		String result = testMediaBizImpl.formatMediaItemDate(item1, sdf);
		String result1 = testMediaBizImpl.formatMediaItemDate(item2, sdf);
		
		logger.debug("Result = " +result +", " +result1);
		assertEquals("2003-11-09 07:52:05 HST", result);
		assertEquals("2005-04-13 14:54:40 PDT", result1);
	}*/
	
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
		BizContext context = new TestBizContext(getContext(contextKey()),null);		
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
		return newUser;
	}
	
}
