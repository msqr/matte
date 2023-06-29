/* ===================================================================
 * MediaEffect.java
 * 
 * Created Mar 20, 2006 4:27:41 PM
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

package magoffin.matt.ma2;

import magoffin.matt.ma2.domain.MediaItem;

/**
 * API for applying effects to media items.
 * 
 * <p>The {@link MediaEffect} is a way for Matte to apply effects to 
 * media items during processing in a call to 
 * {@link magoffin.matt.ma2.MediaHandler#handleMediaRequest}. Standard 
 * effects are resizing (scale) and rotating the media, and those effects
 * have constant keys defined in this API ({@link #KEY_SCALE} and 
 * {@link #KEY_ROTATE}.</p>
 * 
 * <p>The {@link #getKey()} method should return a unique key for the 
 * <em>implementation</em> and <em>function</em> of effect the code implements. 
 * There might be several different implementations of a "scale" effect, but they 
 * all should include the key {@link #KEY_SCALE} at the end of the value returned
 * by {@link #getKey()}.</p>
 * 
 * <p>Each effect implementation might require specific parameters to 
 * function. Those parameters should be passed to the effect via the 
 * {@link MediaRequest#getParameters()} map. For example, a rotate effect
 * needs to know by how many degrees it should rotate an image. This 
 * amount can be passed as a parameter (the {@link #MEDIA_REQUEST_PARAM_ROTATE_DEGREES}
 * parameter is defined for this purpose).</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public interface MediaEffect {
	
	/** The key for scaling the media item. */
	public static final String KEY_SCALE = "scale";
	
	/** The key for rotating the media item. */
	public static final String KEY_ROTATE = "rotate";
	
	/** The key for applying a watermark to a media item. */
	public static final String KEY_WATERMARK = "watermark";
	
	/** 
	 * A MediaRequest parameter key for a integer value representing a rotational 
	 * degree to apply to the media item.
	 */
	public static final String MEDIA_REQUEST_PARAM_ROTATE_DEGREES = "media.rotate.degrees";
	
	/** 
	 * A MediaRequest parameter key for a Spring {@code Resource} instance
	 * that should be used as a watermark image.
	 */
	public static final String MEDIA_REQUEST_PARAM_WATERMARK_RESOURCE = "watermark.resource";
	
	/**
	 * Get the key for this effect.
	 * 
	 * <p>Keys uniquely describe the <em>implementation</em> and 
	 * <em>function</em> of the effect. The implementation key should come first, 
	 * and the function key at the end. Some standard function key values are 
	 * defined in this API: {@link #KEY_SCALE} for re-sizing and {@link #KEY_ROTATE}
	 * for rotating.</p>
	 * 
	 * <p>For example, a key might look like <code>image.awt.rotate</code> for 
	 * an image rotation effect based on a Java AWT implementation.</p>
	 * 
	 * @return the effect key
	 */
	String getKey();
	
	/**
	 * Apply the effect.
	 * 
	 * <p>{@code MediaHandler} and {@code MediaEffect} implementations can utilize 
	 * the {@link MediaRequest#getParameters()} map to keep track of request 
	 * processing state during processing. Different implementations of 
	 * {@link MediaHandler} will support different implementations of 
	 * {@code MediaEffects}.</p>
	 * 
	 * @param item the item being processed
	 * @param request the current request
	 * @param response the current response
	 */
	void apply(MediaItem item, MediaRequest request, MediaResponse response);
	
}
