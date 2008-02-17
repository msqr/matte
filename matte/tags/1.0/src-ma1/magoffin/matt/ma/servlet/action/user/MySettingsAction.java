/* ===================================================================
 * MySettingsAction.java
 * 
 * Created Feb 13, 2004 11:59:06 AM
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
 * $Id: MySettingsAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.FreeDataBiz;
import magoffin.matt.ma.biz.ThemeBiz;
import magoffin.matt.ma.biz.UserBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractMediaAlbumDataAction;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.AlbumTheme;
import magoffin.matt.ma.xsd.FreeData;
import magoffin.matt.ma.xsd.MediaAlbumData;
import magoffin.matt.ma.xsd.User;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * Action to view the My Settings page.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class MySettingsAction extends AbstractMediaAlbumDataAction {
	
	public static final String MY_SETTINGS_TEMPLATE_KEY = "my-settings";

/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.AbstractMediaAlbumDataAction#goMediaAlbum(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, magoffin.matt.ma.xsd.MediaAlbumData, magoffin.matt.ma.servlet.UserSessionData)
 */
public void goMediaAlbum(
	ActionMapping mapping,
	ActionForm form,
	HttpServletRequest request,
	HttpServletResponse response,
	ActionResult result,
	MediaAlbumData data, UserSessionData usd)
	throws Exception 
{
	User user = data.getUser();
	
	// get user's free data and add to user
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	FreeData[] fdata = userBiz.getFreeData(user,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	if ( fdata.length > 0 ) {
		FreeDataBiz fdBiz = (FreeDataBiz)getBiz(BizConstants.FREE_DATA_BIZ);
		fdBiz.populateFreeDataTypeNames(fdata,
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
		
		// check for user watches, and add as 'owners' to data 
		for ( int i = 0; i < fdata.length; i++ ) {
			if ( ApplicationConstants.FREE_DATA_TYPE_EMAIL_NOTIFICATION.equals(
					fdata[i].getDataTypeId() ) ) {
				if ( fdata[i].getUserId() != null ) {
					User owner = userBiz.getUserById(fdata[i].getUserId(),
							ApplicationConstants.CACHED_OBJECT_ALLOWED);
					data.addOwner(owner);
				}
			}
		}
		user.setData(fdata);
	}
	
	// get user's disk usage
	long du = userBiz.getDiskUsage(user.getUserId());
	if ( du > 0 ) {
		user.setDiskUsage(new Long(du));
	} else {
		user.setDiskUsage(null);
	}
	
	// get browse / default album theme data
	ThemeBiz themeBiz = (ThemeBiz)getBiz(BizConstants.THEME_BIZ);
	AlbumTheme browseTheme = null;
	AlbumTheme defTheme = null;
	if ( user.getThemeId() == null ) {
		browseTheme = themeBiz.getDefaultAlbumTheme();
	} else {
		browseTheme = themeBiz.getAlbumThemeById(user.getThemeId(),
				user,ApplicationConstants.CACHED_OBJECT_ALLOWED);
	}
	if ( user.getDefaultThemeId() == null ) {
		defTheme = themeBiz.getDefaultAlbumTheme();
	} else {
		defTheme = themeBiz.getAlbumThemeById(user.getDefaultThemeId(),
				user,ApplicationConstants.CACHED_OBJECT_ALLOWED);
	}
	
	data.addThemes(browseTheme);
	if ( !defTheme.getThemeId().equals(browseTheme.getThemeId()) ) {
		data.addThemes(defTheme);
	}
	
	result.setXslTemplate(MY_SETTINGS_TEMPLATE_KEY);
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

}
