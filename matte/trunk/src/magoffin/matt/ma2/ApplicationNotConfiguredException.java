/* ===================================================================
 * ApplicationNotConfiguredException.java
 * 
 * Created Jan 25, 2006 9:42:07 PM
 * 
 * Copyright (c) 2006 Matt Magoffin.
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
 * $Id: ApplicationNotConfiguredException.java,v 1.1 2007/01/27 07:49:14 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2;

/**
 * Exception thrown when the application has not been configured yet.
 * 
 * <p>This exception causes the setup process to start when accessing
 * the application for the first time.</p>
 * 
 * @author matt.magoffin
 * @version $Revision: 1.1 $ $Date: 2007/01/27 07:49:14 $
 */
public class ApplicationNotConfiguredException extends RuntimeException {

	private static final long serialVersionUID = 5916901801030658062L;

	/**
	 * Constructor.
	 */
	public ApplicationNotConfiguredException() {
		super();
	}

}
