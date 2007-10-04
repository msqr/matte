/* ===================================================================
 * AbstractMediaAlbumDataAction.java
 * 
 * Created Feb 6, 2004 9:19:02 AM
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
 * $Id: AbstractMediaAlbumDataAction.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.ThemeBiz;
import magoffin.matt.ma.biz.UserBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.xsd.Album;
import magoffin.matt.ma.xsd.AlbumTheme;
import magoffin.matt.ma.xsd.Group;
import magoffin.matt.ma.xsd.MediaAlbumData;
import magoffin.matt.ma.xsd.MediaAlbumSettings;
import magoffin.matt.ma.xsd.User;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * Base action for non-anonymous MediaAlbumData GUI actions.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public abstract class AbstractMediaAlbumDataAction extends AbstractAction 
{
	private static final Logger LOG = Logger.getLogger(AbstractMediaAlbumDataAction.class);

/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.AbstractAction#go(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 */
protected final void go(
	ActionMapping mapping,
	ActionForm form,
	HttpServletRequest request,
	HttpServletResponse response, ActionResult result)
	throws Exception 
{
	UserSessionData usd = getUserSessionData(request, ANONYMOUS_USER_NOT_OK);
	
	MediaAlbumSettings settings = (MediaAlbumSettings)borrowPooledObject(
			MediaAlbumSettings.class);
	MediaAlbumData data = (MediaAlbumData)borrowPooledObject(MediaAlbumData.class);
	
	settings.setThumbnail(usd.getThumbSpec());
	settings.setSingle(usd.getSingleSpec());
	
	data.setSettings(settings);
	data.setUser(usd.getUser());
	data.setAdmin(usd.isAdmin());
	
	result.setData(data);
	
	goMediaAlbum(mapping,form,request,response,result,data, usd);
}

/**
 * Set up necessary data on the MediaAlbumData object.
 * 
 * <p>This method will populate the following data:</p>
 * 
 * <ol>
 * <li>Set <var>displayAlbum</var> to <var>albumId</var> if not <em>null</em></li>
 * <li>Call {@link AbstractAction#populateCollectionsForUser(MediaAlbumData, Integer, User)}
 * passing <var>collectionId</var> if <var>albumId</var> is <em>null</em></li>
 * <li>Call {@link UserBiz#getAlbumsViewableByUser(Object)} passing the <var>actingUser</var>'s
 * <code>userId</code></li>
 * <li>If <var>albumId</var> is not <em>null</em> then call 
 * {@link AbstractAction#populateItems(int, Album[], User, int)}</lI>
 * <li>Acting user friends via {@link UserBiz#getFriendsForUser(Object)}</li>
 * <li>Acting user groups via {@link UserBiz#getGroupsForUserId(Object)}</li>
 * </ol>
 * 
 * @param data the MediaAlbumData object to populate
 * @param actingUser the acting user
 * @param albumId the ID of the album to populate media items for (or <em>null</em>
 * to not populate any)
 * @param collectionId the ID of the collection to populate media items for (or <em>null</em>
 * to not populate any)
 * @throws MediaAlbumException if an error occurs
 */
protected void setup(MediaAlbumData data, User actingUser, Integer albumId, Integer collectionId) 
throws MediaAlbumException
{
	if ( albumId != null ) {
		data.setDisplayAlbum(albumId.intValue());
	} else if ( collectionId != null ) {
		verifyUserCanViewCollection(actingUser,collectionId);
	}
	
	// get dirs for user, and items for dir if albumId not provided
	populateCollectionsForUser(data,(albumId==null?collectionId:null),actingUser);

	// get albums for user, and items for album if albumId provided
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);

	Album[] albums = userBiz.getAlbumsViewableByUser(actingUser.getUserId());
	if ( albums != null && albums.length > 0 ) {
		if ( albumId != null ) {
			Album album = populateItems(albumId.intValue(),albums,actingUser,
					ApplicationConstants.POPULATE_MODE_ALL);
			if ( album != null && album.getThemeId() != null ) {
				ThemeBiz themeBiz = (ThemeBiz)getBiz(BizConstants.THEME_BIZ);
				AlbumTheme theme = themeBiz.getAlbumThemeById(album.getThemeId(),
						actingUser,ApplicationConstants.CACHED_OBJECT_ALLOWED);
				data.setTheme(theme);
			}
		}
		if ( LOG.isDebugEnabled() ) {
			for ( int i = 0; i < albums.length; i++ ) {
				LOG.debug("Added Album to MediaAlbumData: " +albums[i].getAlbumId());
			}
		}
		data.setAlbum(albums);
	}
	
	User[] friends = userBiz.getFriendsForUser(actingUser.getUserId());
	data.setFriend(friends);
	
	Group[] groups = userBiz.getGroupsForUserId(actingUser.getUserId());
	data.setGroup(groups);
}

/**
 * Main Media Album method.
 * 
 * @param mapping the action mapping
 * @param form the form bean
 * @param request the request
 * @param response the response
 * @param result TODO
 * @param data the GUI data
 * @param usd the user session data
 * @throws Exception if an error occurs
 */
protected abstract void goMediaAlbum(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response,
		ActionResult result,
		MediaAlbumData data, UserSessionData usd)
throws Exception;

}
