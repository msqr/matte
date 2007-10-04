/* ===================================================================
 * PngMediaRequestHandler.java
 *
 * Copyright (c) 2003 Matt Magoffin
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
 * $Id: PngMediaRequestHandler.java,v 1.1 2006/06/03 22:26:19 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.image.iio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.xml.transform.TransformerException;

import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MediaMetadata;
import magoffin.matt.ma.MediaRequestHandlerParams;
import magoffin.matt.ma.image.ImageMetadata;
import magoffin.matt.ma.xsd.MediaItem;

import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * MediaRequestHandler implementation for PNG images.
 * 
 * <p>Note that the PNG images are returned as JPEG images when
 * not returning the original image.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:19 $
 */
public class PngMediaRequestHandler extends JpegMediaRequestHandler 
{
	/** The PNG image MIME type: <code>image/png</code>. */
	public static final String PNG_MIME = "image/png";
	
	public static final DateFormat PNG_DATE_FORMAT = new SimpleDateFormat("d MMM yyyy H:mm:ss");
	
	private static final Logger LOG = Logger.getLogger(PngMediaRequestHandler.class);
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.image.AbstractImageMediaRequestHandler#getImageMetadataInstance(magoffin.matt.ma.xsd.MediaItem)
 */
protected ImageMetadata getImageMetadataInstance(MediaItem item) {
	return null;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.image.AbstractImageMediaRequestHandler#addMediaItemMetadata(magoffin.matt.ma.image.ImageMetadata, magoffin.matt.ma.xsd.MediaItem, java.util.List)
 */
protected void addMediaItemMetadata(
	ImageMetadata meta,
	MediaItem item,
	List list) {
	// no custom attributes to add
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandler#setMediaItemParameters(java.io.File, magoffin.matt.ma.xsd.MediaItem)
 */
public MediaMetadata setMediaItemParameters(File mediaFile, MediaItem item)
throws MediaAlbumException 
{
	ImageReader reader = null;
	InputStream in = null;
	
	try {
		in = new FileInputStream(mediaFile);
		reader = this.getReaderForItem(in,item);

		// set width, height
		item.setWidth(new Integer(reader.getWidth(0)));
		item.setHeight(new Integer(reader.getHeight(0)));
		
		// try to get date from PNG tEXT chunk
		IIOMetadata meta = reader.getImageMetadata(0);
		if ( meta != null ) {
			String formatName = meta.getNativeMetadataFormatName();
			Node dom = meta.getAsTree(formatName);
			if ( LOG.isDebugEnabled() ) {
				String str = debugMetadata(dom);
				LOG.debug("ImageIO meta DOM: " +str);
			}
			
			// look for tEXt node, with tEXtEntry[@keyword='Creation Time']
			try {
				Node dateNode = XPathAPI.selectSingleNode(dom,"tEXt/tEXtEntry[@keyword='Creation Time']");
				if ( dateNode != null && dateNode.hasAttributes() ) {
					Element elem = (Element)dateNode;
					String dateStr = elem.getAttribute("value");
					if ( dateStr != null ) {
						Date d = PNG_DATE_FORMAT.parse(dateStr);
						item.setCreationDate(d);
					} else {
						if ( LOG.isDebugEnabled() ) {
							if ( LOG.isDebugEnabled() ) {
								LOG.debug("Creation time node found in PNG metadata, but has no value: " +mediaFile.getPath());
							}
						}
					}
				} else {
					if ( LOG.isDebugEnabled() ) {
						LOG.debug("No creation time node found in PNG metadata " +mediaFile.getPath());
					}
				}
			} catch ( TransformerException e ) {
				if ( LOG.isDebugEnabled() ) {
					LOG.debug("Exception evaluating XPath: " +e.toString());
				}
			} catch ( ParseException e ) {
				if ( LOG.isDebugEnabled() ) {
					LOG.debug("Exception parsing PNG date: " +e.toString());
				}
			}
		}
	
		return null;
	} catch ( IOException e ) {
		throw new MediaAlbumException("IOException reading image input stream for " +item,e);
	} finally {
		if ( in != null ) {
			try {
				in.close();
			} catch ( IOException e ) {
				LOG.warn("Exception closing media file input stream: " +e);
			}
		}
		if ( reader != null ) {
			reader.dispose();
		}
	}
}

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
		return super.getOutputMime(item,params);
	}
	return PNG_MIME;
}

}
