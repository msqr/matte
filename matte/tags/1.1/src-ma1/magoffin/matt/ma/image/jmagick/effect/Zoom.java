/* ===================================================================
 * CopySource.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Apr 13, 2004 9:54:02 PM.
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
 * $Id: Zoom.java,v 1.1 2006/06/03 22:26:19 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.image.jmagick.effect;

import org.apache.log4j.Logger;

import magick.FilterType;
import magick.ImageInfo;
import magick.MagickException;
import magick.MagickImage;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.image.ImageMediaRequestHandler;
import magoffin.matt.ma.image.ImageMediaRequestHandlerParams;
import magoffin.matt.ma.util.Geometry;

/**
 * Resizes the image, using a filter.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:19 $
 */
public class Zoom extends AbstractJMagickImageEffect 
{
	private static final Logger LOG = Logger.getLogger(Zoom.class);
	
	private static final String[] FILTER_NAMES = new String[16];
	
	static {
		FILTER_NAMES[FilterType.BesselFilter] = "Bessel";
		FILTER_NAMES[FilterType.BlackmanFilter] = "Blackman";
		FILTER_NAMES[FilterType.BoxFilter] = "Box";
		FILTER_NAMES[FilterType.CatromFilter] = "Catrom";
		FILTER_NAMES[FilterType.CubicFilter] = "Cubic";
		FILTER_NAMES[FilterType.GuassianFilter] = "Guassian";
		FILTER_NAMES[FilterType.HammingFilter] = "Hamming";
		FILTER_NAMES[FilterType.HanningFilter] = "Hanning";
		FILTER_NAMES[FilterType.HermiteFilter] = "Hermite";
		FILTER_NAMES[FilterType.LanczosFilter] = "Lanczos";
		FILTER_NAMES[FilterType.MitchellFilter] = "Mitchell";
		FILTER_NAMES[FilterType.PointFilter] = "Point";
		FILTER_NAMES[FilterType.QuadraticFilter] = "Quadratic";
		FILTER_NAMES[FilterType.SincFilter] = "Sinc";
		FILTER_NAMES[FilterType.TriangleFilter] = "Triangle";
	}

/* (non-Javadoc)
 * @see magoffin.matt.ma.image.jmagick.JMagickImageEffect#applyEffect(magoffin.matt.ma.image.ImageMediaRequestHandler, magick.ImageInfo, magick.MagickImage, magoffin.matt.ma.image.ImageMediaRequestHandlerParams)
 */
public MagickImage applyEffect(ImageMediaRequestHandler handler,
		ImageInfo inInfo,
		MagickImage image, 
		ImageMediaRequestHandlerParams params)
		throws MediaAlbumException 
{
	Geometry geometry = handler.getGeometry(params);
	int quality = handler.getQuality(params);
	int filterType = handler.isThumbnail(params)
		? FilterType.TriangleFilter 	// for thumbnails this will do
		: FilterType.SincFilter;			// for larger go for better quality
		
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Magick zoom: quality = " +quality
				+", dimensions = " +geometry
				+", filter = " +FILTER_NAMES[filterType]);
	}
	
	int width = geometry.getWidth();
	int height = geometry.getHeight();
	
	if ( handler.isRotated(params) ) {
		width = height;
		height = geometry.getWidth();
	}
	
	try {
		// scale image via zoom which uses the defined filter while scaling
		image.setFilter(filterType); // set filter type
		MagickImage result = image.zoomImage(width,height);
		
		// remove profiles
		result.profileImage("*",null);
		return result;
	} catch ( MagickException e ) {
		throw new MediaAlbumException("MagickException zooming: " +e,e);
	}
}

}
