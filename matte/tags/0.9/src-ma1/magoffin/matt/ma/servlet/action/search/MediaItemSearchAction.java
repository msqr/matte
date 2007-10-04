/* ===================================================================
 * MediaItemSearchAction.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Mar 30, 2004 5:05:48 PM.
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
 * $Id: MediaItemSearchAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.search;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.biz.SearchBiz;
import magoffin.matt.ma.search.MediaItemMatch;
import magoffin.matt.ma.search.MediaItemQuery;
import magoffin.matt.ma.search.MediaItemResults;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractMediaAlbumDataAction;
import magoffin.matt.ma.servlet.formbean.MediaItemSearchForm;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.MediaAlbumData;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.ma.xsd.MediaItemSearchData;
import magoffin.matt.ma.xsd.MediaItemSearchResults;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * Action to search media items.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class MediaItemSearchAction extends AbstractMediaAlbumDataAction 
{

/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.AbstractMediaAlbumDataAction#goMediaAlbum(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, magoffin.matt.ma.xsd.MediaAlbumData, magoffin.matt.ma.servlet.UserSessionData)
 */
public void goMediaAlbum(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response,
		ActionResult result, MediaAlbumData data, UserSessionData usd) throws Exception 
{
	if ( isCancelled(request) ) {
		result.setForward(mapping.findForward(
				StrutsConstants.DEFAULT_CANCEL_FORWARD));
		return;
	}
	
	MediaItemSearchForm dForm = (MediaItemSearchForm)form;
	
	setup(data,usd.getUser(),null,null);
	
	SearchBiz searchBiz = (SearchBiz)getBiz(BizConstants.SEARCH_BIZ);
	MediaItemResults results = searchBiz.search(dForm.getQuery(),usd.getUser());
	
	MediaItemQuery query = dForm.getQuery();
	MediaItemSearchData searchData = new MediaItemSearchData();
	BeanUtils.copyProperties(searchData,query);
	data.setItemSearch(searchData);
	
	MediaItemSearchResults searchResults = results.getResults();

	// get media items for IDs
	// FIXME hmmm is isn't a good approach?... can get full data from index?
	MediaItemMatch[] matches = results.getMatches();
	if ( matches != null ) {
		Integer[] ids = new Integer[matches.length];
		for ( int i = 0; i < matches.length; i++ ) {
			ids[i] = matches[i].getItemId();
		}
		MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);
		MediaItem[] items = itemBiz.getMediaItemsById(ids,
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
		itemBiz.populateItems(items,ApplicationConstants.POPULATE_MODE_ALL,
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
		searchResults.setItem(items);
	}
	
	data.setItemSearchResults(searchResults);
	
	result.setXslTemplate(HOME_TEMPLATES_KEY);
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

}
