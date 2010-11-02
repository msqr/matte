/* ===================================================================
 * XmlHelperTest.java
 * 
 * Created May 1, 2006 11:34:28 AM
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

package magoffin.matt.ma2.util;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import magoffin.matt.ma2.AbstractSpringEnabledTransactionalTest;
import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.domain.Album;

/**
 * Test for the XmlHelper class.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
@ContextConfiguration
public class XmlHelperTest extends AbstractSpringEnabledTransactionalTest {

	@Resource private XmlHelper testXmlUtil;
	@Resource private DomainObjectFactory domainObjectFactory;
	
	private final Logger log = Logger.getLogger(XmlHelperTest.class);
	
	/**
	 * Test debugging a JAXB object.
	 */
	@Test
	public void testDebugJAXBObject() {
		Album a = domainObjectFactory.newAlbumInstance();
		a.setName("My Test Album");
		a.setComment("These are comments.");
		
		testXmlUtil.debugJaxbObject("TEST: ", a, log);
	}
	
}
