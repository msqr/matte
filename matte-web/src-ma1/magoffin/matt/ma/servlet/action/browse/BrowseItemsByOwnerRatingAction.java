/* ===================================================================
 * BrowseByOwnerRatingAction.java
 * 
 * Created Jul 27, 2004 9:31:48 AM
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
 * $Id: BrowseItemsByOwnerRatingAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.browse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.biz.UserBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.formbean.BrowseAlbumsForm;
import magoffin.matt.ma.xsd.AlbumTheme;
import magoffin.matt.ma.xsd.BrowseData;
import magoffin.matt.ma.xsd.User;

import org.apache.struts.action.ActionMapping;

/**
 * Action to browse media items by owner's rating view.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class BrowseItemsByOwnerRatingAction extends AbstractBrowseAction 
{

/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.browse.AbstractBrowseAction#goBrowse(org.apache.struts.action.ActionMapping, magoffin.matt.ma.servlet.formbean.BrowseAlbumsForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, magoffin.matt.ma.servlet.ActionResult, magoffin.matt.ma.xsd.BrowseData, magoffin.matt.ma.xsd.AlbumTheme, magoffin.matt.ma.servlet.UserSessionData, magoffin.matt.ma.xsd.User)
 */
protected void goBrowse(ActionMapping mapping, BrowseAlbumsForm form,
		HttpServletRequest request, HttpServletResponse response,
		ActionResult result, BrowseData data, AlbumTheme theme,
		UserSessionData usd, User browseUser) throws Exception 
{
	basicGoBrowse(mapping,form,request,response,result,data,theme,usd,
			browseUser,UserBiz.VIRTUAL_VIEW_MODE_OWNER_RATING);
}

}
