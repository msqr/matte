/* ===================================================================
 * Rotate.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Apr 19, 2004 4:23:11 PM.
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
 * $Id: Rotate.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.image.effect;

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
 * Rotate an image.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class Rotate implements ImageEffect 
{
	private static final Logger LOG = Logger.getLogger(Rotate.class);
	
	private Integer degrees;
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.image.ImageEffect#applyEffect(magoffin.matt.ma.image.ImageMediaRequestHandler, magoffin.matt.ma.image.ImageMediaRequestHandlerParams, java.awt.image.BufferedImage, java.util.List)
 */
public BufferedImage applyEffect(ImageMediaRequestHandler handler,
		ImageMediaRequestHandlerParams params, BufferedImage source,
		List bufferedOpList) throws MediaAlbumException 
{
	if ( degrees == null ) return source;

	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Adding rotate effect to of " +degrees +" degrees...");
	}
	
	Geometry geometry = handler.getGeometry(params);

	//	 neighbor should be ok since 90 degrees
	//double radians = degrees.doubleValue() / 360 * 2 * Math.PI;
	double radians = .5 * Math.PI;
	AffineTransform xform = AffineTransform.getRotateInstance(radians);
	if ( radians < 0 ) {
		xform.preConcatenate(AffineTransform.getTranslateInstance(
				0,geometry.getHeight()));
	} else {
		xform.preConcatenate(AffineTransform.getTranslateInstance(
				geometry.getWidth(),0));
	}
		
	AffineTransformOp op = new AffineTransformOp(xform,
			AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
	bufferedOpList.add(op);
	return source;
}


/* (non-Javadoc)
 * @see magoffin.matt.util.ResetableObject#reset()
 */
public void reset() {
	degrees = null;
}

/**
 * @return Returns the degrees.
 */
public Integer getDegrees() {
	return degrees;
}

/**
 * @param degrees The degrees to set.
 */
public void setDegrees(Integer degrees) {
	this.degrees = degrees;
}

}
