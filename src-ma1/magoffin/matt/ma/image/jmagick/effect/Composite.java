/* ===================================================================
 * Composite.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Apr 13, 2004 10:12:09 PM.
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
 * $Id: Composite.java,v 1.1 2006/06/03 22:26:19 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.image.jmagick.effect;

import java.awt.Dimension;
import java.io.File;

import org.apache.log4j.Logger;

import magick.CompositeOperator;
import magick.ImageInfo;
import magick.MagickException;
import magick.MagickImage;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MediaRequestHandlerParams;
import magoffin.matt.ma.image.ImageMediaRequestHandler;
import magoffin.matt.ma.image.ImageMediaRequestHandlerParams;
import magoffin.matt.ma.util.Geometry;

/**
 * Composite one image onto another.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:19 $
 */
public class Composite extends AbstractJMagickImageEffect 
{
	private static final Logger LOG = Logger.getLogger(Composite.class);
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.image.jmagick.JMagickImageEffect#applyEffect(magoffin.matt.ma.image.ImageMediaRequestHandler, magick.ImageInfo, magick.MagickImage, magoffin.matt.ma.image.ImageMediaRequestHandlerParams)
 */
public MagickImage applyEffect(ImageMediaRequestHandler handler, ImageInfo inInfo,
		MagickImage image, ImageMediaRequestHandlerParams params)
		throws MediaAlbumException 
{
	if ( !params.hasParamSet(MediaRequestHandlerParams.WATERMARK) ) return image;
	File watermarkFile = (File)params.getParam(MediaRequestHandlerParams.WATERMARK);
	try {
		ImageInfo wmInfo = new ImageInfo(watermarkFile.getAbsolutePath());
		MagickImage wmImage = new MagickImage(wmInfo);
		Geometry geometry = handler.getGeometry(params);
		LOG.debug("Applying composite watermark");
		Dimension wmDim = wmImage.getDimension();
		int wmX = geometry.getWidth() - (int)wmDim.getWidth();
		int wmY = geometry.getHeight() - (int)wmDim.getHeight();
		image.compositeImage(CompositeOperator.AtopCompositeOp,wmImage,wmX,wmY);
		LOG.debug("Watermark composite applied");
		return image;
	} catch ( MagickException e ) {
		throw new MediaAlbumException("MagickException compositing: " +e,e);
	}
}

}
