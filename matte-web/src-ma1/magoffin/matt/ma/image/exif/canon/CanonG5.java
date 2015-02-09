/* ===================================================================
 * CanonG5.java
 * 
 * Copyright (c) 2003 Matt Magoffin. Created Aug 22, 2003.
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
 * $Id: CanonG5.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.image.exif.canon;

import magoffin.matt.ma.image.exif.AbstractExifMetadataExtractor;
import magoffin.matt.ma.image.exif.ExifMetadata;

import com.drew.lang.Rational;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectory;

/**
 * ExifMetadataExtractor implementation for the Canon G5 camera.
 * 
 * <p>Created Aug 22, 2003 3:34:08 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class CanonG5 extends AbstractExifMetadataExtractor 
{
	/** The multiplication factor from the G5 focal length to a 35mm equivilent. */
	private static final float FOCAL_EQUIV_35MM_X_FACTOR = 4.861111f;

/* (non-Javadoc)
 * @see magoffin.matt.ma.image.exif.ExifMetadataExtractor#popluateMetadata(java.lang.String, java.lang.String, com.drew.metadata.Metadata, magoffin.matt.ma.image.exif.ExifMetadata)
 */
public void popluateMetadata(
	String make,
	String model,
	Metadata imageMeta,
	ExifMetadata meta) 
{
	super.popluateMetadata(make,model,imageMeta,meta);
	
	// check for focal length
	Rational focalLength = getExifRational(imageMeta,ExifDirectory.TAG_FOCAL_LENGTH);
	if ( focalLength != null ) {
		float fl = focalLength.floatValue();
		meta.setFocalLength35mm(fl*FOCAL_EQUIV_35MM_X_FACTOR);
	}
}

}
