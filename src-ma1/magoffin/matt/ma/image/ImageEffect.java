/* ===================================================================
 * ImageEffect.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Apr 12, 2004 9:03:05 AM.
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
 * $Id: ImageEffect.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */
package magoffin.matt.ma.image;

import java.awt.image.BufferedImage;
import java.util.List;

import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.util.ResetableObject;

/**
 * Interface for image effect processing.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public interface ImageEffect extends ResetableObject {
	
/**
 * Apply an effect.
 * 
 * @param handler the current image request handler
 * @param source the source image
 * @param params the current image request handler params
 * @param bufferedOpList list to add BufferedImageOp objects to if desired, 
 * which will be applied in order after all effects have been called
 * @return BufferedImage after changes applied (return <var>source</var> if no
 * changes made)
 * @throws MediaAlbumException if an error occurs
 */
public BufferedImage applyEffect(ImageMediaRequestHandler handler, 
		ImageMediaRequestHandlerParams params, BufferedImage source, 
		List bufferedOpList) 
throws MediaAlbumException;

}