/* ===================================================================
 * EmailItemFormAction.java
 * 
 * Created Apr 15, 2004 7:30:49 PM
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
 * $Id: EmailItemFormAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.item;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.MediaAlbumValidationException;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractAction;
import magoffin.matt.ma.servlet.formbean.EmailItemForm;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.util.MediaSpecUtil;
import magoffin.matt.ma.xsd.MediaSpec;
import magoffin.matt.util.FileUtil;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * Go to the email item form page.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class EmailItemFormAction extends AbstractAction
{
	public static final long LARGE_ATTACHMENT_SIZE = 1048576; // 1 MB
/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.AbstractAction#go(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 */
protected void go(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response, ActionResult result)
		throws Exception
{
	UserSessionData usd = getUserSessionData(request,ANONYMOUS_USER_OK);
	EmailItemForm dForm = (EmailItemForm)form;
	dForm.setUser(usd.getUser());
	Integer[] itemIds = dForm.getMitems();
	
	if ( itemIds == null || itemIds.length < 1 ) {
		// FIXME handle error condition
		throw new MediaAlbumValidationException("FIXME");
	}
	if ( itemIds.length > 1 ) {
		dForm.setMulti(true);
	} else {
		dForm.setMulti(false);
	}
	
	// calulate size of attachments, and flag if large
	MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);
	MediaSpec spec = MediaSpecUtil.getImageSpec(MediaSpecUtil.SIZE_NORMAL,
			MediaSpecUtil.COMPRESS_HIGH);
	long attachmentSize = itemBiz.calculateItemSize(itemIds,spec,false);
	dForm.setAttachmentSize(FileUtil.getSizeString(attachmentSize,2));
	if ( attachmentSize > LARGE_ATTACHMENT_SIZE ) {
		dForm.setLargeAttachment(true);
	}
	
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

}
