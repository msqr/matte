/* ===================================================================
 * MediaRequest.java
 * 
 * Created Mar 15, 2006 10:00:04 PM
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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2;

import java.util.List;
import java.util.Map;

/**
 * API for media requests.
 * 
 * <p>This API represents a request for a specific media item. In some ways it is 
 * similar to the java {@link javax.servlet.ServletRequest} API. When a user 
 * wants to view a particular media item, the application will create an instance
 * of this class and populate it with values so that {@link #getMediaItemId()},
 * {@link #getQuality()}, and {@link #getSize()} are set according to what the 
 * user requested. It might also populate a list of {@link MediaEffect} objects
 * that the user requested to be applied to the media item.</p>
 * 
 * <p>The implementation of this API should be coded to return a unique key 
 * from the {@link #getCacheKey()} method, based on the properties set in 
 * the object (i.e. the size, quality, etc.). This is so the application can 
 * cache the results of processing this request so that future requests for the 
 * same item with the same properties do not need to be re-processed.</p>
 * 
 * @author matt.magoffin
 * @version $Revision$ $Date$
 */
public interface MediaRequest {
	
	/** 
	 * The parameter key for a File object the OutputStream in MediaResponse is pointed at. 
	 * Some MediaHandler implementations (eg JMagick) can use this during processing.
	 */
	public static final String OUTPUT_FILE_KEY = 
		"magoffin.matt.ma2.OutputFile";
	
	/**
	 * The user agent making this request. This can be useful for certain MediaHandler
	 * implementations that might want to return one content type for specific clients
	 * and another for other clients (eg. JPEG2000 for web browsers that support it and 
	 * JPEG for others).
	 */
	public static final String USER_AGENT_KEY = 
		"magoffin.matt.ma2.UserAgent";
	
	/**
	 * Get the ID of the media item desired.
	 * @return the media item ID
	 */
	Long getMediaItemId();
	
	/**
	 * Return <em>true</em> if the original media item file is desired.
	 * @return boolean
	 */
	boolean isOriginal();
	
	/**
	 * Get the desired size constant.
	 * @return the desired size
	 */
	MediaSize getSize();
	
	/**
	 * Get the desired quality constant.
	 * @return the desired quality
	 */
	MediaQuality getQuality();
	
	/**
	 * Get a Map of optional additional parameters.
	 * 
	 * <p>During request handling, {@link MediaHandler} implementations are 
	 * allowed to add parameters to this map.</p>
	 * 
	 * @return parameters
	 */
	Map<String, Object> getParameters();
	
	/**
	 * Get a List of effects to apply to the request.
	 * 
	 * <p>The effects should be applied in the order of the list. The 
	 * {@link MediaHandler} servicing the request should be allowed to 
	 * make changes to this List as it sees fit.</p>
	 * 
	 * @return effects list
	 */
	List<MediaEffect> getEffects();
	
	/**
	 * Get a key that can uniquely define this media request, for purposes
	 * of using as a key for caching.
	 * @return unique cache key
	 */
	String getCacheKey();
		
}
