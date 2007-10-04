/* ===================================================================
 * JimiMediaRequestHandler.java
 * 
 * Created Mar 2, 2004 12:32:29 PM
 * 
 * Copyright (c) 2004 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: JimiMediaRequestHandler.java,v 1.1 2006/06/03 22:26:19 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.image.jimi;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MediaMetadata;
import magoffin.matt.ma.image.JpegMediaRequestHandler;
import magoffin.matt.ma.xsd.MediaItem;

import com.sun.jimi.core.Jimi;

/**
 * Abstract base class for image media handlers utilizing the JIMI API.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:19 $
 */
public abstract class JimiMediaRequestHandler 
extends JpegMediaRequestHandler
{

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandler#setMediaItemParameters(java.io.File, magoffin.matt.ma.xsd.MediaItem)
 */
public MediaMetadata setMediaItemParameters(File mediaFile, MediaItem item)
throws MediaAlbumException
{
	try {
		InputStream in = new BufferedInputStream(new FileInputStream(mediaFile));
		BufferedImage image = this.getBufferedImage(Jimi.getImage(in,Jimi.SYNCHRONOUS));
		item.setWidth(new Integer(image.getWidth()));
		item.setHeight(new Integer(image.getHeight()));
		return null;
	} catch (IOException e) {
		throw new MediaAlbumException("IOException reading stream for item " +item.getPath()
			+": " +e.getMessage(),e);
	}
}

}
