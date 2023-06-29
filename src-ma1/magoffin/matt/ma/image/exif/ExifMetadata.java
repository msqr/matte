/* ===================================================================
 * ExifMetadata.java
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
 * $Id: ExifMetadata.java,v 1.1 2006/06/03 22:26:19 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.image.exif;

import magoffin.matt.ma.image.ImageMetadata;
import magoffin.matt.ma.util.AbstractMediaMetadata;
import magoffin.matt.util.ArrayUtil;
import magoffin.matt.util.StringUtil;

import org.apache.log4j.Logger;

/**
 * MediaMetadat implementation for EXIF data.
 * 
 * <p> Created on Dec 9, 2002 6:10:45 PM.</p>
 *
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class ExifMetadata extends AbstractMediaMetadata 
implements ImageMetadata
{
	/** The delimiter used to separate meta values when serialized. */
	public static final char FIELD_DELIM = '|';
	
	/** The manufacturer of the camera that took the image (string). */
	public static final char CAMERA_MAKE = 'a';
	
	/** The model of the camera that took the image (string). */
	public static final char CAMERA_MODEL = 'b';
	
	/** The orientation when the image was taken (string). */
	public static final char ORIENTATION = 'c';
	
	/** The shutter speed, in fractional seconds. */
	public static final char SHUTTER_SPEED = 'd';
	
	/** The aperature, in APEX format (float). */
	public static final char APERTURE = 'e';
	
	/** 
	 * The flash setting, where 0 = flash did not fire, 1 = flash fired, 
	 * 5 = flash fired but strobe returned light not detected, 7 = flash fired
	 * and strobe returned light detected.
	 */
	public static final char FLASH = 'f';
	
	/** 
	 * The compression format used, where 1 = uncompressed and 6 = JPEG.
	 */
	public static final char COMPRESSION = 'g';

	/**
	 * The focal length, in mm.
	 */
	public static final char FOCAL_LENGTH = 'h';
	
	/**
	 * The focal length in 35mm.
	 */
	public static final char FOCAL_LENGTH_35MM = 'i';
	
	/**
	 * The exposure bias.
	 */
	public static final char EXPOSURE_BIAS = 'j';
	
	/**
	 * The exposure time.
	 */
	public static final char EXPOSURE_TIME = 'k';
	
	/**
	 * The maximum aperture, in APEX format (float).
	 */
	public static final char MAX_APERTURE = 'l';
	
	private static final Logger LOG = Logger.getLogger(ExifMetadata.class);
	
	protected String cameraMake = null;
	protected String cameraModel = null;
	protected int orientation = -1;
	protected String shutterSpeed = null;
	protected String exposureTime = null;
	protected float aperture = -1;
	protected float maxAperture = -1;
	protected int flash = -1;
	protected int compression = -1;
	protected float focalLength = -1;
	protected float focalLength35mm = -1;
	protected int exposureBias = Integer.MIN_VALUE;
	
public ExifMetadata()
{
	super();
}

/**
 * @see magoffin.matt.ma.MediaMetadata#serializeToString()
 */
public String serializeToString() 
{
	StringBuffer buf = new StringBuffer();
	if ( cameraMake != null && cameraMake.length() > 0 ) {
		buf.append(FIELD_DELIM).append(CAMERA_MAKE).append(cameraMake);
	}
	if ( cameraModel != null && cameraModel.length() > 0 ) {
		buf.append(FIELD_DELIM).append(CAMERA_MODEL).append(cameraModel);
	}
	if ( orientation >= 0 ) {
		buf.append(FIELD_DELIM).append(ORIENTATION).append(orientation);
	}
	if ( shutterSpeed != null ) {
		buf.append(FIELD_DELIM).append(SHUTTER_SPEED).append(shutterSpeed);
	}
	if ( aperture >= 0 ) {
		buf.append(FIELD_DELIM).append(APERTURE).append(aperture);
	}
	if ( flash >= 0 ) {
		buf.append(FIELD_DELIM).append(FLASH).append(flash);
	}
	if ( compression > 0 ) {
		buf.append(FIELD_DELIM).append(COMPRESSION).append(compression);
	}
	if ( focalLength > 0 ) {
		buf.append(FIELD_DELIM).append(FOCAL_LENGTH).append(focalLength);
	}
	if ( focalLength35mm > 0 ) {
		buf.append(FIELD_DELIM).append(FOCAL_LENGTH_35MM).append(focalLength35mm);
	}
	if ( exposureBias > Integer.MIN_VALUE ) {
		buf.append(FIELD_DELIM).append(EXPOSURE_BIAS).append(exposureBias);
	}
	if ( exposureTime != null ) {
		buf.append(FIELD_DELIM).append(EXPOSURE_TIME).append(exposureTime);
	}
	if ( maxAperture >= 0 ) {
		buf.append(FIELD_DELIM).append(MAX_APERTURE).append(maxAperture);
	}
	return buf.length() > 0 ? buf.toString() : null;
}

/**
 * @see magoffin.matt.ma.MediaMetadata#deserializeFromString(java.lang.String)
 */
public void deserializeFromString(String s) 
{
	if ( s == null || s.length() < 0 ) {
		return;
	}
	String[] fields = ArrayUtil.split(s,FIELD_DELIM,-1);
	for ( int i = 1; i < fields.length; i++ ) { // start at 1 because first will be empty
		char tag = fields[i].charAt(0);
		String val = fields[i].substring(1);
		switch (tag) {
			case APERTURE:
				try {
					this.aperture = Float.parseFloat(val);
				} catch ( Exception e ) {
					if ( LOG.isDebugEnabled() ) {
						LOG.debug("Unable to parse aperture from value: " +val);
					}
				}
				break;
			case CAMERA_MAKE:
				this.cameraMake = val;
				break;
			case CAMERA_MODEL:
				this.cameraModel = val;
				break;
			case COMPRESSION:
				try {
					this.compression = Integer.parseInt(val);
				} catch ( Exception e ) {
					if ( LOG.isDebugEnabled() ) {
						LOG.debug("Unable to parse compression from value: " +val);
					}
				}
				break;
			case EXPOSURE_BIAS:
				try {
					this.exposureBias = Integer.parseInt(val);
				} catch ( Exception e ) {
					if ( LOG.isDebugEnabled() ) {
						LOG.debug("Unable to parse exposure bias from value: " +val);
					}
				}
				break;
			case EXPOSURE_TIME:
				this.exposureTime = val;
				break;
			case FLASH:
				try {
					this.flash = Integer.parseInt(val);
				} catch ( Exception e ) {
					if ( LOG.isDebugEnabled() ) {
						LOG.debug("Unable to parse flash from value: " +val);
					}
				}
				break;
			case FOCAL_LENGTH:
				try {
					this.focalLength = Float.parseFloat(val);
				} catch ( Exception e ) {
					if ( LOG.isDebugEnabled() ) {
						LOG.debug("Unable to parse focal length from value: " +val);
					}
				}
				break;
			case FOCAL_LENGTH_35MM:
				try {
					this.focalLength35mm = Float.parseFloat(val);
				} catch ( Exception e ) {
					if ( LOG.isDebugEnabled() ) {
						LOG.debug("Unable to parse focal length 35mm from value: " +val);
					}
				}
				break;
			case MAX_APERTURE:
				try {
					this.maxAperture = Float.parseFloat(val);
				} catch ( Exception e ) {
					if ( LOG.isDebugEnabled() ) {
						LOG.debug("Unable to parse maxAperture from value: " +val);
					}
				}
				break;
			case ORIENTATION:
				try {
					this.orientation = Integer.parseInt(val);
				} catch ( Exception e ) {
					if ( LOG.isDebugEnabled() ) {
						LOG.debug("Unable to parse orientation from value: " +val);
					}
				}
				break;
			case SHUTTER_SPEED:
				this.shutterSpeed = val;
				break;
		}
	}
}

/**
 * Returns the aperture APEX value.
 * @return int
 */
public float getAperture() {
	return aperture;
}

public String getApertureAsFstop()
{
	if ( aperture < 1 ) {
		return null;
	}
	return "F" +StringUtil.roundDecimal(Math.pow(1.4142,aperture),1);
}

/**
 * Returns the camera make.
 * @return String
 */
public String getCameraMake() {
	return cameraMake;
}

/**
 * Returns the camera model.
 * @return String
 */
public String getCameraModel() {
	return cameraModel;
}

/**
 * Returns the orientation.
 * @return String
 */
public int getOrientation() {
	return orientation;
}

public String getOrientationSetting() 
{
	// TODO get human form of orientation
	return String.valueOf(orientation);
}	

/**
 * Returns the shutter speed value (fractional seconds).
 * @return String
 */
public String getShutterSpeed() {
	return shutterSpeed;
}

public String getShutterSpeedAsFractionSecs()
{
	return shutterSpeed;
}

/**
 * Sets the aperture APEX value.
 * @param aperture The aperture to set
 */
public void setAperture(float aperture) {
	this.aperture = aperture;
}

/**
 * Sets the camera make.
 * @param cameraMake The camera make to set
 */
public void setCameraMake(String cameraMake) {
	this.cameraMake = cameraMake;
}

/**
 * Sets the camera model.
 * @param cameraModel The camera model to set
 */
public void setCameraModel(String cameraModel) {
	this.cameraModel = cameraModel;
}

/**
 * Sets the orientation.
 * @param orientation The orientation to set
 */
public void setOrientation(int orientation) {
	this.orientation = orientation;
}

/**
 * Sets the shutter speed fractional seconds value.
 * @param shutterSpeed The shutter speed to set
 */
public void setShutterSpeed(String shutterSpeed) {
	this.shutterSpeed = shutterSpeed;
}

/**
 * Returns the flash code.
 * @return int
 */
public int getFlash() {
	return flash;
}

public String getFlashSetting() {
	switch (flash) {
		case 0:
			return "No flash";
		case 1:
			return "Flash fired";
		case 5:
			return "Flash fired; strobe did not detect light";
		case 7:
			return "Flash fired; strobe detected light";
		default:
			return "";
	}
}

/**
 * Sets the flash code.
 * @param flash The flash to set
 */
public void setFlash(int flash) {
	this.flash = flash;
}

/**
 * Returns the compression.
 * @return int
 */
public int getCompression() {
	return compression;
}

public String getCompressionType()
{
	switch ( compression ) {
		case 1:
			return "Uncompressed";
		case 6:
			return "JPEG";
		default:
			return "Unknown";
	}
}

/**
 * Sets the compression.
 * @param compression The compression to set
 */
public void setCompression(int compression) {
	this.compression = compression;
}

/**
 * @return Returns the exposureTime.
 */
public String getExposureTime() {
	return exposureTime;
}
/**
 * @param exposureTime The exposureTime to set.
 */
public void setExposureTime(String exposureTime) {
	this.exposureTime = exposureTime;
}
/**
 * @return Returns the maxAperture.
 */
public float getMaxAperture() {
	return maxAperture;
}
/**
 * @param maxAperture The maxAperture to set.
 */
public void setMaxAperture(float maxAperture) {
	this.maxAperture = maxAperture;
}
/**
 * @param exposureBias The exposureBias to set.
 */
public void setExposureBias(int exposureBias) {
	this.exposureBias = exposureBias;
}
/**
 * @param focalLength The focalLength to set.
 */
public void setFocalLength(float focalLength) {
	this.focalLength = focalLength;
}
/**
 * @param focalLength35mm The focalLength35mm to set.
 */
public void setFocalLength35mm(float focalLength35mm) {
	this.focalLength35mm = focalLength35mm;
}
/* (non-Javadoc)
 * @see magoffin.matt.ma.image.ImageMetadata#getExposureBias()
 */
public String getExposureBias() {
	return String.valueOf(exposureBias);
}
/* (non-Javadoc)
 * @see magoffin.matt.ma.image.ImageMetadata#getExposureTimeAsFractionSecs()
 */
public String getExposureTimeAsFractionSecs() {
	return exposureTime;
}
/* (non-Javadoc)
 * @see magoffin.matt.ma.image.ImageMetadata#getFocalLength()
 */
public String getFocalLength() {
	if ( focalLength < 1 ) {
		return null;
	}
	return StringUtil.roundDecimal(focalLength,2);
}
/* (non-Javadoc)
 * @see magoffin.matt.ma.image.ImageMetadata#getFocalLength35mm()
 */
public String getFocalLength35mm() {
	if ( focalLength35mm < 0 ) {
		return null;
	}
	return String.valueOf(Math.round(focalLength35mm));
}
/* (non-Javadoc)
 * @see magoffin.matt.ma.image.ImageMetadata#getMaxApertureAsFstop()
 */
public String getMaxApertureAsFstop() {
	if ( maxAperture < 1 ) {
		return null;
	}
	return "F" +StringUtil.roundDecimal(Math.pow(1.4142,maxAperture),1);
}
}
