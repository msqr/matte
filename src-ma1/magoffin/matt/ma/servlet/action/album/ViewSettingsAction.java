/* ===================================================================
 * ViewSettingsAction.java
 * 
 * Copyright (c) 2003 Matt Magoffin. Created Mar 2, 2003.
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
 * $Id: ViewSettingsAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.album;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.UserAccessException;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractAction;
import magoffin.matt.ma.servlet.struts.StrutsConstants;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

/**
 * Action to set settings for viewing album.
 * 
 * <p> Created on Nov 12, 2002 5:44:47 PM.</p>
 *
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class ViewSettingsAction extends AbstractAction 
{

/**
 * @see magoffin.matt.ma.servlet.action.AbstractAction#go(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, ActionResult)
 */
protected void go(
	ActionMapping mapping,
	ActionForm form,
	HttpServletRequest request,
	HttpServletResponse response, 
	ActionResult result)
throws Exception 
{
	reSaveRequestURL(request);

	// see if need to trick refresh
	try {
		UserSessionData usd = getUserSessionData(request, ANONYMOUS_USER_OK);
	} catch ( UserAccessException e ) {
		DynaActionForm dForm = (DynaActionForm)form;
		Boolean refreshed = (Boolean)dForm.get("refreshed");
		if ( refreshed == null || !refreshed.booleanValue() ) {
			// redirect!
			result.setForward(mapping.findForward("refresh"));
			return;
		}
	}

	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

}
