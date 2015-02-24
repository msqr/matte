/* ===================================================================
 * MP3AudioMediaRequestHandler.java
 * 
 * Created Jul 19, 2004 11:35:46 AM
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
 * $Id: MP3AudioMediaRequestHandler.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.audio;

import java.io.File;
import java.util.List;

import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MediaMetadata;
import magoffin.matt.ma.xsd.MediaItem;

/**
 * MPEG audio media request handler.
 * 
 * <p>This handler uses ID3 to extract audio meta data.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public class MP3AudioMediaRequestHandler extends AbstractAudioMediaRequestHandler 
{

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandler#setMediaItemParameters(java.io.File, magoffin.matt.ma.xsd.MediaItem)
 */
public MediaMetadata setMediaItemParameters(File mediaFile, MediaItem item)
throws MediaAlbumException 
{
	super.setMediaItemParameters(mediaFile, item);
	
	ID3AudioMetadata meta = new ID3AudioMetadata(mediaFile);
	
	if ( meta.getSongName() != null && item.getName() == null ) {
		item.setName(meta.getSongName());
	}

	return meta;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.audio.AbstractAudioMediaRequestHandler#addMediaItemMetadata(magoffin.matt.ma.audio.AudioMetadata, magoffin.matt.ma.xsd.MediaItem, java.util.List)
 */
protected void addMediaItemMetadata(AudioMetadata meta, MediaItem item, List list) 
{
	// nothing to do
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.audio.AbstractAudioMediaRequestHandler#getAudioMetadataInstance(magoffin.matt.ma.xsd.MediaItem)
 */
protected AudioMetadata getAudioMetadataInstance(MediaItem item) {
	return new ID3AudioMetadata();
}

}
