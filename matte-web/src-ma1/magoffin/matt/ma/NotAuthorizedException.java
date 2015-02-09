/* ===================================================================
 * NotAuthorizedException.java
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
 * $Id: NotAuthorizedException.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma;

/**
 * Exception thrown with a user does not have sufficient permission to 
 * access some resource.
 * 
 * <p> Created on Nov 7, 2002 3:09:29 PM.</p>
 *
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class NotAuthorizedException extends UserAccessException 
{
	
/**
 * Default constructor.
 */
public NotAuthorizedException() {}

/**
 * Construct an exception with a user and message key.
 * 
 * @param username the username of the user that caused the exception
 * @param messageKey the message key
 */
public NotAuthorizedException(String username, String messageKey)
{
	super(username,null);
	setMessageKey(messageKey);
}

/**
 * Construct an exception with a user and message key and parameters.
 * 
 * @param username the username of the user that caused the exception
 * @param messageKey the message key
 * @param messageParams the message params
 */
public NotAuthorizedException(String username, String messageKey, Object[] messageParams)
{
	super(username,null);
	setMessageKey(messageKey);
	setMessageParams(messageParams);
}

}
