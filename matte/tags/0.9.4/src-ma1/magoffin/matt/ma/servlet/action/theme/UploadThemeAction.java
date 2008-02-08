/* ===================================================================
 * UploadThemeAction.java
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
 * $Id: UploadThemeAction.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.action.theme;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.biz.BizConstants;
import magoffin.matt.ma.biz.ThemeBiz;
import magoffin.matt.ma.servlet.ActionResult;
import magoffin.matt.ma.servlet.ServletConstants;
import magoffin.matt.ma.servlet.UserSessionData;
import magoffin.matt.ma.servlet.action.AbstractAction;
import magoffin.matt.ma.servlet.formbean.AlbumThemeFormData;
import magoffin.matt.ma.servlet.formbean.UploadThemeForm;
import magoffin.matt.ma.servlet.struts.StrutsConstants;
import magoffin.matt.ma.xsd.AlbumTheme;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

/**
 * Allow user to upload a new theme.
 * 
 * <p> Created on Feb 5, 2003 1:51:29 PM.</p>
 *
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class UploadThemeAction extends AbstractAction 
{
	private static final Logger log = Logger.getLogger(UploadThemeAction.class);
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.servlet.action.AbstractAction#go(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, magoffin.matt.ma.servlet.ActionResult)
 */
protected void go(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response,
		ActionResult result) throws Exception 
{
	UploadThemeForm uForm = (UploadThemeForm)form;
	UserSessionData usd = getUserSessionData(request, ANONYMOUS_USER_NOT_OK);
	
	if ( this.isCancelled(request) ) {
		result.setForward(redirectWithParam(request,response,
				mapping.findForward("cancel"),
				ServletConstants.REQ_KEY_THEME_ID,uForm.getTheme()));
		return;
	}
	
	ThemeBiz themeBiz = (ThemeBiz)getBiz(BizConstants.THEME_BIZ);
	
	try {
		AlbumTheme theme = getThemeFromForm(usd, uForm,themeBiz);
		ThemeBiz.AlbumThemeData themeData = new AlbumThemeFormData(uForm);
		if ( uForm.getTheme() != null ) {
			theme = themeBiz.updateAlbumTheme(theme,themeData,usd.getUser());
			this.addActionMessage(request,ActionMessages.GLOBAL_MESSAGE,
				new ActionMessage("upload.theme.update.ok",theme.getName()));
		} else {
			theme = themeBiz.createAlbumTheme(theme,themeData,usd.getUser());
			this.addActionMessage(request,ActionMessages.GLOBAL_MESSAGE,
				new ActionMessage("upload.theme.create.ok",theme.getName()));
		}
		
	} catch ( MediaAlbumException e ) {
		log.error("Exception uploading theme:" +e.toString());
		this.addActionMessage(request,ActionErrors.GLOBAL_ERROR,
				new ActionError(e.getMessageKey() != null ? e.getMessageKey() : "upload.theme.error.general",
								e.getMessage()));
	}
	
	result.setForward(mapping.findForward(StrutsConstants.DEFAULT_OK_FORWARD));
}

/**
 * Update a theme's common fields from a form bean.
 * 
 * <p>If the form does not provide the author's name or email the values will be
 * taken from the UserSessionData object.</p>
 * 
 * @param usd current UserSessionData object
 * @param form the form bean
 * @return the new theme
 */
private AlbumTheme getThemeFromForm(UserSessionData usd, UploadThemeForm form, 
		ThemeBiz themeBiz) throws MediaAlbumException
{
	AlbumTheme theme = null;
	if ( form.getTheme() != null ) {
		theme = themeBiz.getAlbumThemeById(form.getTheme(),usd.getUser(),
				ApplicationConstants.CACHED_OBJECT_ALLOWED);
		try {
			theme = (AlbumTheme)BeanUtils.cloneBean(theme);
		} catch ( Exception e ) {
			throw new MediaAlbumException("Unable to clone data",e);
		}
	} else {
		theme = new AlbumTheme();
	}
	theme.setGlobal(new Boolean(form.isGlobal()));
	theme.setName(form.getName());
	theme.setComment(form.getDescription());
	String author = form.getAuthor()!=null ? form.getAuthor() : usd.getUser().getName();
	String authorEmail = form.getEmail()!=null ? form.getEmail() : usd.getUser().getEmail();
	theme.setAuthor(author);
	theme.setAuthorEmail(authorEmail);
	return theme;
}

}
