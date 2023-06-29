/* ===================================================================
 * AbstractUserAdminAction.java
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
 * $Id: AbstractUserAdminAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.useradmin;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.DynaActionForm;

import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.UserAccessException;
import magoffin.matt.ma.biz.UserBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractAction;
import magoffin.matt.ma.xsd.AdminData;
import magoffin.matt.ma.xsd.UserSearchData;

/**
 * Base action class for user admin actions.
 * 
 * <p> Created on Nov 7, 2002 2:41:02 PM.</p>
 *
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public abstract class AbstractUserAdminAction extends AbstractAction 
{
	/** The XSL template key for the user admin home. */
	public static final String USER_ADMIN_HOME_TEMPLATES_KEY = "user-admin";
	
	/** The default search mode if none provided: {@link UserBiz#USER_SEARCH_USERNAME}*/
	public static final Integer DEFAULT_USER_SEARCH_MODE = 
		new Integer(UserBiz.USER_SEARCH_USERNAME);
	
	/** The session key set after a user performs a user search. */
	public static final String SESS_KEY_HAS_SEARCHED = "ma.user.search";

/**
 * Get an AdminData object for the currently logged in user.
 *  * @param req
 * @param result TODO
 * @return AdminData * @throws MediaAlbumException */
protected final AdminData getUserAdminDataForCurrentUser(
		HttpServletRequest req, 
		ActionForm form, 
		ActionResult result)
throws MediaAlbumException
{
	UserSessionData usd = this.getUserSessionData(req, ANONYMOUS_USER_NOT_OK);
	AdminData data = (AdminData)borrowPooledObject(AdminData.class);
	data.setUser(usd.getUser());
	
	DynaActionForm dForm = (DynaActionForm)form;
	UserSearchData searchData = new UserSearchData();
	
	searchData.setEmail((String)dForm.get("email"));
	searchData.setName((String)dForm.get("name"));
	searchData.setUsername((String)dForm.get("username"));
	
	Integer mode = (Integer)dForm.get("mode");
	if ( mode == null || mode.intValue() == 0 ) {
		mode = DEFAULT_USER_SEARCH_MODE;
	}
	searchData.setMode(mode.intValue());
	
	data.setUserSearch(searchData);
	
	result.setData(data);
	return data;
}


/**
 * Get the currently logged in user session data.
 * 
 * <p>If the currently logged in user is not an admin user, a
 * <code>NotAuthorizedException</code> will be thrown.</p>
 * 
 * @param req
 * @param allowAnonymous
 * @return
 * @throws UserAccessException
 */
protected UserSessionData getUserSessionData(HttpServletRequest req, boolean allowAnonymous)
throws UserAccessException 
{
	UserSessionData usd =  super.getUserSessionData(req, ANONYMOUS_USER_NOT_OK);
	/*
	if ( !usd.isAdmin() ) {
		throw new NotAuthorizedException(usd.getUser().getUsername(),MessageConstants.ERR_AUTH_ADMIN_REQUIRED);
	}
	*/
	return usd;
}

}
