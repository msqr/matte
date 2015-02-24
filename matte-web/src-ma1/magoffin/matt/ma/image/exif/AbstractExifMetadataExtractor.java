/* ===================================================================
 * AbstractExifMetadataExtractor.java
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
 * $Id: AbstractExifMetadataExtractor.java,v 1.1 2006/06/03 22:26:19 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.image.exif;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import magoffin.matt.util.StringUtil;

import org.apache.log4j.Logger;

import com.drew.lang.Rational;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifDirectory;

/**
 * Abstract base class to help ExifMetadataExtractor implementations.
 * 
 * <p> Created on Dec 10, 2002 4:04:05 PM.</p>
 *
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public abstract class AbstractExifMetadataExtractor
implements ExifMetadataExtractor 
{
	
	/** A date format in the form <code>yyyy:MM:dd hh:mm:ss</code>. */
	protected static final SimpleDateFormat BASIC_DATE_FORMAT
		= new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
		
	private static final Logger log = 
		Logger.getLogger(AbstractExifMetadataExtractor.class);
	
protected Date getCreationDate(Metadata imageMeta)
{
	String dStr = this.getExifString(imageMeta,ExifDirectory.TAG_DATETIME_ORIGINAL);
	if ( dStr == null ) {
		// try digi date
		dStr = this.getExifString(imageMeta,ExifDirectory.TAG_DATETIME_DIGITIZED);
	}
	if ( dStr == null ) {
		// try mod date/time as last resort
		dStr = this.getExifString(imageMeta,ExifDirectory.TAG_DATETIME);
	}
	if ( dStr == null ) {
		return null;
	}
	try {
		return BASIC_DATE_FORMAT.parse(dStr);
	} catch (ParseException e) {
		log.warn("Could not parse date from '" +dStr +"': " +e.getMessage());
		return null;
	}
}

protected int getExifInt(Metadata imageMeta, int tagType)
{
	Directory dir = imageMeta.getDirectory(ExifDirectory.class);
	if ( !dir.containsTag(tagType) ) {
		return -1;
	}
	try {
		return dir.getInt(tagType);
	} catch ( MetadataException e ) {
		log.warn("Metadata exception getting Exif int type " +tagType +": "
			+e.getMessage());
	}
	return -1;
}

protected Rational getExifRational(Metadata imageMeta, int tagType)
{
	Directory dir = imageMeta.getDirectory(ExifDirectory.class);
	if ( !dir.containsTag(tagType) ) {
		return null;
	}
	try {
		return dir.getRational(tagType);
	} catch ( MetadataException e ) {
		log.warn("Metadata exception getting Exif rational type " +tagType +": "
			+e.getMessage());
	}
	return null;
}

protected String getExifString(Metadata imageMeta, int tagType)
{
	Directory dir = imageMeta.getDirectory(ExifDirectory.class);
	if ( dir == null ) {
		return null;
	}
	if ( !dir.containsTag(tagType) ) {
		return null;
	}
	return dir.getString(tagType);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.image.exif.ExifMetadataExtractor#popluateMetadata(java.lang.String, java.lang.String, com.drew.metadata.Metadata, magoffin.matt.ma.image.exif.ExifMetadata)
 */
public void popluateMetadata(
	String make,
	String model,
	Metadata imageMeta,
	ExifMetadata meta)
{
	Rational exposure = getExifRational(imageMeta,ExifDirectory.TAG_EXPOSURE_TIME);
	if ( exposure != null ) {
		if ( exposure.getNumerator() > exposure.getDenominator() ) {
			// longer than 1 second, so perform division
			double speed = exposure.doubleValue();
			meta.setExposureTime(StringUtil.roundDecimal(speed,1));
		} else {
			// less than 1 second, so leave as fraction
			String speed = exposure.toSimpleString(false);
			meta.setExposureTime(speed);
		}
	}
	
	Rational shutter = getExifRational(imageMeta,ExifDirectory.TAG_SHUTTER_SPEED);
	if ( shutter != null ) {
		if ( shutter.getNumerator() < shutter.getDenominator() ) {
			// longer than 1 second, so perform division to get simple result
			double speed = 1.0 / Math.pow(2.0,shutter.doubleValue());
			meta.setShutterSpeed(StringUtil.roundDecimal(speed,1));
		} else {
			// less than 1 second, so leave as fraction
			long fracSpeed = Math.round(Math.pow(2.0,shutter.doubleValue()));
			meta.setShutterSpeed("1/"+fracSpeed);
		}
	}
	
	Rational aperture = getExifRational(imageMeta,ExifDirectory.TAG_APERTURE);
	if ( aperture != null ) {
		meta.setAperture(aperture.floatValue());
	}
	
	if ( !ExifMetadataUtil.EXIF_ANY_MAKE_MODEL_KEY.equals(make) ) {
		meta.setCameraMake(make);
	}
	if ( !ExifMetadataUtil.EXIF_ANY_MAKE_MODEL_KEY.equals(model) ) {
		meta.setCameraModel(model);
	}
	meta.setCreationDate(this.getCreationDate(imageMeta));
	meta.setFlash(this.getExifInt(imageMeta,ExifDirectory.TAG_FLASH));
	meta.setOrientation(this.getExifInt(imageMeta,ExifDirectory.TAG_ORIENTATION));
	
	Rational focalLength = getExifRational(imageMeta,ExifDirectory.TAG_FOCAL_LENGTH);
	if ( focalLength != null ) {
		float fl = focalLength.floatValue();
		meta.setFocalLength(fl);
	}
	
	aperture = getExifRational(imageMeta,ExifDirectory.TAG_MAX_APERTURE);
	if ( aperture != null ) {
		meta.setMaxAperture(aperture.floatValue());
	}
	
	Rational exposureBias = getExifRational(imageMeta,ExifDirectory.TAG_EXPOSURE_BIAS);
	if ( exposureBias != null ) {
		meta.setExposureBias(exposureBias.intValue());
	}
}

}
