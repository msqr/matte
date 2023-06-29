/* ===================================================================
 * InviteFriendDeclineAction.java
 * 
 * Created Jan 21, 2004 2:28:48 PM
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
 * $Id: InviteFriendDeclineAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import magoffin.matt.ma.UserAccessException;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.UserBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.ServletConstants;
import magoffin.matt.ma.servlet.ServletUtil;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractAction;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.User;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.DynaActionForm;

/**
 * Decline a friend's invitation.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class InviteFriendDeclineAction extends AbstractAction {

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
	UserSessionData usd = null;
	try {
		usd = getUserSessionData(request, ANONYMOUS_USER_OK);
	} catch ( UserAccessException e ) {
		// first access, so create a new anonymous session data
		usd = new UserSessionData();
		HttpSession session = request.getSession();
		session.setAttribute(ServletConstants.SES_KEY_USER,usd);
	}
	
	DynaActionForm dForm = (DynaActionForm)form;
	
	String key = (String)dForm.get(UserBiz.KEY_URL_PARAM);
	
	if ( !usd.isLoggedIn() ) {
		// save invite key to session, then forward to login
		request.getSession().setAttribute(ServletConstants.SES_KEY_INVITE_KEY,key);
		ServletUtil.saveRequestURL(request);
		result.setForward(mapping.findForward("logon"));
		return;
	}
	
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	
	User inviter = userBiz.declineInvitation(key);
	
	if ( inviter != null ) {
		// add message that invitation declined
		this.addActionMessage(request,ActionMessages.GLOBAL_MESSAGE,
				new ActionMessage("invite.declined",inviter.getName()));
	}
	
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

}
