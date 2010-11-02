/* ===================================================================
 * JpegMediaHandlerTest.java
 * 
 * Created Oct 27, 2010 9:03:52 PM
 * 
 * Copyright (c) 2010 Matt Magoffin.
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

package magoffin.matt.ma2.image.im4java;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.FileCopyUtils;

import magoffin.matt.ma2.AbstractSpringEnabledTransactionalTest;
import magoffin.matt.ma2.MediaEffect;
import magoffin.matt.ma2.MediaQuality;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.MediaSize;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.Metadata;
import magoffin.matt.ma2.image.ImageConstants;
import magoffin.matt.ma2.image.im4java.JpegMediaHandler;
import magoffin.matt.ma2.support.BasicMediaRequest;
import magoffin.matt.ma2.support.BasicMediaResponse;
import magoffin.matt.ma2.support.Geometry;
import magoffin.matt.ma2.support.MutableGeometry;

/**
 * Test case for the {@link magoffin.matt.ma2.image.im4java.JpegMediaHandler}
 * class.
 *
 * @author matt
 * @version $Revision$ $Date$
 */
@ContextConfiguration
public class JpegMediaHandlerTest extends AbstractSpringEnabledTransactionalTest {

	@javax.annotation.Resource private JpegMediaHandler testJpegMediaHandler;
	
	/**
	 * Test the media handler can correctly read image dimensions.
	 * @throws IOException if an error occurs
	 */
	@Test
	public void testReadDimensions() throws IOException {
		Resource testReadDimensions 
			= new ClassPathResource("magoffin/matt/ma2/image/dylan2.jpg");
		MediaItem item = testJpegMediaHandler.createNewMediaItem(
				testReadDimensions.getFile());
		assertNotNull(item);
		assertEquals(1615,item.getWidth());
		assertEquals(1053,item.getHeight());
		
		if ( logger.isDebugEnabled() ) {
			logger.debug("Got WxH: " +item.getWidth() +"x" +item.getHeight());
		}
		
		@SuppressWarnings("unchecked")
		List<Metadata> metaList = item.getMetadata();
		for ( Metadata meta : metaList ) {
			logger.debug("Got metadata [" +meta.getKey() +"]: " +meta.getValue());
		}
		
		assertEquals(ImageConstants.JPEG_MIME, item.getMime());
	}
	
	/**
	 * Test the media handler can correctly scale images.
	 * @throws IOException if an error occurs
	 */
	@Test
	@SuppressWarnings("null")
	public void testAllSizesAndQualities() throws IOException {
		Enumeration<URL> imageDirs = getClass().getClassLoader().getResources("magoffin/matt/ma2/image/");
		File[] images = null;
		while ( imageDirs.hasMoreElements() ) {
			URL imageDir = imageDirs.nextElement();
			UrlResource r = new UrlResource(imageDir);
			images = r.getFile().listFiles(new FilenameFilter() {
				private Set<String> types = new HashSet<String>(Arrays.asList("jpg"));
				public boolean accept(File dir, String name) {
					int idx = name.lastIndexOf('.');
					return idx > 0 && idx < (name.length()-1)
						&& types.contains(name.substring(idx+1).toLowerCase());
				}
			});
		}

		assertNotNull(images);
		assertTrue(images.length > 0);
		
		StringBuilder outMsg = new StringBuilder();
		
		for ( File imageFile : images ) {
			// copy to tmp file so MediaBiz finds
			File tmpFile = File.createTempFile(imageFile.getName()+"-", 
					".jpg", new File("/tmp"));
			tmpFile.deleteOnExit();
			FileCopyUtils.copy(imageFile, tmpFile);
			
			MediaItem item = testJpegMediaHandler.createNewMediaItem(tmpFile);
			item.setPath(tmpFile.getName());
			
			@SuppressWarnings("unchecked")
			List<Metadata> metaList = item.getMetadata();
			for ( Metadata meta : metaList ) {
				logger.debug("Got metadata [" +meta.getKey() +"]: " +meta.getValue());
			}
			
			Set<MediaQuality> qualitySet = EnumSet.allOf(MediaQuality.class);
			Set<MediaSize> sizeSet = EnumSet.allOf(MediaSize.class);
			
			for ( MediaSize size : sizeSet ) {
				for ( MediaQuality quality : qualitySet ) {
					File tmpOutputFile = File.createTempFile(imageFile.getName()+"-"
							+size +"-" +quality +"-", ".jpg");
					BasicMediaRequest request = new BasicMediaRequest(null, size, quality);
					request.getParameters().put(MediaRequest.OUTPUT_FILE_KEY, tmpOutputFile);
					BasicMediaResponse response = new BasicMediaResponse(
							new FileOutputStream(tmpOutputFile));
					
					long time = System.currentTimeMillis();
					testJpegMediaHandler.handleMediaRequest(item,  request, response);
					time = System.currentTimeMillis() - time;
					
					assertTrue(tmpOutputFile.length() > 0);
					if ( logger.isDebugEnabled() ) {
						String msg = "Created size " +size +" quality [" +quality +"] in "
							+time +"ms: " +tmpOutputFile.getAbsolutePath();
						logger.debug(msg);
						outMsg.append(msg).append("\n");
					}
					
					// verify dimensions
					MutableGeometry inGeo = new MutableGeometry(item.getWidth(), item.getHeight());
					Geometry outGeo = testJpegMediaHandler.getMediaBiz().getScaledGeometry(item, request);
					if ( request.getParameters().containsKey(MediaEffect.MEDIA_REQUEST_PARAM_ROTATE_DEGREES) ) {
						Integer degrees = (Integer)request.getParameters().get(
								MediaEffect.MEDIA_REQUEST_PARAM_ROTATE_DEGREES);
						if ( Math.abs(degrees) == 90 ) {
							MutableGeometry copy = new MutableGeometry(outGeo);
							copy.swapWidthAndHeight();
							outGeo = copy;
							inGeo.swapWidthAndHeight();
						}
					}
					MediaItem outItem = testJpegMediaHandler.createNewMediaItem(tmpOutputFile);
					if ( inGeo.getWidth() > outGeo.getWidth() ) {
						assertEquals(outGeo.getWidth(), outItem.getWidth());
					} else {
						assertEquals(inGeo.getWidth(), outItem.getWidth());
					}
					if ( inGeo.getHeight() > outGeo.getHeight() ) {
						assertEquals(outGeo.getHeight(), outItem.getHeight());
					} else {
						assertEquals(inGeo.getHeight(), outItem.getHeight());
					}
				}
			}
		}
		if ( logger.isDebugEnabled() ) {
			logger.debug("Output images: \n" +outMsg);
		}
	}
		
	/**
	 * Test can apply a watermark.
	 * @throws IOException if an error occurs
	 */
	public void testWatermark() throws IOException {
		Resource testReadDimensions 
			= new ClassPathResource("magoffin/matt/ma2/image/bee-action.jpg");
		MediaItem item = testJpegMediaHandler.createNewMediaItem(
				testReadDimensions.getFile());
		assertNotNull(item);

		File tmpFile = File.createTempFile(testReadDimensions.getFilename()+"-", 
				".jpg", new File("/tmp"));
		tmpFile.deleteOnExit();
		FileCopyUtils.copy(testReadDimensions.getFile(), tmpFile);
		item.setPath(tmpFile.getName());
		
		Resource watermarkResource = new ClassPathResource(
				"magoffin/matt/ma2/image/test-watermark.png");
		
		File tmpOutputFile = File.createTempFile("bee-action-watermark-", ".jpg");
		
		BasicMediaRequest request = new BasicMediaRequest(null, 
				MediaSize.NORMAL, MediaQuality.HIGH);
		request.getParameters().put(MediaEffect.MEDIA_REQUEST_PARAM_WATERMARK_RESOURCE, 
				watermarkResource);
		request.getParameters().put(MediaRequest.OUTPUT_FILE_KEY, tmpOutputFile);
		BasicMediaResponse response = new BasicMediaResponse(
				new FileOutputStream(tmpOutputFile));
		
		testJpegMediaHandler.handleMediaRequest(item,  request, response);
	}
	
}
