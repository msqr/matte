/* ===================================================================
 * LogonForm.java
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
 * $Id: LogonForm.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.formbean;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.ValidatorForm;

import javax.servlet.http.HttpServletRequest;

/**
 * Form bean for the logon action.
 * 
  * <p>Created Oct 8, 2002 3:50:32 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class LogonForm extends ValidatorForm 
{
	private String username = null;
	private String password = null;
	

/**
 * Returns the password.
 * @return String
 */
public String getPassword() {
	return password;
}

/**
 * Returns the username.
 * @return String
 */
public String getUsername() {
	return username;
}

/**
 * Sets the password.
 * @param password The password to set
 */
public void setPassword(String password) {
	this.password = password;
}

/**
 * Sets the username.
 * @param username The username to set
 */
public void setUsername(String username) {
	this.username = username;
}



/* (non-Javadoc)
 * @see org.apache.struts.action.ActionForm#reset(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
 */
public void reset(ActionMapping arg0, HttpServletRequest arg1) {
	super.reset(arg0, arg1);
	this.password = null;
	this.username = null;
}

}
