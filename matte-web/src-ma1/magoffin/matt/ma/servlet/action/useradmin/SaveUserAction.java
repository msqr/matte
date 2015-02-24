/* ===================================================================
 * SaveUserAction.java
 * 
 * Created Jan 16, 2004 9:20:21 AM
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
 * $Id: SaveUserAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.useradmin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.biz.ThemeBiz;
import magoffin.matt.ma.biz.UserBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.ServletConstants;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.formbean.UserMaintenanceForm;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.User;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

/**
 * Action to save a user to the backend.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class SaveUserAction extends AbstractUserAdminAction {
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.AbstractAction#go(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 */
protected void go(
	ActionMapping mapping,
	ActionForm form,
	HttpServletRequest request,
	HttpServletResponse response, ActionResult result)
	throws Exception 
{
	if ( this.isCancelled(request) ) {
		result.setForward(mapping.findForward("cancel"));
		return;
	}
	
	UserSessionData usd = getUserSessionData(request,ANONYMOUS_USER_NOT_OK);
	User actingUser = usd.getUser();
	UserMaintenanceForm uForm = (UserMaintenanceForm)form;
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);
	
	// handle user watermark if provided
	String watermarkPath = null;
	ThemeBiz themeBiz = (ThemeBiz)getBiz(BizConstants.THEME_BIZ);
	if ( uForm.isRemoveWatermark() ) {
		if ( actingUser.getWatermark() != null ) {
			themeBiz.deleteUserThemeResource(actingUser,actingUser.getWatermark());
			itemBiz.deleteUserCacheFiles(actingUser,actingUser);
		}
	} else if ( uForm.getWatermark() != null && uForm.getWatermark().getFileSize() > 0 ) {
		watermarkPath = uForm.getWatermark().getFileName();
		themeBiz.saveUserThemeResource(actingUser,
				uForm.getWatermark().getInputStream(),watermarkPath);
		itemBiz.deleteUserCacheFiles(actingUser,actingUser);
	} else if ( !uForm.isCreating() ){
		watermarkPath = uForm.getU().getWatermark();
	}
	
	if ( uForm.isCreating() ) {
		
		uForm.getU().setWatermark(watermarkPath);
		User newUser = userBiz.createUser(uForm.getU(),actingUser);
		
		this.addActionMessage(request,ActionMessages.GLOBAL_MESSAGE,
				new ActionMessage("user.create.ok",newUser.getUsername()));
		
	} else {
		
		User user = uForm.getU();
		if ( ServletConstants.UNASSIGNED_PASSWORD.equals(user.getPassword()) ) {
			user.setPassword(uForm.getRealPassword());
		}
		user.setWatermark(watermarkPath);
		user = userBiz.updateUser(user,actingUser);
		
		ActionMessage msg = null;

		if ( user.getUserId().equals(actingUser.getUserId()) ) {
			msg = new ActionMessage("my.settings.updated");
			// update session user
			result.setChangedUserSettings(true);
		}	else {
			msg = new ActionMessage("user.update.ok",user.getUsername());
		}
		
		this.addActionMessage(request,ActionMessages.GLOBAL_MESSAGE,msg);
	}
	
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

}
