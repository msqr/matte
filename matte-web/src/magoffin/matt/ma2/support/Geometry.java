/* ===================================================================
 * Geometry.java
 * 
 * Copyright (c) 2002 Matt Magoffin.
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

package magoffin.matt.ma2.support;

/**
 * Simple class to represent a width and height.
 * 
 * <p>This class is immuntable. See {@link MutableGeometry} for a geometry
 * object whose values are changable.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class Geometry {

	/**
	 * Geometry mode.
	 */
	public enum Mode {
		/** Max geometry. */
		MAX,

		/** Minimum geometry. */
		MIN,

		/** Exact geometry. */
		EXACT
	}

	/** The default mode value. */
	public static final Mode DEFAULT_MODE = Mode.MAX;
	
	/** The default height and width dimensions. */
	public static final int DEFAULT_DIMENSION = -1;

	/** The width value. */
	protected int width = DEFAULT_DIMENSION;
	
	/** The height value. */
	protected int height = DEFAULT_DIMENSION;
	
	/** The mode. */
	protected Mode mode = DEFAULT_MODE;

	/**
	 * Default constructor.
	 */
	public Geometry() {
		// nothing to do
	}

	/**
	 * Construct a Geometry with width and height.
	 * 
	 * @param width
	 * @param height
	 */
	public Geometry(int width, int height) {
		this.width = width;
		this.height = height;
	}

	/**
	 * Construct a Geometry with width, height, and mode.
	 * 
	 * @param width
	 * @param height
	 * @param mode
	 */
	public Geometry(int width, int height, Mode mode) {
		this.width = width;
		this.height = height;
		this.mode = mode;
	}
	
	/**
	 * Returns the height.
	 * 
	 * @return int
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Returns the width.
	 * 
	 * @return int
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Returns the mode.
	 * 
	 * <p>
	 * The mode should be one of the defined constants {@link Mode#MAX},
	 * {@link Mode#MIN}, or {@link Mode#EXACT}.
	 * </p>
	 * 
	 * @return the mode
	 */
	public Mode getMode() {
		return mode;
	}

	@Override
	public String toString() {
		return width + "x" + height;
	}

}
