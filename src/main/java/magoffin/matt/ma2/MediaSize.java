/* ===================================================================
 * MediaSize.java
 * 
 * Created Mar 16, 2006 10:23:06 PM
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
 */

package magoffin.matt.ma2;

import magoffin.matt.ma2.support.Geometry;

/**
 * Size constants for media items.
 * 
 * <p>This enumeration defines a finite set of sizes that media items can 
 * be requested as. This is to limit the number of sizes the application 
 * needs to generate, and possibly cache. Each enumeration defines a 
 * default {@link Geometry} that represents the width and height of that
 * size, in pixels, but these defaults may be overridden by the application 
 * if desired.</p>
 * 
 * @author matt.magoffin
 * @version 1.0
 */
public enum MediaSize {
	
	/** Biggest size, eg 1600x1200. */
	BIGGEST(new Geometry(1600,1200)),
	
	/** Bigger size, eg 1024x768. */
	BIGGER(new Geometry(1024,768)),
	
	/** Big size, eg 800x600. */
	BIG(new Geometry(800,600)),
	
	/** Normal size, eg 640x480. */
	NORMAL(new Geometry(640,480)),
	
	/** Small size, eg 480x320. */
	SMALL(new Geometry(480,320)),
	
	/** Tiny size, eg 320x240. */
	TINY(new Geometry(320,240)),
	
	/** Big thumbnail size. */
	THUMB_BIGGER(new Geometry(240,180)),
	
	/** Normal thumbnail size. */
	THUMB_BIG(new Geometry(180,135)),
	
	/** Small thumbnail size. */
	THUMB_NORMAL(new Geometry(120,90)),
	
	/** Tiny thumbnail size. */
	THUMB_SMALL(new Geometry(64,48));
	
	private Geometry g;
	
	MediaSize(Geometry g) {
		this.g = g;
	}
	
	/**
	 * Get a default Geometry object for this size.
	 * @return geometry
	 */
	public Geometry getGeometry() {
		return g;
	}
	
}
