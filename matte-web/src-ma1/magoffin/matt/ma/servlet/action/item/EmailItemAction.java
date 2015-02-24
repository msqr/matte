/* ===================================================================
 * EmailItemAction.java
 * 
 * Created Apr 15, 2004 7:29:19 PM
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
 * $Id: EmailItemAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.item;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractAction;
import magoffin.matt.ma.servlet.formbean.EmailItemForm;
import magoffin.matt.ma.util.EmailOptions;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

/**
 * Action to email a media item, either as an attachment or a link.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class EmailItemAction extends AbstractAction
{
/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.AbstractAction#go(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 */
protected void go(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response, ActionResult result)
		throws Exception
{
	EmailItemForm dForm = (EmailItemForm)form;
	
	if ( !isCancelled(request) ) {
		UserSessionData usd = getUserSessionData(request,ANONYMOUS_USER_OK);
		
		MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);
		
		EmailOptions options = new EmailOptions();
		
		BeanUtils.copyProperties(options,dForm);
		
		// TODO support link
		itemBiz.emailItems(dForm.getMitems(),null,options,usd.getUser());
		
		addActionMessage(request,ActionMessages.GLOBAL_MESSAGE,
				new ActionMessage( dForm.isMulti() 
						? MediaItemBiz.MSG_EMAIL_ITEM_MULTI_SENT
						: MediaItemBiz.MSG_EMAIL_ITEM_SENT));
	}
	
	setBounceBackActionForward(dForm,mapping,result, request);
}

}
