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
 * $Id: JpegMediaRequestHandler.java,v 1.1 2006/06/03 22:26:19 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.image.iio;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.imageio.IIOImage;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

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

import org.apache.log4j.Logger;

/**
 * MediaRequestHandler implementation for JPEG images.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:19 $
 */
public class JpegMediaRequestHandler extends ImageIOMediaRequestHandler 
{
	/** The JPEG image MIME type: <code>image/jpeg</code>. */
	public static final String JPEG_MIME = "image/jpeg";
	
	public static final String FORMAT_NAME_IIO_JPEG_1_0 = "javax_imageio_jpeg_image_1.0";
	
	private static final Logger log = Logger.getLogger(JpegMediaRequestHandler.class);
	
	protected Map exifMakeMap = null;
	
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

public MediaMetadata setMediaItemParameters(File mediaFile, MediaItem item) throws MediaAlbumException {
	ImageReader reader = null;
	InputStream in = null;
	
	try {
		in = new FileInputStream(mediaFile);
		reader = this.getReaderForItem(in,item);

		// set width, height
		item.setWidth(new Integer(reader.getWidth(0)));
		item.setHeight(new Integer(reader.getHeight(0)));
	
		MediaMetadata meta = ExifMetadataUtil.getJpegMetadata(mediaFile,exifMakeMap);

		if ( meta != null ) {
			// set creation date
			item.setCreationDate(meta.getCreationDate());
		}
		
		return meta;
	} catch ( IOException e ) {
		throw new MediaAlbumException("IOException reading image input stream for " +item,e);
	} finally {
		if ( in != null ) {
			try {
				in.close();
			} catch ( IOException e ) {
				log.warn("Exception closing media file input stream: " +e);
			}
		}
		if ( reader != null ) {
			reader.dispose();
		}
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandler#getOutputMime(magoffin.matt.ma.xsd.MediaItem, magoffin.matt.ma.MediaRequestHandlerParams)
 */
public String getOutputMime(MediaItem item, MediaRequestHandlerParams params) {
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
	BufferedImage image = defaultHandleRequest(item,out,in,params);
	if ( image != null ) {
		int quality = getQuality(params);
		this.writeJpegStream(image,item,quality,out);
	}
}

/**
 * Encode a JPEG image with the specified quality.
 * 
 * @param image the image to encode as a JPEG stream
 * @param item the item to write
 * @param quality integer between 0 and 100, with 100 being the higest quality
 * @param out the output stream
 * @throws MediaAlbumException if an error occurs
 * @throws IOException if an error occurs
 */
protected void writeJpegStream(BufferedImage image, MediaItem item, int quality, OutputStream out)
throws MediaAlbumException, IOException
{
	// force to INT_RGB
	if ( image.getType() != BufferedImage.TYPE_INT_RGB ) {
		BufferedImage alteredImage = new BufferedImage(image.getWidth(),
				image.getHeight(),BufferedImage.TYPE_INT_RGB);
		Graphics2D g = alteredImage.createGraphics();
		g.drawImage(image,0,0,null);
		g.dispose();
		image = alteredImage;
	}

	if ( log.isDebugEnabled() ) {
		log.debug("Writing JPEG stream: ID = " +item.getItemId()
			+", quality = " +quality
			+", dimensions = " +image.getWidth() +"x" +image.getHeight());
	}
	ImageWriter writer = null;
	try {
		writer =  this.getWriterForItem(out,item);
		ImageWriteParam param = writer.getDefaultWriteParam();
		param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		param.setCompressionQuality(quality / 100.0f);
		
		IIOImage iioi = new IIOImage(image,null,null);
		writer.write(null,iioi,param);
	} finally {
		if ( writer != null ) {
			writer.dispose();
		}
	} 
	if ( log.isDebugEnabled() ) {
		log.debug( "Finished JPEG stream: ID = " +item.getItemId() );
	}
}



} // class JpegMediaRequestHandler
