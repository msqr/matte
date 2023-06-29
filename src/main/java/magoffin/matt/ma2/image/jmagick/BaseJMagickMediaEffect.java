/* ===================================================================
 * BaseJMagickMediaEffect.java
 * 
 * Created Dec 29, 2006 10:04:20 AM
 * 
 * Copyright (c) 2006 Matt Magoffin (spamsqr@msqr.us)
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

import org.apache.log4j.Logger;
import magick.ImageInfo;
import magick.MagickImage;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.MediaResponse;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.domain.MediaItem;

/**
 * Base implementation of
 * {@link magoffin.matt.ma2.image.jmagick.JMagickMediaEffect}.
 * 
 * <p>
 * The configurable properties of this class are:
 * </p>
 * 
 * <dl class="class-properties">
 * <dt>mediaBiz</dt>
 * <dd>The {@link MediaBiz} implementation to use.</dd>
 * </dl>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.1
 */
public abstract class BaseJMagickMediaEffect implements JMagickMediaEffect {

	/** A class logger. */
	protected final Logger log = Logger.getLogger(getClass());

	private MediaBiz mediaBiz;

	@Override
	public void apply(MediaItem item, MediaRequest request, MediaResponse response) {
		MagickImage source = (MagickImage) request.getParameters().get(OUTPUT_MAGICK_IMAGE_KEY);
		if ( source == null ) {
			throw new RuntimeException("MagickImage not available on request");
		}
		ImageInfo info = (ImageInfo) request.getParameters().get(INPUT_IMAGE_INFO_KEY);
		if ( info == null ) {
			throw new RuntimeException("ImageInfo not available on request");
		}
		MagickImage result = applyEffect(item, request, info, source);
		request.getParameters().put(OUTPUT_MAGICK_IMAGE_KEY, result);
	}

	/**
	 * @return Returns the mediaBiz.
	 */
	public MediaBiz getMediaBiz() {
		return mediaBiz;
	}

	/**
	 * @param mediaBiz
	 *        The mediaBiz to set.
	 */
	public void setMediaBiz(MediaBiz mediaBiz) {
		this.mediaBiz = mediaBiz;
	}

}
