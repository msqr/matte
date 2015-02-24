/* ===================================================================
 * PngMediaRequestHandler.java
 *
 * Copyright (c) 2002-2003 Matt Magoffin.
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
 * $Id: PngMediaRequestHandler.java,v 1.1 2006/06/03 22:26:19 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.image.jimi;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MediaRequestHandlerParams;
import magoffin.matt.ma.image.ImageMediaRequestHandlerParams;
import magoffin.matt.ma.util.Geometry;
import magoffin.matt.ma.util.PoolFactory;
import magoffin.matt.ma.util.WorkScheduler;
import magoffin.matt.ma.xsd.MediaAlbumConfig;
import magoffin.matt.ma.xsd.MediaHandlerConfig;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.util.FileUtil;

import org.apache.log4j.Logger;

import com.sun.jimi.core.Jimi;

/**
 * MediaRequestHandler implementation for reading PNG images
 * but writing them out as JPEG.
 * 
 * <p>Created Dec 6, 2002 10:04:37 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class PngMediaRequestHandler extends JimiMediaRequestHandler 
{
	
	private static final Logger log = Logger.getLogger(PngMediaRequestHandler.class);
	
/*
 * @see magoffin.matt.ma.MediaRequestHandler#init(MediaHandlerConfig)
 */
public void init(MediaHandlerConfig config, PoolFactory pf, MediaAlbumConfig appConfig) throws MediaAlbumException
{
	super.init(config,pf, appConfig);
}


/**
 * @see magoffin.matt.ma.MediaRequestHandler#writeMedia(magoffin.matt.ma.xsd.MediaItem, java.io.OutputStream, java.io.InputStream, magoffin.matt.ma.MediaRequestHandlerParams)
 */
public void writeMedia(
	MediaItem item,
	OutputStream out,
	InputStream in,
	MediaRequestHandlerParams params)
throws MediaAlbumException, IOException 
{
	try {
		Geometry geometry = getGeometry(params);
		int quality = getQuality(params);
		if ( quality != 100 || geometry.getWidth() != item.getWidth().intValue() ||
			geometry.getHeight() != item.getHeight().intValue() ) {

			WorkScheduler scheduler = null;
			try {
				scheduler = params.getWorkBiz().schedule(
						(ImageMediaRequestHandlerParams)params);
				BufferedImage image = this.scaleImage(
					this.getBufferedImage(Jimi.getImage(in,Jimi.SYNCHRONOUS)),
					geometry);
				this.writeJpegStream(image,quality,out);
			} finally {
				params.getWorkBiz().done(scheduler);
			}

		} else {

			// no altering needed, simply stream the original file back, no need to alter
			if ( log.isDebugEnabled() ) {
				log.debug("Returning unaltered stream " +item.getPath());
			}
			FileUtil.copy(in,out,false,false);
		}

	} catch ( IOException e ) {
		throw e;
	} catch (Exception e) {
		throw new MediaAlbumException(e.getMessage(),e);
	}
	
}

}
