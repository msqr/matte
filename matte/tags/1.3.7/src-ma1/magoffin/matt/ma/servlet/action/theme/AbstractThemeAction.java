/* ===================================================================
 * AbstractThemeAction.java
 *
 * Copyright (c) 2002-2003 Matt Magoffin.
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
 * $Id: AbstractThemeAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.theme;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.ServletConstants;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractAction;
import magoffin.matt.ma.xsd.ThemeData;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * Abstract base class for theme-related actions.
 * 
 * <p> Created on Feb 3, 2003 1:15:57 PM.</p>
 *
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public abstract class AbstractThemeAction extends AbstractAction 
{
	/** 
	 * Error message key for when a theme is not found, it accepts a single parameter: 
	 * <code>error.theme.notfound</code>. 
	 */
	public static final String ERROR_THEME_NOT_FOUND = "error.theme.notfound";
	
	/** 
	 * Error message key for when a theme is forbidden, it accepts a single parameter: 
	 * <code>error.theme.forbidden</code>. 
	 */
	public static final String ERROR_THEME_FORBIDDEN = "error.theme.forbidden";

/**
 * Issue a redirect response header to a URL represented by an ActionForward along
 * with a URL parameter for a theme ID.
 * 
 * @param themeId the theme ID to add as a URL parameter
 * @param request the request
 * @param response the response
 * @param forward the ActionForward to generate the redirect URL from
 * @return <em>null</em>, so can be returned by calling action class
 * @throws IOException if an IO error occurs
 */
protected final ActionForward redirectToTheme(
	Object themeId, 
	HttpServletRequest request,
	HttpServletResponse response,
	ActionForward forward)
	throws IOException
{
	return redirectWithParam(request,response,forward,
			ServletConstants.REQ_KEY_THEME_ID,themeId);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.AbstractAction#go(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 */
protected final void go(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response, ActionResult result)
		throws Exception 
{
	UserSessionData usd = getUserSessionData(request, ANONYMOUS_USER_NOT_OK);
	
	ThemeData data = (ThemeData)borrowPooledObject(ThemeData.class);
	
	data.setUser(usd.getUser());
	data.setAdmin(usd.isAdmin());
	
	result.setData(data);
	goTheme(mapping,form,request,response,result,data, usd);
}


/**
 * Perform theme action.
 * 
 * @param mapping the action mappping
 * @param form the form bean
 * @param request the request
 * @param response the response
 * @param result TODO
 * @param data a ThemeData instance
 * @param usd the user session data
 * @throws Exception if an error occurs
 */
protected abstract void goTheme(ActionMapping mapping, ActionForm form, 
		HttpServletRequest request, HttpServletResponse response, ActionResult result, 
		ThemeData data, UserSessionData usd) throws Exception;

}
