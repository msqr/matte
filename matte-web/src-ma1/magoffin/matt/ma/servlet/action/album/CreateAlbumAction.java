/* ===================================================================
 * CreateAlbumAction.java
 *
 * Copyright (c) 2002 Matt Magoffin.
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
 * $Id: CreateAlbumAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.album;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.biz.AlbumBiz;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.ServletConstants;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractAction;
import magoffin.matt.ma.servlet.formbean.CreateAlbumForm;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.Album;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * Action to create a new, empty album.
 * 
 * <p>Created Oct 25, 2002 4:04:22 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class CreateAlbumAction extends AbstractAction {

	/** 
	 * The message key for the cancel submit action: 
	 * <code>uploadMediaForm.cancel.displayName</code> 
	 */
	public static final String MSG_SUBMIT_ACTION_CANCEL = 
		"createAlbumForm.cancel.displayName";
	
	/** 
	 * The message key for the upload submit action: 
	 * <code>uploadMediaForm.submit.displayName</code> 
	 */
	public static final String MSG_SUBMIT_ACTION_UPLOAD = 
		"createAlbumForm.submit.displayName";

/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.AbstractAction#go(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 */
protected void go(
	ActionMapping mapping,
	ActionForm form,
	HttpServletRequest request,
	HttpServletResponse response, ActionResult result)
	throws Exception 
{

	CreateAlbumForm caForm = (CreateAlbumForm)form;
	
	// check submit action
	String cancelAction = this.getResources(request).getMessage(MSG_SUBMIT_ACTION_CANCEL);
	
	if ( caForm.getSubmitAction() == null || cancelAction.equals(caForm.getSubmitAction()) ) {
		result.setForward(mapping.findForward(
				StrutsConstants.DEFAULT_CANCEL_FORWARD));
		return;
	}
	
	UserSessionData usd = this.getUserSessionData(request, ANONYMOUS_USER_NOT_OK);
	
	Album newAlbum = new Album();
	newAlbum.setName(caForm.getName());
	if ( caForm.getParentAlbumId() != null ) {
		newAlbum.setParentId(caForm.getParentAlbumId());
	}
	
	AlbumBiz albumBiz = (AlbumBiz)getBiz(BizConstants.ALBUM_BIZ);
	newAlbum = albumBiz.createAlbum(newAlbum,usd.getUser());
		
	request.setAttribute(ServletConstants.REQ_KEY_ALBUM_ID, newAlbum.getAlbumId());
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

}
