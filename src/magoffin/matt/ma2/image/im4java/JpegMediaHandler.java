/* ===================================================================
 * JpegMediaHandler.java
 * 
 * Created Oct 21, 2010 10:41:27 AM
 * 
 * Copyright (c) 2010 Matt Magoffin.
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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2.image.im4java;

import magoffin.matt.ma2.image.ImageConstants;

/**
 * FIXME
 * 
 * <p>TODO</p>
 *
 * @author matt
 * @version $Revision$ $Date$
 */
public class JpegMediaHandler extends BaseIM4JavaMediaHandler {

	/**
	 * Default constructor.
	 */
	public JpegMediaHandler() {
		super(ImageConstants.JPEG_MIME);
		setPreferredFileExtension(ImageConstants.DEFAULT_JPEG_FILE_EXTENSION);
	}
	
}
