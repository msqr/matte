/* ===================================================================
 * UpdateUserAction.java
 * 
 * Created Jan 19, 2004 12:16:15 PM
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
 * $Id: UpdateUserAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.useradmin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.UserBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.ServletConstants;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.formbean.UserMaintenanceForm;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.Permissions;
import magoffin.matt.ma.xsd.User;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * Action to get a user to update.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class UpdateUserAction extends AbstractUserAdminAction {

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
	UserSessionData usd = getUserSessionData(request,ANONYMOUS_USER_NOT_OK);
	User actingUser = usd.getUser();
	
	UserMaintenanceForm uForm = (UserMaintenanceForm)form;
	
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	User user = userBiz.getUserById(uForm.getUser(), 
			ApplicationConstants.CACHED_OBJECT_NOT_ALLOWED);
	user = (User)BeanUtils.cloneBean(user);
	
	// set password to default
	uForm.setRealPassword(user.getPassword());
	user.setPassword(ServletConstants.UNASSIGNED_PASSWORD);
	uForm.setPasswordConfirm(ServletConstants.UNASSIGNED_PASSWORD);
	
	uForm.setCreating(false);
	uForm.setU(user);
	uForm.setActingUser(actingUser);
	
	if ( !actingUser.getUserId().equals(user.getUserId()) ) {
		Permissions perm = actingUser.getPermissions();
		if ( perm != null && (
				(perm.getAssignCreateUser() != null 
						&& perm.getAssignCreateUser().booleanValue() ) ||
						(perm.getAssignSuperUser() != null 
								&& perm.getAssignSuperUser().booleanValue() ) ) ) {
			uForm.setAssignPermissions(true);
			Permissions uPerm = userBiz.getUserPermissions(user.getUserId());
			user.setPermissions(uPerm);
		}
	} else {
		uForm.setAssignPermissions(false);
	}
	
	uForm.setAssignQuota(true);
	
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

}
