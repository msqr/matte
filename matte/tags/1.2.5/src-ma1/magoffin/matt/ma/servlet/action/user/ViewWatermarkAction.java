/* ===================================================================
 * DownloadWatermarkAction.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Apr 9, 2004 11:33:05 AM.
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
 * $Id: ViewWatermarkAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.user;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.biz.ThemeBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractAction;
import magoffin.matt.ma.xsd.User;
import magoffin.matt.util.FileUtil;
import magoffin.matt.util.StringUtil;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

/**
 * Action to download a user's watermark.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class ViewWatermarkAction extends AbstractAction 
{
	private static final Logger LOG = Logger.getLogger(ViewWatermarkAction.class);
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.AbstractAction#go(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 */
protected void go(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response, ActionResult result)
		throws Exception 
{
	UserSessionData usd = getUserSessionData(request,ANONYMOUS_USER_NOT_OK);
	
	User actingUser = usd.getUser();
	
	if ( actingUser.getWatermark() == null ) {
		notFound(mapping,request, response);
		return;
	}
	
	boolean download = false;
	DynaActionForm dForm = (DynaActionForm)form;
	if ( ((Boolean)dForm.get("download")).booleanValue() ) {
		download = true;
	}
	
	MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);
	ThemeBiz themeBiz = (ThemeBiz)getBiz(BizConstants.THEME_BIZ);
	File watermarkFile = themeBiz.getUserThemeResource(actingUser,
			actingUser.getWatermark());
	
	if ( watermarkFile == null ) {
		LOG.warn("Watermark file not found: user = " +actingUser.getUserId()
				+", watermark = " +actingUser.getWatermark());
		notFound(mapping,request, response);
		return;
	}
	
	String mime = itemBiz.getMIMEforExtension(StringUtil.substringAfter(
			watermarkFile.getName(),'.'));
	if ( download ) {
		response.setHeader("Content-Disposition","filename=\"" +watermarkFile.getName()+ "\"");
		response.setContentType("application/force-download");
	} else if ( mime != null ) {
		response.setContentType(mime);
	}
	response.setContentLength((int)watermarkFile.length());
	FileUtil.slurp(watermarkFile,response.getOutputStream());
}

}
