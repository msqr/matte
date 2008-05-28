/* ===================================================================
 * AbstractAudioMediaRequestHandler.java
 * 
 * Created Jul 19, 2004 11:43:46 AM
 * 
 * Copyright (c) 2004 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: AbstractAudioMediaRequestHandler.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.audio;

import java.util.ArrayList;
import java.util.List;

import magoffin.matt.ma.util.BasicFileMediaRequestHandler;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.ma.xsd.MediaItemMetadata;

/**
 * Basic audio media request handler.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public abstract class AbstractAudioMediaRequestHandler extends BasicFileMediaRequestHandler 
{
/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandler#getMetadataForItem(magoffin.matt.ma.xsd.MediaItem)
 */
public MediaItemMetadata[] getMetadataForItem(MediaItem item) 
{
	if ( item == null || item.getMeta() == null ) return null;
	AudioMetadata metadata = getAudioMetadataInstance(item);
	metadata.deserializeFromString(item.getMeta());
	List l = new ArrayList(8);

	addMediaItemMetadata(l,AudioMetadata.META_ALBUM,metadata.getAlbum());
	addMediaItemMetadata(l,AudioMetadata.META_ARTIST,metadata.getArtist());
	addMediaItemMetadata(l,AudioMetadata.META_AUDIO_FORMAT,metadata.getAudioFormat());
	addMediaItemMetadata(l,AudioMetadata.META_BIT_RATE,metadata.getBitRate());
	addMediaItemMetadata(l,AudioMetadata.META_DISC_NUM,metadata.getDiscNumber());
	addMediaItemMetadata(l,AudioMetadata.META_DISC_TOTAL,metadata.getDiscTotal());
	addMediaItemMetadata(l,AudioMetadata.META_DURATION,metadata.getDurationAsTimeCode());
	addMediaItemMetadata(l,AudioMetadata.META_GENRE,metadata.getGenre());
	addMediaItemMetadata(l,AudioMetadata.META_SAMPLE_RATE,metadata.getSampleRate());
	addMediaItemMetadata(l,AudioMetadata.META_SONG_NAME,metadata.getSongName());
	addMediaItemMetadata(l,AudioMetadata.META_TRACK_NUM,metadata.getTrackNumber());
	addMediaItemMetadata(l,AudioMetadata.META_TRACK_TOTAL,metadata.getTrackTotal());
	addMediaItemMetadata(l,AudioMetadata.META_YEAR,metadata.getYear());
	
	this.addMediaItemMetadata(metadata,item,l);
	
	return l.size() < 1 ? null : (MediaItemMetadata[])
		l.toArray(new MediaItemMetadata[l.size()]);
}

/**
 * Get a AudioMetadata instance for a MediaItem.
 * 
 * @param item the item to get the meta instance for
 * @return VideoMetadata
 */
protected abstract AudioMetadata getAudioMetadataInstance(MediaItem item);

/**
 * This method will be called form {@link #getMetadataForItem(MediaItem)} to
 * allow for custom MediaItemMetadata objects to be added.
 * 
 * @param meta the audio metadata object
 * @param item the media item
 * @param list the list of MediaItemMetadata objects to add to
 */
protected abstract void addMediaItemMetadata(AudioMetadata meta, MediaItem item, List list);

}
