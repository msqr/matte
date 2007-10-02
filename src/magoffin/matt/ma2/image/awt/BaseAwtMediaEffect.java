/* ===================================================================
 * BaseAwtMediaEffect.java
 * 
 * Created Mar 20, 2006 5:16:18 PM
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
 * $Id: BaseAwtMediaEffect.java,v 1.4 2007/07/28 10:25:54 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.image.awt;

import java.awt.image.BufferedImage;

import org.apache.log4j.Logger;

import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.MediaResponse;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.domain.MediaItem;

/**
 * Base implementation of {@link magoffin.matt.ma2.image.awt.AwtMediaEffect}.
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl class="class-properties">
 *   <dt>mediaBiz</dt>
 *   <dd>The {@link MediaBiz} implementation to use.</dd>
 * </dl>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.4 $ $Date: 2007/07/28 10:25:54 $
 */
public abstract class BaseAwtMediaEffect implements AwtMediaEffect {

	/** A class logger. */
	protected final Logger log = Logger.getLogger(getClass());
	
	private MediaBiz mediaBiz;
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaEffect#apply(magoffin.matt.ma2.domain.MediaItem, magoffin.matt.ma2.MediaRequest, magoffin.matt.ma2.MediaResponse)
	 */
	public void apply(MediaItem item, MediaRequest request, MediaResponse response) {
		BufferedImage input = (BufferedImage)request.getParameters().get(
				INPUT_BUFFERED_IMAGE_KEY);
		if ( input == null ) {
			throw new RuntimeException("BufferedImage not available on MediaRequest");
		}
		BufferedImage result = applyEffect(item, request, input);
		request.getParameters().put(INPUT_BUFFERED_IMAGE_KEY, result);
	}

	/**
	 * @return Returns the mediaBiz.
	 */
	public MediaBiz getMediaBiz() {
		return mediaBiz;
	}
	
	/**
	 * @param mediaBiz The mediaBiz to set.
	 */
	public void setMediaBiz(MediaBiz mediaBiz) {
		this.mediaBiz = mediaBiz;
	}
	
}
