/* ===================================================================
 * AbstractAdminAction.java
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
 * $Id: AbstractAdminAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.MessageConstants;
import magoffin.matt.ma.NotAuthorizedException;
import magoffin.matt.ma.UserAccessException;
import magoffin.matt.ma.biz.AdminBiz;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractAction;
import magoffin.matt.ma.xsd.AdminData;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * Base action class for admin actions.
 * 
 * <p> Created on Nov 7, 2002 2:41:02 PM.</p>
 *
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public abstract class AbstractAdminAction extends AbstractAction 
{
	/** The XSL template key for the admin home. */
	public static final String ADMIN_HOME_TEMPLATES_KEY = "admin";

/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.AbstractAction#go(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 */
protected final void go(
	ActionMapping mapping,
	ActionForm form,
	HttpServletRequest request,
	HttpServletResponse response, 
	ActionResult result)
	throws Exception 
{
	UserSessionData usd = getUserSessionData(request, ANONYMOUS_USER_NOT_OK);
	
	AdminData data = (AdminData)borrowPooledObject(AdminData.class);
	try {
		AdminBiz adminBiz = (AdminBiz)getBiz(BizConstants.ADMIN_BIZ);
		
		data.setUser(usd.getUser());
		data.setAdmin(usd.isAdmin());
		data.setCacheStatus(adminBiz.getCacheStatuses(null));
		data.setPoolStatus(adminBiz.getObjectPoolStatuses());
		
		goAdmin(mapping,form,request,response,result,data, usd);
	} finally {
		if ( result.getData() == null ) {
			// return our borrowed data
			returnPooledObject(data);
		}
	}
}

/**
 * Main Media Album method.
 * 
 * @param mapping the action mapping
 * @param form the form bean
 * @param request the request
 * @param response the response
 * @param result the action result
 * @param data the GUI data
 * @param usd the user session data
 * @throws Exception if an error occurs
 */
protected abstract void goAdmin(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response,
		ActionResult result,
		AdminData data, 
		UserSessionData usd)
throws Exception;


/**
 * Get the currently logged in user session data.
 * 
 * <p>If the currently logged in user is not an admin user, a
 * <code>NotAuthorizedException</code> will be thrown.</p>
 * 
 * @param req
 * @param allowAnonymous
 * @return the current UserSessionData object
 * @throws UserAccessException
 */
protected final UserSessionData getUserSessionData(HttpServletRequest req, boolean allowAnonymous)
throws UserAccessException 
{
	UserSessionData usd =  super.getUserSessionData(req, ANONYMOUS_USER_NOT_OK);
	if ( !usd.isAdmin() ) {
		throw new NotAuthorizedException(usd.getUser().getUsername(),MessageConstants.ERR_AUTH_ADMIN_REQUIRED);
	}
	return usd;
}

}
