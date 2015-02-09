/* ===================================================================
 * Scale.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Apr 12, 2004 9:26:23 AM.
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
 * $Id: Scale.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.image.effect;

import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.List;

import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.image.ImageEffect;
import magoffin.matt.ma.image.ImageMediaRequestHandler;
import magoffin.matt.ma.image.ImageMediaRequestHandlerParams;
import magoffin.matt.ma.util.Geometry;

import org.apache.log4j.Logger;

/**
 * Copies the source image onto the graphics object, resizing 
 * to the Geometry of the request params if necessary.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class Scale implements ImageEffect {
	
	private static final Logger LOG = Logger.getLogger(Scale.class);
	
	private static final RenderingHints HINTS = new RenderingHints(
			RenderingHints.KEY_INTERPOLATION,
			RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.image.ImageEffect#applyEffect(magoffin.matt.ma.image.ImageMediaRequestHandler, magoffin.matt.ma.image.ImageMediaRequestHandlerParams, java.awt.image.BufferedImage, java.util.List)
 */
public BufferedImage applyEffect(ImageMediaRequestHandler handler,
		ImageMediaRequestHandlerParams params, BufferedImage source,
		List bufferedOpList) throws MediaAlbumException 
{
	Geometry geometry = handler.getGeometry(params);
	
	int width = geometry.getWidth();
	int height = geometry.getHeight();
	
	if ( handler.isRotated(params) ) {
		width = geometry.getHeight();
		height = geometry.getWidth();
	}
	
	if ( width != source.getWidth() || height != source.getHeight() ) {
		
		double sx = (double)width / (double)source.getWidth();
		double sy = (double)height / (double)source.getHeight();
		
		AffineTransformOp op = new AffineTransformOp(
				AffineTransform.getScaleInstance(sx,sy), HINTS);
	
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Adding scale effect to size " +width +"x" +height +"...");
		}
		
		bufferedOpList.add(op);
	}
	return source;
}

/* (non-Javadoc)
 * @see magoffin.matt.util.ResetableObject#reset()
 */
public void reset() {
	// nada
}

}
