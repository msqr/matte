/* ===================================================================
 * UploadMediaAction.java
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
 * $Id: UploadMediaAction.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MessageConstants;
import magoffin.matt.ma.NotAuthorizedException;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.CollectionBiz;
import magoffin.matt.ma.biz.MediaItemBiz;
import magoffin.matt.ma.biz.WorkBiz;
import magoffin.matt.ma.scan.MediaScan;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.ServletUtil;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.formbean.UploadMediaForm;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.Collection;
import magoffin.matt.util.FileUtil;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.upload.FormFile;
import org.apache.struts.util.RequestUtils;

/**
 * Allow user to upload new media.
 * 
 * <p>Created Nov 2, 2002 5:33:30 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class UploadMediaAction extends AbstractAction 
{
	
	/** 
	 * The message key for the cancel submit action: 
	 * <code>uploadMediaForm.cancel.displayName</code> 
	 */
	public static final String MSG_SUBMIT_ACTION_CANCEL = 
		"uploadMediaForm.cancel.displayName";
	
	/** 
	 * The message key for the upload submit action: 
	 * <code>uploadMediaForm.submit.displayName</code> 
	 */
	public static final String MSG_SUBMIT_ACTION_UPLOAD = 
		"uploadMediaForm.submit.displayName";

	private static final Logger LOG = Logger.getLogger(UploadMediaAction.class);
	
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
	if ( isCancelled(request) ) {
		result.setForward(mapping.findForward(
				StrutsConstants.DEFAULT_CANCEL_FORWARD));
		return;
	}
	
	UserSessionData usd = this.getUserSessionData(request, ANONYMOUS_USER_NOT_OK);	
	UploadMediaForm mForm = (UploadMediaForm)form;
	Integer collectionId = mForm.getCollection();
	
	CollectionBiz collectionBiz = (CollectionBiz)getBiz(BizConstants.COLLECTION_BIZ);
	MediaItemBiz itemBiz = (MediaItemBiz)getBiz(BizConstants.MEDIA_ITEM_BIZ);
	
	if ( !collectionBiz.canUserUpdateCollection(usd.getUser(),collectionId) ) {
		throw new NotAuthorizedException(usd.getUser().getUsername(),
				MessageConstants.ERR_AUTH_UPDATE_COLLECTION);
	}
	
	Collection collection = collectionBiz.getCollectionById(collectionId,
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
	
	FormFile formFile = mForm.getFile();
	
	String contentType = formFile.getContentType();
	String name = formFile.getFileName();
	int size = formFile.getFileSize();
	
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Got collection " +collectionId +" to update with " 
			+name +" of type "
			+contentType +" and " 
			+size +" bytes");
	}
	
	boolean runMediaScan = false;
	
	try {
	
		File outFile = new File(collectionBiz.getBaseCollectionDirectory(collection),name);
		
		InputStream in = new BufferedInputStream(
			formFile.getInputStream());
		OutputStream out = new BufferedOutputStream(
			new FileOutputStream(outFile));
			
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Writing file " +outFile +" (" +size +" bytes)");
		}
		
		FileUtil.save(in,outFile,true);
		
		// check for Zip file first
		if ( contentType.equals("application/zip") 
			|| contentType.equals("application/x-zip-compressed")
			|| contentType.equals("application/octet-stream") ) {
			UploadMediaZipWorkRequest.ZipUploadRequestParams params = 
				new UploadMediaZipWorkRequest.ZipUploadRequestParams();
			params.setAutoAlbum(mForm.isAutoAlbum());
			params.setAutoCollection(mForm.isAutoCollection());
			params.setCollection(collection);
			params.setBizFactory(ServletUtil.getBizIntfFactory(servlet.getServletContext()));
			params.setOverwrite(mForm.isOverwrite());
			params.setResources(getResources(request));
			params.setUserEmail(usd.getUser().getEmail());
			params.setZippedMediaFile(outFile);
			params.setUser(usd.getUser());

			URL viewAlbumUrl = RequestUtils.absoluteURL(request,
					mapping.findForward("view-album").getPath());
			URL browseUserUrl = RequestUtils.absoluteURL(request,
					mapping.findForward("browse-albums").getPath());
			params.setViewAlbumUrl(viewAlbumUrl);
			params.setBrowseUserUrl(browseUserUrl);
			
			UploadMediaZipWorkRequest workRequest = 
				new UploadMediaZipWorkRequest(params);
			
			WorkBiz workBiz = (WorkBiz)getBiz(BizConstants.WORK_BIZ);
			workBiz.queue(workRequest);

			this.addActionMessage(request,ActionMessages.GLOBAL_MESSAGE,
				new ActionMessage("upload.zip.complete",name));

		} else {
			// uploaded regular media file
			if ( itemBiz.isFileTypeSupported(outFile.getName()) ) {
				runMediaScan = true;
			} else {
				this.addActionMessage(request, ActionErrors.GLOBAL_ERROR,
					new ActionError("upload.error.file.unsupported",name));
				outFile.delete();
			}
		}
		
		if ( runMediaScan ) {
			boolean ok = true;
			
			// run the media scanner for this source dir
			MediaScan scanner = new MediaScan(
				ServletUtil.getBizIntfFactory(servlet.getServletContext()),
				false);
			try {
				scanner.doScan(collection);
			} catch ( MediaAlbumException e ) {
				LOG.error("Exception in MediaScan: " +e.getMessage());
				this.addActionMessage(request, ActionErrors.GLOBAL_ERROR,
					new ActionError("upload.error.general", e.getMessage()));
				ok = false;
			} finally {
				if ( scanner.hasErrors() ) {
					Map errMap = scanner.getErrors();
					for ( Iterator itr = errMap.entrySet().iterator(); itr.hasNext(); ) {
						Map.Entry me = (Map.Entry)itr.next();
						this.addActionMessage(request, ActionErrors.GLOBAL_ERROR,
							new ActionError("upload.error.general.file", me.getKey(), me.getValue()));
					}
				} else if ( ok ) {
					this.addActionMessage(request,ActionMessages.GLOBAL_MESSAGE,
							new ActionMessage("upload.media.complete",name));
				}
			}
		}
	
	} finally {
		
		formFile.destroy();
		
	}
	
	result.setForward(redirectToCollection(collectionId,request,response,
			mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD)));
}

}
