/* ===================================================================
 * BaseAwtImageMediaHandler.java
 * 
 * Created Mar 20, 2006 4:39:10 PM
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

import java.util.Map;

import magoffin.matt.ma2.MediaEffect;
import magoffin.matt.ma2.image.BaseImageMediaHandler;

/**
 * Base implementation of {@link magoffin.matt.ma2.MediaHandler} that uses the AWT
 * for image processing.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public abstract class BaseAwtImageMediaHandler extends BaseImageMediaHandler {
	
	private Map<String,AwtMediaEffect> awtMediaEffectMap;

	/**
	 * Construct with MIME type.
	 * @param mime the MIME type
	 */
	public BaseAwtImageMediaHandler(String mime) {
		super(mime);
	}

	@Override
	public MediaEffect getEffect(String key, Map<String, ?> effectParameters) {
		return awtMediaEffectMap.get(key);
	}

	/**
	 * @return Returns the awtMediaEffectMap.
	 */
	public Map<String, AwtMediaEffect> getAwtMediaEffectMap() {
		return awtMediaEffectMap;
	}
	
	/**
	 * @param awtMediaEffectMap The awtMediaEffectMap to set.
	 */
	public void setAwtMediaEffectMap(Map<String, AwtMediaEffect> awtMediaEffectMap) {
		this.awtMediaEffectMap = awtMediaEffectMap;
	}

}
