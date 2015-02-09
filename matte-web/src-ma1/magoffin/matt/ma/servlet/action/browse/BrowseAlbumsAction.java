/* ===================================================================
 * BrowseAlbumsAction.java
 * 
 * Created Feb 5, 2004 7:57:13 PM
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
 * $Id: BrowseAlbumsAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.browse;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MessageConstants;
import magoffin.matt.ma.biz.AlbumBiz;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.UserBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.ServletConstants;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.formbean.BrowseAlbumsForm;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.Album;
import magoffin.matt.ma.xsd.AlbumTheme;
import magoffin.matt.ma.xsd.BrowseData;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.ma.xsd.User;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;

/**
 * Action to browse albums.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class BrowseAlbumsAction extends AbstractBrowseAction {

/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.browse.AbstractBrowseAction#goBrowse(org.apache.struts.action.ActionMapping, magoffin.matt.ma.servlet.formbean.BrowseAlbumsForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, magoffin.matt.ma.xsd.BrowseData, magoffin.matt.ma.xsd.AlbumTheme, magoffin.matt.ma.servlet.UserSessionData)
 */
protected void goBrowse(
	ActionMapping mapping,
	BrowseAlbumsForm form,
	HttpServletRequest request,
	HttpServletResponse response,
	ActionResult result,
	BrowseData data,
	AlbumTheme theme, 
	UserSessionData usd, 
	User browseUser)
	throws Exception 
{
	// get the user biz
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	
	int pageSize = ApplicationConstants.DEFAULT_BROWSE_ALBUM_PAGE_SIZE;
	if ( form.getPageSize() != null && form.getPageSize().intValue() > 1 ) {
		pageSize = form.getPageSize().intValue();
	}
	
	int page = 0;
	if ( form.getPage() != null && form.getPage().intValue() > 0 ) {
		page = form.getPage().intValue();
	}
	
	Album[] albums = null;
	
	// get list of albums available for user to see
	try {
		// TODO allow sorting to be specified by request
		albums = userBiz.getAlbumsViewableForUser(browseUser,AlbumBiz.SORT_ALBUM_BY_DATE, usd.getUser());
	} catch ( MediaAlbumException e ) {
		if ( e.hasErrorCode(UserBiz.ERROR_USER_NOT_FOUND) ) {
			notFound(mapping, request, response);
			return;
		}
		throw e;
	}

	int totalAlbums = 0;
	int totalPages = 0;
	if ( albums != null ) {
		totalPages = albums.length % pageSize == 0
		? albums.length / pageSize
		: (int)Math.ceil((double)albums.length / (double)pageSize);
	}
	
	if ( page >= totalPages ) {
		page = 0;
	}
	
	if ( albums != null && albums.length > 0 ) {
		if ( albums.length <= pageSize ) {
			data.setAlbum(albums);
		} else {
			int start = pageSize * page;
			if ( start >= albums.length ) {
				page = 0;
				start = 0;
			}
			for ( int i = start; i < (start + pageSize) && i < albums.length; i++ ) {
				data.addAlbum(albums[i]);
			}
		}
		
		// TODO calculate total album size in Biz
		totalAlbums = getTotalAlbums(albums);
		
	} else {
		data.setTotalItems(0);
	}
		
	if ( !setAlbumRoot(data,form.getAlbum(),browseUser) && usd.getUser() == null ) {
		// add message that perhaps need to log on
		addActionMessage(request,ActionMessages.GLOBAL_MESSAGE, 
				new ActionMessage(MessageConstants.MSG_BROWSE_ALBUM_NOT_AVAILABLE));
	}
	
	// fill in top-level album items
	AlbumBiz albumBiz = (AlbumBiz)getBiz(BizConstants.ALBUM_BIZ);
	for ( int i = 0; i < data.getAlbumCount(); i++ ) {
		Album a = data.getAlbum(i);
		if ( a.getItemCount() < 1 ) {
			MediaItem[] items = albumBiz.getMediaItemsForAlbum(a.getAlbumId(),
					ApplicationConstants.POPULATE_MODE_ALL, 
					ApplicationConstants.CACHED_OBJECT_ALLOWED, browseUser);
			albumBiz.sortAlbumItems(a,items); // sort properly
			a.setItem(items);
		}
	}
	
	// populate owners
	if ( albums.length < 1 ) {
		data.addOwner(browseUser);
	} else {
		data.setOwner(userBiz.getOwnersWithFreeData(albums,
				ApplicationConstants.CACHED_OBJECT_ALLOWED));
	}
	
	// save total items
	int totalItems = userBiz.getTotalAlbumItemsViewableForUser(browseUser,usd.getUser());
	data.setTotalItems(totalItems);
	
	// save total albums
	data.setTotalAlbums(totalAlbums);
	
	// save total pages
	data.setTotalPages(totalPages);

	MessageResources msgs = getResources(request);
	String browseName = msgs.getMessage(request.getLocale(),
			"browse.mode." +UserBiz.VIRTUAL_VIEW_MODE_NORMAL_ALBUMS +".displayName");
	data.setBrowseName(browseName);
	data.setBrowseMode(UserBiz.VIRTUAL_VIEW_MODE_NORMAL_ALBUMS);
	
	// save the page number  and size
	data.setDisplayPage(page);
	data.setPageSize(pageSize);
	
	// save the display item if available
	data.deleteDisplayItem();
	if ( form.getMitem() != null ) {
		data.setDisplayItem(form.getMitem().intValue());
	}

	Map params = new HashMap(2,1);
	params.put(ServletConstants.XSL_PARAM_KEY_URL_PATH,request.getServletPath());

	result.setXslParams(params);
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

}
