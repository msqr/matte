/* ===================================================================
 * RegisterForm.java
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
 * $Id: RegisterForm.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.formbean;

import javax.servlet.http.HttpServletRequest;

import magoffin.matt.ma.xsd.Registration;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.ValidatorForm;

/**
 * Form bean for registration.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public class RegisterForm extends ValidatorForm 
{	
	private Registration reg = null;
	private String submitAction = null;
	private String passwordConfirm = null;
	private String inviteKey = null;
	
/* (non-Javadoc)
 * @see org.apache.struts.action.ActionForm#reset(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
 */
public void reset(ActionMapping mapping, HttpServletRequest request) {
	super.reset(mapping, request);
	submitAction = null;
	reg = new Registration();
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
 * @return Returns the inviteKey.
 */
public String getInviteKey() {
	return inviteKey;
}

/**
 * @param inviteKey The inviteKey to set.
 */
public void setInviteKey(String inviteKey) {
	this.inviteKey = inviteKey;
}

/**
 * @return Returns the reg.
 */
public Registration getReg() {
	return reg;
}

/**
 * @param reg The reg to set.
 */
public void setReg(Registration reg) {
	this.reg = reg;
}

}
