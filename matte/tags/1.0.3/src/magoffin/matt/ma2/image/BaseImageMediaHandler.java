/* ===================================================================
 * BaseImageMediaHandler.java
 * 
 * Created Mar 5, 2006 5:40:16 PM
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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2.image;

import java.util.List;

import org.springframework.core.io.Resource;

import magoffin.matt.ma2.MediaEffect;
import magoffin.matt.ma2.MediaMetadata;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.Metadata;
import magoffin.matt.ma2.support.AbstractMediaHandler;
import magoffin.matt.meta.image.ImageMetadataType;

/**
 * Base implementation of MediaMetadata for image media types.
 * 
 * <p>The {@link #DEFAULT_ORIENTATION_90_CLOCKWISE_VALUE}, 
 * {@link #DEFAULT_ORIENTATION_90_COUNTER_CLOCKWISE_VALUE}, and 
 * {@link #DEFAULT_ORIENTATION_180_VALUE} constants are 
 * set to {@code 8}, {@code 6}, and {@code 3} to work with EXIF camera 
 * orientation values by default.</p>
 * 
 * @author matt.magoffin
 * @version $Revision$ $Date$
 */
public abstract class BaseImageMediaHandler extends AbstractMediaHandler {
	
	/** The rotation degrees for 90 degrees clockwise. */
	public static final Integer ROTATE_90_CW = new Integer(90);
	
	/** The rotation degrees for 90 degrees counter-clockwise. */
	public static final Integer ROTATE_90_CCW = new Integer(-90);

	/** The rotation degrees for 180 degrees. */
	public static final Integer ROTATE_180 = new Integer(180);

	/** The default value for the <code>orientationMetadataKey</code> property. */
	public static final String DEFAULT_ORIENTATION_METADATA_KEY = "ORIENTATION";
	
	/** The default value for the <code>orientationCounterClockwiseValue</code> property. */
	public static final String DEFAULT_ORIENTATION_90_COUNTER_CLOCKWISE_VALUE
		= String.valueOf(6);
	
	/** The default value for the <code>orientationClockwiseValue</code> property. */
	public static final String DEFAULT_ORIENTATION_90_CLOCKWISE_VALUE
		= String.valueOf(8);
	
	/** The default value for the <code>orientation180</code> property. */
	public static final String DEFAULT_ORIENTATION_180_VALUE
		= String.valueOf(3);
	
	private String orientationMetadataKey = DEFAULT_ORIENTATION_METADATA_KEY;
	private String orientation180Value = DEFAULT_ORIENTATION_180_VALUE;
	private String orientation90CounterClockwiseValue = DEFAULT_ORIENTATION_90_COUNTER_CLOCKWISE_VALUE;
	private String orientation90ClockwiseValue = DEFAULT_ORIENTATION_90_CLOCKWISE_VALUE;
	

	/**
	 * Construct with a MIME type.
	 * @param mime the MIME
	 */
	public BaseImageMediaHandler(String mime) {
		super(mime);
	}
	
	@Override
	protected MediaMetadata handleMetadata(MediaRequest request, Resource mediaResource, 
			MediaItem item) {
		MediaMetadata result = super.handleMetadata(request, mediaResource, item);
		if ( item.getName() == null && result.getMetadataMap().containsKey(
				ImageMetadataType.TITLE.toString()) ) {
			String name = result.getMetadataMap().get(ImageMetadataType.TITLE.toString());
			item.setName(name);
		}
		return result;
	}

	/**
	 * Check if rotation needs to be performed for a given media item based on 
	 * the orientation metadata available in the media item.
	 * 
	 * <p>This method looks in the {@link MediaItem#getMetadata()} list for
	 * a {@link Metadata#getKey()} value equal to {@link #getOrientationMetadataKey()}
	 * as configured on this class.</p>
	 * 
	 * <p>If rotation should be applied, this method will set the 
	 * {@link MediaEffect#MEDIA_REQUEST_PARAM_ROTATE_DEGREES} parameter on 
	 * the {@link MediaRequest#getParameters()} map to the degrees by which 
	 * the image should be rotated. This is determined by examining the 
	 * {@link Metadata#getValue()} of the found orientation metadata object.
	 * If it is equal to {@link #getOrientation90ClockwiseValue()} then the degrees
	 * will be set to {@link #ROTATE_90_CW}. If the value is equal to
	 * {@link #getOrientation90CounterClockwiseValue()} then the degrees will be set 
	 * to {@link #ROTATE_90_CCW}. If the value is equal to {@link #getOrientation180Value()}
	 * then the degrees will be set to {@link #ROTATE_180}.</p>
	 * 
	 * @param item the item being processed
	 * @param request the request the current request
	 * @return boolean if rotation should be applied to the image
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected boolean needToRotate(MediaItem item, MediaRequest request) {
		if ( super.needToRotate(item, request) ) {
			return true;
		}
		
		// check for EXIF orientation
		if ( item.getMetadata() != null ) {
			for ( Metadata meta : (List<Metadata>)item.getMetadata() ) {
				if ( orientationMetadataKey.equals(meta.getKey()) ) {
					try {
						String orientation = meta.getValue();
						if ( orientation90ClockwiseValue.equals(orientation) 
								|| orientation90CounterClockwiseValue.equals(orientation) ) {
							if ( item.getWidth() > item.getHeight() ) {
								// we'll be needing to rotate, yes
								Integer degrees = orientation90CounterClockwiseValue.equals(orientation)
									? ROTATE_90_CCW : ROTATE_90_CW;
								request.getParameters().put(
									MediaEffect.MEDIA_REQUEST_PARAM_ROTATE_DEGREES, 
									degrees);
								return true;
							}
							break;
						} else if ( orientation180Value.equals(orientation) ) {
							// rotate 180 degrees
							request.getParameters().put(
									MediaEffect.MEDIA_REQUEST_PARAM_ROTATE_DEGREES,
									ROTATE_180);
							return true;
						}
					} catch ( Exception e ) {
						log.warn("Unable to parse integer from orientation [" +meta.getValue() +"]");
					}
				}
			}
		}
		
		// don't think we need to rotate
		return false;
	}

	/**
	 * @return the orientationMetadataKey
	 */
	public String getOrientationMetadataKey() {
		return orientationMetadataKey;
	}

	/**
	 * @param orientationMetadataKey the orientationMetadataKey to set
	 */
	public void setOrientationMetadataKey(String orientationMetadataKey) {
		this.orientationMetadataKey = orientationMetadataKey;
	}

	/**
	 * @return the orientation90CounterClockwiseValue
	 */
	public String getOrientation90CounterClockwiseValue() {
		return orientation90CounterClockwiseValue;
	}

	/**
	 * @param orientation90CounterClockwiseValue the orientation90CounterClockwiseValue to set
	 */
	public void setOrientation90CounterClockwiseValue(
			String orientation90CounterClockwiseValue) {
		this.orientation90CounterClockwiseValue = orientation90CounterClockwiseValue;
	}

	/**
	 * @return the orientation90ClockwiseValue
	 */
	public String getOrientation90ClockwiseValue() {
		return orientation90ClockwiseValue;
	}

	/**
	 * @param orientation90ClockwiseValue the orientation90ClockwiseValue to set
	 */
	public void setOrientation90ClockwiseValue(String orientation90ClockwiseValue) {
		this.orientation90ClockwiseValue = orientation90ClockwiseValue;
	}

	/**
	 * @return the orientation180Value
	 */
	public String getOrientation180Value() {
		return orientation180Value;
	}

	/**
	 * @param orientation180Value the orientation180Value to set
	 */
	public void setOrientation180Value(String orientation180Value) {
		this.orientation180Value = orientation180Value;
	}

}
