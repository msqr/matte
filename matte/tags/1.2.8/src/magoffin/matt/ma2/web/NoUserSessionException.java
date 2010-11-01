/* ===================================================================
 * NoUserSessionException.java
 * 
 * Created Jan 6, 2007 6:36:43 PM
 * 
 * Copyright (c) 2007 Matt Magoffin (spamsqr@msqr.us)
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

package magoffin.matt.ma2.web;

import magoffin.matt.ma2.domain.Session;

/**
 * Exception thrown when no {@link Session} is available in the current
 * HttpSession.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class NoUserSessionException extends RuntimeException {

	private static final long serialVersionUID = -2409822353604172356L;

	/**
	 * Default constructor.
	 */
	public NoUserSessionException() {
		super();
	}

	/**
	 * Construct with a message.
	 * @param msg the message
	 */
	public NoUserSessionException(String msg) {
		super(msg);
	}

}
