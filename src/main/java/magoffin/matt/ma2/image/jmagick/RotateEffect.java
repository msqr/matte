/* ===================================================================
 * RotateEffect.java
 * 
 * Copyright (c) 2007 Matt Magoffin (spamsqr@msqr.us)
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

package magoffin.matt.ma2.image.jmagick;

import magick.ImageInfo;
import magick.MagickException;
import magick.MagickImage;
import magoffin.matt.ma2.MediaEffect;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.domain.MediaItem;

/**
 * Effect that rotates an image, based on the degrees specified by
 * the {@link MediaEffect#MEDIA_REQUEST_PARAM_ROTATE_DEGREES} request 
 * parameter.
 * 
 * @author matt.magoffin
 * @version 1.0
 */
public class RotateEffect extends BaseJMagickMediaEffect {

	/**
	 * The key for this effect.
	 */
	public static final String ROTATE_KEY = "image.jmagick." +MediaEffect.KEY_ROTATE;
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.image.jmagick.JMagickMediaEffect#applyEffect(magoffin.matt.ma2.domain.MediaItem, magoffin.matt.ma2.MediaRequest, magick.ImageInfo, magick.MagickImage)
	 */
	public MagickImage applyEffect(MediaItem item, MediaRequest request,
			ImageInfo inInfo, MagickImage image) {
		Integer degrees = getRotateDegrees(item, request);	
		if ( degrees == null ) return image;
		
		if ( log.isDebugEnabled() ) {
			log.debug("Applying rotate effect on item [" +item.getItemId() +"] of " 
					+degrees +" degrees");
		}
		double deg = degrees.doubleValue();
		try {
			MagickImage result = image.rotateImage(deg);
			if ( log.isDebugEnabled() ) {
				log.debug("Rotate effect complete on item [" +item.getItemId() +"] of " 
						+degrees +" degrees");
			}
			return result;
		} catch ( MagickException e ) {
			throw new RuntimeException("MagickException rotating: " +e,e);
		}
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
