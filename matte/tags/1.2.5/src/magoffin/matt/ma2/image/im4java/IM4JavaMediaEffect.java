/* ===================================================================
 * IM4JavaMediaEffect.java
 * 
 * Created Oct 21, 2010 10:49:33 AM
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

import magoffin.matt.ma2.MediaEffect;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.domain.MediaItem;

import org.im4java.core.IMOperation;

/**
 * MediaEffect API for IM4Java effects.
 *
 * @author matt
 * @version $Revision$ $Date$
 */
public interface IM4JavaMediaEffect extends MediaEffect {

	/** The MediaRequest parameter key for the base IMOperation. */
	public static final String IM_OPERATION = 
		"magoffin.matt.ma2.image.im4java.IMOperation";

	/** The MediaRequest parameter key for the List&lt;ImageCommandAndOperation&gt;. */
	public static final String SUB_COMMAND_LIST = 
		"magoffin.matt.ma2.image.im4java.SubCommandList";

	/**
	 * Apply effect with IM4Java.
	 * 
	 * @param item the MediaItem the effect is being applied to
	 * @param request the request
	 * @param baseOperation the IMOperation to add the effect commands to,
	 * as part of a "convert" command
	 * @return an optional additional command to execute after the 
	 * base operations have been completed
	 */
	public ImageCommandAndOperation applyEffect(MediaItem item, MediaRequest request, 
			IMOperation baseOperation);

}
