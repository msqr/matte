/* ===================================================================
 * AddMediaEndpointTest.java
 * 
 * Created Mar 13, 2006 5:23:42 PM
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

package magoffin.matt.ma2.web.service;

import java.io.File;
import java.io.FileOutputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import magoffin.matt.ma2.AbstractSpringEnabledTransactionalTest;
import magoffin.matt.ma2.biz.IOBiz;

/**
 * Test the AddMediaEndpoint.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class AddMediaEndpointTest extends AbstractSpringEnabledTransactionalTest {
	
	/** The IOBiz. */
	protected IOBiz ioBiz;

	/**
	 * Test able to add media, normal situation.
	 * @throws Exception if any error occurs
	 */
	public void testAddMedia() throws Exception {
		AddMediaEndpoint endpoint = new AddMediaEndpoint();
		endpoint.setIoBiz(ioBiz);
		
		Resource r = new ClassPathResource("add-media-test-01.xml", getClass());
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		File resultFile = File.createTempFile("AddmediaEndpointTest-", ".xml");
		endpoint.invokeInternal(inputFactory.createXMLStreamReader(r.getInputStream()), 
				outputFactory.createXMLStreamWriter(new FileOutputStream(resultFile)));
	}
}
