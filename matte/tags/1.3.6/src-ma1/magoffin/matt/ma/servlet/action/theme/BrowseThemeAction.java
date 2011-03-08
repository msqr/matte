/* ===================================================================
 * BrowseThemeAction.java
 *
 * Copyright (c) 2003 Matt Magoffin.
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
 * $Id: BrowseThemeAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.theme;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.biz.AlbumBiz;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.ThemeBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.ServletConstants;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.Album;
import magoffin.matt.ma.xsd.AlbumTheme;
import magoffin.matt.ma.xsd.ThemeData;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

/**
 * Action to browse available themes.
 * 
 * <p> Created on Jan 30, 2003 2:06:55 PM.</p>
 *
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class BrowseThemeAction extends AbstractThemeAction 
{	
	/** The Xform XSL key: <code>browse-theme</code>. */
	public static final String BROWSE_THEME_TEMPLATES_KEY = "browse-theme";

	public static final String SORT_BY_NAME = "name";
	public static final String SORT_BY_AUTHOR = "author";
	public static final String SORT_BY_DATE = "date";
	public static final String DEFAULT_SORT = SORT_BY_NAME;
	
	public static final Integer DEFAULT_PAGE = new Integer(0);
	public static final Integer DEFAULT_PAGE_SIZE = new Integer(5);
	

/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.theme.AbstractThemeAction#goTheme(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, magoffin.matt.ma.xsd.ThemeData, magoffin.matt.ma.servlet.UserSessionData)
 */
protected void goTheme(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response,
		ActionResult result, ThemeData data, UserSessionData usd) throws Exception 
{
	DynaActionForm dForm = (DynaActionForm)form;
	
	Integer albumId = (Integer)dForm.get(ServletConstants.REQ_KEY_ALBUM_ID);
	Integer page = (Integer)dForm.get(ServletConstants.REQ_KEY_PAGE);
	if ( page == null ) {
		page = DEFAULT_PAGE;
	}
	Integer pageSize = (Integer)dForm.get(ServletConstants.REQ_KEY_PAGE_SIZE);
	if ( pageSize == null ) {
		pageSize = DEFAULT_PAGE_SIZE;
	}
	String sort = (String)dForm.get(ServletConstants.REQ_KEY_SORT);
	if ( sort == null ) {
		sort = DEFAULT_SORT;
	}
	
	int sortMode;
	if ( SORT_BY_NAME.equals(sort) ) {
		sortMode = ThemeBiz.SORT_MODE_NAME;
	} else if ( SORT_BY_AUTHOR.equals(sort) ) {
		sortMode = ThemeBiz.SORT_MODE_AUTHOR;
	} else {
		sortMode = ThemeBiz.SORT_MODE_DATE;
	}
	
	// get all themes browsable for user
	ThemeBiz themeBiz = (ThemeBiz)getBiz(BizConstants.THEME_BIZ);
	AlbumTheme[] themes = themeBiz.getAlbumThemesViewableForUser(
			usd.getUser(), sortMode, ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
	if ( albumId != null ) {
		AlbumBiz albumBiz = (AlbumBiz)getBiz(BizConstants.ALBUM_BIZ);
		Album album = albumBiz.getAlbumById(albumId,usd.getUser(),
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
		data.setAlbum(album);
	}
	
	int offset = page.intValue();
	int ps = pageSize.intValue();
	int start = offset * ps;
	int end = start + ps;
	
	if ( start >= themes.length ) {
		offset = start = 0;
		end = ps;
	}
	
	if ( end >= themes.length ) {
		end = themes.length;
	}
	
	if ( end > start ) {
		AlbumTheme[] subset = new AlbumTheme[end-start];
		System.arraycopy(themes,start,subset,0,subset.length);
		data.setTheme(subset);
		data.setDisplayMax(ps);
	}
	
	if ( offset > 0 ) {
		data.setDisplayPage(offset);
	}
	
	data.setDisplayTotal(themes.length);
	data.setDisplaySort(sort);
	
	result.setXslTemplate(BROWSE_THEME_TEMPLATES_KEY);
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

} 
