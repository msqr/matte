/* ===================================================================
 * GetCollectionListEndpoint.java
 * 
 * Created Dec 3, 2007 2:28:40 PM
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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2.web.service;

import java.util.List;

import magoffin.matt.ma2.SystemConstants;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.CollectionListItemType;
import magoffin.matt.ma2.domain.GetCollectionListRequest;
import magoffin.matt.ma2.domain.GetCollectionListResponse;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.util.BizContextUtil;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;

/**
 * Web service endpoint for getting the list of collections for the current user.
 * 
 * <p>Note that a {@link BizContext} must be available via {@link BizContextUtil#getBizContext()}
 * prior to invoking this service, to pass the user authentication to the import.</p> 
 *
 * @author matt
 * @version $Revision$ $Date$
 */
@Endpoint
public class GetCollectionListEndpoint {
	
	private UserBiz userBiz;
	private DomainObjectFactory domainObjectFactory;

	/**
	 * Get a list of all collections for the current user.
	 * 
	 * @param request the request
	 * @return the response
	 */
	@SuppressWarnings("unchecked")
	@PayloadRoot(localPart = "GetCollectionList", 
			namespace = SystemConstants.MATTE_XML_NAMESPACE_URI)
	public GetCollectionListResponse getCollectionList(GetCollectionListRequest request) {
		BizContext context = BizContextUtil.getBizContext();
		User user = context.getActingUser();
		List<Collection> userCollections = userBiz.getCollectionsForUser(user, context);
		GetCollectionListResponse response = domainObjectFactory
			.newGetCollectionListResponseInstance();
		for ( Collection c : userCollections ) {
			CollectionListItemType colItem = domainObjectFactory
				.newCollectionListItemTypeInstance();
			colItem.setCollectionId(c.getCollectionId());
			colItem.setName(c.getName());
			response.getCollection().add(colItem);
		}
		return response;
	}

	/**
	 * @return the userBiz
	 */
	public UserBiz getUserBiz() {
		return userBiz;
	}

	/**
	 * @param userBiz the userBiz to set
	 */
	public void setUserBiz(UserBiz userBiz) {
		this.userBiz = userBiz;
	}

	/**
	 * @return the domainObjectFactory
	 */
	public DomainObjectFactory getDomainObjectFactory() {
		return domainObjectFactory;
	}

	/**
	 * @param domainObjectFactory the domainObjectFactory to set
	 */
	public void setDomainObjectFactory(DomainObjectFactory domainObjectFactory) {
		this.domainObjectFactory = domainObjectFactory;
	}

}
