/* ===================================================================
 * ManageThemesAction.java
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
 * $Id: ManageThemesAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.theme;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.ThemeBiz;
import magoffin.matt.ma.biz.UserBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.ServletConstants;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.AlbumTheme;
import magoffin.matt.ma.xsd.ThemeData;
import magoffin.matt.ma.xsd.ThemeMetaData;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

/**
 * Allow user to manage their themes.
 * 
 * <p> Created on Feb 3, 2003 1:07:42 PM.</p>
 *
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class ManageThemesAction extends AbstractThemeAction 
{

	/** The Xform XSL key: <code>manage-themes</code>. */
	public static final String MANAGE_THEMES_TEMPLATES_KEY = "manage-themes";

/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.theme.AbstractThemeAction#goTheme(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, magoffin.matt.ma.xsd.ThemeData, magoffin.matt.ma.servlet.UserSessionData)
 */
protected void goTheme(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response,
		ActionResult result, ThemeData data, UserSessionData usd) throws Exception 
{	
	DynaActionForm dForm = (DynaActionForm)form;
	
	// get all themes browsable for user
	ThemeBiz themeBiz = (ThemeBiz)getBiz(BizConstants.THEME_BIZ);
	AlbumTheme[] themes = themeBiz.getAlbumThemesEditableForUser(
			usd.getUser(),ApplicationConstants.CACHED_OBJECT_ALLOWED);
		
	if ( themes != null ) {
		data.setTheme(themes);

		// populate meta
		for ( int i = 0; i < themes.length; i++ ) {
			ThemeMetaData meta = themeBiz.getThemeMetaData(themes[i].getThemeId(),
					ApplicationConstants.CACHED_OBJECT_ALLOWED);
			data.addThemeMeta(meta);
		}
	}
	
	// if user is NOT an admin user, add global themes 
	// (global themes are editable for admin users)
	
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	if ( !userBiz.isUserSuperUser(usd.getUser().getUserId()) ) {
		AlbumTheme[] globalThemes = themeBiz.getGlobalAlbumThemes(
				usd.getUser(),
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
		if ( globalThemes != null ) {
			data.setGlobalTheme(globalThemes);

			// populate meta
			for ( int i = 0; i < globalThemes.length; i++ ) {
				ThemeMetaData meta = themeBiz.getThemeMetaData(
						globalThemes[i].getThemeId(),
						ApplicationConstants.CACHED_OBJECT_ALLOWED);
				data.addThemeMeta(meta);
			}
		}
	}
	
	Integer displayThemeId = (Integer)dForm.get(ServletConstants.REQ_KEY_THEME_ID);
	if ( displayThemeId != null ) {
		data.setDisplayTheme(displayThemeId.intValue());
	}
		
	result.setXslTemplate(MANAGE_THEMES_TEMPLATES_KEY);
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

}
