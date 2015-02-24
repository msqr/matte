/* ===================================================================
 * AbstractJMagickImageEffect.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Apr 13, 2004 9:28:55 PM.
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
 * $Id: AbstractJMagickImageEffect.java,v 1.1 2006/06/03 22:26:19 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.image.jmagick.effect;

import java.awt.image.BufferedImage;
import java.util.List;

import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.image.ImageMediaRequestHandler;
import magoffin.matt.ma.image.ImageMediaRequestHandlerParams;
import magoffin.matt.ma.image.jmagick.JMagickImageEffect;

/**
 * Abstract JMagick ImageEffect to implement the base ImageEffect interface.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:19 $
 */
public abstract class AbstractJMagickImageEffect implements JMagickImageEffect 
{
/* (non-Javadoc)
 * @see magoffin.matt.util.ResetableObject#reset()
 */
public void reset() {
	// nada
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.image.ImageEffect#applyEffect(magoffin.matt.ma.image.ImageMediaRequestHandler, magoffin.matt.ma.image.ImageMediaRequestHandlerParams, java.awt.image.BufferedImage, java.util.List)
 */
public BufferedImage applyEffect(ImageMediaRequestHandler handler,
		ImageMediaRequestHandlerParams params, BufferedImage source,
		List bufferedOpList) throws MediaAlbumException 
{
	throw new UnsupportedOperationException();
}

}
