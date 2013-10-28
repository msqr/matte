/* ===================================================================
 * ImageMediaRequestHandlerParams.java
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
 * $Id: ImageMediaRequestHandlerParams.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.image;

import java.util.Arrays;

import magoffin.matt.ma.util.AbstractMediaRequestHandlerParams;
import magoffin.matt.ma.util.WorkScheduler;

/**
 * MediaRequestHandlerParams object for images.
 * 
 * <p>Created on Sep 30, 2002 4:55:17 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class ImageMediaRequestHandlerParams
extends AbstractMediaRequestHandlerParams 
implements WorkScheduler.ScheduleOrdering
{
	public static final String MAX_WIDTH = "w";
	public static final String MAX_HEIGHT = "h";
	public static final String SET_WIDTH = "W";
	public static final String SET_HEIGHT = "H";
	public static final String QUALITY = "q";
	
	private static final String[] PARAM_NAMES = new String[]
		{MAX_WIDTH, MAX_HEIGHT, SET_WIDTH, SET_HEIGHT, 
			QUALITY, SIZE, COMPRESSION, WATERMARK, WATERMARK_PARAM};
			
	private static final String[] ADMIN_PARAM_NAMES = new String[]
		{MAX_WIDTH, MAX_HEIGHT, SET_WIDTH, SET_HEIGHT, QUALITY};
	
	static {
		Arrays.sort(PARAM_NAMES);
		Arrays.sort(ADMIN_PARAM_NAMES);
	}
	
/**
 * Return a list of supported parameter names.
 * 
 * @return array of parameter names
 */
public String[] getSupportedParamNames() {
	return PARAM_NAMES;
}


/**
 * Return a list of admin-only parameter names.
 * 
 * <p>The following parameters are considered admin-only:</p>
 * 
 * <ul>
 * <li><code>MAX_WIDTH</code></li>
 * <li><code>MAX_HEIGHT</code></li>
 * <li><code>SET_WIDTH</code></li>
 * <li><code>SET_HEIGHT</code></li>
 * </ul>
 * 
 * @see magoffin.matt.ma.MediaRequestHandlerParams#getAdminOnlyParamNames()
 */
public String[] getAdminOnlyParamNames() 
{
	return ADMIN_PARAM_NAMES;
}


/**
 * @see magoffin.matt.ma.util.WorkScheduler.ScheduleOrdering#scheduleBefore(magoffin.matt.ma.util.WorkScheduler.ScheduleOrdering)
 */
public boolean scheduleBefore(WorkScheduler.ScheduleOrdering s) 
{
	/* TODO: implement schedule
	if ( !(s instanceof ImageMediaRequestHandlerParams ) {
		return false;
	}
	ImageMediaRequestHandlerParams params = (ImageMediaRequestHandlerParams)s;
	*/
	return false;
}

} // class ImageMediaRequestHandlerParams
