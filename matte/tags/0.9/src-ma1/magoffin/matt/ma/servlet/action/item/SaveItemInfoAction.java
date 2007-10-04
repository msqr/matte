/* ===================================================================
 * SaveItemInfoAction.java
 * 
 * Created Jun 9, 2004 10:42:55 PM
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
 * $Id: SaveItemInfoAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.item;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MessageConstants;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractAction;
import magoffin.matt.ma.servlet.formbean.ItemInfoForm;
import magoffin.matt.ma.util.MediaUtil;
import magoffin.matt.ma.xsd.FreeData;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.util.StringUtil;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;

/**
 * Action to save the update item info form data.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class SaveItemInfoAction extends AbstractAction 
{
	private static final Logger LOG = Logger.getLogger(SaveItemInfoAction.class);

/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.AbstractAction#go(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, magoffin.matt.ma.servlet.ActionResult)
 */
protected void go(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response,
		ActionResult result) throws Exception 
{
	UserSessionData usd = getUserSessionData(request,ANONYMOUS_USER_NOT_OK);
	ItemInfoForm dForm = (ItemInfoForm)form;
	
	if ( isCancelled(request) ) {
		setBounceBackActionForward(dForm,mapping,result, request);
		return;
	}
	
	MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);

	MediaItem[] items = dForm.getItems();
	
	// handle custom date fields
	String[] customDates = dForm.getCustomDate();
	
	// attempt to convert to Date
	MessageResources msgs = getResources(request);
	DateFormat dateTimeFormat = new SimpleDateFormat(
			msgs.getMessage(request.getLocale(),
			MessageConstants.DATE_TIME_FORMAT_PATTERN));
	DateFormat dateFormat = new SimpleDateFormat(
			msgs.getMessage(request.getLocale(),
			MessageConstants.DATE_FORMAT_PATTERN));
	for ( int i = 0; i < customDates.length; i++ ) {
		String oneDate = StringUtil.normalizeWhitespace(customDates[i]);
		if ( oneDate == null ) {
			items[i].setCustomDate(Boolean.FALSE);
			// TODO re-parse the date from the original here?
			continue;
		}
		Date d = null;
		try {
			d = dateTimeFormat.parse(oneDate);
		} catch ( Exception e ) {
			// failed to parse date time, attempt just date
			try {
				d = dateFormat.parse(oneDate);
			} catch ( Exception e2 ) {
				if ( LOG.isDebugEnabled() ) {
					LOG.debug("Unable to parse date from " +oneDate
							+e2.toString());
				}
			}
		}
		if ( d != null ) {
			items[i].setCreationDate(d);
			items[i].setCustomDate(Boolean.TRUE);
		}
	}
	
	itemBiz.populateItemFreeData(items,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	List changedItems = new ArrayList(items.length);
	int changedCount = 0;
	FreeData[] editedFreeData = dForm.getFreeData();
	
	for ( int i = 0; i < items.length; i++  ) {
		MediaItem item = itemBiz.getMediaItemById(items[i].getItemId(),
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
		item = (MediaItem)BeanUtils.cloneBean(item);
		
		boolean itemChanged = false;
		
		if ( MediaUtil.copyChanges(items[i],item, itemBiz) ) {
			changedItems.add(item);
			itemChanged = true;
			
		}
		
		// check for FreeData change
		FreeData[] fdata = itemBiz.getFreeData(item.getItemId(),
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
		item.setData(fdata);
		if ( MediaUtil.copyFreeDataChanges(editedFreeData,item) ) {
			itemBiz.setFreeData(item.getItemId(),item.getData(),
					usd.getUser());
			itemChanged = true;
		}
		
		if ( itemChanged ) {
			changedCount++;
		}
	}
	
	if ( changedItems.size() > 0 ) {
		MediaItem[] saveItems = (MediaItem[])changedItems.toArray(new
				MediaItem[changedItems.size()]);
		itemBiz.updateMediaItems(saveItems,usd.getUser());
	}
	
	ActionMessage msg = new ActionMessage("item.info.updated", 
			new Integer(changedCount));
	addActionMessage(request,ActionMessages.GLOBAL_MESSAGE,msg);
	
	setBounceBackActionForward(dForm,mapping,result, request);
}

}
