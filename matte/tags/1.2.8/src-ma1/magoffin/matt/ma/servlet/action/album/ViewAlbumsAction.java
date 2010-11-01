/* ===================================================================
 * ViewAlbumsAction.java
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
 * =========================================================================
 * $Id: ViewAlbumsAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * =========================================================================
 */
 
package magoffin.matt.ma.servlet.action.album;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.ThemeBiz;
import magoffin.matt.ma.biz.UserBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.ServletConstants;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.Album;
import magoffin.matt.ma.xsd.AlbumTheme;
import magoffin.matt.ma.xsd.MediaAlbumData;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

/**
 * Action to view a list of available albums for a given user.
 * 
 * <p>Created Sep 9, 2003</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class ViewAlbumsAction extends AbstractAlbumAction {

	/** The Xform XSL key: view-album. */
	public static final String VIEW_ALBUMS_TEMPLATES_KEY = "view-album";
	
	private static final Map XSLT_PARAM_MAP = new HashMap(3);

	static {
		XSLT_PARAM_MAP.put(ServletConstants.XSL_PARAM_KEY_ALBUM_MODE,ServletConstants.XSL_PARAM_ALBUM_MODE_LISTING);
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

	// 1: get the key, which is the user's anonymous key
	String key = (String)dForm.get("key");
	if ( key == null ) {
		notFound(mapping, request, response);
		return;
	}
	
	// 2: get the user biz
	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	
	// 3: get list of albums available for user to see
	Album[] albums = null;
	try {
		albums = userBiz.getAnonymousAlbumsForUser(key);
	} catch ( MediaAlbumException e ) {
		if ( e.hasErrorCode(UserBiz.ERROR_USER_NOT_FOUND) ) {
			notFound(mapping,request, response);
			return;
		}
	}
	
	if ( albums != null ) {
		for ( int i = 0; i < albums.length; i++ ) {
			data.addAlbum(albums[i]);
		}
	}
	
	// TODO sort albums by album date, creation date
	
	// 4: get the theme
	// TODO support non-default theme
	ThemeBiz themeBiz = (ThemeBiz)getBiz(BizConstants.THEME_BIZ);
	AlbumTheme theme = themeBiz.getDefaultAlbumTheme();
	data.setTheme( theme );
	
	result.setXslTemplate(isDefaultTheme(theme)?VIEW_ALBUMS_TEMPLATES_KEY:null);
	result.setXslParams(XSLT_PARAM_MAP);
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

}
