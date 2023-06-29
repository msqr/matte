/* ===================================================================
 * CreateCollectionAction.java
 * 
 * Copyright (c) 2003 Matt Magoffin. Created Nov 23, 2003.
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
 * $Id: CreateCollectionAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.CollectionBiz;
import magoffin.matt.ma.biz.UserBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.ServletConstants;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractAction;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.Collection;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.DynaActionForm;

/**
 * Create a new collection.
 * 
 * <p>Created Nov 23, 2003 3:14:56 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class CreateCollectionAction extends AbstractAction {

/*
 * @see magoffin.matt.ma.servlet.action.AbstractAction#go(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 */
protected void go(
	ActionMapping mapping,
	ActionForm form,
	HttpServletRequest request,
	HttpServletResponse response, 
	ActionResult result)
	throws Exception 
{

	UserSessionData usd = this.getUserSessionData(request, ANONYMOUS_USER_NOT_OK);
	
	DynaActionForm dForm = (DynaActionForm)form;
	
	String collectionName = (String)dForm.get(ServletConstants.REQ_KEY_NAME);
	
	CollectionBiz collectionBiz = (CollectionBiz)getBiz(BizConstants.COLLECTION_BIZ);
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	
	collectionBiz.createCollection(collectionName,usd.getUser());
	
	Collection[] collections = userBiz.getCollectionsForUser(usd.getUser().getUserId());
	usd.setCollections(collections);
	request.getSession().setAttribute(
			ServletConstants.SES_KEY_USER_MEDIA_DIRS,
			collections);
	
	addActionMessage(request,ActionMessages.GLOBAL_MESSAGE,
					new ActionMessage("collection.create.ok",collectionName));
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

}
