/* ===================================================================
 * SaveItemDataAction.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Mar 27, 2004 6:55:23 PM.
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
 * $Id: SaveItemDataAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.item;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractAction;
import magoffin.matt.ma.servlet.formbean.FreeDataForm;
import magoffin.matt.ma.xsd.FreeData;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.util.StringUtil;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

/**
 * Action to save an item's free data.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class SaveItemDataAction extends AbstractAction 
{
/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.AbstractAction#go(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 */
protected void go(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response, 
		ActionResult result) throws Exception 
{
	UserSessionData usd = getUserSessionData(request,ANONYMOUS_USER_NOT_OK);
	FreeDataForm dForm = (FreeDataForm)form;
		
	if ( isCancelled(request) ) {
		setBounceBackActionForward(dForm,mapping,result, request);
		return;
	}
	
	Integer[] itemIds = dForm.getMitems();
	if ( itemIds == null || itemIds.length < 1 ) {
		notFound(mapping,request, response);
		return;
	}
	
	MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);
	MediaItem item = null;
	if ( itemIds.length == 1 ) {
		item = itemBiz.getMediaItemById(itemIds[0],
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
	}	
	
	List fdList = new ArrayList(8);
	
	if ( dForm.getCopyright() != null ) {
		FreeData copyright = dForm.getCopyright();
		String value = copyright.getDataValue();
		value = StringUtil.normalizeWhitespace(value);
		if ( value != null && value.length() > 0 ) {
			copyright.setDataValue(value);
			copyright.setDataTypeId(ApplicationConstants.FREE_DATA_TYPE_COPYRIGHT);
			fdList.add(copyright);
		}
	}
	
	if ( dForm.getKeywords() != null ) {
		FreeData keywords = dForm.getKeywords();
		String value = keywords.getDataValue();
		value = StringUtil.normalizeWhitespace(value);
		if ( value != null ) {
			keywords.setDataValue(value);
			keywords.setDataTypeId(ApplicationConstants.FREE_DATA_TYPE_KEYWORD);
			fdList.add(keywords);
		}
	}
	
	FreeData[] fdata = (FreeData[])fdList.toArray(new FreeData[fdList.size()]);
	
	itemBiz.setCopyrightKeywords(itemIds,fdata,usd.getUser());
	
	ActionMessage msg = null;
	if ( item != null ) {
		msg = new ActionMessage("item.data.updated",item.getName() != null 
				? item.getName() : item.getPath());
	} else {
		msg = new ActionMessage("items.data.updated", new Integer(itemIds.length));
	}
	
	addActionMessage(request,ActionMessages.GLOBAL_MESSAGE,msg);
	setBounceBackActionForward(dForm,mapping,result, request);
}

}
