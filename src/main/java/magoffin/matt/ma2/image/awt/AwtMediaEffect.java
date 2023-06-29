/* ===================================================================
 * AwtMediaEffect.java
 * 
 * Created Mar 20, 2006 5:08:16 PM
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

package magoffin.matt.ma2.image.awt;

import java.awt.image.BufferedImage;

import magoffin.matt.ma2.MediaEffect;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.domain.MediaItem;

/**
 * An API for AWT-based implementations of {@link MediaEffect}.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public interface AwtMediaEffect extends MediaEffect {

	/** The MediaRequest parameter key for the input BufferedImage object. */
	public static final String INPUT_BUFFERED_IMAGE_KEY = 
		"magoffin.matt.ma2.image.awt.InputBufferedImage";
	
	/**
	 * Apply an effect on a <code>BufferedImage</code> and return the result
	 * as a new <code>BufferedImage</code>.
	 * 
	 * <p>If no change is made to the <em>source</em> <code>BufferedImage</code> then 
	 * <em>source</em> can be returned from this method.</p>
	 * 
	 * @param item the MediaItem the effect is being applied to
	 * @param request the request
	 * @param source the source <code>BufferedImage</code>
	 * @return the updated <code>BufferedImage</code>
	 */
	public BufferedImage applyEffect(MediaItem item, MediaRequest request, BufferedImage source);
	
}
