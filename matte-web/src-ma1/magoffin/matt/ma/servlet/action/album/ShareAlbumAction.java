/* ===================================================================
 * ShareAlbumAction.java
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
 * $Id: ShareAlbumAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.album;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.biz.AlbumBiz;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.EmailNotificationBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractMediaAlbumDataAction;
import magoffin.matt.ma.servlet.formbean.ShareAlbumForm;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.Album;
import magoffin.matt.ma.xsd.AlbumPermissions;
import magoffin.matt.ma.xsd.MediaAlbumData;
import magoffin.matt.ma.xsd.User;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.RequestUtils;

/**
 * Action to allow a user to share an album.
 * 
 * <p>Created Nov 8, 2002 4:42:02 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class ShareAlbumAction extends AbstractMediaAlbumDataAction 
{
	/** 
	 * The message key for the cancel submit action: 
	 * <code>share.settings.cancel.displayName</code> 
	 */
	public static final String MSG_SUBMIT_ACTION_CANCEL = 
		"share.settings.cancel.displayName";
	
	/** 
	 * The message key for the upload submit action: 
	 * <code>share.settings.submit.displayName</code> 
	 */
	public static final String MSG_SUBMIT_ACTION_SHARE = 
		"share.settings.submit.displayName";

	private static Logger log = Logger.getLogger(ShareAlbumFormAction.class);

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
	ShareAlbumForm saForm = (ShareAlbumForm)form;
	
	// check submit action
	//String shareAction = this.getResources(request).getMessage(MSG_SUBMIT_ACTION_SHARE);
	String cancelAction = this.getResources(request).getMessage(MSG_SUBMIT_ACTION_CANCEL);
	
	Integer albumId = saForm.getAlbum();
	if ( albumId != null && albumId.intValue() == 0 ) {
		albumId = null;
	}
	
	if ( saForm.getSubmitAction() == null || cancelAction.equals(saForm.getSubmitAction()) ) {
		ActionForward cancel = mapping.findForward(StrutsConstants.DEFAULT_CANCEL_FORWARD);
		redirectToAlbum(albumId,request,response,cancel);
		return;
	}
	
	if ( albumId == null ) {
		notFound(mapping,request, response);
		return;
	}
	
	User user = usd.getUser();
	
	AlbumBiz albumBiz = (AlbumBiz)getBiz(BizConstants.ALBUM_BIZ);
	
	Album album = albumBiz.getAlbumById(albumId,user,ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
	Boolean allowAnon = saForm.getAllowAnonymous();
	Boolean allowOrig = saForm.getAllowOriginal();
	Boolean recurse = saForm.getRecurse();
	
	if ( log.isDebugEnabled() ) {
		log.debug("Got albumId = " +albumId +", allowAnon = " +allowAnon 
			+", allowOrig = " +allowOrig);
	}
	
	if ( recurse != null && recurse.booleanValue() ) {
		albumBiz.fillInChildAlbums(album,null,ApplicationConstants.POPULATE_MODE_NONE, user, AlbumBiz.UNLIMITED_DESCENT);
	}
	
	List permList = new ArrayList(10);
	if ( saForm.getFriends() != null && saForm.getFriends().length > 0 ) {
		Integer[] friends = saForm.getFriends();
		for ( int i = 0; i < friends.length; i++ ) {
			AlbumPermissions perm = new AlbumPermissions();
			perm.setPermId(album.getAlbumId());
			perm.setView(Boolean.TRUE);
			perm.setUserId(friends[i]);
			permList.add(perm);
		}
	}

	if ( saForm.getGroups() != null && saForm.getGroups().length > 0 ) {
		Integer[] groups = saForm.getGroups();
		for ( int i = 0; i < groups.length; i++ ) {
			AlbumPermissions perm = new AlbumPermissions();
			perm.setPermId(album.getAlbumId());
			perm.setView(Boolean.TRUE);
			perm.setGroupId(groups[i]);
			permList.add(perm);
		}
	}
	
	AlbumPermissions[] perms = (AlbumPermissions[])permList.toArray(
			new AlbumPermissions[permList.size()]);
	
	saveAlbumSettings(album,user,allowAnon,allowOrig,perms,albumBiz);		
	
	saForm.setMediaAlbum(album);
	
	ActionMessage msg = new ActionMessage("share.settings.saved");
	addActionMessage(request,ActionMessages.GLOBAL_MESSAGE,msg);
	
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
	
	if ( perms.length > 0 || allowAnon.booleanValue() ) {
		// send email notification for top-level album
		EmailNotificationBiz alertBiz = (EmailNotificationBiz)getBiz(
				BizConstants.EMAIL_NOTIFICATIONS_BIZ);
		URL viewAlbumUrl = RequestUtils.absoluteURL(request,
				mapping.findForward("view-album").getPath());
		URL browseUserUrl = RequestUtils.absoluteURL(request,
				mapping.findForward("browse-albums").getPath());
		alertBiz.processUpdatedAlbumNotifications(new Album[] {album},
				null,viewAlbumUrl,browseUserUrl);
	}
}

private void saveAlbumSettings(
		Album album, 
		User user, 
		Boolean allowAnon, 
		Boolean allowOrig, 
		AlbumPermissions[] perms,
		AlbumBiz albumBiz) 
throws MediaAlbumException
{
	// save this album's settings
	album.setAllowAnonymous(allowAnon);
	album.setAllowOriginal(allowOrig);
	albumBiz.updateAlbum(album,user);
	albumBiz.updateAlbumPermissions(album.getAlbumId(),perms,user);
	
	if ( album.getAlbumCount() < 1 ) {
		return;
	}

	// apply settings to any children albums present
	Album[] children = album.getAlbum();
	for ( int i = 0; i < children.length; i++ ) {
		saveAlbumSettings(children[i],user, allowAnon, allowOrig, perms, albumBiz);
	}
}

}