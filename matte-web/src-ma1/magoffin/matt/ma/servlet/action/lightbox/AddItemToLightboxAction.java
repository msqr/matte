/* ===================================================================
 * AddItemToLightboxAction.java
 * 
 * Created Jun 14, 2004 5:37:50 PM
 * 
 * Copyright (c) 2004 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: AddItemToLightboxAction.java,v 1.1 2006/06/03 22:26:19 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.lightbox;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.LightboxBiz;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractAction;
import magoffin.matt.ma.servlet.formbean.MultiItemsForm;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.Lightbox;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.util.StringUtil;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * Add a single item to the user's lightbox.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:19 $
 */
public class AddItemToLightboxAction extends AbstractAction {
	
	public static final String MSG_ITEM_ALREADY_IN_LIGHTBOX = 
		"lightbox.item.already.present";

/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.AbstractAction#go(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, magoffin.matt.ma.servlet.ActionResult)
 */
protected void go(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response,
		ActionResult result) throws Exception 
{
	MultiItemsForm dForm = (MultiItemsForm)form;
	UserSessionData usd = getUserSessionData(request,ANONYMOUS_USER_OK);
	
	Integer itemId = dForm.getMitem();
	
	LightboxBiz lightboxBiz = (LightboxBiz)getBiz(BizConstants.LIGHTBOX_BIZ);

	Lightbox lb = usd.getLightbox();
	if ( lb == null ) {
		lb = lightboxBiz.getLightboxInstance(usd.getUser());
		usd.setLightbox(lb);
	}
	
	boolean added = lightboxBiz.addMediaItemToLightbox(lb,itemId,usd.getUser());
	
	if ( !added ) {
		MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);
		MediaItem item = itemBiz.getMediaItemById(itemId,
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
		String name = StringUtil.escapeSingleQuotes(item.getName() != null ? item.getName() : item.getPath());
		addActionMessage(request,ActionErrors.GLOBAL_ERROR,
				new ActionError(MSG_ITEM_ALREADY_IN_LIGHTBOX,name));
	}
	
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

}
