/* ===================================================================
 * WatchAlbumAction.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Apr 27, 2004 7:00:12 PM.
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
 * $Id: WatchAlbumAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.album;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.EmailNotificationBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractAction;
import magoffin.matt.ma.servlet.formbean.WatchForm;
import magoffin.matt.ma.servlet.struts.StrutsConstants;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

/**
 * Action to start/stop watching an album.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class WatchAlbumAction extends AbstractAction 
{
/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.AbstractAction#go(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, magoffin.matt.ma.servlet.ActionResult)
 */
protected void go(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response,
		ActionResult result) throws Exception 
{
	UserSessionData usd = getUserSessionData(request,ANONYMOUS_USER_NOT_OK);
	WatchForm dForm = (WatchForm)form;
	
	EmailNotificationBiz alertBiz = (EmailNotificationBiz)getBiz(
			BizConstants.EMAIL_NOTIFICATIONS_BIZ);
	
	String msg = null;
	
	if ( dForm.isWatch() ) {
		alertBiz.watchForNewItemsInAlbum(dForm.getAlbum(),usd.getUser());
		msg = EmailNotificationBiz.MSG_WATCH_ALBUM_ENABLED;
	} else {
		alertBiz.stopWatchingForNewItemsInAlbum(dForm.getAlbum(),usd.getUser());
		msg = EmailNotificationBiz.MSG_WATCH_ALBUM_DISABLED;
	}
	
	addActionMessage(request,ActionMessages.GLOBAL_MESSAGE, new ActionMessage(msg));
	
	if ( dForm.getKey() != null ) {
		// ok, in KEY mode, so in album slideshow OR browse
		if ( dForm.getBrowsePage() != null ) {
			result.setForward(mapping.findForward(StrutsConstants.BROWSE_ALBUMS_FORWARD));
		} else {
			// slideshow
			result.setForward(mapping.findForward(StrutsConstants.ALBUM_SLIDESHOW_FORWARD));
		}
	}
}

}
