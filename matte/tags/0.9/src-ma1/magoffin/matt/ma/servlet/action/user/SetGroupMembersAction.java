/* ===================================================================
 * SetGroupMembersAction.java
 * 
 * Created Dec 8, 2003 9:19:18 AM
 * 
 * Copyright (c) 2003 Matt Magoffin.
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
 * $Id: SetGroupMembersAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.UserBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.ServletConstants;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.Group;
import magoffin.matt.ma.xsd.User;
import magoffin.matt.ma.xsd.UserData;
import magoffin.matt.util.ArrayUtil;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

/**
 * Set the group membership for a group.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class SetGroupMembersAction extends AbstractUserAction {
	
	private static final Logger log = Logger.getLogger(SetGroupMembersAction.class);

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
	DynaActionForm dForm = null;
	if ( form != null ) {
		dForm = (DynaActionForm)form;
	}
	
	Integer groupId = (Integer)dForm.get(ServletConstants.REQ_KEY_GROUP_ID);
	if ( groupId != null && groupId.intValue() == 0 ) {
		log.warn("No group passed to action.");
		notFound(mapping,request, response);
		return;
	}
	
	UserSessionData usd = getUserSessionData(request, ANONYMOUS_USER_NOT_OK);
	User user = usd.getUser();
	
	UserData data = getUserDataForCurrentUser(request,result);
	
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	
	Group[] groups = userBiz.getGroupsForUserId(user.getUserId());
	
	if ( groups == null ) {
		notFound(mapping,request, response);
		return;
	}
	
	data.setGroup(groups);
	
	Group group = null;
	for ( int i = 0; i < groups.length; i++ ) {
		if ( groupId.equals(groups[i].getGroupId()) ) {
			group = groups[i];
			break;
		}
	}
	
	if ( group == null ) {
		notFound(mapping,request, response);
		return;
	}
	
	Integer[] memberIds = (Integer[])dForm.get("members");
	
	Integer[] allMemberIds = (Integer[])ArrayUtil.merge(memberIds,new Integer[] {user.getUserId()});
	
	userBiz.setMembersForGroup(groupId,allMemberIds);
	
	result.setForward(redirectToGroup(groupId,request,response,
			mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD)));
}

}
