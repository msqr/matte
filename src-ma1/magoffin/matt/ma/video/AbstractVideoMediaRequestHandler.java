/* ===================================================================
 * AbstractVideoMediaRequestHandler.java
 * 
 * Created Jul 14, 2004 5:04:06 PM
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
 * $Id: AbstractVideoMediaRequestHandler.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.video;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

import magoffin.matt.ma.MediaRequestHandlerParams;
import magoffin.matt.ma.image.iio.JpegMediaRequestHandler;
import magoffin.matt.ma.image.iio.PngMediaRequestHandler;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.ma.xsd.MediaItemMetadata;

/**
 * Abstract base class for video media handlers which extends
 * the {@link magoffin.matt.ma.image.iio.JpegMediaRequestHandler} class
 * to return JPEG images for resized media from video files.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public abstract class AbstractVideoMediaRequestHandler 
extends JpegMediaRequestHandler
implements VideoMediaRequestHandler 
{
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.image.iio.ImageIOMediaRequestHandler#getWriterForItem(magoffin.matt.ma.xsd.MediaItem)
 */
protected ImageWriter getWriterForItem(MediaItem item) 
{
	// return JPEG
	Iterator writers = ImageIO.getImageWritersByMIMEType(JPEG_MIME);
	if ( !writers.hasNext() ) {
		return null;
	}
	return (ImageWriter)writers.next();
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandler#getOutputMime(magoffin.matt.ma.xsd.MediaItem, magoffin.matt.ma.MediaRequestHandlerParams)
 */
public String getOutputMime(MediaItem item, MediaRequestHandlerParams params) {
	if ( needToAlter(item,params) ) {
		if ( item.getUseIcon().booleanValue() ) {
			return PngMediaRequestHandler.PNG_MIME;
		}
		return super.getOutputMime(item,params);
	}
	return item.getMime();
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandler#getMetadataForItem(magoffin.matt.ma.xsd.MediaItem)
 */
public MediaItemMetadata[] getMetadataForItem(MediaItem item) 
{
	if ( item == null || item.getMeta() == null ) return null;
	VideoMetadata metadata = this.getVideoMetadataInstance(item);
	metadata.deserializeFromString(item.getMeta());
	List l = new ArrayList(8);

	addMediaItemMetadata(l,VideoMetadata.META_DURATION,metadata.getDurationAsTimeCode());
	addMediaItemMetadata(l,VideoMetadata.META_VIDEO_FORMAT,metadata.getVideoFormat());
	addMediaItemMetadata(l,VideoMetadata.META_FRAMES_PER_SECOND,metadata.getFramesPerSecond());
	addMediaItemMetadata(l,VideoMetadata.META_AUDIO_FORMAT,metadata.getAudioFormat());
	
	this.addMediaItemMetadata(metadata,item,l);
	
	return l.size() < 1 ? null : (MediaItemMetadata[])
		l.toArray(new MediaItemMetadata[l.size()]);
}

/**
 * Get a VideoMetadata instance for a MediaItem.
 * 
 * @param item the item to get the meta instance for
 * @return VideoMetadata
 */
protected abstract VideoMetadata getVideoMetadataInstance(MediaItem item);

/**
 * This method will be called form {@link #getMetadataForItem(MediaItem)} to
 * allow for custom MediaItemMetadata objects to be added.
 * 
 * @param meta the video metadata object
 * @param item the media item
 * @param list the list of MediaItemMetadata objects to add to
 */
protected abstract void addMediaItemMetadata(VideoMetadata meta, MediaItem item, List list);

}
