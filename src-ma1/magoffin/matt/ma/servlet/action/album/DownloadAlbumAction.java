/* ===================================================================
 * DownloadAlbumAction.java
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
 * $Id: DownloadAlbumAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.album;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MediaRequestHandler;
import magoffin.matt.ma.MediaRequestHandlerParams;
import magoffin.matt.ma.biz.AlbumBiz;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.ServletConstants;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.util.InternalMediaResponse;
import magoffin.matt.ma.util.MediaUtil;
import magoffin.matt.ma.xsd.Album;
import magoffin.matt.ma.xsd.MediaAlbumData;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.ma.xsd.MediaSpec;
import magoffin.matt.ma.xsd.User;
import magoffin.matt.util.StringUtil;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

/**
 * Action to download an entire album as a ZIP file.
 * 
 * <p>Created Mar 2, 2003 12:43:13 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class DownloadAlbumAction extends AbstractAlbumAction 
{	
	/** Request parameter name for requesting the original media. */
	public final static String REQ_PARAM_ORIG = "original";

	private static final Logger LOG = Logger.getLogger(DownloadAlbumAction.class);

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
	User user = usd.getUser();
	DynaActionForm dForm = (DynaActionForm)form;
	
	String key = (String)dForm.get(ServletConstants.REQ_KEY_ALBUM_KEY);
	if ( key == null ) {
		notFound(mapping, request, response);
		return;
	}
	
	Album rootAlbum = this.getAlbumAndChildren(key,user,null,
			ApplicationConstants.POPULATE_MODE_NONE);
	
	Integer childAlbumId = (Integer)dForm.get("album");
	if ( childAlbumId != null && childAlbumId.intValue() != 0 ) {
		rootAlbum = MediaUtil.findAlbum(rootAlbum,childAlbumId);
	}
	
	response.setContentType("application/zip");
	response.setHeader("Content-Disposition","filename=\"" +rootAlbum.getName()+ ".zip\"");
	
	ZipOutputStream zOut = new ZipOutputStream(response.getOutputStream());
	
	AlbumBiz albumBiz = (AlbumBiz)getBiz(BizConstants.ALBUM_BIZ);
	MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);
	
	boolean wantOriginal = ((Boolean)dForm.get(REQ_PARAM_ORIG)).booleanValue();
	boolean notAdmin = !usd.isAdmin();
	Map paramValues = new HashMap();
	
	MediaSpec spec = usd.getSingleSpec();
	paramValues.put(MediaRequestHandlerParams.COMPRESSION, spec.getCompress());
	paramValues.put(MediaRequestHandlerParams.SIZE,spec.getSize());
	
	try {
		this.getAlbumMedia(rootAlbum,"",paramValues,wantOriginal,notAdmin,zOut,albumBiz,
				itemBiz,user);
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

private void getAlbumMedia(
	Album rootAlbum, 
	String basePath, 
	Map paramValues,
	boolean wantOriginal,
	boolean notAdmin,
	ZipOutputStream out, 
	AlbumBiz albumBiz,
	MediaItemBiz itemBiz,
	User actingUser)
throws IOException, MediaAlbumException
{
	// get all imagse for this album
	MediaItem[] items = albumBiz.getMediaItemsForAlbum(rootAlbum.getAlbumId(),
			ApplicationConstants.POPULATE_MODE_NONE, ApplicationConstants.CACHED_OBJECT_ALLOWED, actingUser);
	
	if ( items != null && items.length > 0 ) {
		for ( int i = 0; i < items.length; i++ ) {
			MediaRequestHandler handler = itemBiz.getHandlerForItem(items[i]);
			MediaRequestHandlerParams params = handler.getParamInstance();
			if ( wantOriginal ) {
				params.setParam(MediaRequestHandlerParams.WANT_ORIGINAL,Boolean.TRUE);
			} else if ( params != null ) {
				String[] optionNames = params.getSupportedParamNames();
				String[] adminNames = params.getAdminOnlyParamNames();
				if ( optionNames != null ) {
					for ( int o = 0; o < optionNames.length; o++ ) {
						if ( notAdmin &&
							Arrays.binarySearch(adminNames,optionNames[o]) > -1 ) {
							if ( LOG.isDebugEnabled() ) {
								LOG.debug("Skipping admin-only option param "+optionNames[o]);
							}
							continue;
						}
						String val = (String)paramValues.get(optionNames[o]);
						if ( val != null ) {
							if ( LOG.isDebugEnabled() ) {
								LOG.debug("Setting option param " +optionNames[o]
									+ " to " +val );
							}
							params.setParam(optionNames[o], val );
						}
					}
				}
			}

			this.zipItem(basePath,items[i],handler,params,wantOriginal,out,itemBiz);
		}
	}
	
	if ( rootAlbum.getAlbumCount() < 1 ) {
		return;
	}
	Album[] children = rootAlbum.getAlbum();
	for ( int i = 0; i < children.length; i++ ) {
		this.getAlbumMedia(children[i],basePath+children[i].getName()+"/",
			paramValues,wantOriginal,notAdmin,out,albumBiz,itemBiz,actingUser);
	}
}

/**
 * Append a file as a ZipEntry to a ZipOutputStream.
 * 
 * @param basePath
 * @param item
 * @param handler
 * @param params
 * @param wantOriginal
 * @param out
 * @throws IOException
 * @throws MediaAlbumException
 */
private void zipItem(
	String basePath, 
	MediaItem item, 
	MediaRequestHandler handler,
	MediaRequestHandlerParams params,
	boolean wantOriginal,
	ZipOutputStream out, 
	MediaItemBiz itemBiz) 
throws IOException, MediaAlbumException
{
	StringBuffer buf = new StringBuffer(basePath);
	
	// see about renaming file in case handler alters MIME type
	String itemMime = item.getMime();
	String outMime = handler.getOutputMime(item,params);
	if ( !outMime.equalsIgnoreCase(itemMime) ) {
		String outExt = itemBiz.getExtensionForMIME(outMime);
		String path = StringUtil.substringBeforeLast(item.getPath(),'.');
		buf.append(path).append('.').append(outExt);
	} else {
		buf.append(item.getPath());
	}
	
	// start new ZipEntry
	ZipEntry entry = new ZipEntry(buf.toString());
	out.putNextEntry(entry);
	
	
	itemBiz.handleMediaItem(out,item,wantOriginal,handler,
			params,InternalMediaResponse.INTERNAL_RESPONSE);
	
	out.closeEntry();
}

}
