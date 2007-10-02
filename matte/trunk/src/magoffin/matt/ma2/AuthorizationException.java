/* ===================================================================
 * AuthorizationException.java
 * 
 * Created Sep 29, 2004 11:04:35 AM
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
 * $Id: AuthorizationException.java,v 1.4 2007/06/06 09:54:51 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2;

/**
 * Exception thrown when authorization fails.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.4 $ $Date: 2007/06/06 09:54:51 $
 */
public class AuthorizationException extends RuntimeException {

	private static final long serialVersionUID = -3263671199035716651L;

	/** Authorization exception reason. */
	public enum Reason {
		/** Bad password. */
		BAD_PASSWORD,
		
		/** Unknown login. */
		UNKNOWN_LOGIN,
		
		/** Duplicate login. */
		DUPLICATE_LOGIN,
		
		/** Duplicate email. */
		DUPLICATE_EMAIL,
		
		/** Registration not confirmed. */
		REGISTRATION_NOT_CONFIRMED,
		
		/** Registration already confirmed. */
		REGISTRATION_ALREADY_CONFIRMED,
		
		/** Forgotten password not confirmed. */
		FORGOTTEN_PASSWORD_NOT_CONFIRMED,
		
		/** Access denied to something. */
		ACCESS_DENIED,
		
		/** Access for anonymous users denied. */
		ANONYMOUS_ACCESS_DENIED,
	}
		
	private Reason reason;
	private String login;
	
	/**
	 * Construct authorization exception.
	 * @param login the attempted login
	 * @param reason the reason for the exception
	 */
	public AuthorizationException(String login, Reason reason) {
		this.reason = reason;
		this.login = login;
	}

	/**
	 * Get the attempted login.
	 * @return login value
	 */
	public String getLogin() {
		return login;
	}
	
	/**
	 * Get the authorization exception reason.
	 * @return reason
	 */
	public Reason getReason() {
		return reason;
	}
}
