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

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import magoffin.matt.ma2.AbstractSpringEnabledTransactionalTest;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.Metadata;
import magoffin.matt.ma2.image.ImageConstants;
import magoffin.matt.ma2.image.im4java.JpegMediaHandler;

/**
 * Test case for the {@link magoffin.matt.ma2.image.im4java.JpegMediaHandler}
 * class.
 *
 * @author matt
 * @version $Revision$ $Date$
 */
public class JpegMediaHandlerTest extends AbstractSpringEnabledTransactionalTest {

	/** The JpegMediaHandler to test. */
	protected JpegMediaHandler testJpegMediaHandler;
	
	/**
	 * Test the media handler can correctly read image dimensions.
	 * @throws IOException if an error occurs
	 */
	@SuppressWarnings("unchecked")
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
		
		List<Metadata> metaList = item.getMetadata();
		for ( Metadata meta : metaList ) {
			logger.debug("Got metadata [" +meta.getKey() +"]: " +meta.getValue());
		}
		
		assertEquals(ImageConstants.JPEG_MIME, item.getMime());
	}
	

}
