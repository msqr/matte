/* ===================================================================
 * BasicAudioMediaHandler.java
 * 
 * Created Feb 1, 2007 4:21:03 PM
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

package magoffin.matt.ma2.audio;

import magoffin.matt.ma2.MediaMetadata;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.support.BasicIconBasedMediaHandler;

import org.springframework.core.io.Resource;

/**
 * {@link magoffin.matt.ma2.MediaHandler} implementation for audio files.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public class BasicAudioMediaHandler extends BasicIconBasedMediaHandler {
	
	/** The default value for the <code>songTitleMetaKey</code> property. */
	public static final String DEFAULT_SONG_TITLE_META_KEY = "SONG_NAME";
	
	private String songTitleMetaKey = DEFAULT_SONG_TITLE_META_KEY;

	/**
	 * Constructor.
	 * 
	 * @param mime the MIME
	 * @param preferredFileExtension the preferred file extension
	 */
	public BasicAudioMediaHandler(String mime, String preferredFileExtension) {
		super(mime, preferredFileExtension);
	}

	@Override
	protected MediaMetadata handleMetadata(MediaRequest request, Resource mediaResource, MediaItem item) {
		MediaMetadata meta = super.handleMetadata(request, mediaResource, item);
		
		// look for song title, and set that as item name if available
		if ( meta != null && meta.getMetadataMap().containsKey(songTitleMetaKey) ) {
			Object metaValue = meta.getMetadataMap().get(songTitleMetaKey);
			if ( metaValue != null ) {
				item.setName(metaValue.toString());
			}
		}
		
		return meta;
	}
	
	/**
	 * @return the songTitleMetaKey
	 */
	public String getSongTitleMetaKey() {
		return songTitleMetaKey;
	}
	
	/**
	 * @param songTitleMetaKey the songTitleMetaKey to set
	 */
	public void setSongTitleMetaKey(String songTitleMetaKey) {
		this.songTitleMetaKey = songTitleMetaKey;
	}

}
