/* ===================================================================
 * BasicFileMediaRequestHandlerParams.java
 * 
 * Created Jul 18, 2004 7:14:27 PM
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
 * $Id: BasicFileMediaRequestHandlerParams.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.util;

/**
 * Basic request handler params object that does not accept any parameters.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public class BasicFileMediaRequestHandlerParams 
extends AbstractMediaRequestHandlerParams 
{
	private static final String[] NO_PARAMS = new String[0];

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandlerParams#getAdminOnlyParamNames()
 */
public String[] getAdminOnlyParamNames() {
	return NO_PARAMS;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandlerParams#getSupportedParamNames()
 */
public String[] getSupportedParamNames() {
	return NO_PARAMS;
}

}
