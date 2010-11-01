/* ===================================================================
 * ImageMediaRequestHandler.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Apr 12, 2004 8:54:34 AM.
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
 * $Id: ImageMediaRequestHandler.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MediaRequestHandler;
import magoffin.matt.ma.MediaRequestHandlerParams;
import magoffin.matt.ma.util.Geometry;
import magoffin.matt.ma.xsd.MediaItem;

/**
 * Handler for images.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public interface ImageMediaRequestHandler extends MediaRequestHandler 
{
	
/**
 * Return <em>true</em> if output image needs any altering.
 * 
 * @param item the media item
 * @param params the current params
 * @return <em>true</em> only if output image needs to be altered
 */
public boolean needToAlter(MediaItem item, MediaRequestHandlerParams params);

/**
 * Return <em>true</em> if there are effects to process.
 * 
 * @param params request params
 * @return <em>true</em> if there are effects to process
 */
public boolean hasEffects(MediaRequestHandlerParams params);
	
/**
 * Get any effects associated with request.
 * 
 * @param params request params
 * @return effects, or <em>null</em> if none available
 */
public ImageEffect[] getEffects(MediaRequestHandlerParams params);

/**
 * Get a BufferedImage for a MediaItem from an InputStream.
 * 
 * @param item the MediaItem
 * @param in the InputStream
 * @param params the request params
 * @return the buffered image
 */
public BufferedImage getBufferedImage(MediaItem item, InputStream in, 
		MediaRequestHandlerParams params) throws MediaAlbumException;

/**
 * Get a BufferedImage from a file.
 * 
 * @param f the file
 * @return the buffered image
 */
public BufferedImage getBufferedImage(File f) throws MediaAlbumException;

/**
 * Get the Geometry of the output image.
 * 
 * @param params the current params
 * @return the Geometry
 */
public Geometry getGeometry(MediaRequestHandlerParams params);

/**
 * Get the quality setting, defaulting to 100 if not provided.
 * 
 * <p>This method assumes quality to be an integer between 0 and 100
 * where 100 is the best quality.</p>
 *
 * @param params the current params
 * @return quality
 */
public int getQuality(MediaRequestHandlerParams params);

/**
 * Return <em>true</em> if the request params represent a thumbnail
 * sized image.
 * 
 * @param params the parameters
 * @return <em>true</em> if is a thumbnail request
 */
public boolean isThumbnail(MediaRequestHandlerParams params);

/**
 * Returns <em>true</em> if the output image is rotated plus or minus 
 * 90 degrees.
 * @param params the parameters
 * @return <em>true</em> if the output image is rotated
 */
public boolean isRotated(MediaRequestHandlerParams params);

}
