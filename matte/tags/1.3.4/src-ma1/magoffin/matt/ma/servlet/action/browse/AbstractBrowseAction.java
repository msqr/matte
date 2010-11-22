/* ===================================================================
 * AbstractBrowseAction.java
 * 
 * Created Feb 5, 2004 3:48:36 PM
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
 * $Id: AbstractBrowseAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.browse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MessageConstants;
import magoffin.matt.ma.UserAccessException;
import magoffin.matt.ma.biz.AlbumBiz;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.biz.ThemeBiz;
import magoffin.matt.ma.biz.UserBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.ServletConstants;
import magoffin.matt.ma.servlet.ServletUtil;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractAction;
import magoffin.matt.ma.servlet.formbean.BrowseAlbumsForm;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.Album;
import magoffin.matt.ma.xsd.AlbumCrumb;
import magoffin.matt.ma.xsd.AlbumTheme;
import magoffin.matt.ma.xsd.BrowseData;
import magoffin.matt.ma.xsd.MediaAlbumSettings;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.ma.xsd.User;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;

/**
 * Base action for browsing sets of albums.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public abstract class AbstractBrowseAction extends AbstractAction {

	private static final Logger LOG = Logger.getLogger(AbstractBrowseAction.class);

	private static final Map XSLT_PARAM_MAP_BROWSE = new HashMap(3);

	static {
		XSLT_PARAM_MAP_BROWSE.put(ServletConstants.XSL_PARAM_KEY_ALBUM_MODE,
				ServletConstants.XSL_PARAM_ALBUM_MODE_BROWSE);
	}
	
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
	BrowseAlbumsForm bForm = (BrowseAlbumsForm)form;
	UserSessionData usd = null;
	
	// see if settings stored in session, if not then forward to view-settings
	try {
		usd = this.getUserSessionData(request, ANONYMOUS_USER_OK);
	} catch ( UserAccessException e ) {
		// first access, so forward to view-settings
		ServletUtil.saveRequestURL(request);
		result.setForward(mapping.findForward("view-settings"));
		return;
	}
	
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	User browseUser = null;
	String key = bForm.getKey();
	if ( key == null ) {
		notFound(mapping, request, response);
		return;
	}
	try {
		browseUser = userBiz.getUserByAnonymousKey(key);
	} catch ( MediaAlbumException e ) {
		if ( e.hasErrorCode(UserBiz.ERROR_USER_NOT_FOUND) ) {
			notFound(mapping, request, response);
			return;
		}
	}

	MediaAlbumSettings settings = (MediaAlbumSettings)borrowPooledObject(
			MediaAlbumSettings.class);
	BrowseData data = (BrowseData)borrowPooledObject(BrowseData.class);
	AlbumTheme theme = getAlbumThemeForBrowsing(request,bForm.getTheme(),
			usd.getUser(), browseUser);
	
	settings.setThumbnail(usd.getThumbSpec());
	settings.setSingle(usd.getSingleSpec());
	
	data.setSettings(settings);
	data.setUser(usd.getUser());
	data.setAdmin(usd.isAdmin());
	data.setKey(key);
	data.setTheme(theme);
	
	result.setData(data);
	
	goBrowse(mapping,bForm,request,response,result,data,theme, usd, browseUser);
}

/**
 * Get the theme for browsing, defaulting to the application default 
 * theme if ID not specified or available.
 * 
 * <p>This method will also set the appropriate request attributes 
 * for Xform to transform using the theme, including the appropriate 
 * XSL parameter map parameter to set the mode to <code>browse</code>.
 * </p> 
 * 
 * @param request the current request
 * @param themeId the ID of the theme to get (may be <em>null</em>)
 * @param actingUser the acting user
 * @param browseUser the browse user
 * @return the AlbumTheme to use
 * @throws MediaAlbumException
 */
protected AlbumTheme getAlbumThemeForBrowsing(
		HttpServletRequest request, 
		Integer themeId,
		User actingUser, 
		User browseUser)
throws MediaAlbumException
{
	ThemeBiz themeBiz = (ThemeBiz)getBiz(BizConstants.THEME_BIZ);
	AlbumTheme theme = null;
	
	if ( themeId != null && themeId.intValue() > 0 ) {
		theme = themeBiz.getAlbumThemeById(themeId,actingUser,
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
	}
	
	if ( theme == null ) {
		
		// check if browse user has theme set
		if ( browseUser.getThemeId() != null ) {
			theme = themeBiz.getAlbumThemeById(browseUser.getThemeId(),
					actingUser, ApplicationConstants.CACHED_OBJECT_ALLOWED);
		}
		
		if ( theme == null ) {
			theme = themeBiz.getDefaultAlbumTheme();
		}
		
		request.setAttribute(ServletConstants.REQ_ATTR_XFORM_XSL,
				ServletConstants.THEME_DEFAULT_TEMPLATE_KEY);
		
	} else {
	
		request.setAttribute(ServletConstants.REQ_ATTR_XFORM_XSL_THEME,
				ServletUtil.getAlbumThemePath(theme));
		request.setAttribute(ServletConstants.REQ_ATTR_XFORM_XSL_THEME_HEADER,
				ServletConstants.THEME_XSL_HEADER_ALBUM);
		request.setAttribute(ServletConstants.REQ_ATTR_XFORM_XSL_THEME_FOOTER,
				ServletConstants.THEME_XSL_FOOTER_ALBUM);
		
	}

	request.setAttribute(ServletConstants.REQ_ATTR_XFORM_PARAM,XSLT_PARAM_MAP_BROWSE);
	
	return theme;
}


protected int getTotalAlbums(Album[] albums) {
	if ( albums == null || albums.length < 1 ) return 0;
	int count = 0;
	for ( int i = 0; i < albums.length; i++ ) {
		count++;
		if ( albums[i].getAlbumCount() > 0 ) {
			count += getTotalAlbums(albums[i].getAlbum());
		}
	}
	return count;
}

/**
 * Browse action main method.
 * @param mapping the action mapping
 * @param form the browse form
 * @param request the request
 * @param response the response
 * @param result action result
 * @param data the GUI data
 * @param theme the theme to use
 * @param usd the user session data
 * @param browseUser TODO
 * 
 * @throws Exception if an error occurs
 */
protected abstract void goBrowse(
	ActionMapping mapping,
	BrowseAlbumsForm form,
	HttpServletRequest request,
	HttpServletResponse response,
	ActionResult result,
	BrowseData data,
	AlbumTheme theme, 
	UserSessionData usd, User browseUser)
throws Exception;

/**
 * 
 * @param data
 * @param albumId
 * @param actingUser
 * @return <em>true</em> if <var>albumId</var> was found in album data
 * @throws MediaAlbumException
 */
protected boolean setAlbumRoot(BrowseData data, Integer albumId, User actingUser) throws MediaAlbumException {
	if ( albumId == null ) return true;
	
	AlbumBiz albumBiz = (AlbumBiz)getBiz(BizConstants.ALBUM_BIZ);
	Album[] albums = data.getAlbum();
	Album root = albumBiz.findAlbum(albums,albumId);
	
	if ( root == null ) return false;
	
	// get parent albums
	List parentAlbums = new ArrayList(10);
	Album album = root;
	Integer parentId = null;
	while ( (parentId = album.getParentId()) != null ) {
		album = albumBiz.findAlbum(albums,parentId);
		parentAlbums.add(album);
	}
	
	// set up crumbs for parent albums
	int numParents = parentAlbums.size();
	for ( int i = numParents-1; i >= 0; i-- ) {
		Album parent = (Album)parentAlbums.get(i);
		AlbumCrumb crumb = new AlbumCrumb();
		crumb.setAlbum(parent.getAlbumId().intValue());
		crumb.setName(parent.getName());
		crumb.setPage(0); // TODO support pages for nested albums
		data.addAlbumCrumb(crumb);
	}
	
	// and add crumb for root album
	AlbumCrumb crumb = new AlbumCrumb();
	crumb.setAlbum(root.getAlbumId().intValue());
	crumb.setName(root.getName());
	crumb.setPage(0);
	data.addAlbumCrumb(crumb);
	
	// for all children of root, populate items
	if ( root.getAlbumCount() > 0 ) {
		int count = root.getAlbumCount();
		for ( int i = 0; i < count; i++ ) {
			Album a = root.getAlbum(i);
			if ( a.getItemCount() < 1 ) {
				MediaItem[] items = albumBiz.getMediaItemsForAlbum(a.getAlbumId(),
						ApplicationConstants.POPULATE_MODE_NONE, ApplicationConstants.CACHED_OBJECT_ALLOWED, actingUser);
				if ( items != null && items.length > 0 ) {
					a.setItem(items);
				}
			}
		}
	}
	
	// reset in GUI data
	data.clearAlbum();
	data.addAlbum(root);
	return true;
}

protected void basicGoBrowse(ActionMapping mapping, BrowseAlbumsForm form,
		HttpServletRequest request, HttpServletResponse response,
		ActionResult result, BrowseData data, AlbumTheme theme,
		UserSessionData usd, User browseUser, int virtualMode) throws Exception
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
		albums = userBiz.getVirtualAlbumsViewableForUser(browseUser,
				virtualMode,usd.getUser());
	} catch ( MediaAlbumException e ) {
		if ( e.hasErrorCode(UserBiz.ERROR_USER_NOT_FOUND) ) {
			notFound(mapping, request, response);
			return;
		}
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
	
	
	MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);
	
	// fill in top-level album item comments
	for ( int i = 0; i < data.getAlbumCount(); i++ ) {
		Album a = data.getAlbum(i);
		if ( a.getItemCount() > 0 ) {
			MediaItem[] items = a.getItem();
			itemBiz.populateItems(items,
					ApplicationConstants.POPULATE_MODE_ALL,
					ApplicationConstants.CACHED_OBJECT_ALLOWED);
		}
	}
	
	// populate owners
	if ( albums == null || albums.length < 1 ) {
		data.addOwner(browseUser);
	} else {
		data.setOwner(userBiz.getOwnersWithFreeData(albums,
				ApplicationConstants.CACHED_OBJECT_ALLOWED));
	}
	
	// save total items
	int totalItems = userBiz.getTotalItemsViewableForUser(browseUser,usd.getUser());
	data.setTotalItems(totalItems);
	
	// save total albums
	data.setTotalAlbums(totalAlbums);
	
	// save total pages
	data.setTotalPages(totalPages);

	MessageResources msgs = getResources(request);
	String browseName = msgs.getMessage(request.getLocale(),
			"browse.mode." +virtualMode +".displayName");
	data.setBrowseName(browseName);
	data.setBrowseMode(virtualMode);
	
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
