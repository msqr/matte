/* ===================================================================
 * MediaAlbumException.java
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
 * $Id: MediaAlbumException.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma;

import magoffin.matt.exception.NestableException;

/**
 * Base exception for the Media Album application.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class MediaAlbumException 
extends NestableException 
{
	
/**
 * Constructor for MediaAlbumException.
 */
public MediaAlbumException() {
	super();
}

/**
 * Constructor for MediaAlbumException.
 * 
 * @param msg
 */
public MediaAlbumException(String msg) {
	super(msg);
}

/**
 * Constructor for MediaAlbumException.
 * 
 * @param msg the exception message
 * @param messageKey the message key
 */
public MediaAlbumException(String msg, String messageKey) {
	super(msg, messageKey);
}

/**
 * Constructor for MediaAlbumException.
 * 
 * @param msg the exception message
 * @param nestedException the nested exception
 */
public MediaAlbumException(String msg, Exception nestedException) {
	super(msg, nestedException);
}

/**
 * Constructor for MediaAlbumException.
 * 
 * @param messageKey the message key
 * @param messageParams the message parameter values
 */
public MediaAlbumException(String messageKey, Object[] messageParams) {
	super(messageKey,messageParams);
}

public String getMessageKey() {
	return getErrorCode();
}

public void setMessageKey(String messageKey) {
	setErrorCode(messageKey);
}

public Object[] getMessageParams() {
	return getErrorParams();
}

public void setMessageParams(Object[] messageParams) {
	setErrorParams(messageParams);
}

}
