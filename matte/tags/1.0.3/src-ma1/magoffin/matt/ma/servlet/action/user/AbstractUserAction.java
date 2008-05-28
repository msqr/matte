/* ===================================================================
 * AbstractUserAction.java
 * 
 * Created Dec 1, 2003 9:16:18 AM
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
 * $Id: AbstractUserAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.user;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.ServletConstants;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractAction;
import magoffin.matt.ma.xsd.UserData;

import org.apache.struts.action.ActionForward;

/**
 * Abstract action for user maintenance.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public abstract class AbstractUserAction extends AbstractAction {

	/** The Xform XSL key for user maintenance: <code>user-maint</code>. */
	public static final String USER_MAINTENANCE_TEMPLATES_KEY = "user-maint";

/**
 * Get a UserData instance for the currently logged in user.
 * 
 * @param request the current request
 * @param result TODO
 * @return UserData
 */
protected final UserData getUserDataForCurrentUser(
	HttpServletRequest request,
	ActionResult result )
	throws MediaAlbumException
{
	UserSessionData usd = getUserSessionData(request, ANONYMOUS_USER_NOT_OK);
	UserData data = (UserData)borrowPooledObject(UserData.class);
	data.setUser(usd.getUser());
	data.setUser(usd.getUser());
	result.setData(data);
	return data;
}

/**
 * Issue a redirect response header to a URL represented by an ActionForward along
 * with a URL parameter for a group ID.
 * 
 * @param groupId the group ID to add as a URL parameter
 * @param request the request
 * @param response the response
 * @param forward the ActionForward to generate the redirect URL from
 * @return <em>null</em>, so can be returned by calling action class
 * @throws IOException if an IO error occurs
 */
protected final ActionForward redirectToGroup(
		Object groupId, 
		HttpServletRequest request,
		HttpServletResponse response,
		ActionForward forward)
throws IOException
{
	return redirectWithParam(request,response,forward,
			ServletConstants.REQ_KEY_GROUP_ID,groupId);
}

}
