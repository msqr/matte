/* ===================================================================
 * HomeAction.java
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
 * ===================================================================
 * $Id: HomeAction.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.ServletConstants;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.MediaAlbumData;
import magoffin.matt.ma.xsd.User;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

/**
 * Display the main logged-in home page.
 * 
 * <p>Created Oct 12, 2002 7:03:27 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class HomeAction extends AbstractMediaAlbumDataAction {
	
	private static Logger log = Logger.getLogger(HomeAction.class);
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.AbstractAnonymousMediaAlbumDataAction#goMediaAlbum(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, magoffin.matt.ma.xsd.MediaAlbumData, magoffin.matt.ma.servlet.UserSessionData)
 */
public void goMediaAlbum(
	ActionMapping mapping,
	ActionForm form,
	HttpServletRequest request,
	HttpServletResponse response,
	ActionResult result,
	MediaAlbumData data, 
	UserSessionData usd)
	throws Exception 
{
	DynaActionForm dForm = null;
	if ( form != null ) {
		dForm = (DynaActionForm)form;
	}
	
	Integer albumId = (Integer)dForm.get(ServletConstants.REQ_KEY_ALBUM_ID);
	if ( albumId != null && albumId.intValue() == 0 ) {
		albumId = null;
	}
	Integer collectionId = (Integer)dForm.get(ServletConstants.REQ_KEY_COLLECTION_ID);
	if ( collectionId != null && collectionId.intValue() == 0 ) {
		collectionId = null;
	}
	
	if ( log.isDebugEnabled() ) {
		log.debug("Got albumId = " +albumId +", collectionId = " +collectionId);
	}
	
	User user = usd.getUser();
	
	setup(data,user,albumId,collectionId);
	
	result.setXslTemplate(HOME_TEMPLATES_KEY);
	
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

}
