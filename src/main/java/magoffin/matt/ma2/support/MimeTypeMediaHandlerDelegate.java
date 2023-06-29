/* ===================================================================
 * MimeTypeMediaHandlerDelegate.java
 * 
 * Created Jan 24, 2007 11:30:40 AM
 * 
 * Copyright (c) 2007 Matt Magoffin (spamsqr@msqr.us)
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

import java.util.Map;
import org.springframework.core.io.Resource;
import magoffin.matt.ma2.MediaHandlerDelegate;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.MediaResponse;
import magoffin.matt.ma2.domain.MediaItem;

/**
 * Implementation of {@link MediaHandlerDelegate} that uses a MIME type mapping
 * to delegate to any number of different {@link MediaHandlerDelegate}
 * implementations.
 * 
 * <p>
 * The configurable properties of this class are:
 * </p>
 * 
 * <dl class="class-properties">
 * <dt>delegateMap</dt>
 * <dd>A Map with String keys representing MIME types and corresponding
 * {@link MediaHandlerDelegate} implementations to handle requests of that MIME
 * type. The {@link #handleDelegateMediaRequest} method will look up MIME types
 * in this map and delegate to the matching handler.</dd>
 * </dl>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.1
 */
public class MimeTypeMediaHandlerDelegate implements MediaHandlerDelegate {

	private Map<String, MediaHandlerDelegate> delegateMap;

	@Override
	public void handleDelegateMediaRequest(Resource mediaResource, String mimeType, MediaItem item,
			MediaRequest request, MediaResponse response) {
		MediaHandlerDelegate delegate = getDelegateMap().get(mimeType);
		if ( delegate == null ) {
			throw new UnsupportedOperationException("The MIME type [" + mimeType + "] is not supported");
		}
		delegate.handleDelegateMediaRequest(mediaResource, mimeType, item, request, response);
	}

	@Override
	public String getDelegateFileExtension(Resource mediaResource, String mimeType, MediaItem item,
			MediaRequest request) {
		MediaHandlerDelegate delegate = getDelegateMap().get(mimeType);
		if ( delegate == null ) {
			throw new UnsupportedOperationException("The MIME type [" + mimeType + "] is not supported");
		}
		return delegate.getDelegateFileExtension(mediaResource, mimeType, item, request);
	}

	/**
	 * @return the delegateMap
	 */
	public Map<String, MediaHandlerDelegate> getDelegateMap() {
		return delegateMap;
	}

	/**
	 * @param delegateMap
	 *        the delegateMap to set
	 */
	public void setDelegateMap(Map<String, MediaHandlerDelegate> delegateMap) {
		this.delegateMap = delegateMap;
	}

}
