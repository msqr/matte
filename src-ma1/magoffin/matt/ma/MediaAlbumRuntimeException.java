/* ===================================================================
 * MediaAlbumRuntimeException.java
 * 
 * Created Dec 2, 2003 7:46:30 PM
 * 
 * Copyright (c) 2003 Matt Magoffin.
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
 * $Id: MediaAlbumRuntimeException.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma;

import magoffin.matt.exception.NestableRuntimeException;

/**
 * Base runtime exception for Media Album.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public class MediaAlbumRuntimeException extends NestableRuntimeException {

/**
 * Default constructor.
 */
public MediaAlbumRuntimeException() {
	super();
}

/**
 * @param msg
 */
public MediaAlbumRuntimeException(String msg) {
	super(msg);
}

/**
 * @param msg
 * @param errorCode
 */
public MediaAlbumRuntimeException(String msg, String errorCode) {
	super(msg, errorCode);
}

/**
 * @param msg
 * @param nestedException
 */
public MediaAlbumRuntimeException(String msg, Exception nestedException) {
	super(msg, nestedException);
}

/**
 * @param msg
 * @param nestedException
 * @param errorCode
 */
public MediaAlbumRuntimeException(
	String msg,
	Exception nestedException,
	String errorCode) {
	super(msg, nestedException, errorCode);
}

}
