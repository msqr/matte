/* ===================================================================
 * ItemInfoFormAction.java
 * 
 * Created Jun 9, 2004 7:25:57 PM
 * 
 * Copyright (c) 2004 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: ItemInfoFormAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.item;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractMediaAlbumDataAction;
import magoffin.matt.ma.servlet.formbean.ItemInfoForm;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.MediaAlbumData;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.util.ArrayUtil;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * Display the form to edit multiple items' info (name, comment, etc).
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class ItemInfoFormAction extends AbstractMediaAlbumDataAction 
{
	/** The Xform XSL key: <code>edit-item-info</code>. */
	public static final String EDIT_ITEM_INFO_TEMPLATES_KEY = "edit-item-info";
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.AbstractMediaAlbumDataAction#goMediaAlbum(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, magoffin.matt.ma.servlet.ActionResult, magoffin.matt.ma.xsd.MediaAlbumData, magoffin.matt.ma.servlet.UserSessionData)
 */
protected void goMediaAlbum(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response,
		ActionResult result, MediaAlbumData data, UserSessionData usd)
		throws Exception 
{
	ItemInfoForm dForm = (ItemInfoForm)form;
	MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);
	Integer[] itemIds = dForm.getMitems();
	
	if ( itemIds == null || itemIds.length < 1 ) {
		notFound(mapping,request, response);
		return;
	}
	
	
	MediaItem[] items = itemBiz.getMediaItemsById(itemIds,
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
	// clone for editing
	items = (MediaItem[])ArrayUtil.clone(items);
	dForm.setItems(items);
	dForm.setSize(items.length);
	
	// throw in free data
	itemBiz.populateItemFreeData(items,ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
	data.setItem(items);
	
	result.setXslTemplate(EDIT_ITEM_INFO_TEMPLATES_KEY);
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

}
