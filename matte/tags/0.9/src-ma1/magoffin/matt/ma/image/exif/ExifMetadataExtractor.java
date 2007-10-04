/* ===================================================================
 * ExifMetadataExtractor.java
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
 * $Id: ExifMetadataExtractor.java,v 1.1 2006/06/03 22:26:19 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.image.exif;

import com.drew.metadata.Metadata;

/**
 * Interface for extracting EXIF information out of an image.
 * 
 * <p>This interface exists to allow for different implementations based on
 * different camera manufacturers implementations of EXIF data.</p>
 * 
 * <p> Created on Dec 9, 2002 10:21:08 PM.</p>
 *
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public interface ExifMetadataExtractor 
{

public void popluateMetadata(String make, String model, Metadata imageMeta, ExifMetadata meta);

} // interface ExifMetadataExtractor
