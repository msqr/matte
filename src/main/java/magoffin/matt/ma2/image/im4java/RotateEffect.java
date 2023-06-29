/* ===================================================================
 * RotateEffect.java
 * 
 * Created Oct 28, 2010 9:43:03 AM
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
 */

package magoffin.matt.ma2.image.im4java;

import magoffin.matt.ma2.MediaEffect;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.domain.MediaItem;

import org.im4java.core.IMOperation;

/**
 * Effect that rotates an image, based on the degrees specified by
 * the {@link MediaEffect#MEDIA_REQUEST_PARAM_ROTATE_DEGREES} request 
 * parameter.
 *
 * @author matt
 * @version 1.0
 */
public class RotateEffect extends BaseIM4JavaMediaEffect {

	/**
	 * Default constructor.
	 */
	public RotateEffect() {
		super(MediaEffect.KEY_ROTATE);
	}
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.image.im4java.IM4JavaMediaEffect#applyEffect(magoffin.matt.ma2.domain.MediaItem, magoffin.matt.ma2.MediaRequest, org.im4java.core.IMOperation)
	 */
	public ImageCommandAndOperation applyEffect(MediaItem item, MediaRequest request,
			IMOperation baseOperation) {
		Integer degrees = getRotateDegrees(item, request);	
		if ( degrees == null ) return null;
		
		if ( log.isDebugEnabled() ) {
			log.debug("Applying rotate effect on item [" +item.getItemId() +"] of " 
					+degrees +" degrees");
		}
		baseOperation.rotate(degrees.doubleValue());
		return null;
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

}
