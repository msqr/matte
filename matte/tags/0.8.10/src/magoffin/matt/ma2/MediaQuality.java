/* ===================================================================
 * MediaQuality.java
 * 
 * Created Mar 16, 2006 10:27:54 PM
 * 
 * Copyright (c) 2006 Matt Magoffin.
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
 * $Id: MediaQuality.java,v 1.3 2007/07/29 08:42:43 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2;

/**
 * Quality constants for media items.
 * 
 * <p>This enumeration defines a finite set of quality values that media items 
 * can be requested as. This is to limit the number of different quality values
 * the application needs to generate, and possibly cache, for each media item
 * it processes. Each enumeration defines a default quality value as a float, 
 * with <em>0</em> being the lowest quality and <em>100</em> being the highest
 * quality. These defauls can be overridden by the application, however, and 
 * quality values will have different meanings to different types of media 
 * types. For many media types the quality value corresponds to the amount of 
 * lossy compression applied to them, e.g. the higher the quality the lower
 * the amount of compression.</p>
 * 
 * @author matt.magoffin
 * @version $Revision: 1.3 $ $Date: 2007/07/29 08:42:43 $
 */
public enum MediaQuality {
	
	/** The highest quality possible. */
	HIGHEST(1.0f),
	
	/** Very high quality. */
	HIGH(0.875f),
	
	/** Good quality. */
	GOOD(0.75f),
	
	/** Average quality. */
	AVERAGE(0.5f),
	
	/** Low quality. */
	LOW(0.25f);
	
	private float qualityValue;
	
	MediaQuality(float qualityValue) {
		this.qualityValue = qualityValue;
	}
	
	/**
	 * Get the default quality value.
	 * @return quality value
	 */
	public float getQualityValue() {
		return this.qualityValue;
	}

}
