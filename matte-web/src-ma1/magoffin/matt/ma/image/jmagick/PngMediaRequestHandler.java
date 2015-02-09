/* ===================================================================
 * PngMediaRequestHandler.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Apr 13, 2004 11:05:13 PM.
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
 * $Id: PngMediaRequestHandler.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.image.jmagick;

import java.io.File;
import java.util.List;

import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MediaMetadata;
import magoffin.matt.ma.image.ImageMetadata;
import magoffin.matt.ma.xsd.MediaItem;

/**
 * MediaRequestHandler implementation for PNG images.
 * 
 * <p>Note that the PNG images are returned as JPEG images when
 * not returning the original image.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class PngMediaRequestHandler extends JpegMediaRequestHandler {
	
	/** The PNG image MIME type: <code>image/png</code>. */
	public static final String PNG_MIME = "image/png";
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.image.AbstractImageMediaRequestHandler#getImageMetadataInstance(magoffin.matt.ma.xsd.MediaItem)
 */
protected ImageMetadata getImageMetadataInstance(MediaItem item) {
	return null;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.image.AbstractImageMediaRequestHandler#addMediaItemMetadata(magoffin.matt.ma.image.ImageMetadata, magoffin.matt.ma.xsd.MediaItem, java.util.List)
 */
protected void addMediaItemMetadata(
	ImageMetadata meta,
	MediaItem item,
	List list) 
{
	// no custom attributes to add
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandler#setMediaItemParameters(java.io.File, magoffin.matt.ma.xsd.MediaItem)
 */
public MediaMetadata setMediaItemParameters(File mediaFile, MediaItem item)
throws MediaAlbumException 
{
	magoffin.matt.ma.image.iio.PngMediaRequestHandler pngHandler = new magoffin.matt.ma.image.iio.PngMediaRequestHandler();

	return pngHandler.setMediaItemParameters(mediaFile,item);
	
}

}
