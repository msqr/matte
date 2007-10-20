/* ===================================================================
 * EditUserValidator.java
 * 
 * Created Sep 16, 2006 5:14:49 PM
 * 
 * Copyright (c) 2006 Matt Magoffin (spamsqr@msqr.us)
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

import magoffin.matt.ma2.domain.Edit;

import org.springframework.validation.Errors;

/**
 * Supports validating a User within an Edit object.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class EditUserValidator extends UserValidator {
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean supports(Class clazz) {
		return Edit.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object obj, Errors errors) {
		Edit edit = (Edit)obj;
		if ( edit == null ) return;
		super.validate(edit.getUser(), errors);
	}

}
