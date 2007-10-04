/* ===================================================================
 * MediaMetadata.java
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
 * $Id: MediaMetadata.java,v 1.5 2007/07/29 08:42:43 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2;

import java.util.Date;
import java.util.Map;

import org.springframework.core.io.Resource;

/**
 * Metadata about a media item.
 * 
 * <p>This API exists to give a simple method to get meta data
 * about a variety of media types (image, video, sound, etc)
 * without needing to know the method the meta data was retrieved by.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public interface MediaMetadata {
	
	/**
	 * Set the media resource from which to extract metadata from.
	 * 
	 * <p>After this method is called, the other methods on the returned
	 * instance should return meta data extracted from this resource. 
	 * Note the returned instance may not be the same instance as the 
	 * one the method is called on, so that during the extraction process
	 * a more-specific instance can be returned (e.g. for a digital camera
	 * metadata instance, a generic image metadata might be instantiated, 
	 * but a more specific one returned by this method when it discovers 
	 * the type of camera make/model the media resource was taken with).</p>
	 * 
	 * @param resource the media resource being processed
	 * @return a MediaMetadata instance, may be a new instance or 
	 * the same instance this method was called on
	 */
	public MediaMetadata setMediaResource(Resource resource);

	/**
	 * Get the date the item was created.
	 * 
	 * <p>This method should return a "creation date" for the media 
	 * resource, if found in the resource's metadat. For example the EXIF
	 * metadata format stores the date/time the image was created, i.e. 
	 * the date/time the photo was taken. This method should return that 
	 * date. If no appropriate date can be found in the media resource's 
	 * metadata, this method should return <em>null</em>.</p>
	 * 
	 * <p>This method can only be called after {@link #setMediaResource(Resource)}
	 * has been called on this particular {@link MediaMetadata} instance.</p>
	 * 
	 * @return Date, or <em>null</em> if unknown
	 */
	public Date getCreationDate();

	/**
	 * Get a Map of all meta data values.
	 * 
	 * <p>This method should return a Map of all avaialble metadata values
	 * extracted from the media resource that can be represented as simple
	 * String values. These values can then be added to a {@code MediaItem}
	 * instance's {@code Metadata} (i.e. the List returned by 
	 * {@link magoffin.matt.ma2.domain.MediaItem#getMetadata()}.</p>
	 * 
	 * <p>This method can only be called after {@link #setMediaResource(Resource)}
	 * has been called on this particular {@link MediaMetadata} instance.</p>
	 * 
	 * @return map of all available meta data extracted from the resource
	 */
	public Map<String, String> getMetadataMap();

}
