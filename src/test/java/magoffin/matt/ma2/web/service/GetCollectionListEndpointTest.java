/* ===================================================================
 * GetCollectionListEndpointTest.java
 * 
 * Created Dec 3, 2007 2:50:01 PM
 * 
 * Copyright (c) 2007 Matt Magoffin.
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

package magoffin.matt.ma2.web.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.annotation.Resource;

import magoffin.matt.ma2.AbstractSpringEnabledTransactionalTest;
import magoffin.matt.ma2.ProcessingException;
import magoffin.matt.ma2.TestConstants;
import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.biz.impl.TestBizContext;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.CollectionListItemType;
import magoffin.matt.ma2.domain.GetCollectionListRequest;
import magoffin.matt.ma2.domain.GetCollectionListResponse;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.util.BizContextUtil;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

/**
 * Test case for the {@link GetCollectionListEndpoint} class.
 *
 * @author matt
 * @version 1.0
 */
@ContextConfiguration
public class GetCollectionListEndpointTest extends AbstractSpringEnabledTransactionalTest {

	@Resource private UserBiz testUserBiz;
	@Resource private DomainObjectFactory domainObjectFactory;
//	@Resource private CollectionDao collectionDao;
	
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
		
		TestBizContext context = new TestBizContext(applicationContext,null);
		String confKey = null;
		try {
			confKey = testUserBiz.registerUser(newUser,context);
		} catch ( ProcessingException e ) {
			// whatever!
			confKey = (String)e.getProcessResult();
		}
		this.testUser = testUserBiz.confirmRegisteredUser(newUser.getLogin(),
				confKey,context);
		context.setActingUser(this.testUser);
		
		List<Collection> collections = testUserBiz.getCollectionsForUser(this.testUser,context);
		this.testCollection = collections.get(0);
		BizContextUtil.attachBizContext(context);
	}

	/**
	 * Test able to get user collection list.
	 */
	@Test
	public void testGetCollectionList() {
		GetCollectionListEndpoint endpoint = new GetCollectionListEndpoint();
		endpoint.setDomainObjectFactory(this.domainObjectFactory);
		endpoint.setUserBiz(this.testUserBiz);
		
		GetCollectionListRequest request = 
			this.domainObjectFactory.newGetCollectionListRequestInstance();
		GetCollectionListResponse response = endpoint.getCollectionList(request);
		assertNotNull(response);
		assertNotNull(response.getCollection());
		assertEquals(1, response.getCollection().size());
		
		CollectionListItemType colItem = (CollectionListItemType)response.getCollection().get(0);
		assertNotNull(colItem);
		assertEquals(this.testCollection.getCollectionId(), colItem.getCollectionId());
		assertEquals(this.testCollection.getName(), colItem.getName());
	}
	
}
