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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import magoffin.matt.ma2.AbstractSpringEnabledTransactionalTest;
import magoffin.matt.ma2.ProcessingException;
import magoffin.matt.ma2.TestConstants;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.biz.IOBiz;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.biz.WorkBiz;
import magoffin.matt.ma2.biz.WorkBiz.WorkInfo;
import magoffin.matt.ma2.biz.impl.TestBizContext;
import magoffin.matt.ma2.dao.CollectionDao;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.util.BizContextUtil;
import magoffin.matt.ma2.util.XmlHelper;

import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test the AddMediaEndpoint.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class AddMediaEndpointTest extends AbstractSpringEnabledTransactionalTest {
	
	/** The IOBiz. */
	protected IOBiz testIOBiz;

	/** The DomainObjectFactory. */
	protected DomainObjectFactory domainObjectFactory;
	
	/** The UserBiz. */
	protected UserBiz testUserBiz;
	
	/** The XmlHelper to help with XML. */
	protected XmlHelper xmlHelper;
	
	/** The WorkBiz. */
	protected WorkBiz testWorkBiz;
	
	/** The CollectionDAO. */
	protected CollectionDao collectionDao;
	
	private User testUser;
	private Collection testCollection;
	
	private final Logger log = Logger.getLogger(getClass());

	@Override
	protected void onSetUpInTransaction() throws Exception {
		super.onSetUpInTransaction();
		deleteFromTables(TestConstants.ALL_TABLES_FOR_CLEAR);
		
		User newUser = domainObjectFactory.newUserInstance();
		newUser.setEmail("nobody@localhost");
		newUser.setName("Test User");
		newUser.setPassword("test");
		newUser.setLogin("nobody");
		
		BizContext context = new TestBizContext(getContext(contextKey()),null);
		String confKey = null;
		try {
			confKey = testUserBiz.registerUser(newUser,context);
		} catch ( ProcessingException e ) {
			// whatever!
			confKey = (String)e.getProcessResult();
		}
		this.testUser = testUserBiz.confirmRegisteredUser(newUser.getLogin(),
				confKey,context);

		List<Collection> collections = testUserBiz.getCollectionsForUser(this.testUser,context);
		this.testCollection = collections.get(0);
		BizContextUtil.attachBizContext(context);
	}

	/**
	 * Test able to add media, normal situation.
	 * @throws Exception if any error occurs
	 */
	public void testAddMedia() throws Exception {
		AddMediaEndpoint endpoint = new AddMediaEndpoint();
		endpoint.setIoBiz(testIOBiz);
		
		File xmlIn = getTestXml();
		Source in = new StreamSource(xmlIn);
		Source out = endpoint.invoke(in);
		assertNotNull(out);
		
		DOMResult result = new DOMResult();
		xmlHelper.transformXml(out, result);
		if ( log.isDebugEnabled() ) {
			xmlHelper.debugXml("Got response XML: ", new DOMSource(result.getNode()), log);
		}		
		Element addMediaResponse = ((Document)result.getNode()).getDocumentElement();
		assertEquals("true", addMediaResponse.getAttribute("success"));
		assertNotNull(addMediaResponse.getAttribute("ticket"));
		
		Long ticket = Long.valueOf(addMediaResponse.getAttribute("ticket"));
		WorkInfo workInfo = testWorkBiz.getInfo(ticket);
		assertNotNull(workInfo);
		
		// wait for work to complete
		workInfo.get();
		
		// verify collection item has been imported
		Collection c = collectionDao.get(testCollection.getCollectionId());
		assertNotNull(c);
		assertNotNull(c.getItem());
		assertEquals(1, c.getItem().size());
		MediaItem testItem = (MediaItem)c.getItem().get(0);
		assertEquals("AddMediaTestAlbum/arrow-closed.png", testItem.getPath());
		assertEquals("Arrow Closed", testItem.getName());
		assertEquals("This is an arrow, closed.", testItem.getDescription());
	}
	
	private File getTestXml() throws Exception {
		Resource r = new ClassPathResource("add-media-test-01.xml", getClass());
		// copy resource, replacing collection-id with the test one
		File tmp = File.createTempFile("AddMediaEndpointTest-input-", ".xml");
		if ( log.isDebugEnabled() ) {
			log.debug("Generating test <m:collection-import> XML: " +tmp.getAbsolutePath());
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(r.getInputStream()));
		BufferedWriter out = new BufferedWriter(new FileWriter(tmp));
		String line = in.readLine();
		Pattern p = Pattern.compile("collection-id=\"[^\"]+\"");
		boolean doneReplacing = false;
		while ( line != null ) {
			if ( doneReplacing ) {
				out.write(line);
				out.write("\n");
			} else {
				Matcher m = p.matcher(line);
				if ( m.find() ) {
					out.write(m.replaceAll("collection-id=\""
							+this.testCollection.getCollectionId().toString()
							+"\""));
					out.write("\n");
				} else {
					out.write(line);
					out.write("\n");
				}
			}
			line = in.readLine();
		}
		out.flush();
		out.close();
		in.close();
		return tmp;
	}
}
