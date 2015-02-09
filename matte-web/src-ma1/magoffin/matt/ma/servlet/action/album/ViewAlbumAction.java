/* ===================================================================
 * ViewAlbumAction.java
 * 
 * Copyright (c) 2002-2003 Matt Magoffin.
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
 * $Id: ViewAlbumAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.album;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaRequestHandler;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.biz.UserBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.ServletConstants;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.util.MediaUtil;
import magoffin.matt.ma.xsd.Album;
import magoffin.matt.ma.xsd.AlbumTheme;
import magoffin.matt.ma.xsd.Collection;
import magoffin.matt.ma.xsd.Lightbox;
import magoffin.matt.ma.xsd.MediaAlbumData;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.ma.xsd.MediaItemMetadata;
import magoffin.matt.ma.xsd.User;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

/**
 * Action to allow for anonymous access to an album.
 * 
 * <p> Created on Nov 11, 2002 1:53:16 PM.</p>
 *
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class ViewAlbumAction extends AbstractAlbumAction 
{
	/** The Xform XSL key: view-album. */
	public static final String VIEW_ALBUM_TEMPLATES_KEY = "view-album";
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.AbstractAnonymousMediaAlbumDataAction#goMediaAlbum(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, magoffin.matt.ma.xsd.MediaAlbumData, magoffin.matt.ma.servlet.UserSessionData)
 */
protected void goMediaAlbum(
	ActionMapping mapping,
	ActionForm form,
	HttpServletRequest request,
	HttpServletResponse response,
	ActionResult result,
	MediaAlbumData data, UserSessionData usd)
	throws Exception 
{
	DynaActionForm dForm = (DynaActionForm)form;
	String key = (String)dForm.get(ServletConstants.REQ_KEY_ALBUM_KEY);
	if ( key == null ) {
		notFound(mapping,request, response);
		return;
	}
	
	Integer displayId = (Integer)dForm.get(ServletConstants.REQ_KEY_ALBUM_ID);
	
	Album rootAlbum = this.getAlbumAndChildren(key,usd.getUser(),
		(displayId == null || displayId.intValue() == 0 ? (Object)key : (Object)displayId),
		ApplicationConstants.POPULATE_MODE_ALL);
	
	if ( rootAlbum == null ) {
		notFound(mapping,request, response);
		return;
	}
	
	Album[] rootAlbumArray = new Album[]{rootAlbum};
	
	// item metadata support for single item
	MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);
	Integer itemId = (Integer)dForm.get(ServletConstants.REQ_KEY_ITEM_ID);
	if ( itemId != null ) {
		MediaItem item = MediaUtil.findItem(rootAlbumArray,itemId);
		if ( item != null ) {
			MediaRequestHandler handler = itemBiz.getHandlerForItem(item);
			MediaItemMetadata[] metadata = handler.getMetadataForItem(item);
			if ( metadata != null ) {
				item.setMetadata(metadata);
			}		
		}
	}
	
	if ( displayId == null || displayId.intValue() == 0 ) {
		displayId = rootAlbum.getAlbumId();
	}
	
	data.setDisplayAlbum( displayId.intValue() );
	data.addAlbum(rootAlbum);
	
	// get the theme
	AlbumTheme theme = this.getAlbumTheme(request,
		MediaUtil.findAlbum(rootAlbum,displayId).getAlbumId(),
		THEME_COMPONENT_ALBUM);
	data.setTheme( theme );
	
	// populate owners
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	User[] owners = userBiz.getOwnersWithFreeData(rootAlbumArray,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	data.setOwner(owners);
	
	// populate owner collections
	for ( int i = 0; i < owners.length; i++ ) {
		Collection[] collections = userBiz.getCollectionsForUser(owners[i].getUserId());
		for ( int j = 0; j < collections.length; j++ ) {
			data.addCollection(collections[j]);
		}
	}
	
	// add lightbox
	Lightbox lb = usd.getLightbox();
	if ( lb != null && lb.getItemIdCount() != lb.getItemCount() ) {
		lb.clearItem();
		if ( lb.getItemIdCount() > 0 ) {
			MediaItem[] items = itemBiz.getMediaItemsById(lb.getItemId(),
					ApplicationConstants.CACHED_OBJECT_ALLOWED);
			lb.setItem(items);
		}
	}
	data.setLightbox(usd.getLightbox());
	
	result.setXslTemplate(isDefaultTheme(theme)?VIEW_ALBUM_TEMPLATES_KEY:null);
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

}
