/* ===================================================================
 * JpegMediaRequestHandler.java
 * 
 * Copyright (c) 2002-2003 Matt Magoffin.
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

package magoffin.matt.ma.image;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MediaMetadata;
import magoffin.matt.ma.MediaRequestHandlerParams;
import magoffin.matt.ma.image.exif.ExifMetadata;
import magoffin.matt.ma.image.exif.ExifMetadataUtil;
import magoffin.matt.ma.util.Geometry;
import magoffin.matt.ma.util.PoolFactory;
import magoffin.matt.ma.util.WorkScheduler;
import magoffin.matt.ma.xsd.MediaAlbumConfig;
import magoffin.matt.ma.xsd.MediaHandlerConfig;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.util.FileUtil;

import org.apache.log4j.Logger;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * MediaRequestHandler implementation for JPEG images using the Sun
 * JPEG codec classes.
 * 
 * <p>Created on Sep 30, 2002 5:04:21 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class JpegMediaRequestHandler extends AbstractImageMediaRequestHandler 
{
	
	/** The JPEG image MIME type: <code>image/jpeg</code>. */
	public static final String JPEG_MIME = "image/jpeg";
	
	private static final Logger log = Logger.getLogger(JpegMediaRequestHandler.class);
	
	private Map exifMakeMap = null;
	
/*
 * @see magoffin.matt.ma.MediaRequestHandler#init(MediaHandlerConfig)
 */
public void init(MediaHandlerConfig config, PoolFactory pf, MediaAlbumConfig appConfig) throws MediaAlbumException 
{
	super.init(config,pf, appConfig);
	
	exifMakeMap = ExifMetadataUtil.getExifMakeMap(config);
}

/**
 * Write a JPEG media stream.
 * 
 * <p>This method will make use of the WorkScheduler supplied
 * with the MediaRequestHandlerParams if it is available, but only if 
 * the output JPEG needs to be altered (resized, re-compressed, etc.).</p>
 * 
 * @see magoffin.matt.ma.MediaRequestHandler#writeMedia(MediaItem, OutputStream, InputStream, MediaRequestHandlerParams)
 */
public void writeMedia(
	MediaItem item,
	OutputStream out,
	InputStream in,
	MediaRequestHandlerParams params)
throws MediaAlbumException, IOException
{
	try {
		Geometry geometry = getGeometry(params);
		int quality = getQuality(params);
		if ( quality != 100 || geometry.getWidth() != item.getWidth().intValue() || 
			geometry.getHeight() != item.getHeight().intValue() ) {
			
			WorkScheduler scheduler = null;
			try {
				scheduler = params.getWorkBiz().schedule(
						(ImageMediaRequestHandlerParams)params);
			
			    JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(in);
			    BufferedImage image = this.scaleImage(
			    	decoder.decodeAsBufferedImage(),
			    	geometry);
				
				this.writeJpegStream(image,quality,out);

			} finally {
				params.getWorkBiz().done(scheduler);
			}
			
		} else {
			
			// no altering needed, simply stream the original file back, no need to alter
			if ( log.isDebugEnabled() ) {
				log.debug("Returning unaltered stream " +item.getPath());
			}
			FileUtil.copy(in,out,false,false);
		}
		
	} catch ( IOException e ) {
		throw e;
	} catch (Exception e) {
		throw new MediaAlbumException(e.getMessage(),e);
	}
}


/**
 * Encode a JPEG image with the specified quality.
 * 
 * @param image the image to encode as a JPEG stream
 * @param quality integer between 0 and 100, with 100 being the higest quality
 * @param out the output stream
 * @throws IOException if an error occurs
 */
protected void writeJpegStream(BufferedImage image, int quality, OutputStream out)
throws IOException
{
	JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
	JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(image);
	param.setQuality(quality / 100.0f, false);
	encoder.setJPEGEncodeParam(param);
	encoder.encode(image);
}


public MediaMetadata setMediaItemParameters(File mediaFile, MediaItem item) throws MediaAlbumException {
	try {
		InputStream in = new BufferedInputStream(new FileInputStream(mediaFile));
	    JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(in);
	    BufferedImage image = decoder.decodeAsBufferedImage();
	    item.setWidth(new Integer(image.getWidth()));
	    item.setHeight(new Integer(image.getHeight()));
	   	   
		MediaMetadata meta = ExifMetadataUtil.getJpegMetadata(mediaFile,exifMakeMap);
		
		if ( meta != null ) {
			item.setCreationDate(meta.getCreationDate());
		}
	    return meta;
	} catch (IOException e) {
		throw new MediaAlbumException("IOException reading stream for item " +item.getPath()
			+": " +e.getMessage(),e);
	}
}

/**
 * Always returns {@link JpegMediaRequestHandler#JPEG_MIME}.
 * 
 * @see magoffin.matt.ma.MediaRequestHandler#getOutputMime(magoffin.matt.ma.xsd.MediaItem, magoffin.matt.ma.MediaRequestHandlerParams)
 */
public String getOutputMime(
	MediaItem item,
	MediaRequestHandlerParams params) 
{
	return JPEG_MIME;
}


/* (non-Javadoc)
 * @see magoffin.matt.ma.image.AbstractImageMediaRequestHandler#getImageMetadataInstance(magoffin.matt.ma.xsd.MediaItem)
 */
protected ImageMetadata getImageMetadataInstance(MediaItem item) 
{
	return new ExifMetadata();
}


/* (non-Javadoc)
 * @see magoffin.matt.ma.image.AbstractImageMediaRequestHandler#addMediaItemMetadata(magoffin.matt.ma.image.ImageMetadata, magoffin.matt.ma.xsd.MediaItem, java.util.List)
 */
protected void addMediaItemMetadata(
	ImageMetadata meta,
	MediaItem item,
	List list) 
{
	// no custom attributes added
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandler#useStreamsForWrite()
 */
public boolean useStreamsForWrite() {
	return true;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.image.ImageMediaRequestHandler#getBufferedImage(java.io.File)
 */
public BufferedImage getBufferedImage(File f) throws MediaAlbumException {
	try {
		InputStream in = new BufferedInputStream(new FileInputStream(f));
		JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(in);
		return decoder.decodeAsBufferedImage();
	} catch ( IOException e ) {
		throw new MediaAlbumException("IOException reading JPEG file " +f.getName(),e);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.image.ImageMediaRequestHandler#getBufferedImage(magoffin.matt.ma.xsd.MediaItem, java.io.InputStream, magoffin.matt.ma.MediaRequestHandlerParams)
 */
public BufferedImage getBufferedImage(MediaItem item, InputStream in,
		MediaRequestHandlerParams params) throws MediaAlbumException {
	try {
		JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(in);
		return decoder.decodeAsBufferedImage();
	} catch ( IOException e ) {
		throw new MediaAlbumException("IOException reading JPEG stream for item " +item.getPath(),e);
	}
}

}
