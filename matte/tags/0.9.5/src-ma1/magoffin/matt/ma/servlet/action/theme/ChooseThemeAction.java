/* ===================================================================
 * ChooseThemeAction.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Mar 12, 2004 6:11:21 PM.
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
 * $Id: ChooseThemeAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.theme;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.biz.AlbumBiz;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.ThemeBiz;
import magoffin.matt.ma.biz.UserBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.ServletConstants;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractAction;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.AlbumTheme;
import magoffin.matt.ma.xsd.User;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.DynaActionForm;

/**
 * Action to choose a theme for an album or the default theme for a user.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class ChooseThemeAction extends AbstractAction 
{

/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.AbstractAction#go(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, magoffin.matt.ma.servlet.ActionResult)
 */
protected void go(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response,
		ActionResult result) throws Exception 
{
	UserSessionData usd = getUserSessionData(request,ANONYMOUS_USER_NOT_OK);
	DynaActionForm dForm = (DynaActionForm)form;
	
	Integer albumId = (Integer)dForm.get(ServletConstants.REQ_KEY_ALBUM_ID);
	Integer themeId = (Integer)dForm.get(ServletConstants.REQ_KEY_THEME_ID);
	
	// use album ID first
	if ( albumId != null ) {
		AlbumBiz albumBiz = (AlbumBiz)getBiz(BizConstants.ALBUM_BIZ);
		albumBiz.setAlbumTheme(themeId,albumId,usd.getUser());
		
		// add message
		this.addActionMessage(request, ActionMessages.GLOBAL_MESSAGE,
				new ActionMessage("album.theme.saved"));
	} else if ( themeId != null ) {
		User currUser = usd.getUser();
		UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
		User u = userBiz.getUserById(currUser.getUserId(),
				ApplicationConstants.CACHED_OBJECT_NOT_ALLOWED);
		ThemeBiz themeBiz = (ThemeBiz)getBiz(BizConstants.THEME_BIZ);
		AlbumTheme defaultTheme = themeBiz.getDefaultAlbumTheme();
		AlbumTheme theme = themeBiz.getAlbumThemeById(themeId,currUser,
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
		
		boolean changed = false;
		boolean forDefault = ((Boolean)dForm.get("forDefault")).booleanValue();
		
		if ( forDefault ) {
			if ( themeId.equals(defaultTheme.getThemeId()) ) {
				if ( u.getDefaultThemeId() != null ) {
					u.setDefaultThemeId(null);
					changed = true;
				}
			} else {
				if ( !themeId.equals(u.getDefaultThemeId()) ) {
					u.setDefaultThemeId(themeId);
					changed = true;
				}
			}
		} else {
			if ( themeId.equals(defaultTheme.getThemeId()) ) {
				if ( u.getThemeId() != null ) {
					u.setThemeId(null);
					changed = true;
				}
			} else {
				if ( !themeId.equals(u.getThemeId()) ) {
					u.setThemeId(themeId);
					changed = true;
				}
			}
		}
		
		if ( changed ) {
			u = userBiz.updateUser(u,currUser);
			usd.setUser(u);
		}
		
		if ( forDefault ) {
			addActionMessage(request,ActionMessages.GLOBAL_MESSAGE,
					new ActionMessage("user.defaultTheme.saved",theme.getName()));
		} else {
			addActionMessage(request,ActionMessages.GLOBAL_MESSAGE,
				new ActionMessage("user.theme.saved",theme.getName()));
		}
	}
	
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

}
