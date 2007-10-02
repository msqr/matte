/* ===================================================================
 * MediaSpecUtil.java
 * 
 * Copyright (c) 2002-2003 Matt Magoffin.
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
 * $Id: MediaSpecUtil.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.util;

import java.util.HashMap;
import java.util.Map;

import magoffin.matt.ma.xsd.MediaSpec;

import org.apache.log4j.Logger;

/**
 * Static utility methods for dealing with MediaSpec objects.
 * 
 * <p>The <code>SIZE_<em>x</em></code> constants define the supported
 * sizes the MediaServer will allow. This is done to minimize the 
 * number of different sizes that are created (and thus cached).</p>
 * 
 * <p>The <code>COMPRESS_<em>x</em></code> constants define the supported
 * compression values the MediaServer will allow. This is also done to 
 * minimize the number of different compression values that are used
 * (and thus cached).</p>
 * 
 * <p> Created on Nov 12, 2002 1:18:34 PM.</p>
 *
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public final class MediaSpecUtil 
{
	/** Huge size: 1600x1200. */
	public static final String SIZE_HUGE = "huge";

	/** Big size: 1024x768. */
	public static final String SIZE_BIG = "big";

	/** Medium size: 800x600. */
	public static final String SIZE_MEDIUM = "medium";

	/** Normal size: 640x480. */
	public static final String SIZE_NORMAL = "normal";

	/** Normal size: 480x360. */
	public static final String SIZE_SMALL = "small";

	/** Small size: 320x240. */
	public static final String SIZE_TINY = "tiny";
	
	/** Big thumbnail size: 240x180. */
	public static final String SIZE_THUMB_HUGE = "t"+SIZE_HUGE;

	/** Big thumbnail size: 160x120. */
	public static final String SIZE_THUMB_BIG = "t"+SIZE_BIG;

	/** Medium thumbnail size: 120x90. */
	public static final String SIZE_THUMB_NORMAL = "t"+SIZE_NORMAL;

	/** Big thumbnail size: 80x60. */
	public static final String SIZE_THUMB_SMALL = "t"+SIZE_SMALL;
	
	/** High compression: <code>high</code>. */
	public static final String COMPRESS_HIGH = "high";

	/** Medium compression: <code>medium</code>. */
	public static final String COMPRESS_MEDIUM = "medium";

	/** Normal compression: <code>normal</code>. */
	public static final String COMPRESS_NORMAL = "normal";

	/** No compression: <code>none</code>. */
	public static final String COMPRESS_NONE = "none";
	
	/** Defatul thumbnail MediaSpec object. */
	public static final MediaSpec DEFAULT_THUMB_SPEC = new MediaSpec();
	
	private static final Logger log = Logger.getLogger(MediaSpecUtil.class);
	private static final Map IS_SIZE_CACHE = new HashMap(8,1);
	private static final Map SIZE_MAP = new HashMap(14,1);
	private static final Map COMP_MAP = new HashMap(8,1);
	
	static {
		SIZE_MAP.put(SIZE_HUGE,new int[] {1600,1200});
		SIZE_MAP.put(SIZE_BIG,new int[] {1024,768});
		SIZE_MAP.put(SIZE_MEDIUM,new int[] {800,600});
		SIZE_MAP.put(SIZE_NORMAL,new int[] {640,480});
		SIZE_MAP.put(SIZE_SMALL,new int[] {480,360});
		SIZE_MAP.put(SIZE_TINY,new int[] {320,240});
		
		SIZE_MAP.put(SIZE_THUMB_HUGE, new int[] {240,180});
		SIZE_MAP.put(SIZE_THUMB_BIG, new int[] {160,120});
		SIZE_MAP.put(SIZE_THUMB_NORMAL, new int[] {120,90});
		SIZE_MAP.put(SIZE_THUMB_SMALL, new int[] {80,60});
		
		COMP_MAP.put(COMPRESS_HIGH, new Integer(30));
		COMP_MAP.put(COMPRESS_MEDIUM, new Integer(50));
		COMP_MAP.put(COMPRESS_NORMAL, new Integer(75));
		COMP_MAP.put(COMPRESS_NONE, new Integer(100));
		
		// set the defaults for thumbnails
		DEFAULT_THUMB_SPEC.setCompress(COMPRESS_NORMAL);
		DEFAULT_THUMB_SPEC.setWidth(((int[])SIZE_MAP.get(SIZE_THUMB_NORMAL))[0]);
		DEFAULT_THUMB_SPEC.setHeight(((int[])SIZE_MAP.get(SIZE_THUMB_NORMAL))[1]);
		
	}


/**
 * Get an MediaSpec object based on size and quality names.
 *  * @param size * @param compression * @return MediaSpec */
public static MediaSpec getImageSpec(String size, String compression)
{
	return getImageSpec(size,compression,false);
}


/**
 * Get an MediaSpec object based on size and quality names.
 * 
 * @param size
 * @param compression
 * @return MediaSpec
 */
public static MediaSpec getThumbImageSpec(String size, String compression)
{
	return getImageSpec(size,compression,true);
}

/**
 * Get an MediaSpec for either a regular or thumbnail.
 *  * @param size the size name * @param compression the compression name * @param forThumb <em>true</em> if for thumbnail size * @return MediaSpec */
private static MediaSpec getImageSpec(String size, String compression, boolean forThumb)
{
	// we use different keys for thumb than regular so we set width/height 
	// appropriately, but (for now) we set the size value the same for both
	String s = forThumb ? "t"+size : size;
	Map qualMap = (Map)IS_SIZE_CACHE.get(s);
	if ( qualMap == null ) {
		if ( !SIZE_MAP.containsKey(s) ) {
			log.warn("MediaSpec size '" +size +"' not supported");
			return null;
		}
		qualMap = new HashMap(6,1);
		IS_SIZE_CACHE.put(s,qualMap);
	}
	MediaSpec spec = (MediaSpec)qualMap.get(compression);
	if ( spec == null ) {
		if ( !COMP_MAP.containsKey(compression) ) {
			log.warn("MediaSpec compression '" +compression +"' not supported");
			return null;
		}
		spec = new MediaSpec();
		int[] sizes = (int[])SIZE_MAP.get(s);
		spec.setWidth(sizes[0]);
		spec.setHeight(sizes[1]);
		spec.setCompress(compression);
		spec.setSize(size);
		qualMap.put(compression,spec);
	}
	return spec;
}

/**
 * Returns <em>true</em> if the size name is a thumbnail size.
 * @param size the size name to test
 * @return <em>true</em> if size name is a thumbnail size
 */
public static boolean isThumbnailSize(String size) {
	return size.charAt(0) == 't';
}

/**
 * Get a compression number from a name.
 * 
 * <p>A compression number is assumed to be from 0 to 100, with 0 meaning
 * the least quality (most compression) and 100 being the best quality
 * (least compression).</p>
 *  * @param name * @return Integer, never <em>null</em> */
public static Integer getCompressionValue(String name)
{
	if ( !COMP_MAP.containsKey(name) ) {
		name = COMPRESS_NORMAL;
	}
	return (Integer)COMP_MAP.get(name);
}


/**
 * Get an int array with width and height from a name.
 * 
 * <p>The returned int array will have two elements, the first
 * the width, the second the height, to be treated as pixel 
 * dimensions.</p>
 *  * @param name the name to get * @return int array of length 2, never <em>null</em> */
public static int[] getWidthHeightValue(String name)
{
	if ( !SIZE_MAP.containsKey(name) ) {
		name = SIZE_NORMAL;
	}
	return (int[])SIZE_MAP.get(name);
}


} // class MediaSpecUtil
