/* ===================================================================
 * DeleteItemAction.java
 * 
 * Copyright (c) 2002 Matt Magoffin.
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
 * $Id: DeleteItemAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.item;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.ServletConstants;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractAction;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.MediaItem;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

/**
 * Allow a user to delete an item.
 * 
 * <p> Created on Oct 31, 2002 1:23:52 PM.</p>
 *
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class DeleteItemAction extends AbstractAction 
{		
	private static final Logger LOG = Logger.getLogger(DeleteItemAction.class);

/**
 * @param mapping
 * @param form
 * @param request
 * @param response
 * @see magoffin.matt.ma.servlet.action.AbstractAction#go(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, ActionResult)
 * @throws Exception
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
	
	Integer itemId = (Integer)dForm.get(ServletConstants.REQ_KEY_ITEM_ID);
	Integer collectionId = (Integer)dForm.get(ServletConstants.REQ_KEY_COLLECTION_ID);
	
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("itemId = " +itemId );
	}
	
	MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);
	
	MediaItem item = itemBiz.getMediaItemById(itemId,
			ApplicationConstants.CACHED_OBJECT_NOT_ALLOWED);
	if ( item == null ) {
		notFound(mapping, request, response);
		return;
	}
	
	// delete item, assume database will delete media from foreign key ref
	itemBiz.deleteMediaItem(item.getItemId(),usd.getUser());
	
	result.setForward(redirectToCollection(collectionId,request,response,
		mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD)));
}

}
