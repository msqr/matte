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
 * $Id: Geometry.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.util;

/**
 * Simple class to represent a width and height.
 * 
 * <p>Created Oct 16, 2002 8:00:01 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class Geometry 
{
	
	public static final char MAX = 1;
	public static final char MIN = 2;
	public static final char EXACT = 3;
	
	private static final char DEFAULT_MODE = MAX;
	
	private int width = -1;
	private int height = -1;
	private char mode = DEFAULT_MODE;


/**
 * Default constructor. */
public Geometry() {}


/**
 * Construct a Geometry with width and height.
 *  * @param width * @param height */
public Geometry(int width, int height)
{
	this.width = width;
	this.height = height;
}


/**
 * Construct a Geometry with width, height, and mode.
 *  * @param width * @param height * @param mode */
public Geometry(int width, int height, char mode)
{
	this.width = width;
	this.height = height;
	this.mode = mode;
}


/**
 * Returns the height.
 * @return int
 */
public int getHeight() {
	return height;
}

/**
 * Returns the width.
 * @return int
 */
public int getWidth() {
	return width;
}

/**
 * Sets the height.
 * @param height The height to set
 */
public void setHeight(int height) {
	this.height = height;
}

/**
 * Sets the width.
 * @param width The width to set
 */
public void setWidth(int width) {
	this.width = width;
}

/**
 * Returns the mode.
 * 
 * <p>The mode should be one of the defined constants {@link #MAX},
 * {@link #MIN}, or {@link #EXACT}.</p>
 * 
 * @return the mode
 */
public char getMode() {
	return mode;
}

/**
 * Sets the mode.
 * 
 * <p>The mode should be one of the defined constants {@link #MAX},
 * {@link #MIN}, or {@link #EXACT}.</p>
 * 
 * @param mode the new mode
 */
public void setMode(char mode) {
	this.mode = mode;
}

/**
 * Reset this object to its default state. */
public void reset()
{
	width = height = -1;
	mode = DEFAULT_MODE;
}

/* (non-Javadoc)
 * @see java.lang.Object#toString()
 */
public String toString() {
	return width + "x" + height;
}

} // class Geometry
