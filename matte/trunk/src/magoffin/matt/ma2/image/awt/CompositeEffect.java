/* ===================================================================
 * CompositeEffect.java
 * 
 * Copyright (c) 2007 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: CompositeEffect.java,v 1.1 2007/09/10 10:34:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.image.awt;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageReader;

import org.springframework.core.io.Resource;

import magoffin.matt.ma2.MediaEffect;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.image.ImageMediaHelper;

/**
 * A watermark effect for AWT based processing, that composites the watermark
 * image on top of the media image.
 * 
 * @author matt.magoffin
 * @version $Revision: 1.1 $ $Date: 2007/09/10 10:34:16 $
 */
public class CompositeEffect extends BaseAwtMediaEffect {

	/**
	 * The key for this effect.
	 */
	public static final String COMPOSITE_KEY 
		= "image.awt.composite." +MediaEffect.KEY_WATERMARK;

	private ImageMediaHelper imageMediaHelper = null;

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.image.awt.AwtMediaEffect#applyEffect(magoffin.matt.ma2.domain.MediaItem, magoffin.matt.ma2.MediaRequest, java.awt.image.BufferedImage)
	 */
	public BufferedImage applyEffect(MediaItem item, MediaRequest request,
			BufferedImage source) {
		Resource watermarkResource = (Resource)request.getParameters().get(
				MediaEffect.MEDIA_REQUEST_PARAM_WATERMARK_RESOURCE);
		if ( watermarkResource == null || !watermarkResource.exists() ) {
			return source;
		}
		File watermarkFile = null;
		BufferedImage watermark = null;
		try {
			watermarkFile = watermarkResource.getFile();
			ImageReader reader = imageMediaHelper.getReaderForFile(watermarkFile);
			watermark = reader.read(0);
		} catch ( IOException e ) {
			throw new RuntimeException(e);
		}
		int wmX = source.getWidth() - watermark.getWidth();
		int wmY = source.getHeight() - watermark.getHeight();

		if ( log.isDebugEnabled() ) {
			log.debug("Setting composite watermark for item [" 
					+request.getMediaItemId() +']');
		}
		Graphics2D g = source.createGraphics();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
		g.drawImage(watermark, wmX, wmY, null);
		g.dispose();
		if ( log.isDebugEnabled() ) {
			log.debug("Composite watermark complete for item [" 
					+request.getMediaItemId() +']');
		}
		return source;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaEffect#getKey()
	 */
	public String getKey() {
		return COMPOSITE_KEY;
	}

	/**
	 * @return the imageMediaHelper
	 */
	public ImageMediaHelper getImageMediaHelper() {
		return imageMediaHelper;
	}

	/**
	 * @param imageMediaHelper the imageMediaHelper to set
	 */
	public void setImageMediaHelper(ImageMediaHelper imageMediaHelper) {
		this.imageMediaHelper = imageMediaHelper;
	}

}
