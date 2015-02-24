/* ===================================================================
 * DeleteCollectionAction.java
 * 
 * Created Jan 9, 2004 5:32:36 PM
 * 
 * Copyright (c) 2004 Matt Magoffin.
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
 * $Id: DeleteCollectionAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.CollectionBiz;
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
 * Action to delete a collection.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class DeleteCollectionAction extends AbstractAction {

/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.AbstractAction#go(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 */
protected void go(
	ActionMapping mapping,
	ActionForm form,
	HttpServletRequest request,
	HttpServletResponse response, ActionResult result)
	throws Exception 
{
	UserSessionData usd = this.getUserSessionData(request, ANONYMOUS_USER_NOT_OK);
	
	DynaActionForm dForm = (DynaActionForm)form;
	
	Integer collectionId = (Integer)dForm.get(ServletConstants.REQ_KEY_COLLECTION_ID);
	Boolean move = (Boolean)dForm.get("move");
	Integer moveToCollectionId = (Integer)dForm.get("moveTo");
	
	CollectionBiz collectionBiz = (CollectionBiz)getBiz(BizConstants.COLLECTION_BIZ);
	Collection collection = collectionBiz.getCollectionById(collectionId,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
	collectionBiz.deleteCollection(collectionId,move.booleanValue(),moveToCollectionId,usd.getUser());
	
	ActionMessage msg = null;
	if ( move != null && move.booleanValue() ) {
		Collection moveCollection = collectionBiz.getCollectionById(moveToCollectionId,
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
		msg = new ActionMessage("collection.deletemove.ok",collection.getName(),moveCollection.getName());
	} else {
		msg = new ActionMessage("collection.delete.ok",collection.getName());
	}
	
	addActionMessage(request,ActionMessages.GLOBAL_MESSAGE,msg);
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

}
