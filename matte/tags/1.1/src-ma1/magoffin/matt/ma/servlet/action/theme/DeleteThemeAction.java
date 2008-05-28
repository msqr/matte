/* ===================================================================
 * DeleteThemeAction.java
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
 * $Id: DeleteThemeAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.theme;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.ThemeBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.ServletConstants;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.AlbumTheme;
import magoffin.matt.ma.xsd.ThemeData;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.DynaActionForm;

/**
 * Allow user to delete a theme.
 * 
 * <p>Only the owner of a theme or an admin is allowed to delete.</p>
 * 
 * <p> Created on Feb 6, 2003 2:07:56 PM.</p>
 *
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class DeleteThemeAction extends AbstractThemeAction 
{
	private static final Logger LOG = Logger.getLogger(DeleteThemeAction.class);
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.theme.AbstractThemeAction#goTheme(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, magoffin.matt.ma.xsd.ThemeData, magoffin.matt.ma.servlet.UserSessionData)
 */
protected void goTheme(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response,
		ActionResult result, ThemeData data, UserSessionData usd) throws Exception 
{	
	DynaActionForm dForm = (DynaActionForm)form;
	
	Integer themeId = (Integer)dForm.get(ServletConstants.REQ_KEY_THEME_ID);
	
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Delete theme: themeId = " +themeId);
	}
	
	ThemeBiz themeBiz = (ThemeBiz)getBiz(BizConstants.THEME_BIZ);
	AlbumTheme theme = themeBiz.getAlbumThemeById(themeId,usd.getUser(),
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
	try {
		themeBiz.deleteAlbumTheme(themeId,usd.getUser());
	} catch ( MediaAlbumException e ) {
		LOG.error("Exception deleting theme " +themeId +": " +e.getMessage());
		addActionMessage(request, ActionErrors.GLOBAL_ERROR,
			new ActionError("delete.theme.error.general",e.getMessage()));
		result.setForward(mapping.findForward(StrutsConstants.DEFAULT_ERROR_FORWARD));
		return;
	}
	
	addActionMessage(request,ActionMessages.GLOBAL_MESSAGE,
		new ActionMessage("delete.theme.ok", theme.getName()));
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

} 
