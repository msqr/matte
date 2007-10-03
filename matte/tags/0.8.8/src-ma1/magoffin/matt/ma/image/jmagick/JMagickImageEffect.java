/* ===================================================================
 * JMagickImageEffect.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Apr 13, 2004 9:29:28 PM.
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
 * $Id: JMagickImageEffect.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.image.jmagick;

import magick.ImageInfo;
import magick.MagickImage;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.image.ImageEffect;
import magoffin.matt.ma.image.ImageMediaRequestHandler;
import magoffin.matt.ma.image.ImageMediaRequestHandlerParams;

/**
 * Interface for JMagick version of ImageEffect interface.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public interface JMagickImageEffect extends ImageEffect {

/**
 * Apply effect with ImageMagick.
 * 
 * <p>The <var>inInfo</var> will have the <code>quality</code> set 
 * before this method is called.</p>
 * 
 * @param handler current handler
 * @param inInfo the ImageInfo used to open the image
 * @param image current image
 * @param params current params
 * @return the resulting image
 * @throws MediaAlbumException if an error occurs
 */
public MagickImage applyEffect(ImageMediaRequestHandler handler, ImageInfo inInfo,
		MagickImage image, ImageMediaRequestHandlerParams params) 
throws MediaAlbumException;
}
