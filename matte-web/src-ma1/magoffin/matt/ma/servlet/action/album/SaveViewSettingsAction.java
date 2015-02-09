/* ===================================================================
 * SaveViewSettingsAction.java
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
 * $Id: SaveViewSettingsAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.album;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import magoffin.matt.ma.UserAccessException;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.ServletConstants;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractAction;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.util.MediaSpecUtil;
import magoffin.matt.ma.xsd.MediaSpec;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

/**
 * Action to save the user's album viewing settings.
 * 
 * <p>This only saves the settings into session, not the back end.</p>
 * 
 * <p> Created on Nov 12, 2002 12:58:35 PM.</p>
 *
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class SaveViewSettingsAction extends AbstractAction 
{

/**
 * @see magoffin.matt.ma.servlet.action.AbstractAction#go(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, ActionResult)
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
		usd = this.getUserSessionData(request, ANONYMOUS_USER_OK);
	} catch ( UserAccessException e ) {
		// first access, so create a new anonymous session data
		usd = new UserSessionData();
		HttpSession session = request.getSession();
		session.setAttribute(ServletConstants.SES_KEY_USER,usd);
	}
	
	DynaActionForm dForm = (DynaActionForm)form;
	String size = (String)dForm.get("size");
	String qual = (String)dForm.get("quality");
	
	MediaSpec spec = MediaSpecUtil.getImageSpec(size,qual);
	if ( spec == null ) {
		this.addActionMessage(request, ActionErrors.GLOBAL_ERROR,
			new ActionError("view.settings.spec.error"));
	} else {
		usd.setSingleSpec(spec);
	}
	
	if ( usd.getThumbSpec() == null ) {
		// give default thumbnail spec
		usd.setThumbSpec(MediaSpecUtil.DEFAULT_THUMB_SPEC);
	}
	
	result.setForward(redirectToSavedURL(request,response,
			mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD)));
}

}
