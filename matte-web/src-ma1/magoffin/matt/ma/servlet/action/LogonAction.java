/* ===================================================================
 * LogonAction.java
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
 * $Id: LogonAction.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MessageConstants;
import magoffin.matt.ma.UserAccessException;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.LightboxBiz;
import magoffin.matt.ma.biz.UserBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.ServletConstants;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.formbean.LogonForm;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.util.MediaSpecUtil;
import magoffin.matt.ma.xsd.Collection;
import magoffin.matt.ma.xsd.Group;
import magoffin.matt.ma.xsd.Lightbox;
import magoffin.matt.ma.xsd.MediaSpec;
import magoffin.matt.ma.xsd.User;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * Action to log a user into the system.
 * 
 * <p>Created Oct 8, 2002 3:32:13 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class LogonAction extends AbstractAction 
{
	private static Logger LOG = Logger.getLogger(LogonAction.class);
	
/*
 * @see magoffin.matt.ma.servlet.action.AbstractAction#go(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 */
protected void go(
	ActionMapping mapping,
	ActionForm form,
	HttpServletRequest request,
	HttpServletResponse response, 
	ActionResult result)
throws Exception {
	
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Logon: " +form);
	}
	
	LogonForm logonForm = (LogonForm)form;

	UserBiz userBiz = (UserBiz)getBiz(BizConstants.USER_BIZ);
	
	User user = null;
	
	if ( request.getSession().getAttribute(ServletConstants.SES_KEY_AUTO_LOGON) instanceof User ) {
		user = (User)request.getSession().getAttribute(ServletConstants.SES_KEY_AUTO_LOGON);
		request.getSession().removeAttribute(ServletConstants.SES_KEY_AUTO_LOGON);
	} else {
		try {
			user = userBiz.logonUser(logonForm.getUsername(),logonForm.getPassword());
		} catch ( UserAccessException e ) {
			 // special handling of UAE so don't save URL for redirect
			addActionMessage(request,ActionErrors.GLOBAL_ERROR,
				new ActionError(MessageConstants.ERR_USER_SESSION));
			result.setForward(mapping.findForward(
					StrutsConstants.DEFAULT_ERROR_FORWARD));
			return;
		}
	}
	
	UserSessionData currUsd = (UserSessionData) request.getSession()
			.getAttribute(ServletConstants.SES_KEY_USER);
	Lightbox currLightbox = currUsd == null ? null : currUsd.getLightbox();
	
	UserSessionData usd = new UserSessionData();
	usd.setLoggedIn(true);
	usd.setUser(user);
	
	// get list of MediaDirs for user
	Collection[] collections = userBiz.getCollectionsForUser(user.getUserId());
	usd.setCollections(collections);
	
	// get all groups user belongs to
	Group[] groups = userBiz.getGroupsForUserId(user.getUserId());
	usd.setGroups(groups);
	
	// get the user's saved lightbox
	LightboxBiz lightboxBiz = (LightboxBiz)getBiz(BizConstants.LIGHTBOX_BIZ);
	Lightbox lightbox = lightboxBiz.getLightbox(user,
			ApplicationConstants.CACHED_OBJECT_ALLOWED);
	if ( lightbox  != null ) {
		if ( currLightbox != null ) {
			// merge user's saved lightbox with current one
			Integer[] currItemIds = currLightbox.getItemId();
			lightboxBiz.addMediaItemsToLightbox(lightbox,currItemIds,user);
		}
	} else {
		if ( currLightbox != null ) {
			lightbox = currLightbox;
		}
	}
	usd.setLightbox(lightbox);
	
	// check if user is admin
	if ( userBiz.isUserSuperUser(user.getUserId()) ) {
		usd.setAdmin(true);
	}
	
	// set the default image specs
	MediaSpec spec = MediaSpecUtil.getThumbImageSpec(user.getThumbSize(),user.getThumbCompress());
	usd.setThumbSpec(spec);
	spec = MediaSpecUtil.getImageSpec(user.getSingleSize(),user.getSingleCompress());
	usd.setSingleSpec(spec);
	
	if ( LOG.isInfoEnabled() ) {
		LOG.info("Logged in user: " +usd);
	}
	
	HttpSession session = request.getSession();
	session.setAttribute(ServletConstants.SES_KEY_USER,usd);
	session.setAttribute(ServletConstants.SES_KEY_USER_MEDIA_DIRS,usd.getCollections());
	
	result.setForward(redirectToSavedURL(request,response,
			mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD)));
}

}
