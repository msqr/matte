/* ===================================================================
 * ModifyItemsAction.java
 * 
 * Created Jan 26, 2004 2:44:36 PM
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
 * $Id: ModifyItemsAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.item;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.biz.AlbumBiz;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.CollectionBiz;
import magoffin.matt.ma.biz.LightboxBiz;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.search.MediaItemQuery;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.ServletConstants;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractAction;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.Album;
import magoffin.matt.ma.xsd.Collection;
import magoffin.matt.ma.xsd.Lightbox;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.ma.xsd.User;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.util.MessageResources;

/**
 * Action to modify collection items.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class ModifyItemsAction extends AbstractAction 
{
	/** The forward for updating item free data: <code>fdata</code> */
	public static final String UPDATE_ITEM_FREE_DATA_FORWARD = "fdata";
	
	/** The forward for updating item info: <code>info</code> */
	public static final String UPDATE_ITEM_INFO_FORWARD = "info";
	
	/** The forward for emailing items: <code>email</code> */
	public static final String EMAIL_ITEM_FORWARD = "email";
	
	public static final int MODE_MOVE = 1;
	
	public static final int MODE_DELETE = 2;
	
	public static final int MODE_TIMEZONE = 3;

	public static final int MODE_FREE_DATA = 4;

	public static final int MODE_INFO = 5;

	public static final int MODE_ADD_TO_ALBUM = 6;

	public static final int MODE_ADD_TO_LIGHTBOX = 7;

	public static final int MODE_SET_CUSTOM_TYPE = 8;

	public static final int MODE_REMOVE_FROM_ALBUM = 9;

	public static final int MODE_EMAIL = 10;

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
	UserSessionData usd = getUserSessionData(request,ANONYMOUS_USER_NOT_OK);
	DynaActionForm dForm = (DynaActionForm)form;
	
	Integer mode = (Integer)dForm.get("mode");
	if ( mode == null ) {
		mode = new Integer(0);
	}
	//Integer collectionId = (Integer)dForm.get(ServletConstants.REQ_KEY_COLLECTION_ID);
	Integer[] itemIds = (Integer[])dForm.get(ServletConstants.REQ_KEY_ITEMS_ID);
	Integer moveToId = (Integer)dForm.get("moveTo");
	
	if ( itemIds == null || itemIds.length < 1 ) {
		// add message that friends now
		addActionMessage(request,ActionMessages.GLOBAL_MESSAGE,
				new ActionMessage("mod.items.none"));
		result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
		return;
	}
	
	MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);
	User actingUser = usd.getUser();
	AlbumBiz albumBiz = (AlbumBiz)getBiz(BizConstants.ALBUM_BIZ);
	CollectionBiz collectionBiz = (CollectionBiz)getBiz(BizConstants.COLLECTION_BIZ);
	
	switch ( mode.intValue() ) {
		case MODE_MOVE:
			MediaItem[] items = itemBiz.getMediaItemsById(itemIds,
					ApplicationConstants.CACHED_OBJECT_ALLOWED);
			collectionBiz.moveItemsToCollection(moveToId,items,usd.getUser());
			Collection movedToCollection = collectionBiz.getCollectionById(moveToId,
					ApplicationConstants.CACHED_OBJECT_ALLOWED);
			addActionMessage(request,ActionMessages.GLOBAL_MESSAGE,
					new ActionMessage("mod.items.moved",
							new Object[] {movedToCollection.getName()}));
			break;
			
		case MODE_DELETE:
			for ( int i = 0; i < itemIds.length; i++ ) {
				itemBiz.deleteMediaItem(itemIds[i],actingUser);
			}
			addActionMessage(request,ActionMessages.GLOBAL_MESSAGE,
					new ActionMessage("mod.items.deleted"));
			break;
		
		case MODE_TIMEZONE:
			String tzCode = (String)dForm.get("tzCode");
			itemBiz.setTimezone(itemIds,tzCode);
			addActionMessage(request,ActionMessages.GLOBAL_MESSAGE,
					new ActionMessage("mod.items.set.timezone", tzCode));
			break;
			
		case MODE_FREE_DATA:
			result.setForward(mapping.findForward(UPDATE_ITEM_FREE_DATA_FORWARD));
			return;
			
		case MODE_INFO:
			result.setForward(mapping.findForward(UPDATE_ITEM_INFO_FORWARD));
			return;
			
		case MODE_ADD_TO_ALBUM:
			albumBiz.addMediaItemsToAlbum(moveToId,itemIds,usd.getUser());
			Album album = albumBiz.getAlbumById(moveToId,usd.getUser(),
					ApplicationConstants.CACHED_OBJECT_ALLOWED);
			addActionMessage(request,ActionMessages.GLOBAL_MESSAGE,
					new ActionMessage("mod.items.added.to.album",
							new Object[] {album.getName()}));
			break;
			
		case MODE_REMOVE_FROM_ALBUM:
			int numRemoved = albumBiz.removeMediaItemsFromAlbum(moveToId,itemIds,usd.getUser());
			album = albumBiz.getAlbumById(moveToId,usd.getUser(),
					ApplicationConstants.CACHED_OBJECT_ALLOWED);
			addActionMessage(request,ActionMessages.GLOBAL_MESSAGE,
					new ActionMessage("mod.items.removed.from.album",
							new Object[] {new Integer(numRemoved),album.getName()}));
			break;
			
		case MODE_ADD_TO_LIGHTBOX:
			addToLightbox(usd, itemIds);
			addActionMessage(request,ActionMessages.GLOBAL_MESSAGE,
					new ActionMessage("mod.items.added.to.lightbox"));
			break;
			
		case MODE_SET_CUSTOM_TYPE:
			itemBiz.setCustomType(itemIds,moveToId,usd.getUser());
			MessageResources rsrc = getResources(request);
			ActionMessage msg = new ActionMessage("mod.items.set.custom.type",
					rsrc.getMessage(request.getLocale(),
							"item.type."+moveToId));
			addActionMessage(request,ActionMessages.GLOBAL_MESSAGE,msg);
			break;
			
		case MODE_EMAIL:
			result.setForward(mapping.findForward(EMAIL_ITEM_FORWARD));
			return;

	}
	
	Object o = dForm.get("query");
	if ( o != null ) {
		MediaItemQuery query = (MediaItemQuery)o;
		if ( query.getSimple() != null || query.getName() != null || 
				query.getKeyword() != null || query.getText() != null ) {
			result.setForward(mapping.findForward("search"));
			return;
		}
	}
	
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

/**
 * Add items to the user's lightbox.
 * @param usd the UserSessionData
 * @param itemIds the item IDs
 */
private void addToLightbox(UserSessionData usd, Integer[] itemIds) 
throws MediaAlbumException
{
	LightboxBiz lightboxBiz = (LightboxBiz)getBiz(BizConstants.LIGHTBOX_BIZ);

	Lightbox lb = usd.getLightbox();
	if ( lb == null ) {
		lb = lightboxBiz.getLightboxInstance(usd.getUser());
		usd.setLightbox(lb);
	}
	
	lightboxBiz.addMediaItemsToLightbox(lb,itemIds,usd.getUser());
}

}
