/* ===================================================================
 * BaseIM4JavaMediaEffect.java
 * 
 * Created Oct 21, 2010 10:24:42 AM
 * 
 * Copyright (c) 2010 Matt Magoffin.
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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2.image.im4java;

import java.util.List;

import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.MediaResponse;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.domain.MediaItem;

import org.apache.log4j.Logger;
import org.im4java.core.IMOperation;

/**
 * Base implementation of {@link magoffin.matt.ma2.image.im4java.IM4JavaMediaEffect}.
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl class="class-properties">
 *   <dt>mediaBiz</dt>
 *   <dd>The {@link MediaBiz} implementation to use.</dd>
 * </dl>
 *
 * @author matt
 * @version $Revision$ $Date$
 */
public abstract class BaseIM4JavaMediaEffect implements IM4JavaMediaEffect {

	private String key;
	private MediaBiz mediaBiz;

	/** A class logger. */
	protected final Logger log = Logger.getLogger(getClass());
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaEffect#apply(magoffin.matt.ma2.domain.MediaItem, magoffin.matt.ma2.MediaRequest, magoffin.matt.ma2.MediaResponse)
	 */
	public final void apply(MediaItem item, MediaRequest request, MediaResponse response) {
		IMOperation baseOperation = (IMOperation)request.getParameters().get(IM_OPERATION);
		if ( baseOperation == null ) {
			throw new RuntimeException("IMOperation not available on request");
		}
		ImageCommandAndOperation cmd = applyEffect(item, request, baseOperation);
		if ( cmd != null ) {
			@SuppressWarnings("unchecked")
			List<ImageCommandAndOperation> list = (List<ImageCommandAndOperation>)
				request.getParameters().get(SUB_COMMAND_LIST);
			if ( list == null ) {
				throw new RuntimeException("Sub command list not available on request.");
			}
			list.add(cmd);
		}
	}
	
	/**
	 * Constructor.
	 * 
	 * @param key the key to use
	 */
	public BaseIM4JavaMediaEffect(String key) {
		this.key = "image.im4java." +key;
	}
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaEffect#getKey()
	 */
	public final String getKey() {
		return key;
	}
	
	/**
	 * @return the mediaBiz
	 */
	public MediaBiz getMediaBiz() {
		return mediaBiz;
	}
	
	/**
	 * @param mediaBiz the mediaBiz to set
	 */
	public void setMediaBiz(MediaBiz mediaBiz) {
		this.mediaBiz = mediaBiz;
	}	
	
}
