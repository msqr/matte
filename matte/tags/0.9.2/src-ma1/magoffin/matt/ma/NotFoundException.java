/* ===================================================================
 * NotFoundException.java
 * 
 * Copyright (c) 2003 Matt Magoffin. Created Feb 12, 2003.
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
 * $Id: NotFoundException.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma;

/**
 * Exception thrown with a requested object is not found.
 * 
 * <p>Created Feb 12, 2003 11:47:56 AM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public class NotFoundException extends MediaAlbumException 
{

/**
 * @param msg
 * @param messageKey
 */
public NotFoundException(String msg, String messageKey) {
	super(msg, messageKey);
}

/**
 * @param messageKey
 * @param messageParams
 */
public NotFoundException(String messageKey, Object[] messageParams) {
	super(messageKey, messageParams);
}

}
