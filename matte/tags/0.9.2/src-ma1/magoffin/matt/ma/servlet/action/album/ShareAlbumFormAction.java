/* ===================================================================
 * ShareAlbumFormAction.java
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
 * $Id: ShareAlbumFormAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.album;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MessageConstants;
import magoffin.matt.ma.NotAuthorizedException;
import magoffin.matt.ma.biz.AlbumBiz;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.UserBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractAction;
import magoffin.matt.ma.servlet.formbean.ShareAlbumForm;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.Album;
import magoffin.matt.ma.xsd.AlbumPermissions;
import magoffin.matt.ma.xsd.User;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * Action to display the form to allow a user to share an album.
 * 
 * <p>Created Nov 8, 2002 3:51:18 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class ShareAlbumFormAction extends AbstractAction 
{
	private static Logger log = Logger.getLogger(ShareAlbumFormAction.class);

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
	ShareAlbumForm saForm = (ShareAlbumForm)form;
	
	Integer albumId = saForm.getAlbum();
	if ( albumId != null && albumId.intValue() == 0 ) {
		albumId = null;
	}
	
	if ( albumId == null ) {
		notFound(mapping,request, response);
		return;
	}
	
	if ( log.isDebugEnabled() ) {
		log.debug("Got albumId = " +albumId);
	}
	
	UserSessionData usd = this.getUserSessionData(request, ANONYMOUS_USER_NOT_OK);
	User user = usd.getUser();
	
	AlbumBiz albumBiz = (AlbumBiz)getBiz(BizConstants.ALBUM_BIZ);
	
	Album album = albumBiz.getAlbumById(albumId,user,
			ApplicationConstants.CACHED_OBJECT_NOT_ALLOWED);
	
	if ( !albumBiz.canUserUpdateAlbum(user,albumId) ) {
		throw new NotAuthorizedException(user.getUsername(),
				MessageConstants.ERR_AUTH_UPDATE_ALBUM);
	}
	
	Album[] children = albumBiz.getAlbumChildren(albumId);
	if ( children != null && children.length > 0 ) {
		saForm.setHasChildren(true);
	} else {
		saForm.setHasChildren(false);
	}
	
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	saForm.setFriendList(userBiz.getFriendsForUser(user.getUserId()));
	saForm.setGroupList(userBiz.getGroupsForUserId(user.getUserId()));
	
	saForm.setShared(false);
	
	AlbumPermissions[] perm = album.getPermissions();
	if ( perm != null ) {
		List friendList = new ArrayList(perm.length);
		List groupList = new ArrayList(perm.length);
		for ( int i = 0; i < perm.length; i++ ) {
			if ( perm[i].getUserId() != null ) {
				friendList.add(perm[i].getUserId());
			} else if ( perm[i].getGroupId() != null ) {
				groupList.add(perm[i].getGroupId());
			}
		}
		saForm.setFriends((Integer[])friendList.toArray(
				new Integer[friendList.size()]));
		saForm.setGroups((Integer[])groupList.toArray(
				new Integer[groupList.size()]));
		
		if ( friendList.size() > 0 || groupList.size() > 0 ) {
			saForm.setShared(true);
		}
	}
	
	saForm.setAllowAnonymous(album.getAllowAnonymous());
	saForm.setAllowOriginal(album.getAllowOriginal());
	saForm.setMediaAlbum(album);
	
	if ( album.getAllowAnonymous() != null && album.getAllowAnonymous().booleanValue() ) {
		saForm.setShared(true);
	}
	
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

}
