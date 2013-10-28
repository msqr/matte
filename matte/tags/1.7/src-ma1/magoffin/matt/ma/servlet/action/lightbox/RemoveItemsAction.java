/* ===================================================================
 * RemoveItemsAction.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Jun 13, 2004 10:28:47 PM.
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
 * $Id: RemoveItemsAction.java,v 1.1 2006/06/03 22:26:19 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.lightbox;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractAction;
import magoffin.matt.ma.servlet.formbean.MultiItemsForm;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.Lightbox;

import org.apache.commons.collections.ListUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

/**
 * Action to remove items from the user's lightbox.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:19 $
 */
public class RemoveItemsAction extends AbstractAction {

/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.AbstractAction#go(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, magoffin.matt.ma.servlet.ActionResult)
 */
protected void go(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response,
		ActionResult result) throws Exception 
{
	UserSessionData usd = getUserSessionData(request,ANONYMOUS_USER_OK);

	Lightbox lb = usd.getLightbox();
	if ( lb != null ) {
		MultiItemsForm dForm = (MultiItemsForm)form;
		Integer[] removeIds = dForm.getMitems();
		if ( removeIds != null && removeIds.length > 0 ) {
			List removeList = Arrays.asList(removeIds);
			List currList = Arrays.asList(lb.getItemId());
			currList = ListUtils.subtract(currList,removeList);
			Integer[] newIds = (Integer[])currList.toArray(new Integer[currList.size()]);
			if ( newIds.length < 1 ) {
				lb.clearItemId();
				lb.clearItem();
			} else {
				lb.setItemId(newIds);
			}
			lb.setDirty(Boolean.TRUE);
			addActionMessage(request,ActionMessages.GLOBAL_MESSAGE,
					new ActionMessage("lightbox.items.removed"));
		}
	}
	
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

}
