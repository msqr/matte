/* ===================================================================
 * UpdateAlbumAction.java
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
 * $Id: UpdateAlbumAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.album;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MessageConstants;
import magoffin.matt.ma.NotAuthorizedException;
import magoffin.matt.ma.biz.AlbumBiz;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.ThemeBiz;
import magoffin.matt.ma.biz.UserBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.ServletConstants;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractMediaAlbumDataAction;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.Album;
import magoffin.matt.ma.xsd.AlbumTheme;
import magoffin.matt.ma.xsd.MediaAlbumData;
import magoffin.matt.ma.xsd.User;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

/**
 * Update an album action.
 * 
 * <p>Created Oct 26, 2002 7:23:31 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class UpdateAlbumAction extends AbstractMediaAlbumDataAction 
{

	private static Logger log = Logger.getLogger(UpdateAlbumAction.class);
	
	/** The Xform XSL key: <code>update-album</code>. */
	public static final String UPDATE_ALBUM_TEMPLATES_KEY = "update-album";
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.AbstractMediaAlbumDataAction#goMediaAlbum(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, magoffin.matt.ma.servlet.ActionResult, magoffin.matt.ma.xsd.MediaAlbumData, magoffin.matt.ma.servlet.UserSessionData)
 */
protected void goMediaAlbum(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response,
		ActionResult result, MediaAlbumData data, UserSessionData usd)
		throws Exception 
{
	DynaActionForm dForm = dForm = (DynaActionForm)form;
	Integer albumId = (Integer)dForm.get(ServletConstants.REQ_KEY_ALBUM_ID);
	if ( albumId != null && albumId.intValue() == 0 ) {
		albumId = null;
	}
	Integer collectionId = (Integer)dForm.get(ServletConstants.REQ_KEY_COLLECTION_ID);
	if ( collectionId != null && collectionId.intValue() == 0 ) {
		collectionId = null;
	}
	if ( albumId == null && request.getAttribute(ServletConstants.REQ_KEY_ALBUM_ID) != null ) {
		albumId = (Integer)request.getAttribute(ServletConstants.REQ_KEY_ALBUM_ID);
	}
	if ( collectionId == null && request.getAttribute(ServletConstants.REQ_KEY_COLLECTION_ID) != null ) {
		collectionId = (Integer)request.getAttribute(ServletConstants.REQ_KEY_COLLECTION_ID);
	}
	
	if ( albumId == null ) {
		notFound(mapping,request, response);
		return;
	}
	
	if ( log.isDebugEnabled() ) {
		log.debug("Got albumId = " +albumId +", collectionId = " +collectionId);
	}
	
	User user = usd.getUser();
	
	AlbumBiz albumBiz = (AlbumBiz)getBiz(BizConstants.ALBUM_BIZ);
	
	Album album = albumBiz.getAlbumById(albumId,user,
			ApplicationConstants.CACHED_OBJECT_NOT_ALLOWED);
	
	if ( !albumBiz.canUserUpdateAlbum(user,albumId) ) {
		throw new NotAuthorizedException(user.getUsername(),MessageConstants.ERR_AUTH_UPDATE_ALBUM);
	}
	
	if ( collectionId != null ) {
		// verify user has permission to view dir
		this.verifyUserCanViewCollection(user,collectionId);
	} else {
		// use the first dir for the user
		collectionId = usd.getCollections()[0].getCollectionId();
		if ( log.isDebugEnabled() ) {
			log.debug("Defaulting to collectionId = " +collectionId);
		}
	}
	
	// get album theme
	if ( album.getThemeId() != null ) {
		ThemeBiz themeBiz = (ThemeBiz)getBiz(BizConstants.THEME_BIZ);
		AlbumTheme theme = themeBiz.getAlbumThemeById(
				album.getThemeId(),usd.getUser(),
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
		if ( theme == null ) {
			this.addActionMessage(request, ActionErrors.GLOBAL_ERROR,
					new ActionError("update.album.error.theme.notfound"));
		} else {
			data.setTheme(theme);
		}
	}
	
	// need to populate all items for selected collectionId, as well as all items in current album
	this.populateCollectionsForUser(data,collectionId,user);

	album.setItem(albumBiz.getMediaItemsForAlbum(album.getAlbumId(),
			ApplicationConstants.POPULATE_MODE_NONE, ApplicationConstants.CACHED_OBJECT_NOT_ALLOWED, user));
	data.setDisplayAlbum(albumId.intValue());
	//data.addAlbum(album);

	// get albums for user, and items for album if albumId provided
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	Album[] albums = userBiz.getAlbumsOwnedByUser(user.getUserId());
	if ( albums != null && albums.length > 0 ) {
		if ( albumId != null ) {
			populateItems(albumId.intValue(),albums,user, 
					ApplicationConstants.POPULATE_MODE_NONE);
		}
		if ( log.isDebugEnabled() ) {
			for ( int i = 0; i < albums.length; i++ ) {
				log.debug("Added Album to MediaAlbumData: " +albums[i].getAlbumId());
			}
		}
		data.setAlbum(albums);
	}
		
	result.setXslTemplate(UPDATE_ALBUM_TEMPLATES_KEY);
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

}
