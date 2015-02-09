/* ===================================================================
 * UploadThemeForm.java
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
 * $Id: UploadThemeForm.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.formbean;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;
import org.apache.struts.validator.ValidatorForm;

import javax.servlet.http.HttpServletRequest;

/**
 * Form bean for uploading a new theme.
 * 
 * <p> Created on Feb 5, 2003 11:19:04 AM.</p>
 *
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class UploadThemeForm extends ValidatorForm 
{
	private FormFile zipData = null;
	private FormFile css = null;
	private FormFile xsl = null;
	private FormFile icon = null;
	private FormFile preview = null;
	private String name = null;
	private String author = null;
	private String email = null;
	private String description = null;
	private boolean global = false;
	private String submitAction = null;
	private Integer theme = null;


	/**
	 * Returns the author.
	 * @return String
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * Returns the css.
	 * @return FormFile
	 */
	public FormFile getCss() {
		return css;
	}

	/**
	 * Returns the description.
	 * @return String
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the email.
	 * @return String
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Returns the global.
	 * @return boolean
	 */
	public boolean isGlobal() {
		return global;
	}

	/**
	 * Returns the icon.
	 * @return FormFile
	 */
	public FormFile getIcon() {
		return icon;
	}

	/**
	 * Returns the name.
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the preview.
	 * @return FormFile
	 */
	public FormFile getPreview() {
		return preview;
	}

	/**
	 * Returns the submitAction.
	 * @return String
	 */
	public String getSubmitAction() {
		return submitAction;
	}

	/**
	 * Returns the xsl.
	 * @return FormFile
	 */
	public FormFile getXsl() {
		return xsl;
	}

	/**
	 * Sets the author.
	 * @param author The author to set
	 */
	public void setAuthor(String author) {
		this.author = author==null||author.length()<1 ? null : author;
	}

	/**
	 * Sets the css.
	 * @param css The css to set
	 */
	public void setCss(FormFile css) {
		this.css = css;
	}

	/**
	 * Sets the description.
	 * @param description The description to set
	 */
	public void setDescription(String description) {
		this.description = description==null||description.length()<1 ? null : description;
	}

	/**
	 * Sets the email.
	 * @param email The email to set
	 */
	public void setEmail(String email) {
		this.email = email==null||email.length()<1 ? null : email;
	}

	/**
	 * Sets the global.
	 * @param global The global to set
	 */
	public void setGlobal(boolean global) {
		this.global = global;
	}

	/**
	 * Sets the icon.
	 * @param icon The icon to set
	 */
	public void setIcon(FormFile icon) {
		this.icon = icon;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(String name) {
		this.name = name==null||name.length()<1 ? null : name;
	}

	/**
	 * Sets the preview.
	 * @param preview The preview to set
	 */
	public void setPreview(FormFile preview) {
		this.preview = preview;
	}

	/**
	 * Sets the submitAction.
	 * @param submitAction The submitAction to set
	 */
	public void setSubmitAction(String submitAction) {
		this.submitAction = submitAction;
	}

	/**
	 * Sets the xsl.
	 * @param xsl The xsl to set
	 */
	public void setXsl(FormFile xsl) {
		this.xsl = xsl;
	}

	/**
	 * Returns the updateId.
	 * @return Integer
	 */
	public Integer getTheme() {
		return theme;
	}

	/**
	 * Sets the updateId.
	 * @param updateId The updateId to set
	 */
	public void setTheme(Integer updateId) {
		this.theme = updateId == null || updateId.intValue() == 0 ? null : updateId;
	}

	/**
	 * @return FormFile
	 */
	public FormFile getZipData() {
		return zipData;
	}

	/**
	 * Sets the zipData.
	 * @param zipData The zipData to set
	 */
	public void setZipData(FormFile zipData) {
		this.zipData = zipData;
	}


	/*
	 * @see org.apache.struts.action.ActionForm#reset(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		super.reset(mapping, request);
		global = false;
		theme = null;
		xsl = css = icon = preview = zipData = null;
		name = description = author = email = null;
	}
	
	

	/* (non-Javadoc)
	 * @see org.apache.struts.action.ActionForm#validate(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
	 */
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		ActionErrors errs = super.validate(mapping, request);
		if ( css == null || css.getFileSize() < 1 ) {
			errs = addFileUploadError(errs,"upload.theme.error.css");
		}
		if ( icon == null || icon.getFileSize() < 1 ) {
			errs = addFileUploadError(errs,"upload.theme.error.icon");
		}
		if ( preview == null || preview.getFileSize() < 1 ) {
			errs = addFileUploadError(errs,"upload.theme.error.preview");
		}
		return errs;
	}
	
	private ActionErrors addFileUploadError(ActionErrors errors, String msgKey) {
		if ( errors == null ) {
			errors = new ActionErrors();
		}
		errors.add(ActionErrors.GLOBAL_ERROR,new ActionError(msgKey));
		return errors;
	}

} // class UploadThemeForm
