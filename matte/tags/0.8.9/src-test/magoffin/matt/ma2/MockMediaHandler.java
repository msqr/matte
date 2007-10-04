/* ===================================================================
 * MockMediaHandler.java
 * 
 * Created Mar 3, 2006 9:29:59 PM
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
 * $Id: MockMediaHandler.java,v 1.3 2007/01/25 01:47:51 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2;

import java.io.File;
import java.util.Map;

import magoffin.matt.ma2.domain.MediaItem;

/**
 * Test implementation of MediaHandler.
 * 
 * @author matt.magoffin
 * @version $Revision: 1.3 $ $Date: 2007/01/25 01:47:51 $
 */
public class MockMediaHandler implements MediaHandler {

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaHandler#createNewMediaItem(java.io.File)
	 */
	public MediaItem createNewMediaItem(File inputFile) {
		return null;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaHandler#handleMediaRequest(magoffin.matt.ma2.domain.MediaItem, magoffin.matt.ma2.MediaRequest, magoffin.matt.ma2.MediaResponse)
	 */
	public void handleMediaRequest(MediaItem item, MediaRequest request, MediaResponse response) {
		// nothing
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaHandler#getEffect(java.lang.String, java.util.Map)
	 */
	public MediaEffect getEffect(String effectKey, Map<String, ?> effectParameters) {
		return null; // nothing
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaHandler#getFileExtension(magoffin.matt.ma2.domain.MediaItem, magoffin.matt.ma2.MediaRequest)
	 */
	public String getFileExtension(MediaItem item, MediaRequest request) {
		return null;
	}

}
