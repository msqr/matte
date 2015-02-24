/* ===================================================================
 * UserAdminHomeAction.java
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
 * $Id: UserAdminHomeAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.useradmin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.struts.StrutsConstants;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * Action to generate the administration home page.
 * 
 * <p> Created on Nov 7, 2002 2:40:18 PM.</p>
 *
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class UserAdminHomeAction extends AbstractUserAdminAction 
{
	
	public static final String SESS_KEY_USER_SEARCH_DATA = "userSearchForm";
	

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
	
	if ( request.getSession().getAttribute(SESS_KEY_HAS_SEARCHED) != null ) {
		result.setForward(mapping.findForward("search"));
	}
	
	getUserAdminDataForCurrentUser(request,form, result); // don't need data

	result.setXslTemplate(USER_ADMIN_HOME_TEMPLATES_KEY);
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

}
