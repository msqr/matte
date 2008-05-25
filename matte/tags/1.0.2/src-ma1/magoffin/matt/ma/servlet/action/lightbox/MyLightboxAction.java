/* ===================================================================
 * MyLightboxAction.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Jun 12, 2004 8:03:27 PM.
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
 * $Id: MyLightboxAction.java,v 1.1 2006/06/03 22:26:19 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.lightbox;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.biz.AlbumBiz;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractMediaAlbumDataAction;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.Album;
import magoffin.matt.ma.xsd.Lightbox;
import magoffin.matt.ma.xsd.MediaAlbumData;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.ma.xsd.User;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * Display the "My Lightbox" page.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:19 $
 */
public class MyLightboxAction  extends AbstractMediaAlbumDataAction
{
	/** The Xform XSL template key: <code>my-lightbox</code>. */
	public static final String MY_LIGHTBOX_TEMPLATES_KEY = "my-lightbox";

	/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.AbstractMediaAlbumDataAction#goMediaAlbum(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, magoffin.matt.ma.servlet.ActionResult, magoffin.matt.ma.xsd.MediaAlbumData, magoffin.matt.ma.servlet.UserSessionData)
 */
protected void goMediaAlbum(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response,
		ActionResult result, MediaAlbumData data, UserSessionData usd)
throws Exception 
{
	User user = usd.getUser();
	
	// populate media items on the lightbox for display
	Lightbox lb = usd.getLightbox();
	if ( lb != null ) {
		if ( lb.getItemIdCount() > 0 ) {
			MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);
			MediaItem[] items = itemBiz.getMediaItemsById(lb.getItemId(),
					ApplicationConstants.CACHED_OBJECT_ALLOWED);
			lb.setItem(items);
		} else {
			lb.clearItem(); // make sure none lingering
		}
		
		if ( lb.getAlbumIdCount() > 0 ) {
			AlbumBiz albumBiz = (AlbumBiz)getBiz(BizConstants.ALBUM_BIZ);
			Album[] albums = albumBiz.getAlbumsById(lb.getAlbumId(),user,
					ApplicationConstants.CACHED_OBJECT_ALLOWED);
			lb.setAlbum(albums);
		} else {
			lb.clearAlbum(); // make sure none lingering
		}
		
		data.setLightbox(lb);
	}
	
	setup(data,user,null,null);
	
	result.setXslTemplate(MY_LIGHTBOX_TEMPLATES_KEY);
	
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

}
