/* ===================================================================
 * JpegMediaRequestHandler.java
 *
 * Copyright (c) 2003 Matt Magoffin.
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
 * $Id: JpegMediaRequestHandler.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.image.jmagick;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import magick.ImageInfo;
import magick.MagickException;
import magick.MagickImage;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MediaMetadata;
import magoffin.matt.ma.MediaRequestHandlerParams;
import magoffin.matt.ma.image.ImageMetadata;
import magoffin.matt.ma.image.exif.ExifMetadata;
import magoffin.matt.ma.image.exif.ExifMetadataUtil;
import magoffin.matt.ma.util.PoolFactory;
import magoffin.matt.ma.xsd.MediaAlbumConfig;
import magoffin.matt.ma.xsd.MediaHandlerConfig;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.util.ArrayUtil;

/**
 * MediaRequestHandler implementation for JPEG images.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class JpegMediaRequestHandler extends JMagickMediaRequestHandler {

	/** The JPEG image MIME type: <code>image/jpeg</code>. */
	public static final String JPEG_MIME = "image/jpeg";
	
	private Map exifMakeMap = null;
	
/*
 * @see magoffin.matt.ma.MediaRequestHandler#init(MediaHandlerConfig)
 */
public void init(MediaHandlerConfig config, PoolFactory pf, MediaAlbumConfig appConfig) throws MediaAlbumException 
{
	super.init(config,pf, appConfig);
	exifMakeMap = ExifMetadataUtil.getExifMakeMap(config);
}


/*
 * @see magoffin.matt.ma.image.AbstractImageMediaRequestHandler#getImageMetadata(magoffin.matt.ma.xsd.MediaItem)
 */
protected ImageMetadata getImageMetadataInstance(MediaItem item) 
{
	return new ExifMetadata();
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.image.AbstractImageMediaRequestHandler#addMediaItemMetadata(magoffin.matt.ma.image.ImageMetadata, magoffin.matt.ma.xsd.MediaItem, java.util.List)
 */
protected void addMediaItemMetadata(ImageMetadata meta, MediaItem item, List list) 
{
	// no custom attributes to add
}


/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandler#setMediaItemParameters(java.io.File, magoffin.matt.ma.xsd.MediaItem)
 */
public MediaMetadata setMediaItemParameters(File mediaFile, MediaItem item)
throws MediaAlbumException 
{
	ImageInfo info = null;	//InputStream in = null;
	
	try {
		info = new ImageInfo(mediaFile.getAbsolutePath());
		if ( info.getSize() != null ) {
			// set width, height
			String[] dimensions = ArrayUtil.split(info.getSize(),'x',2);
			item.setWidth( Integer.valueOf(dimensions[0]) );
			item.setHeight( Integer.valueOf(dimensions[1]) );
		} else {
			// load image and see
			MagickImage image = new MagickImage(info);
			Dimension dim = image.getDimension();
			item.setWidth(new Integer((int)dim.getWidth()));
			item.setHeight(new Integer((int)dim.getHeight()));
		}

		
		MediaMetadata meta = ExifMetadataUtil.getJpegMetadata(mediaFile,exifMakeMap);

		if ( meta != null ) {
			// set creation date
			item.setCreationDate(meta.getCreationDate());
		}
		
		return meta;
	} catch (MagickException e) {
		throw new MediaAlbumException("MagickException reading image info for " +item,e);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandler#getOutputMime(magoffin.matt.ma.xsd.MediaItem, magoffin.matt.ma.MediaRequestHandlerParams)
 */
public String getOutputMime(
	MediaItem item,
	MediaRequestHandlerParams params) 
{
	return JPEG_MIME;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandler#writeMedia(magoffin.matt.ma.xsd.MediaItem, java.io.OutputStream, java.io.InputStream, magoffin.matt.ma.MediaRequestHandlerParams)
 */
public void writeMedia(
	MediaItem item,
	OutputStream out,
	InputStream in,
	MediaRequestHandlerParams params)
	throws MediaAlbumException, IOException 
{
	jmagickHandleRequest(item,params);
}

}
