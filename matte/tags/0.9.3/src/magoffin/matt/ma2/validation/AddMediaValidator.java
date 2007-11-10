/* ===================================================================
 * AddMediaValidator.java
 * 
 * Created May 4, 2006 11:32:40 AM
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

import magoffin.matt.ma2.support.AddMediaCommand;

import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for AddMediaCommand objects.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class AddMediaValidator implements Validator {

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean supports(Class clazz) {
		return AddMediaCommand.class.isAssignableFrom(clazz);
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	public void validate(Object obj, Errors errors) {
		AddMediaCommand cmd = (AddMediaCommand)obj;
		if ( cmd.getCollectionId() == null ) {
			errors.rejectValue("collectionId", "error.required", null, "collection.displayName");
		}
		if ( cmd.getTempFile() == null || !StringUtils.hasText(cmd.getTempFile().getName()) ) {
			errors.rejectValue("tempFile", "error.required", null, "upload.media.file.displayName");
		}
	}

}
