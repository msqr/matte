/* ===================================================================
 * MP3MediaHandlerTest.java
 * 
 * Created Jan 13, 2007 9:58:01 AM
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
 */

package magoffin.matt.ma2.audio;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import magoffin.matt.ma2.AbstractSpringEnabledTransactionalTest;
import magoffin.matt.ma2.MediaHandler;
import magoffin.matt.ma2.MediaQuality;
import magoffin.matt.ma2.MediaSize;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.support.BasicMediaRequest;
import magoffin.matt.ma2.support.BasicMediaResponse;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.FileCopyUtils;

/**
 * Test case for handling an MP3 media file.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
@ContextConfiguration
public class MP3MediaHandlerTest extends AbstractSpringEnabledTransactionalTest {

	@javax.annotation.Resource private MediaHandler testMP3MediaHandler;
	
	/**
	 * Test the media handler can correctly read meta data.
	 * @throws IOException if an error occurs
	 */
	@Test
	public void testReadMetadata_22() throws IOException {
		Resource testReadDimensions 
			= new ClassPathResource("id3-test-2.2.mp3");
		MediaItem item = testMP3MediaHandler.createNewMediaItem(
				testReadDimensions.getFile());
		assertNotNull(item);
		assertTrue(item.getWidth() > 0);
		assertTrue(item.getHeight() > 0);
		
		debugLog(item);
		
		assertEquals(AudioConstants.MPEG_AUDIO_MIME, item.getMime());
		
		handleAlbumCover(testReadDimensions, ".jpg", item);
	}

	private void handleAlbumCover(Resource testReadDimensions, String extension,
			MediaItem item) throws IOException, FileNotFoundException {
		// test extracting image via media request
		File tmpFile = File.createTempFile("MP3MediaHandlerTest-", ".mp3", new File("/tmp"));
		FileCopyUtils.copy(testReadDimensions.getFile(), tmpFile);
		item.setPath(tmpFile.getName());
		
		File tmpOutFile1 = File.createTempFile("MP3MediaHandlerTest-", extension);
		BasicMediaRequest request = new BasicMediaRequest(item.getItemId());
		BasicMediaResponse response = new BasicMediaResponse(new FileOutputStream(tmpOutFile1));
		testMP3MediaHandler.handleMediaRequest(item, request, response);
		assertTrue(tmpOutFile1.length() > 0);
		logger.debug("Created normal MP3 image: " +tmpOutFile1.getAbsolutePath());
		
		File tmpOutFile2 = File.createTempFile("MP3MediaHandlerTest-", extension);
		request = new BasicMediaRequest(item.getItemId(), MediaSize.THUMB_NORMAL, 
				MediaQuality.GOOD);
		response = new BasicMediaResponse(new FileOutputStream(tmpOutFile2));
		testMP3MediaHandler.handleMediaRequest(item, request, response);
		logger.debug("Created thumbnail MP3 image: " +tmpOutFile2.getAbsolutePath());
	}

	/**
	 * Test the media handler can correctly read meta data.
	 * @throws IOException if an error occurs
	 */
	@Test
	public void testReadMetadata_23() throws IOException {
		Resource testReadDimensions 
			= new ClassPathResource("id3-test-2.3.mp3");
		MediaItem item = testMP3MediaHandler.createNewMediaItem(
				testReadDimensions.getFile());
		assertNotNull(item);
		assertTrue(item.getWidth() > 0);
		assertTrue(item.getHeight() > 0);
		
		debugLog(item);
		
		assertEquals(AudioConstants.MPEG_AUDIO_MIME, item.getMime());

		handleAlbumCover(testReadDimensions, ".jpg", item);
	}
	
	/**
	 * Test the media handler can correctly read meta data.
	 * @throws IOException if an error occurs
	 */
	@Test
	public void testReadMetadata_24() throws IOException {
		Resource testReadDimensions 
			= new ClassPathResource("id3-test-2.4.mp3");
		MediaItem item = testMP3MediaHandler.createNewMediaItem(
				testReadDimensions.getFile());
		assertNotNull(item);
		assertTrue(item.getWidth() > 0);
		assertTrue(item.getHeight() > 0);
		
		debugLog(item);
		
		assertEquals(AudioConstants.MPEG_AUDIO_MIME, item.getMime());
	}
	
}
