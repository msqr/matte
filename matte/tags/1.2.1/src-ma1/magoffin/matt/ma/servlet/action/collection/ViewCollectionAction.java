/* ===================================================================
 * ViewCollectionAction.java
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
 * $Id: ViewCollectionAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.CollectionBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.ServletConstants;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractMediaAlbumDataAction;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.Collection;
import magoffin.matt.ma.xsd.MediaAlbumData;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.ma.xsd.User;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

/**
 * View all media items within a single collection.
 * 
 * <p>Created Sep 23, 2003</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class ViewCollectionAction extends AbstractMediaAlbumDataAction {

	private static Logger log = Logger.getLogger(ViewCollectionAction.class);
	
	/** The Xform XSL key: <code>view-collection</code>. */
	public static final String VIEW_COLLECTION_TEMPLATES_KEY = "view-collection";
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.AbstractMediaAlbumDataAction#goMediaAlbum(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, magoffin.matt.ma.servlet.ActionResult, magoffin.matt.ma.xsd.MediaAlbumData, magoffin.matt.ma.servlet.UserSessionData)
 */
protected void goMediaAlbum(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response,
		ActionResult result, MediaAlbumData data, UserSessionData usd)
		throws Exception 
{
	DynaActionForm dForm = dForm = (DynaActionForm)form;
	Integer collectionId = (Integer)dForm.get(ServletConstants.REQ_KEY_COLLECTION_ID);
	if ( collectionId == null || collectionId.intValue() == 0 ) {
		notFound(mapping,request, response);
		return;
	}
	
	if ( log.isDebugEnabled() ) {
		log.debug("Got collectionId = " +collectionId);
	}
	
	User user = usd.getUser();
	
	// verify user has permission to view collection
	verifyUserCanViewCollection(user,collectionId);

	CollectionBiz collectionBiz = (CollectionBiz)getBiz(BizConstants.COLLECTION_BIZ);
	
	Collection collection = collectionBiz.getCollectionById(collectionId, 
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
	MediaItem[] items = collectionBiz.getMediaItemsForCollection(collectionId, 
			ApplicationConstants.CACHED_OBJECT_ALLOWED, user);
	
	collection.setItem(items);
	data.addCollection(collection);
	data.setDisplaySource(collectionId.intValue());
	
	result.setXslTemplate(VIEW_COLLECTION_TEMPLATES_KEY);
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}
	
}
