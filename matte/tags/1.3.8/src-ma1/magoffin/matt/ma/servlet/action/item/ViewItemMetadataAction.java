/* ===================================================================
 * ViewItemMetadataAction.java
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
 * $Id: ViewItemMetadataAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.item;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaRequestHandler;
import magoffin.matt.ma.biz.AlbumBiz;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.CollectionBiz;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.ServletConstants;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.album.AbstractAlbumAction;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.Album;
import magoffin.matt.ma.xsd.AlbumTheme;
import magoffin.matt.ma.xsd.Collection;
import magoffin.matt.ma.xsd.MediaAlbumData;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.ma.xsd.MediaItemMetadata;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

/**
 * Allow user to view meta information from a media item.
 * 
 * <p> Created on Dec 11, 2002 4:17:31 PM.</p>
 *
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class ViewItemMetadataAction extends AbstractAlbumAction 
{

	/** The Xform XSL key: view-album. */
	public static final String VIEW_ALBUMS_TEMPLATES_KEY = "view-album";
	
	private static final Map XSLT_PARAM_MAP = new HashMap(3);

	static {
		XSLT_PARAM_MAP.put(
			ServletConstants.XSL_PARAM_KEY_ALBUM_MODE,
			ServletConstants.XSL_PARAM_ALBUM_MODE_ITEM_METADATA);
	}
	
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

	Integer itemId = (Integer)dForm.get(ServletConstants.REQ_KEY_ITEM_ID);
	if ( itemId == null ) {
		notFound(mapping, request, response);
		return;
	}
	
	MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);
	MediaItem item = itemBiz.getMediaItemById(itemId,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	if ( item == null ) {
		notFound(mapping, request, response);
		return;
	}
	itemBiz.populateItems(new MediaItem[] {item},ApplicationConstants.POPULATE_MODE_ALL,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
	MediaRequestHandler handler = itemBiz.getHandlerForItem(item);
	MediaItemMetadata[] metadata = handler.getMetadataForItem(item);
	if ( metadata != null ) {
		item.setMetadata(metadata);
	}
	
	AlbumTheme theme = null;

	// place item in album (if key available) or source
	String key = (String)dForm.get(ServletConstants.REQ_KEY_ALBUM_KEY);
	if ( key != null && key.length() > 0 ) {
		
		AlbumBiz biz = (AlbumBiz)getBiz(BizConstants.ALBUM_BIZ);
		Album album = biz.getAlbumByKey(key,usd.getUser());
		album.addItem(item);
		data.addAlbum(album);
		
		// get the theme
		theme = this.getAlbumTheme(request,album.getAlbumId(),THEME_COMPONENT_ALBUM);
		data.setTheme( theme );

	} else {
		// no key, so place in dir
		CollectionBiz collectionBiz = (CollectionBiz)getBiz(BizConstants.COLLECTION_BIZ);
		Collection dir = collectionBiz.getCollectionById(item.getCollection(),
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
		if ( dir == null ) {
			notFound(mapping, request, response);
			return;
		}
		verifyUserCanViewCollection(usd.getUser(),dir.getCollectionId());
		dir.addItem(item);
		data.addCollection(dir);
	}
	data.setTheme( theme );
	
	// add lightbox
	data.setLightbox(usd.getLightbox());
	
	result.setXslTemplate(isDefaultTheme(theme)?VIEW_ALBUMS_TEMPLATES_KEY:null);
	result.setXslParams(XSLT_PARAM_MAP);
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

}
