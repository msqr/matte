/* ===================================================================
 * NotInitializedException.java
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
 * $Id: NotInitializedException.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma;

/**
 * Exception thrown with the application is not configured appropriately.
 * 
 * <p>Created Oct 8, 2002 6:21:28 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class NotInitializedException extends MediaAlbumException 
{

/**
 * Constructor for NotInitializedException.
 */
public NotInitializedException() {
	super();
}

/**
 * Constructor for NotInitializedException.
 * @param msg
 */
public NotInitializedException(String msg) {
	super(msg);
}

/**
 * Constructor for NotInitializedException.
 * @param msg
 * @param nestedException
 */
public NotInitializedException(String msg, Exception nestedException) {
	super(msg, nestedException);
}

} // class NotInitializedException
