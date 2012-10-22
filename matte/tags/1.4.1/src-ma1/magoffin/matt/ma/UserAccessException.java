/* ===================================================================
 * UserAccessException.java
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
 * $Id: UserAccessException.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma;


/**
 * Exception thrown with a user is not allowed to access something.
 * 
 * <p>Created Oct 24, 2002 4:39:22 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class UserAccessException extends MediaAlbumException 
{
	
	private String username = null;
	

/**
 * Constructor for UserAccessException.
 */
public UserAccessException() {
	this(null,null);
}

/**
 * Constructor for UserAccessException.
 * @param msg
 */
public UserAccessException(String msg) {
	this(null,msg);
}

/**
 * Constructor for UserAccessException.
 * @param username username of the user
 * @param msg the exception message
 */
public UserAccessException(String username, String msg) {
	super(msg);
	this.username = username;
}

/**
 * @return Returns the username.
 */
public String getUsername() {
	return username;
}

}
