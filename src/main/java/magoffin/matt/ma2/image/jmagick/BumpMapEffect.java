/* ===================================================================
 * BumpMapEffect.java
 * 
 * Created Sep 10, 2007 2:23:06 PM
 * 
 * Copyright (c) 2007 Matt Magoffin.
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
 */

package magoffin.matt.ma2.image.jmagick;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import org.springframework.core.io.Resource;
import magick.CompositeOperator;
import magick.ImageInfo;
import magick.MagickException;
import magick.MagickImage;
import magoffin.matt.ma2.MediaEffect;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.domain.MediaItem;

/**
 * A watermark effect for JMagick based processing, that creates a 3D bump map
 * from the watermark image.
 *
 * @author matt
 * @version 1.1
 */
public class BumpMapEffect extends BaseJMagickMediaEffect {

	/**
	 * The key for this effect.
	 */
	public static final String BUMP_MAP_KEY = "image.jmagick.bump." + MediaEffect.KEY_WATERMARK;

	@Override
	public MagickImage applyEffect(MediaItem item, MediaRequest request, ImageInfo inInfo,
			MagickImage image) {
		Resource watermarkResource = (Resource) request.getParameters()
				.get(MediaEffect.MEDIA_REQUEST_PARAM_WATERMARK_RESOURCE);
		if ( watermarkResource == null || !watermarkResource.exists() ) {
			return image;
		}
		File watermarkFile = null;
		try {
			watermarkFile = watermarkResource.getFile();
		} catch ( IOException e ) {
			throw new RuntimeException(e);
		}
		try {
			ImageInfo wmInfo = new ImageInfo(watermarkFile.getAbsolutePath());
			MagickImage wmImage = new MagickImage(wmInfo);
			if ( log.isDebugEnabled() ) {
				log.debug("Applying BumpmapComposite watermakr to item [" + request.getMediaItemId()
						+ ']');
			}
			Dimension wmDim = wmImage.getDimension();
			int wmX = image.getDimension().width - (int) wmDim.getWidth();
			int wmY = image.getDimension().height - (int) wmDim.getHeight();
			image.compositeImage(CompositeOperator.BumpmapCompositeOp, wmImage, wmX, wmY);

			if ( log.isDebugEnabled() ) {
				log.debug("BumpmapComposite watermark complete for item [" + request.getMediaItemId()
						+ ']');
			}

			return image;
		} catch ( MagickException e ) {
			throw new RuntimeException("MagickException compositing: " + e, e);
		}
	}

	@Override
	public String getKey() {
		return BUMP_MAP_KEY;
	}

}
