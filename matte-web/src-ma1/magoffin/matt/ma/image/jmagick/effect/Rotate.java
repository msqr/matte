/* ===================================================================
 * Rotate.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Apr 20, 2004 2:01:34 PM.
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
 * $Id: Rotate.java,v 1.1 2006/06/03 22:26:19 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.image.jmagick.effect;

import magick.ImageInfo;
import magick.MagickException;
import magick.MagickImage;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.image.ImageMediaRequestHandler;
import magoffin.matt.ma.image.ImageMediaRequestHandlerParams;

/**
 * Rotate the image.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:19 $
 */
public class Rotate extends AbstractJMagickImageEffect 
{
	
	private Integer degrees;

/* (non-Javadoc)
 * @see magoffin.matt.ma.image.jmagick.JMagickImageEffect#applyEffect(magoffin.matt.ma.image.ImageMediaRequestHandler, magick.ImageInfo, magick.MagickImage, magoffin.matt.ma.image.ImageMediaRequestHandlerParams)
 */
public MagickImage applyEffect(ImageMediaRequestHandler handler,
		ImageInfo inInfo, MagickImage image,
		ImageMediaRequestHandlerParams params) throws MediaAlbumException 
{
	double deg = -degrees.doubleValue();
	try {
		return image.rotateImage(deg);
	} catch ( MagickException e ) {
		throw new MediaAlbumException("MagickException rotating: " +e,e);
	}
}

/**
 * @return Returns the degrees.
 */
public Integer getDegrees() {
	return degrees;
}

/**
 * @param degrees The degrees to set.
 */
public void setDegrees(Integer degrees) {
	this.degrees = degrees;
}
}
