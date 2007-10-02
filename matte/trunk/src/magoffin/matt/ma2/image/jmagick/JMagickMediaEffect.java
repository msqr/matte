/* ===================================================================
 * JMagickMediaEffect.java
 * 
 * Created Dec 28, 2006 9:35:24 PM
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
 * $Id: JMagickMediaEffect.java,v 1.2 2006/12/29 23:34:26 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.image.jmagick;

import magick.ImageInfo;
import magick.MagickImage;
import magoffin.matt.ma2.MediaEffect;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.domain.MediaItem;

/**
 * An API for JMagick-based implementations of {@link MediaEffect}.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.2 $ $Date: 2006/12/29 23:34:26 $
 */
public interface JMagickMediaEffect extends MediaEffect {
	
	/** The MediaRequest parameter key for the input ImageInfo object. */
	public static final String INPUT_IMAGE_INFO_KEY = 
		"magoffin.matt.ma2.image.jmagick.InputImageInfo";
	
	/** The MediaRequest parameter key for the input MagickImage object. */
	public static final String INPUT_MAGICK_IMAGE_KEY =
		"magoffin.matt.ma2.image.jmagick.InputMagickImage";
	
	/** The MediaRequest parameter key for the result MagickImage object. */
	public static final String OUTPUT_MAGICK_IMAGE_KEY = 
		"magoffin.matt.ma2.image.jmagick.OutputMagickImage";
	
	/**
	 * Apply effect with ImageMagick.
	 * 
	 * @param item the MediaItem the effect is being applied to
	 * @param request the request
	 * @param inInfo the ImageInfo used to open the image
	 * @param image the current ImageMagick MagickImage
	 * @return the resulting image
	 */
	public MagickImage applyEffect(MediaItem item, MediaRequest request, 
			ImageInfo inInfo, MagickImage image);
	
}
