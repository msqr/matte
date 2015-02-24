/* ===================================================================
 * AbstractAlbumAction.java
 * 
 * Copyright (c) 2003 Matt Magoffin. Created Mar 2, 2003.
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
 * $Id: AbstractAlbumAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.album;

import javax.servlet.http.HttpServletRequest;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.biz.AlbumBiz;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.ThemeBiz;
import magoffin.matt.ma.servlet.ServletConstants;
import magoffin.matt.ma.servlet.ServletUtil;
import magoffin.matt.ma.servlet.action.AbstractAnonymousMediaAlbumDataAction;
import magoffin.matt.ma.xsd.Album;
import magoffin.matt.ma.xsd.AlbumTheme;
import magoffin.matt.ma.xsd.User;

import org.apache.log4j.Logger;

/**
 * Base class for viewing album actions.
 * 
 * <p>Created Mar 2, 2003 12:55:31 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public abstract class AbstractAlbumAction extends AbstractAnonymousMediaAlbumDataAction 
{
	
	/** The theme component for viewing the album. */
	protected static final int THEME_COMPONENT_ALBUM = 1;
	
	/** The theme component for viewing the metadata. */
	protected static final int THEME_COMPONENT_META = 2;
	
	private static final Logger log = Logger.getLogger(AbstractAlbumAction.class);
	
/**
 * Get the AlbumTheme for the given album ID.
 * 
 * <p>The appropriate request attributes for Xform to handle the theme will be set
 * in this method, depending on the <var>themeComponent</var> parameter.</p>
 * 
 * @param request the current request
 * @param albumId the ID of the album to get the theme for
 * @param themeComponent the theme component to save to the request
 * @return AlbumTheme
 * @throws MediaAlbumException
 */
protected AlbumTheme getAlbumTheme(HttpServletRequest request, Integer albumId, int themeComponent)
throws MediaAlbumException
{
	// get the theme
	AlbumBiz albumBiz = (AlbumBiz)getBiz(BizConstants.ALBUM_BIZ);
	AlbumTheme theme = null;
	if ( albumId != null ) {
		theme = albumBiz.getAlbumThemeForAlbum( albumId, 
				ApplicationConstants.CACHED_OBJECT_ALLOWED );
		if ( theme != null ) {
			switch ( themeComponent ) {
				case THEME_COMPONENT_ALBUM:
					request.setAttribute(ServletConstants.REQ_ATTR_XFORM_XSL_THEME,
						ServletUtil.getAlbumThemePath(theme));
					request.setAttribute(ServletConstants.REQ_ATTR_XFORM_XSL_THEME_HEADER,
						ServletConstants.THEME_XSL_HEADER_ALBUM);
					request.setAttribute(ServletConstants.REQ_ATTR_XFORM_XSL_THEME_FOOTER,
						ServletConstants.THEME_XSL_FOOTER_ALBUM);
					break;
				
			}
		}
	}
	if ( theme == null ) {
		ThemeBiz themeBiz = (ThemeBiz)getBiz(BizConstants.THEME_BIZ);
		theme = themeBiz.getDefaultAlbumTheme();
	}
	return theme;
}


/**
 * Tell if a theme is the default theme or not.
 * 
 * @param theme the theme to check
 * @return boolean <em>true</em> if <var>theme</var> is the default theme
 */
protected boolean isDefaultTheme(AlbumTheme theme)
{
	try {
		ThemeBiz themeBiz = (ThemeBiz)getBiz(BizConstants.THEME_BIZ);
		AlbumTheme defaultTheme = themeBiz.getDefaultAlbumTheme();
		if ( theme != null && defaultTheme.getThemeId().equals(theme.getThemeId())) {
			return true;
		}
	} catch ( Exception e ) {
		log.warn("Exception getting default theme: " +e.getMessage(),e);
	}
	return false;
}


protected Album getAlbumAndChildren(String key, User user, Object populateAlbum, 
		int itemPopulateMode)
throws MediaAlbumException
{
	AlbumBiz albumBiz = (AlbumBiz)getBiz(BizConstants.ALBUM_BIZ);
	Album rootAlbum = albumBiz.getAlbumByKey(key,user);
	if ( rootAlbum == null ) {
		return null;
	}
	
	Integer populateAlbumId = null;
	if ( populateAlbum != null && populateAlbum.equals(key) ) {
		populateAlbumId = rootAlbum.getAlbumId();
	} else if ( populateAlbum instanceof Integer ) {
		populateAlbumId = (Integer)populateAlbum;
	}
	
	albumBiz.fillInChildAlbums(rootAlbum,populateAlbumId,itemPopulateMode, user, AlbumBiz.UNLIMITED_DESCENT);
	return rootAlbum;
}

}
