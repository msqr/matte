/* ===================================================================
 * AbstractAnonymousAlbumDataAction.java
 * 
 * Created Feb 6, 2004 8:43:31 AM
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
 * $Id: AbstractAnonymousMediaAlbumDataAction.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.UserAccessException;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.ServletUtil;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.xsd.MediaAlbumData;
import magoffin.matt.ma.xsd.MediaAlbumSettings;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * Base action for anonymous MediaAlbumData GUI actions.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public abstract class AbstractAnonymousMediaAlbumDataAction extends AbstractAction {

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
	UserSessionData usd = null;
	
	// see if settings stored in session, if not then forward to view-settings
	try {
		usd = this.getUserSessionData(request, ANONYMOUS_USER_OK);
	} catch ( UserAccessException e ) {
		// first access, so forward to view-settings
		ServletUtil.saveRequestURL(request);
		result.setForward(mapping.findForward("view-settings"));
		return;
	}
	
	MediaAlbumSettings settings = (MediaAlbumSettings)borrowPooledObject(
			MediaAlbumSettings.class);
	MediaAlbumData data = (MediaAlbumData)borrowPooledObject(MediaAlbumData.class);
	
	settings.setThumbnail(usd.getThumbSpec());
	settings.setSingle(usd.getSingleSpec());
	
	data.setSettings(settings);
	data.setUser(usd.getUser());
	data.setAdmin(usd.isAdmin());

	result.setData(data);
	
	goMediaAlbum(mapping,form,request,response,result,data, usd);
}

/**
 * Main Media Album method.
 * 
 * @param mapping the action mapping
 * @param form the form bean
 * @param request the request
 * @param response the response
 * @param result TODO
 * @param data the GUI data
 * @param usd the user session data
 * @throws Exception if an error occurs
 */
protected abstract void goMediaAlbum(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response,
		ActionResult result,
		MediaAlbumData data, UserSessionData usd)
throws Exception;

}
