/* ===================================================================
 * ItemDataFormAction.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Mar 27, 2004 6:14:38 PM.
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
 * $Id: ItemDataFormAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
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
import magoffin.matt.ma.servlet.action.AbstractAction;
import magoffin.matt.ma.servlet.formbean.FreeDataForm;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.FreeData;
import magoffin.matt.ma.xsd.MediaItem;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * Action to view the form to modify an item's free data.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class ItemDataFormAction extends AbstractAction 
{
	/** The Xform XSL key: <code>update-item-info</code>. */
	public static final String UPDATE_ITEM_INFO_TEMPLATES_KEY = "update-item-info";
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.AbstractAction#go(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 */
protected void go(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response, ActionResult result)
		throws Exception
{
	UserSessionData usd = getUserSessionData(request,ANONYMOUS_USER_NOT_OK);	
	FreeDataForm dForm = (FreeDataForm)form;
	MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);
	Integer[] itemIds = dForm.getMitems();
	
	if ( itemIds == null || itemIds.length < 1 ) {
		notFound(mapping,request, response);
		return;
	}
	
	
	MediaItem item = null;
	if ( itemIds.length == 1 ) {
		item = itemBiz.getMediaItemById(itemIds[0],
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
		dForm.setItem(item);

		// get current item free data
		FreeData[] data = itemBiz.getFreeData(itemIds[0], 
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
		
		if ( data.length > 0 ) {
			// sort data into copyright, keywords
			for ( int i = 0; i < data.length; i++ ) {
				Integer type = data[i].getDataTypeId();
				if ( ApplicationConstants.FREE_DATA_TYPE_COPYRIGHT.equals(type) ) {
					dForm.setCopyright(data[i]);
				} else if (ApplicationConstants.FREE_DATA_TYPE_KEYWORD.equals(type) ) {
					dForm.setKeywords(data[i]);
				}
			}
		}
		dForm.setMulti(false);
	} else {
		dForm.setMulti(true);
	}
	
	if ( dForm.getCopyright() == null ) {
		dForm.setCopyright(new FreeData());
	}
	
	if ( dForm.getKeywords() == null ) {
		dForm.setKeywords(new FreeData());
	}
	
	dForm.setUser(usd.getUser());
	
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}
}
