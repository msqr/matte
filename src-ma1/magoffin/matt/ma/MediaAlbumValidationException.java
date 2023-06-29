/* ===================================================================
 * MediaAlbumValidationException.java
 * 
 * Created Jan 19, 2004 2:57:37 PM
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
 * $Id: MediaAlbumValidationException.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma;

import magoffin.matt.exception.ValidationExceptionIntf;

/**
 * Media album validation exception.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public class MediaAlbumValidationException
	extends MediaAlbumException
	implements ValidationExceptionIntf 
{
	
	public String[] paramKeys = null;
	
/**
 * Construct exception with a message key.
 * 
 * @param messageKey the error message key
 */
public MediaAlbumValidationException(String messageKey)
{
	super(messageKey,(Object[])null);
}

/**
 * Construct exception with a message key and message params.
 * 
 * @param messageKey the error message key
 * @param messageParams the error message parameters
 */
public MediaAlbumValidationException(
	String messageKey,
	Object[] messageParams) 
{
	super(messageKey, messageParams);
}

public MediaAlbumValidationException(String messageKey, String[] messageParamKeys) 
{
	super(messageKey,(Object[])null);
	paramKeys = messageParamKeys;
}

/**
 * @return Returns the paramKeys.
 */
public String[] getParamKeys() {
	return paramKeys;
}

/**
 * @param paramKeys The paramKeys to set.
 */
public void setParamKeys(String[] paramKeys) {
	this.paramKeys = paramKeys;
}

}
