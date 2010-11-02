/* ===================================================================
 * JpegMediaHandlerTest.java
 * 
 * Created Mar 3, 2006 10:04:27 PM
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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2.image.jmagick;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import magoffin.matt.ma2.AbstractSpringEnabledTransactionalTest;
import magoffin.matt.ma2.MediaQuality;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.MediaSize;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.Metadata;
import magoffin.matt.ma2.image.ImageConstants;
import magoffin.matt.ma2.support.BasicMediaRequest;
import magoffin.matt.ma2.support.BasicMediaResponse;
import magoffin.matt.ma2.support.Geometry;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.FileCopyUtils;

/**
 * Test case for the {@link magoffin.matt.ma2.image.jmagick.PngMediaHandler}
 * class.
 * 
 * @author matt.magoffin
 * @version $Revision$ $Date$
 */
@ContextConfiguration
public class PngMediaHandlerTest extends AbstractSpringEnabledTransactionalTest {

	@javax.annotation.Resource private PngMediaHandler testPngMediaHandler;
	
	/**
	 * Test the media handler can correctly read image dimensions.
	 * @throws IOException if an error occurs
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testReadDimensions() throws IOException {
		Resource testReadDimensions 
			= new ClassPathResource("magoffin/matt/ma2/image/add-media.png");
		MediaItem item = testPngMediaHandler.createNewMediaItem(
				testReadDimensions.getFile());
		assertNotNull(item);
		assertEquals(520,item.getWidth());
		assertEquals(400,item.getHeight());
		
		if ( logger.isDebugEnabled() ) {
			logger.debug("Got WxH: " +item.getWidth() +"x" +item.getHeight());
		}
		
		List<Metadata> metaList = item.getMetadata();
		for ( Metadata meta : metaList ) {
			logger.debug("Got metadata [" +meta.getKey() +"]: " +meta.getValue());
		}
		
		assertEquals(ImageConstants.PNG_MIME, item.getMime());
	}
	
	/**
	 * Test able to scale an image to a thumbnail size.
	 * @throws Exception if an error occurs
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testAllSizesAndQualities() throws Exception {
		// copy to tmp file so MediaBiz finds
		File tmpFile = File.createTempFile("PngMediaHandlerTest-input-", 
				".png", new File("/tmp"));
		ClassPathResource cpResource = new ClassPathResource(
			"magoffin/matt/ma2/image/add-media.png");
		FileCopyUtils.copy(cpResource.getFile(), tmpFile);
		
		MediaItem item = testPngMediaHandler.createNewMediaItem(tmpFile);
		item.setPath(tmpFile.getName());
		
		List<Metadata> metaList = item.getMetadata();
		for ( Metadata meta : metaList ) {
			logger.debug("Got metadata [" +meta.getKey() +"]: " +meta.getValue());
		}
		
		Set<MediaQuality> qualitySet = EnumSet.allOf(MediaQuality.class);
		Set<MediaSize> sizeSet = EnumSet.allOf(MediaSize.class);
		
		StringBuilder outMsg = new StringBuilder();
		
		for ( MediaSize size : sizeSet ) {
			for ( MediaQuality quality : qualitySet ) {
				File tmpOutputFile = File.createTempFile("PngMediaHandlerTest-"
						+size +"-" +quality +"-", ".jpg");
				BasicMediaRequest request = new BasicMediaRequest(null, size, quality);
				request.getParameters().put(MediaRequest.OUTPUT_FILE_KEY, tmpOutputFile);
				BasicMediaResponse response = new BasicMediaResponse(
						new FileOutputStream(tmpOutputFile));
				
				long time = System.currentTimeMillis();
				testPngMediaHandler.handleMediaRequest(item,  request, response);
				time = System.currentTimeMillis() - time;
				
				assertTrue(tmpOutputFile.length() > 0);
				if ( logger.isDebugEnabled() ) {
					String msg = "Created size " +size +" quality [" +quality +"] in "
						+time +"ms: " +tmpOutputFile.getAbsolutePath();
					logger.debug(msg);
					outMsg.append(msg).append("\n");
				}
				
				// verify dimensions
				Geometry outGeo = testPngMediaHandler.getMediaBiz().getScaledGeometry(item, request);
				MediaItem outItem = testPngMediaHandler.createNewMediaItem(tmpOutputFile);
				assertEquals(outGeo.getWidth(), outItem.getWidth());
				assertEquals(outGeo.getHeight(), outItem.getHeight());
			}
		}
		
		if ( logger.isDebugEnabled() ) {
			logger.debug("Output images: \n" +outMsg);
		}
	}
	
}
