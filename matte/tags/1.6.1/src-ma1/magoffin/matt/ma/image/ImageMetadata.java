/* ===================================================================
 * ImageMetadata.java
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
 * $Id: ImageMetadata.java,v 1.1 2006/06/03 22:26:19 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.image;

import magoffin.matt.ma.MediaMetadata;

/**
 * Metadata interface for images.
 * 
 * <p> Created on Dec 9, 2002 5:11:52 PM.</p>
 *
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public interface ImageMetadata extends MediaMetadata 
{
	
	public static final String META_APERTURE = "Aperture";
	public static final String META_CAMERA_MAKE = "Camera make";
	public static final String META_CAMERA_MODEL = "Camera model";
	public static final String META_COMPRESSION = "Compression";
	public static final String META_FLASH = "Flash";
	public static final String META_ORIENTATION = "Orientation";
	public static final String META_SHUTTER_SPEED = "Shutter Speed";
	public static final String META_FOCAL_LENGTH = "Focal length";
	public static final String META_FOCAL_LENGTH_35_EQUIV = "Focal length (35mm equiv)";
	public static final String META_MAX_APERTURE = "Max aperture";
	public static final String META_EXPOSURE_BIAS = "Exposure bias";
	public static final String META_EXPOSURE_TIME = "Exposure time";

/**
 * Get the aperture as an F-stop value.
 * 
 * @return String
 */
public String getApertureAsFstop();

/**
 * Get the original compression format used.
 * 
 * @return String
 */
public String getCompressionType();

/**
 * Get the shutter speed as fraction of seconds.
 * 
 * <p>For example: <code>1/16</code>.</p>
 * 
 * @return String
 */
public String getShutterSpeedAsFractionSecs();

/**
 * Get the flash setting.
 * 
 * @return String
 */
public String getFlashSetting();

/**
 * Get the camera make (manufacturer).
 * 
 * @return String
 */
public String getCameraMake();

/**
 * Get the camera model.
 * 
 * @return String
 */
public String getCameraModel();

/**
 * Get the camera's orientation.
 * 
 * @return String
 */
public String getOrientationSetting();

/**
 * Get the focal length, in mm.
 * @return String
 */
public String getFocalLength();

/**
 * Get the focal length 35mm equivilent.
 * @return String
 */
public String getFocalLength35mm();

/**
 * Get the max aperture as an F-stop value.
 * @return String
 */
public String getMaxApertureAsFstop();

/**
 * Get the exposure bias.
 * @return String
 */
public String getExposureBias();


/**
 * Get the exposure time as fraction of seconds.
 * 
 * <p>For example: <code>1/16</code>.</p>
 * 
 * @return String
 */
public String getExposureTimeAsFractionSecs();
}
