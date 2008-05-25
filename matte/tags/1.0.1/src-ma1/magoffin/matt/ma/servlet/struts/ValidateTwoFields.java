/* ===================================================================
 * ValidateTwoFields.java
 * 
 * Created Jan 16, 2004 7:44:03 AM
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
 * $Id: ValidateTwoFields.java,v 1.1 2006/06/03 22:26:19 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet.struts;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.validator.Field;
import org.apache.commons.validator.GenericValidator;
import org.apache.commons.validator.Validator;
import org.apache.commons.validator.ValidatorAction;
import org.apache.commons.validator.ValidatorUtil;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.validator.Resources;

/**
 * Validate two fields are equal.
 * 
 * <p>Code derived from example on Struts Validation Guide located at
 * <a href="http://jakarta.apache.org/struts/userGuide/dev_validator.html">
 * http://jakarta.apache.org/struts/userGuide/dev_validator.html</a></p>
 * 
 * <p>Created Sep 4, 2003</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:19 $
 */
public class ValidateTwoFields {

public static boolean validateTwoFields(
	Object bean,
	ValidatorAction va, 
	Field field,
	ActionErrors errors,
	Validator validator,
	HttpServletRequest request) {

	String value = ValidatorUtil.getValueAsString(
		bean, 
		field.getProperty());
	String sProperty2 = field.getVarValue("secondProperty");
	String value2 = ValidatorUtil.getValueAsString(
		bean, 
		sProperty2);

	if (!GenericValidator.isBlankOrNull(value)) {
	   try {
		  if (!value.equals(value2)) {
			 errors.add(field.getKey(),
				Resources.getActionError(
					request,
					va,
					field));

			 return false;
		  }
	   } catch (Exception e) {
			 errors.add(field.getKey(),
				Resources.getActionError(
					request,
					va,
					field));
			 return false;
	   }
	}

	return true;
}

}
