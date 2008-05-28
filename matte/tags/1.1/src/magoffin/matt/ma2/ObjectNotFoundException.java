/* ===================================================================
 * ObjectNotFoundException.java
 * 
 * Created Apr 1, 2007 7:57:08 PM
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

package magoffin.matt.ma2;

/**
 * Exception thrown when a requested object is not found.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class ObjectNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -4651287642379301427L;

	/**
	 * Construct with a message.
	 * @param message the message
	 */
	public ObjectNotFoundException(String message) {
		super(message);
	}

	/**
	 * Construct with a message and nested exception.
	 * @param message the message
	 * @param exception the exception
	 */
	public ObjectNotFoundException(String message, Throwable exception) {
		super(message, exception);
	}

}
