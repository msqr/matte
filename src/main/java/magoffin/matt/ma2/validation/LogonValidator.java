/* ===================================================================
 * IngredientValidator.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Aug 18, 2004 8:42:27 PM.
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
 */

package magoffin.matt.ma2.validation;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import magoffin.matt.ma2.support.LogonCommand;

/**
 * Validator for LogonCommand objects.
 * 
 * @author Matt Magoffin (mmagoffi@yahoo.com)
 * @version 1.1
 */
public class LogonValidator implements Validator {

	@Override
	public boolean supports(@SuppressWarnings("rawtypes") Class clazz) {
		return LogonCommand.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object obj, Errors errors) {
		LogonCommand logon = (LogonCommand) obj;
		if ( logon.getLogin() == null || logon.getLogin().trim().length() < 1 ) {
			errors.rejectValue("login", "error.required", null, "login.displayName");
		}
		if ( logon.getPassword() == null || logon.getPassword().trim().length() < 1 ) {
			errors.rejectValue("password", "error.required", null, "password.displayName");
		}
	}

}
