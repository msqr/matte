/* ===================================================================
 * SVGMediaHandlerTest.java
 * 
 * Created Feb 2, 2007 12:56:20 PM
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

package magoffin.matt.ma2.image;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

import magoffin.matt.ma2.AbstractSpringEnabledTransactionalTest;
import magoffin.matt.ma2.MediaHandler;
import magoffin.matt.ma2.MediaQuality;
import magoffin.matt.ma2.MediaRequest;
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
 * Test case for the {@link SvgMediaHandler} class.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
@ContextConfiguration
public class SvgMediaHandlerTest extends AbstractSpringEnabledTransactionalTest {

	@javax.annotation.Resource private MediaHandler testSvgMediaHandler;
	
	/**
	 * Test the media handler can correctly create a new item.
	 * @throws IOException if an error occurs
	 */
	@Test
	public void testCreateNewItem() throws IOException {
		Resource testReadDimensions 
			= new ClassPathResource("magoffin/matt/ma2/image/batikBatik.svg");
		MediaItem item = testSvgMediaHandler.createNewMediaItem(
				testReadDimensions.getFile());
		assertNotNull(item);
		
		assertEquals(ImageConstants.SVG_MIME, item.getMime());
	}
	
	/**
	 * Test able to scale an image to a thumbnail size.
	 * @throws Exception if an error occurs
	 */
	@Test
	public void testAllSizesAndQualities() throws Exception {
		// copy to tmp file so MediaBiz finds
		File tmpFile = File.createTempFile("SvgMediaHandlerTest-input-", 
				".svg", new File("/tmp"));
		ClassPathResource cpResource = new ClassPathResource(
			"magoffin/matt/ma2/image/batikBatik.svg");
		FileCopyUtils.copy(cpResource.getFile(), tmpFile);
		
		MediaItem item = testSvgMediaHandler.createNewMediaItem(tmpFile);
		item.setPath(tmpFile.getName());
		
		Set<MediaSize> sizeSet = EnumSet.allOf(MediaSize.class);
		sizeSet.remove(MediaSize.BIGGEST); // Batik can chew up tons of RAM
		
		StringBuilder outMsg = new StringBuilder();
		
		for ( MediaSize size : sizeSet ) {
			MediaQuality quality = MediaQuality.GOOD;
			File tmpOutputFile = File.createTempFile("SvgMediaHandlerTest-" 
					+size +"-" +quality +"-", ".png");
			BasicMediaRequest request = new BasicMediaRequest(null, size, quality);
			request.getParameters().put(MediaRequest.OUTPUT_FILE_KEY, tmpOutputFile);
			BasicMediaResponse response = new BasicMediaResponse(
					new FileOutputStream(tmpOutputFile));
			
			long time = System.currentTimeMillis();
			testSvgMediaHandler.handleMediaRequest(item,  request, response);
			time = System.currentTimeMillis() - time;
			
			assertTrue(tmpOutputFile.length() > 0);
			if ( logger.isDebugEnabled() ) {
				String msg = "Created size " +size +" quality [" +quality +"] in "
					+time +"ms: " +tmpOutputFile.getAbsolutePath();
				logger.debug(msg);
				outMsg.append(msg).append("\n");
			}
		}
		
		if ( logger.isDebugEnabled() ) {
			logger.debug("Output images: \n" +outMsg);
		}
	}

}
