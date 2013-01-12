/* ===================================================================
 * LoginForm.java
 * 
 * Created Oct 4, 2004 12:32:26 PM
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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2.web;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.IOBiz;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.biz.WorkBiz.WorkInfo;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.Model;
import magoffin.matt.ma2.support.AddMediaCommand;
import magoffin.matt.ma2.web.util.WebConstants;
import magoffin.matt.util.TemporaryFile;
import magoffin.matt.util.TemporaryFileMultipartFileEditor;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

/**
 * Form controller uploading new media items.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class UploadMediaForm extends AbstractForm {
	
	private IOBiz ioBiz;
	private UserBiz userBiz;
	private long secondsToWaitToComplete = 2;
	
	@Override
	protected Object formBackingObject(HttpServletRequest request)
	throws Exception {
		BizContext context = getWebHelper().getBizContext(request, true);
		AddMediaCommand cmd = (AddMediaCommand)super.formBackingObject(request);
		if ( cmd.getLocalTz() == null ) {
			cmd.setLocalTz(context.getActingUser().getTz() != null 
					? context.getActingUser().getTz().getCode()
					: getSystemBiz().getDefaultTimeZone().getCode());
		}
		if ( cmd.getMediaTz() == null ) {
			cmd.setMediaTz(context.getActingUser().getTz() != null 
					? context.getActingUser().getTz().getCode()
					: getSystemBiz().getDefaultTimeZone().getCode());
		}
		return cmd;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
		BizContext context = getWebHelper().getBizContext(request,true);
		Map<String,Object> ref =  new LinkedHashMap<String,Object>();
		Model model = getDomainObjectFactory().newModelInstance();
		List<Collection> collections = userBiz.getCollectionsForUser(
				context.getActingUser(),context);
		model.getCollection().addAll(collections);
		model.getTimeZone().addAll(getSystemBiz().getAvailableTimeZones());
		
		ref.put(WebConstants.DEFALUT_REFERENCE_DATA_OBJECT, model);
		
		// also stash the AddMediaCommand values so can be referenced by view
		AddMediaCommand cmd = (AddMediaCommand)command;
		ref.put("collectionId", cmd.getCollectionId());
		ref.put("localTz", cmd.getLocalTz());
		ref.put("mediaTz", cmd.getMediaTz());
		
		return ref;
	}

	@Override
	protected ModelAndView onSubmit(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		BizContext context = getWebHelper().getBizContext(request,true);
		AddMediaCommand form = (AddMediaCommand)command;

		WorkInfo workInfo = ioBiz.importMedia(form,context);
		
		// if work not complete right now, wait a short moment for it 
		// to possibly finish...
		if ( !workInfo.isDone() ) {
			try {
				workInfo.get(secondsToWaitToComplete,TimeUnit.SECONDS);
			} catch ( TimeoutException e ) {
				// work not finished... just continue
			}
		}
		
		Map<String,Object> model = new LinkedHashMap<String,Object>();
		MessageSourceResolvable msg = null;
		if ( workInfo.isDone() ) {
			if ( workInfo.getException() != null ) {
				msg = new DefaultMessageSourceResolvable(
						new String[] {"upload.exception"}, 
						new Object[] {workInfo.getException().getMessage()}, 
						"There was an error processing your media items.");
			} else {
				msg = new DefaultMessageSourceResolvable(
						new String[] {"upload.complete"}, null,
						"The media items have been uploaded.");
			}
		} else {
			getWebHelper().populateModelWorkInfo(request, workInfo, model);
			msg = new DefaultMessageSourceResolvable(
					new String[] {"upload.pending"}, null,
					"The media items are being processed.");
		}
		model.put(WebConstants.ALERT_MESSAGES_OBJECT,msg);
		model.put("collectionId", form.getCollectionId());
		
		return new ModelAndView(getSuccessView(),model);
	}

	@Override
	protected void initBinder(HttpServletRequest request,
			ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		
		// register our Multipart TemporaryFile binder...
		binder.registerCustomEditor(TemporaryFile.class, new TemporaryFileMultipartFileEditor());
	}
	
	/**
	 * @return Returns the ioBiz.
	 */
	public IOBiz getIoBiz() {
		return ioBiz;
	}

	/**
	 * @param ioBiz The ioBiz to set.
	 */
	public void setIoBiz(IOBiz ioBiz) {
		this.ioBiz = ioBiz;
	}

	/**
	 * @return Returns the secondsToWaitToComplete.
	 */
	public long getSecondsToWaitToComplete() {
		return secondsToWaitToComplete;
	}

	/**
	 * @param secondsToWaitToComplete The secondsToWaitToComplete to set.
	 */
	public void setSecondsToWaitToComplete(long secondsToWaitToComplete) {
		this.secondsToWaitToComplete = secondsToWaitToComplete;
	}
	
	/**
	 * @return Returns the userBiz.
	 */
	public UserBiz getUserBiz() {
		return userBiz;
	}
	
	/**
	 * @param userBiz The userBiz to set.
	 */
	public void setUserBiz(UserBiz userBiz) {
		this.userBiz = userBiz;
	}

}
