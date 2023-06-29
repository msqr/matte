/* ===================================================================
 * MutableGeometry.java
 * 
 * Created Oct 28, 2010 11:30:41 AM
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
 */

package magoffin.matt.ma2.support;

import magoffin.matt.ma2.support.Geometry.Mode;

/**
 * Mutable version of {@link Geometry}.
 *
 * @author matt
 * @version 1.0
 */
public class MutableGeometry extends Geometry {

	/**
	 * Default constructor.
	 */
	public MutableGeometry() {
		super();
	}
	
	/**
	 * Construct a Geometry with width and height.
	 * 
	 * @param width
	 * @param height
	 */
	public MutableGeometry(int width, int height) {
		super(width, height);
	}

	/**
	 * Construct a Geometry with width, height, and mode.
	 * 
	 * @param width
	 * @param height
	 * @param mode
	 */
	public MutableGeometry(int width, int height, Mode mode) {
		super(width, height, mode);
	}
	
	/**
	 * Construct a mutable copy of another Geometry instance.
	 * @param other
	 */
	public MutableGeometry(Geometry other) {
		super(other.width, other.height, other.mode);
	}
	
	/**
	 * Swap the width and height values.
	 */
	public void swapWidthAndHeight() {
		int tmp = this.width;
		this.width = this.height;
		this.height = tmp;
	}

	/**
	 * Sets the height.
	 * 
	 * @param height The height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * Sets the width.
	 * 
	 * @param width The width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * Sets the mode.
	 * 
	 * <p>
	 * The mode should be one of the defined constants {@link Mode#MAX},
	 * {@link Mode#MIN}, or {@link Mode#EXACT}.
	 * </p>
	 * 
	 * @param mode the new mode
	 */
	public void setMode(Mode mode) {
		this.mode = mode;
	}

	/**
	 * Reset this object to its default state.
	 */
	public void reset() {
		width = height = DEFAULT_DIMENSION;
		mode = DEFAULT_MODE;
	}

}
