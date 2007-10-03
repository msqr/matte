/* ===================================================================
 * ValidationException.java
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
 * $Id: ValidationException.java,v 1.1 2007/01/07 05:50:40 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2;

import org.springframework.validation.Errors;

/**
 * Application exception for validation errors.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2007/01/07 05:50:40 $
 */
public class ValidationException extends RuntimeException {
	
	private static final long serialVersionUID = 4297984109932790210L;
	
	private Errors errors;
	
	/**
	 * Default constructor.
	 */
	public ValidationException() {
		this(null);
	}
	
	/**
	 * Constructor with Errors.
	 * @param errors
	 */
	public ValidationException(Errors errors) {
		super();
		this.errors = errors;
	}

	/**
	 * @return Returns the errors.
	 */
	public Errors getErrors() {
		return errors;
	}

	/**
	 * @param errors The errors to set.
	 */
	public void setErrors(Errors errors) {
		this.errors = errors;
	}

}
