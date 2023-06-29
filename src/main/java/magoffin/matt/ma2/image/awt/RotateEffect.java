/* ===================================================================
 * ScaleEffect.java
 * 
 * Created Mar 20, 2006 4:42:41 PM
 * 
 * Copyright (c) 2006 Matt Magoffin (spamsqr@msqr.us)
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
 */

package magoffin.matt.ma2.image.awt;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import magoffin.matt.ma2.MediaEffect;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.domain.MediaItem;

/**
 * Effect that rotates an image, based on the degrees specified by
 * the {@link MediaEffect#MEDIA_REQUEST_PARAM_ROTATE_DEGREES} request 
 * parameter.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public class RotateEffect extends BaseAwtMediaEffect {

	/**
	 * The key for this effect.
	 */
	public static final String ROTATE_KEY = "image.awt." +MediaEffect.KEY_ROTATE;
	

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.image.awt.AwtMediaEffect#applyEffect(magoffin.matt.ma2.domain.MediaItem, magoffin.matt.ma2.MediaRequest, java.awt.image.BufferedImage)
	 */
	public BufferedImage applyEffect(MediaItem item, MediaRequest request, BufferedImage source) {
		Integer degrees = getRotateDegrees(item,request);	
		if ( degrees == null ) return source;
		
		double radians = degrees.doubleValue() / 180 * Math.PI;
		AffineTransform xform = AffineTransform.getRotateInstance(radians);
		if ( degrees == -90 ) {
			xform.preConcatenate(AffineTransform.getTranslateInstance(
					0,source.getWidth()));
		} else if ( degrees == 90 ){
			xform.preConcatenate(AffineTransform.getTranslateInstance(
					source.getHeight(),0));
		} else {
			xform.preConcatenate(AffineTransform.getTranslateInstance(
					0,source.getHeight()));
			xform.preConcatenate(AffineTransform.getTranslateInstance(
					source.getWidth(),0));
			
		}
			
		// nearest-neighbor should be ok since 90 degree intervals
		AffineTransformOp op = new AffineTransformOp(xform,
				AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		
		if ( log.isDebugEnabled() ) {
			log.debug("Applying rotate effect on item [" +item.getItemId() +"] of " 
					+degrees +" degrees");
		}
		BufferedImage newImage = null;
		if ( degrees != 180 ) {
			newImage = new BufferedImage(source.getHeight(), 
					source.getWidth(), source.getType());
		} else {
			newImage = new BufferedImage(source.getWidth(),
					source.getHeight(), source.getType());
		}
		BufferedImage result = op.filter(source, newImage);
		if ( log.isDebugEnabled() ) {
			log.debug("Rotate effect complete on item [" +item.getItemId() +"] of " 
					+degrees +" degrees");
		}
		return result;
	}

	/**
	 * Get the degrees necessary for rotation of a media item that has a 
	 * {@link MediaEffect#MEDIA_REQUEST_PARAM_ROTATE_DEGREES} request 
	 * parameter set.
	 * 
	 * @param item the item being processed
	 * @param request the current request
	 * @return integer value, or <em>null</em> if no rotation should be performed
	 */
	private Integer getRotateDegrees(MediaItem item, MediaRequest request) {
		if ( request.getParameters().containsKey(MediaEffect.MEDIA_REQUEST_PARAM_ROTATE_DEGREES) ) {
			Object val = request.getParameters().get(MediaEffect.MEDIA_REQUEST_PARAM_ROTATE_DEGREES);
			if ( val instanceof Integer ) {
				return (Integer)val;
			}
			try {
				return Integer.valueOf(val.toString());
			} catch ( Exception e ) {
				log.warn("Unable to parse integer from degree [" +val +"]");
			}
		}	
		return null;
	}
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaEffect#getKey()
	 */
	public String getKey() {
		return ROTATE_KEY;
	}

}
