/* ===================================================================
 * DownloadItemsAction.java
 * 
 * Created Jun 14, 2004 10:38:24 AM
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
 * $Id: DownloadItemsAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.item;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MediaRequestHandler;
import magoffin.matt.ma.MediaRequestHandlerParams;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractAction;
import magoffin.matt.ma.servlet.formbean.MultiItemsForm;
import magoffin.matt.ma.util.InternalMediaResponse;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.ma.xsd.MediaSpec;
import magoffin.matt.util.StringUtil;

/**
 * Action to download a set of media items as a Zip archive.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class DownloadItemsAction extends AbstractAction 
{

/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.AbstractAction#go(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, magoffin.matt.ma.servlet.ActionResult)
 */
protected void go(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response,
		ActionResult result) throws Exception 
{
	UserSessionData usd = getUserSessionData(request,ANONYMOUS_USER_OK);
	
	MultiItemsForm dForm = (MultiItemsForm)form;
		
	response.setContentType("application/zip");
	response.setHeader("Content-Disposition","filename=\"Media Items.zip\"");
	
	ZipOutputStream zOut = new ZipOutputStream(response.getOutputStream());
	Integer[] itemIds = dForm.getMitems();
	
	MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);
	MediaItem[] items = itemBiz.getMediaItemsById(itemIds,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	if ( items == null ) return;
	
	MediaSpec spec = null;
	if ( dForm.getBooleanFlag() == null || !dForm.getBooleanFlag().booleanValue() ) {
		spec = usd.getSingleSpec();
	}
	
	Map pathMap = new HashMap(items.length);

	try {
		for ( int i = 0; i < items.length; i++ ) {
			zipItem("",items[i],zOut,itemBiz,spec,pathMap);
		}
	} finally {
		if ( zOut != null ) {
			try {
				zOut.finish();
				zOut.flush();
				zOut.close();
			} catch ( Exception e ) {
				// ignore
			}
		}
	}
}

private void zipItem(
	String basePath, 
	MediaItem item, 
	//MediaRequestHandlerParams params,
	//boolean wantOriginal,
	ZipOutputStream out, 
	MediaItemBiz itemBiz,
	MediaSpec userSpec,
	Map pathMap) 
throws IOException, MediaAlbumException
{
	// start new ZipEntry
	String zipPath = basePath + item.getPath();
	
	if ( pathMap.containsKey(zipPath) ) {
		// try to add number before extension, if no extension then
		// at end of name
		String prefix = StringUtil.substringBeforeLast(zipPath,'.');
		if ( prefix == null ) {
			prefix = zipPath;
		}
		String suffix = StringUtil.substringAfter(zipPath,'.');
		if ( suffix == null ) {
			suffix = "";
		} else {
			suffix = "."+suffix;
		}
		for ( int i = 1; true; i++ ) {
			String newPath = prefix + "-" +i +suffix;
			if ( !pathMap.containsKey(newPath) ) {
				zipPath = newPath;
				break;
			}
		}
	}
	
	ZipEntry entry = new ZipEntry(zipPath);
	out.putNextEntry(entry);
	pathMap.put(zipPath,Boolean.TRUE);
	
	MediaRequestHandler handler = itemBiz.getHandlerForItem(item);
	boolean wantOriginal = userSpec == null ? true : false;
	MediaRequestHandlerParams params = handler.getParamInstance();
	if ( !wantOriginal ) {
		params.setParam(MediaRequestHandlerParams.COMPRESSION, userSpec.getCompress());
		params.setParam(MediaRequestHandlerParams.SIZE,userSpec.getSize());
	}
	
	itemBiz.handleMediaItem(out,item,wantOriginal,handler,
			params,InternalMediaResponse.INTERNAL_RESPONSE);
	
	out.closeEntry();
}

}
