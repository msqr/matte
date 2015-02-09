/* ===================================================================
 * EditThemeAction.java
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
 * $Id: EditThemeAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.theme;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.ThemeBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.formbean.UploadThemeForm;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.AlbumTheme;
import magoffin.matt.ma.xsd.ThemeData;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * Allow user to modify an existing theme.
 * 
 * <p>Created Feb 7, 2003 5:45:46 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class EditThemeAction extends AbstractThemeAction 
{

/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.theme.AbstractThemeAction#goTheme(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, magoffin.matt.ma.xsd.ThemeData, magoffin.matt.ma.servlet.UserSessionData)
 */
protected void goTheme(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response,
		ActionResult result, ThemeData data, UserSessionData usd) throws Exception 
{	
	UploadThemeForm uForm = (UploadThemeForm)form;
	
	ThemeBiz themeBiz = (ThemeBiz)getBiz(BizConstants.THEME_BIZ);
	AlbumTheme theme = themeBiz.getAlbumThemeById(uForm.getTheme(),usd.getUser(),
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
	uForm.setName(theme.getName());
	uForm.setAuthor(theme.getAuthor());
	uForm.setEmail(theme.getAuthorEmail());
	uForm.setDescription(theme.getComment());
	uForm.setGlobal(theme.getGlobal().booleanValue());
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

}
