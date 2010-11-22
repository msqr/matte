/* ===================================================================
 * UserValidator.java
 * 
 * Created Sep 30, 2004 1:06:17 PM
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

package magoffin.matt.ma2.validation;

import magoffin.matt.ma2.domain.User;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validation for a user object.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class UserValidator implements Validator {

	public boolean supports(@SuppressWarnings("rawtypes") Class clazz) {
		return User.class.isAssignableFrom(clazz);
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	public void validate(Object obj, Errors errors) {
		User user = (User)obj;
		if ( user == null ) return;
		if ( user.getName() == null || user.getName().trim().length() < 1 ) {
			errors.rejectValue("user.name", "error.required", null, "name.displayName");
		}
		if ( user.getEmail() == null || user.getEmail().trim().length() < 1 ) {
			errors.rejectValue("user.email", "error.required", null, "email.displayName");
		}
		if ( user.getPassword() == null || user.getPassword().trim().length() < 1 ) {
			errors.rejectValue("user.password", "error.required", null, "password.displayName");
		}
		if ( user.getLogin() == null || user.getLogin().trim().length() < 1 ) {
			errors.rejectValue("user.login", "error.required", null, "login.displayName");
		}
		if ( user.getTz() == null || user.getTz().getCode() == null ) {
			errors.rejectValue("user.tz.code", "error.required", null, "timeZone.displayName");
		}
	}

}
