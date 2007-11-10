/* ===================================================================
 * UserMaintenanceForm.java
 * 
 * Created Jan 14, 2004 3:08:25 PM
 * 
 * Copyright (c) 2004 Matt Magoffin.
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
 * $Id: UserMaintenanceForm.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.formbean;

import javax.servlet.http.HttpServletRequest;

import magoffin.matt.ma.xsd.User;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;
import org.apache.struts.validator.ValidatorForm;

/**
 * Form bean for user maintenance.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public class UserMaintenanceForm extends ValidatorForm {
	
	private Integer user = null;
	private User u = null;
	private User actingUser = null;
	private String submitAction = null;
	private boolean creating = false;
	private String passwordConfirm = null;
	private boolean assignPermissions = false;
	private String realPassword = null;
	private FormFile watermark = null;
	private String watermarkPath = null;
	private boolean removeWatermark = false;
	private boolean assignQuota = false;
	private boolean assignMediaSpec = false;
	
/* (non-Javadoc)
 * @see org.apache.struts.action.ActionForm#reset(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
 */
public void reset(ActionMapping mapping, HttpServletRequest request) {
	super.reset(mapping, request);
	submitAction = null;
}

/**
 * @return Returns the assignMediaSpec.
 */
public boolean isAssignMediaSpec() {
	return assignMediaSpec;
}

/**
 * @param assignMediaSpec The assignMediaSpec to set.
 */
public void setAssignMediaSpec(boolean assignMediaSpec) {
	this.assignMediaSpec = assignMediaSpec;
}

/**
 * @return Returns the assignQuota.
 */
public boolean isAssignQuota() {
	return assignQuota;
}
/**
 * @param assignQuota The assignQuota to set.
 */
public void setAssignQuota(boolean assignQuota) {
	this.assignQuota = assignQuota;
}
/**
 * @return Returns the creating.
 */
public boolean isCreating() {
	return creating;
}

/**
 * @param creating The creating to set.
 */
public void setCreating(boolean creating) {
	this.creating = creating;
}

/**
 * @return Returns the submitAction.
 */
public String getSubmitAction() {
	return submitAction;
}

/**
 * @param submitAction The submitAction to set.
 */
public void setSubmitAction(String submitAction) {
	this.submitAction = submitAction;
}

/**
 * @return Returns the user.
 */
public User getU() {
	return u;
}

/**
 * @param user The user to set.
 */
public void setU(User user) {
	this.u = user;
}

/**
 * @return Returns the passwordConfirm.
 */
public String getPasswordConfirm() {
	return passwordConfirm;
}

/**
 * @param passwordConfirm The passwordConfirm to set.
 */
public void setPasswordConfirm(String passwordConfirm) {
	this.passwordConfirm = passwordConfirm;
}

/**
 * @return Returns the assignPermissions.
 */
public boolean isAssignPermissions() {
	return assignPermissions;
}

/**
 * @param assignPermissions The assignPermissions to set.
 */
public void setAssignPermissions(boolean assignPermissions) {
	this.assignPermissions = assignPermissions;
}

/**
 * @return Returns the actingUser.
 */
public User getActingUser() {
	return actingUser;
}

/**
 * @param actingUser The actingUser to set.
 */
public void setActingUser(User actingUser) {
	this.actingUser = actingUser;
}

/**
 * @return Returns the user ID.
 */
public Integer getUser() {
	return user;
}

/**
 * @param user The user ID to set.
 */
public void setUser(Integer user) {
	this.user = user;
}

/**
 * @return Returns the realPassword.
 */
public String getRealPassword() {
	return realPassword;
}

/**
 * @param realPassword The realPassword to set.
 */
public void setRealPassword(String realPassword) {
	this.realPassword = realPassword;
}

/**
 * @return Returns the watermark.
 */
public FormFile getWatermark() {
	return watermark;
}

/**
 * @param watermark The watermark to set.
 */
public void setWatermark(FormFile watermark) {
	this.watermark = watermark;
}

/**
 * @return Returns the watermarkPath.
 */
public String getWatermarkPath() {
	return watermarkPath;
}

/**
 * @param watermarkPath The watermarkPath to set.
 */
public void setWatermarkPath(String watermarkPath) {
	this.watermarkPath = watermarkPath;
}

/**
 * @return Returns the removeWatermark.
 */
public boolean isRemoveWatermark() {
	return removeWatermark;
}

/**
 * @param removeWatermark The removeWatermark to set.
 */
public void setRemoveWatermark(boolean removeWatermark) {
	this.removeWatermark = removeWatermark;
}

}
