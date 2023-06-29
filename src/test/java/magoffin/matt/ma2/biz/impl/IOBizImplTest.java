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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.FileCopyUtils;

import magoffin.matt.ma2.AbstractSpringEnabledTransactionalTest;
import magoffin.matt.ma2.MediaQuality;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.MediaResponse;
import magoffin.matt.ma2.MediaSize;
import magoffin.matt.ma2.ProcessingException;
import magoffin.matt.ma2.TestConstants;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.biz.IOBiz.TwoPhaseExportRequest;
import magoffin.matt.ma2.biz.SystemBiz;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.biz.WorkBiz;
import magoffin.matt.ma2.biz.WorkBiz.WorkInfo;
import magoffin.matt.ma2.dao.AlbumDao;
import magoffin.matt.ma2.dao.CollectionDao;
import magoffin.matt.ma2.dao.TimeZoneDao;
import magoffin.matt.ma2.domain.Album;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.support.AddMediaCommand;
import magoffin.matt.ma2.support.BasicMediaRequest;
import magoffin.matt.ma2.support.BasicMediaResponse;
import magoffin.matt.ma2.support.ExportItemsCommand;
import magoffin.matt.util.TemporaryFile;

/**
 * Test the {@link magoffin.matt.ma2.biz.impl.IOBizImpl} class.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
@ContextConfiguration
public class IOBizImplTest extends AbstractSpringEnabledTransactionalTest {

	@javax.annotation.Resource
	private IOBizImpl testIOBizImpl;
	@javax.annotation.Resource
	private WorkBiz testWorkBiz;
	@javax.annotation.Resource
	private UserBiz testUserBiz;
	@javax.annotation.Resource
	private SystemBiz testSystemBiz;
	@javax.annotation.Resource
	private CollectionDao collectionDao;
	@javax.annotation.Resource
	private AlbumDao albumDao;
	@javax.annotation.Resource
	private DomainObjectFactory domainObjectFactory;
	@javax.annotation.Resource
	private TimeZoneDao timeZoneDao;

	private User testUser;
	private Collection testCollection;

	@Before
	@Override
	public void onSetUpInTransaction() {
		super.onSetUpInTransaction();
		deleteFromTables(TestConstants.ALL_TABLES_FOR_CLEAR);

		User newUser = domainObjectFactory.newUserInstance();
		newUser.setEmail("nobody@localhost");
		newUser.setName("Test User");
		newUser.setPassword("test");
		newUser.setLogin("nobody");
		newUser.setTz(timeZoneDao.get("UTC"));

		BizContext context = new TestBizContext(applicationContext, null);
		String confKey = null;
		try {
			confKey = testUserBiz.registerUser(newUser, context);
		} catch (ProcessingException e) {
			// whatever!
			confKey = (String) e.getProcessResult();
		}
		this.testUser = testUserBiz.confirmRegisteredUser(newUser.getLogin(), confKey, context);

		List<Collection> collections = testUserBiz.getCollectionsForUser(this.testUser, context);
		this.testCollection = collections.get(0);
	}

	/**
	 * Test importing a single JPEG image.
	 * 
	 * @throws Exception if an error occurs
	 */
	@Test
	public void testImportSingleJpeg() throws Exception {
		importImage("magoffin/matt/ma2/image/bee-action.jpg", collectionDao, testIOBizImpl, testCollection, testUser);
	}

	/**
	 * Test importing a Zip of JPEG images.
	 * 
	 * @throws Exception if an error occurs
	 */
	@Test
	public void testImportZip() throws Exception {
		File tempZipFile = File.createTempFile("IOBizImplTest-", ".zip");
		ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(tempZipFile));

		Resource testJpegImage = new ClassPathResource("magoffin/matt/ma2/image/bee-action.jpg");
		ZipEntry entry = new ZipEntry("bee-action.jpg");
		zout.putNextEntry(entry);
		addToZip(testJpegImage, zout);

		testJpegImage = new ClassPathResource("magoffin/matt/ma2/image/IMG_4215.jpg");
		entry = new ZipEntry("IMG_4215.jpg");
		zout.putNextEntry(entry);
		addToZip(testJpegImage, zout);

		zout.closeEntry();
		zout.finish();
		zout.close();

		AddMediaCommand addCmd = new AddMediaCommand();
		addCmd.setAutoAlbum(false);
		addCmd.setCollectionId(this.testCollection.getCollectionId());
		final Resource testZip = new FileSystemResource(tempZipFile);
		addCmd.setTempFile(new TemporaryFile() {

			@Override
			public InputStream getInputStream() throws IOException {
				return testZip.getInputStream();
			}

			@Override
			public String getName() {
				return testZip.getFilename();
			}

			@Override
			public String getContentType() {
				return "application/zip";
			}

			@Override
			public long getSize() {
				try {
					return testZip.getFile().length();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});

		BizContext context = new TestBizContext(applicationContext, testUser);
		WorkInfo info = testIOBizImpl.importMedia(addCmd, context);

		assertNotNull("Returned WorkInfo must not be null", info);

		// wait at most 10 minutes for job to complete
		info.get(600, TimeUnit.SECONDS);
		assertTrue(info.isDone());
		assertNull(info.getException());
	}

	/**
	 * Test importing a Zip of JPEG images with AutoAlbum mode enabled
	 * 
	 * @throws Exception if an error occurs
	 */
	@Test
	public void testImportZipAutoAlbum() throws Exception {
		File tempZipFile = File.createTempFile("IOBizImplTest-", ".zip");
		ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(tempZipFile));

		Resource testJpegImage = new ClassPathResource("magoffin/matt/ma2/image/bee-action.jpg");
		ZipEntry entry = new ZipEntry("My Test Album/bee-action.jpg");
		zout.putNextEntry(entry);
		addToZip(testJpegImage, zout);

		testJpegImage = new ClassPathResource("magoffin/matt/ma2/image/IMG_4215.jpg");
		entry = new ZipEntry("My Test Album/IMG_4215.jpg");
		zout.putNextEntry(entry);
		addToZip(testJpegImage, zout);

		zout.closeEntry();
		zout.finish();
		zout.close();

		AddMediaCommand addCmd = new AddMediaCommand();
		addCmd.setAutoAlbum(true);
		addCmd.setCollectionId(this.testCollection.getCollectionId());
		final Resource testZip = new FileSystemResource(tempZipFile);
		addCmd.setTempFile(new TemporaryFile() {

			@Override
			public InputStream getInputStream() throws IOException {
				return testZip.getInputStream();
			}

			@Override
			public String getName() {
				return testZip.getFilename();
			}

			@Override
			public String getContentType() {
				return "application/zip";
			}

			@Override
			public long getSize() {
				try {
					return testZip.getFile().length();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});

		BizContext context = new TestBizContext(applicationContext, testUser);
		WorkInfo info = testIOBizImpl.importMedia(addCmd, context);

		assertNotNull("Returned WorkInfo must not be null", info);

		// wait at most 10 minutes for job to complete
		info.get(600, TimeUnit.SECONDS);
		assertTrue(info.isDone());
		assertNull(info.getException());

		// get user albums, items should be in there
		List<Album> albums = albumDao.findAlbumsForUser(testUser.getUserId());
		assertNotNull(albums);
		assertEquals(1, albums.size());
		Album testAlbum = albums.get(0);
		assertEquals("My Test Album", testAlbum.getName());
		assertEquals(2, testAlbum.getItem().size());
	}

	/**
	 * Test importing a Zip of JPEG images with AutoAlbum mode enabled
	 * 
	 * @throws Exception if an error occurs
	 */
	@Test
	public void testImportZipAutoAlbumNestedAlbums() throws Exception {
		File tempZipFile = File.createTempFile("IOBizImplTest-", ".zip");
		ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(tempZipFile));

		Resource testJpegImage = new ClassPathResource("magoffin/matt/ma2/image/bee-action.jpg");
		ZipEntry entry = new ZipEntry("My Test Album/bee-action.jpg");
		zout.putNextEntry(entry);
		addToZip(testJpegImage, zout);

		testJpegImage = new ClassPathResource("magoffin/matt/ma2/image/IMG_4215.jpg");
		entry = new ZipEntry("My Test Album/My Nested Album/IMG_4215.jpg");
		zout.putNextEntry(entry);
		addToZip(testJpegImage, zout);

		testJpegImage = new ClassPathResource("magoffin/matt/ma2/image/dylan2.jpg");
		entry = new ZipEntry("My Test Album/My Nested Album/dylan2.jpg");
		zout.putNextEntry(entry);
		addToZip(testJpegImage, zout);

		testJpegImage = new ClassPathResource("magoffin/matt/ma2/image/IMG_879.jpg");
		entry = new ZipEntry("IMG_879.jpg");
		zout.putNextEntry(entry);
		addToZip(testJpegImage, zout);

		zout.closeEntry();
		zout.finish();
		zout.close();

		AddMediaCommand addCmd = new AddMediaCommand();
		addCmd.setAutoAlbum(true);
		addCmd.setCollectionId(this.testCollection.getCollectionId());
		final Resource testZip = new FileSystemResource(tempZipFile);
		addCmd.setTempFile(new TemporaryFile() {

			@Override
			public InputStream getInputStream() throws IOException {
				return testZip.getInputStream();
			}

			@Override
			public String getName() {
				return testZip.getFilename();
			}

			@Override
			public String getContentType() {
				return "application/zip";
			}

			@Override
			public long getSize() {
				try {
					return testZip.getFile().length();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});

		BizContext context = new TestBizContext(applicationContext, testUser);
		WorkInfo info = testIOBizImpl.importMedia(addCmd, context);

		assertNotNull("Returned WorkInfo must not be null", info);

		// wait at most 10 minutes for job to complete
		info.get(600, TimeUnit.SECONDS);
		assertTrue(info.isDone());
		assertNull(info.getException());

		// get user albums, items should be in there
		List<Album> albums = albumDao.findAlbumsForUser(testUser.getUserId());
		assertNotNull(albums);
		assertEquals(1, albums.size());
		Album testAlbum = albums.get(0);
		assertEquals("My Test Album", testAlbum.getName());
		assertEquals(1, testAlbum.getItem().size());

		// get nested album
		assertEquals(1, testAlbum.getAlbum().size());
		Album testNestedAlbum = (Album) testAlbum.getAlbum().get(0);
		assertEquals("My Nested Album", testNestedAlbum.getName());
		assertEquals(2, testNestedAlbum.getItem().size());

		// verify collection has 4 images, for top-level import item
		Collection c = this.collectionDao.get(this.testCollection.getCollectionId());
		assertEquals(4, c.getItem().size());
	}

	/**
	 * Test importing a Zip of JPEG images with an album XML metadata file.
	 * 
	 * @throws Exception if an error occurs
	 */
	@Test
	public void testImportZipWithAlbumXml() throws Exception {
		File tempZipFile = File.createTempFile("IOBizImplTest-", ".zip");
		ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(tempZipFile));

		Resource importXml = new ClassPathResource("magoffin/matt/ma2/biz/impl/import-test.xml");
		ZipEntry entry = new ZipEntry("metadata.xml");
		zout.putNextEntry(entry);
		addToZip(importXml, zout);

		Resource testJpegImage = new ClassPathResource("magoffin/matt/ma2/image/bee-action.jpg");
		entry = new ZipEntry("My Test Import With XML Album/bee-action.jpg");
		zout.putNextEntry(entry);
		addToZip(testJpegImage, zout);

		testJpegImage = new ClassPathResource("magoffin/matt/ma2/image/IMG_4215.jpg");
		entry = new ZipEntry("My Test Import With XML Album/IMG_4215.jpg");
		zout.putNextEntry(entry);
		addToZip(testJpegImage, zout);

		zout.closeEntry();
		zout.finish();
		zout.close();

		AddMediaCommand addCmd = new AddMediaCommand();
		addCmd.setAutoAlbum(false);
		addCmd.setCollectionId(this.testCollection.getCollectionId());
		final Resource testZip = new FileSystemResource(tempZipFile);
		addCmd.setTempFile(new TemporaryFile() {

			@Override
			public InputStream getInputStream() throws IOException {
				return testZip.getInputStream();
			}

			@Override
			public String getName() {
				return testZip.getFilename();
			}

			@Override
			public String getContentType() {
				return "application/zip";
			}

			@Override
			public long getSize() {
				try {
					return testZip.getFile().length();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});

		BizContext context = new TestBizContext(applicationContext, testUser);
		WorkInfo info = testIOBizImpl.importMedia(addCmd, context);

		assertNotNull("Returned WorkInfo must not be null", info);

		// wait at most 10 minutes for job to complete
		info.get(600, TimeUnit.SECONDS);
		assertTrue(info.isDone());
		assertNull(info.getException());

		// now verify parsed item info and created album
		List<Album> testAlbums = albumDao.findAlbumsForUserAndName(testUser.getUserId(),
				"My Test Import With XML Album");
		assertNotNull(testAlbums);
		assertEquals(1, testAlbums.size());

		// now verify album has the two items in it!
		assertEquals(2, testAlbums.get(0).getItem().size());
	}

	/**
	 * Test able to import a Mac OS X zip archive that contains resource forks
	 * (skipping resource forks).
	 * 
	 * @throws Exception if an error occurs
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testImportMacZip() throws Exception {
		AddMediaCommand addCmd = new AddMediaCommand();
		addCmd.setAutoAlbum(false);
		addCmd.setCollectionId(this.testCollection.getCollectionId());
		final Resource testZip = new ClassPathResource("/magoffin/matt/ma2/image/Archive.zip");
		addCmd.setTempFile(new TemporaryFile() {

			@Override
			public InputStream getInputStream() throws IOException {
				return testZip.getInputStream();
			}

			@Override
			public String getName() {
				return testZip.getFilename();
			}

			@Override
			public String getContentType() {
				return "application/zip";
			}

			@Override
			public long getSize() {
				try {
					return testZip.getFile().length();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});

		BizContext context = new TestBizContext(applicationContext, testUser);
		WorkInfo info = testIOBizImpl.importMedia(addCmd, context);

		assertNotNull("Returned WorkInfo must not be null", info);

		// wait at most 10 minutes for job to complete
		info.get(600, TimeUnit.SECONDS);
		assertTrue(info.isDone());
		assertNull(info.getException());

		// verify imported two items
		Collection c = collectionDao.get(this.testCollection.getCollectionId());
		List<MediaItem> items = c.getItem();
		assertNotNull(items);
		assertEquals(2, items.size());
	}

	/**
	 * Test exporting an item.
	 * 
	 * @throws Exception if an error occurs
	 */
	@Test
	public void testExportItem() throws Exception {
		// import first...
		importImage("magoffin/matt/ma2/image/bee-action.jpg", collectionDao, testIOBizImpl, testCollection, testUser);

		Collection c = collectionDao.get(this.testCollection.getCollectionId());
		MediaItem item = (MediaItem) c.getItem().get(0);
		MediaRequest request = new BasicMediaRequest(item.getItemId());

		// output to tmp file so can visually inspect results if needed
		File outputFile = new File("/var/tmp/IOBizImplTest_testExportItem.jpg");
		MediaResponse response = new BasicMediaResponse(new FileOutputStream(outputFile));
		BizContext context = new TestBizContext(applicationContext, testUser);
		testIOBizImpl.exportMedia(request, response, context);

		assertTrue("Should have exported something", outputFile.length() > 0);
	}

	/**
	 * Test exporting an image with roration of 270 degrees.
	 * 
	 * @throws Exception if any error occurs
	 */
	@Test
	public void testExportItemWithRotate270() throws Exception {
		// import first...
		importImage("magoffin/matt/ma2/image/IMG_ORIENTATION_8.JPG", collectionDao, testIOBizImpl, testCollection,
				testUser);
		Collection c = collectionDao.get(this.testCollection.getCollectionId());
		MediaItem item = (MediaItem) c.getItem().get(0);
		MediaRequest request = new BasicMediaRequest(item.getItemId());

		// output to tmp file so can visually inspect results if needed
		File outputFile = new File("/var/tmp/IOBizImplTest_testExportItem_orientation_270.jpg");
		MediaResponse response = new BasicMediaResponse(new FileOutputStream(outputFile));
		BizContext context = new TestBizContext(applicationContext, testUser);
		testIOBizImpl.exportMedia(request, response, context);

		assertTrue("Should have exported something", outputFile.length() > 0);
	}

	/**
	 * Test exporting an image with roration of 180 degrees.
	 * 
	 * @throws Exception if any error occurs
	 */
	@Test
	public void testExportItemWithRotate180() throws Exception {
		// import first...
		importImage("magoffin/matt/ma2/image/IMG_ORIENTATION_3.JPG", collectionDao, testIOBizImpl, testCollection,
				testUser);
		Collection c = collectionDao.get(this.testCollection.getCollectionId());
		MediaItem item = (MediaItem) c.getItem().get(0);
		MediaRequest request = new BasicMediaRequest(item.getItemId());

		// output to tmp file so can visually inspect results if needed
		File outputFile = new File("/var/tmp/IOBizImplTest_testExportItem_orientation_180.jpg");
		MediaResponse response = new BasicMediaResponse(new FileOutputStream(outputFile));
		BizContext context = new TestBizContext(applicationContext, testUser);
		testIOBizImpl.exportMedia(request, response, context);

		assertTrue("Should have exported something", outputFile.length() > 0);
	}

	/**
	 * Test exporting an image with roration of 90 degrees.
	 * 
	 * @throws Exception if any error occurs
	 */
	@Test
	public void testExportItemWithRotate90() throws Exception {
		// import first...
		importImage("magoffin/matt/ma2/image/IMG_ORIENTATION_6.JPG", collectionDao, testIOBizImpl, testCollection,
				testUser);
		Collection c = collectionDao.get(this.testCollection.getCollectionId());
		MediaItem item = (MediaItem) c.getItem().get(0);
		MediaRequest request = new BasicMediaRequest(item.getItemId());

		// output to tmp file so can visually inspect results if needed
		File outputFile = new File("/var/tmp/IOBizImplTest_testExportItem_orientation_90.jpg");
		MediaResponse response = new BasicMediaResponse(new FileOutputStream(outputFile));
		BizContext context = new TestBizContext(applicationContext, testUser);
		testIOBizImpl.exportMedia(request, response, context);

		assertTrue("Should have exported something", outputFile.length() > 0);
	}

	/**
	 * Test exporting of an album.
	 * 
	 * @throws Exception if an error occurs
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testExportAlbum() throws Exception {
		testImportZip();
		Album album = domainObjectFactory.newAlbumInstance();
		album.setOwner(testUser);
		album.setAnonymousKey("***testalbum***");
		album.setAllowAnonymous(true);
		album.setAllowOriginal(true);
		album.setCreationDate(Calendar.getInstance());
		album.setName("TEST ALBUM");

		Collection c = collectionDao.get(testCollection.getCollectionId());
		for (MediaItem item : (List<MediaItem>) c.getItem()) {
			album.getItem().add(item);
		}

		album = albumDao.get(albumDao.store(album));

		BizContext context = new TestBizContext(applicationContext, testUser);

		// now export album in "normal" size
		File outputZip = File.createTempFile("IOBizImplTest-", ".zip");
		if (logger.isDebugEnabled()) {
			logger.debug("Exporting album archive: " + outputZip.getAbsolutePath());
		}
		BasicMediaRequest request = new BasicMediaRequest(null, MediaSize.NORMAL, MediaQuality.GOOD);
		BasicMediaResponse response = new BasicMediaResponse(new FileOutputStream(outputZip));
		ExportItemsCommand cmd = new ExportItemsCommand(album.getAlbumId());
		WorkInfo info = testIOBizImpl.exportItems(cmd, request, response, context);
		assertNotNull(info);
		info.get();

		List<Long> objectIds = info.getObjectIds();
		assertNotNull(objectIds);
		assertEquals(2, objectIds.size());

		assertTrue(outputZip.length() > 0);
		if (logger.isDebugEnabled()) {
			logger.debug("Export album  complete to archive: " + outputZip.getAbsolutePath());
		}

		// verify all items + metadata exported into zip
		int count = 0;
		boolean metaFound = false;
		try (ZipFile zFile = new ZipFile(outputZip)) {
			Enumeration<? extends ZipEntry> entries = zFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (logger.isDebugEnabled()) {
					logger.debug("Got entry: " + entry);
				}
				if (entry.getName().equals("metadata.xml")) {
					metaFound = true;
				}
				count++;
			}
		}
		assertEquals(3, count);
		assertTrue(metaFound);
	}

	/**
	 * Test exporting a set of items.
	 * 
	 * @throws Exception if an error occurs
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testExportItems() throws Exception {
		testImportZip();

		Collection c = collectionDao.get(testCollection.getCollectionId());
		Long[] itemIds = new Long[c.getItem().size()];
		int i = 0;
		for (Iterator<MediaItem> itr = c.getItem().iterator(); itr.hasNext(); i++) {
			itemIds[i] = itr.next().getItemId();
		}

		BizContext context = new TestBizContext(applicationContext, testUser);

		// now export album in "normal" size
		File outputZip = File.createTempFile("IOBizImplTest-", ".zip");
		if (logger.isDebugEnabled()) {
			logger.debug("Exporting item archive: " + outputZip.getAbsolutePath());
		}
		BasicMediaRequest request = new BasicMediaRequest(null, MediaSize.NORMAL, MediaQuality.GOOD);
		BasicMediaResponse response = new BasicMediaResponse(new FileOutputStream(outputZip));
		ExportItemsCommand cmd = new ExportItemsCommand(itemIds);
		WorkInfo info = testIOBizImpl.exportItems(cmd, request, response, context);
		assertNotNull(info);
		info.get();

		List<Long> objectIds = info.getObjectIds();
		assertNotNull(objectIds);
		assertEquals(2, objectIds.size());

		assertTrue(outputZip.length() > 0);
		if (logger.isDebugEnabled()) {
			logger.debug("Export items  complete to archive: " + outputZip.getAbsolutePath());
		}

		// verify all items exported into zip
		int count = 0;
		try (ZipFile zFile = new ZipFile(outputZip)) {
			Enumeration<? extends ZipEntry> entries = zFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (logger.isDebugEnabled()) {
					logger.debug("Got entry: " + entry);
				}
				count++;
			}
		}
		assertEquals(2, count);
	}

	/**
	 * Test exporting a set of items in two-phase style.
	 * 
	 * @throws Exception if an error occurs
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testExportItemsTwoPhase() throws Exception {
		testImportZip();

		Collection c = collectionDao.get(testCollection.getCollectionId());
		Long[] itemIds = new Long[c.getItem().size()];
		int i = 0;
		for (Iterator<MediaItem> itr = c.getItem().iterator(); itr.hasNext(); i++) {
			itemIds[i] = itr.next().getItemId();
		}

		BizContext context = new TestBizContext(applicationContext, testUser);

		// now export album in "normal" size
		File outputZip = File.createTempFile("IOBizImplTest-", ".zip");
		if (logger.isDebugEnabled()) {
			logger.debug("Exporting item archive: " + outputZip.getAbsolutePath());
		}
		BasicMediaRequest request = new BasicMediaRequest(null, MediaSize.NORMAL, MediaQuality.GOOD);
		ExportItemsCommand cmd = new ExportItemsCommand(itemIds);
		WorkInfo info = testIOBizImpl.exportItems(cmd, request, null, context);
		assertNotNull(info);
		final long ticket = info.getTicket();

		// now wait a bit
		Thread.sleep(2000);

		WorkInfo info2 = testWorkBiz.getInfo(ticket);
		assertNotNull(info2);
		assertSame(info, info2);
		assertTrue(info.getWorkRequest() instanceof TwoPhaseExportRequest);

		// now set response
		TwoPhaseExportRequest tper = (TwoPhaseExportRequest) info.getWorkRequest();
		tper.setMediaResponse(new BasicMediaResponse(new FileOutputStream(outputZip)));

		info.get();

		List<Long> objectIds = info.getObjectIds();
		assertNotNull(objectIds);
		assertEquals(2, objectIds.size());

		assertTrue(outputZip.length() > 0);
		if (logger.isDebugEnabled()) {
			logger.debug("Export items  complete to archive: " + outputZip.getAbsolutePath());
		}

		// verify all items exported into zip
		int count = 0;
		try (ZipFile zFile = new ZipFile(outputZip)) {
			Enumeration<? extends ZipEntry> entries = zFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (logger.isDebugEnabled()) {
					logger.debug("Got entry: " + entry);
				}
				count++;
			}
		}
		assertEquals(2, count);
	}

	/**
	 * Test able to move media files.
	 * 
	 * @throws Exception if any error occurs
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testMoveMedia() throws Exception {
		testImportZip();
		Collection c = collectionDao.get(testCollection.getCollectionId());

		Collection newCollection = domainObjectFactory.newCollectionInstance();
		newCollection.setName("New Collection");
		newCollection.setOwner(c.getOwner());
		newCollection.setPath("test-move-media");
		newCollection.setCreationDate(Calendar.getInstance());
		File destDir = new File(testSystemBiz.getCollectionRootDirectory(), newCollection.getPath());
		if (destDir.exists()) {
			for (File f : destDir.listFiles()) {
				f.delete();
			}
			destDir.delete();
		}
		destDir.mkdirs();

		newCollection = collectionDao.get(collectionDao.store(newCollection));

		// now move items into new collection dir
		int moved = testIOBizImpl.moveMedia(c.getItem(), newCollection);
		assertEquals(c.getItem().size(), moved);
		String[] destNames = destDir.list();
		assertEquals(moved, destNames.length);
	}

	/**
	 * Copy file to Zip, without closing the output stream.
	 */
	private void addToZip(Resource resource, ZipOutputStream out) throws Exception {
		InputStream in = resource.getInputStream();
		try {
			byte[] buffer = new byte[FileCopyUtils.BUFFER_SIZE];
			int bytesRead = -1;
			while ((bytesRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
			}
			out.flush();
		} finally {
			try {
				in.close();
			} catch (IOException ex) {
				logger.warn("Could not close InputStream", ex);
			}
		}
	}
}
