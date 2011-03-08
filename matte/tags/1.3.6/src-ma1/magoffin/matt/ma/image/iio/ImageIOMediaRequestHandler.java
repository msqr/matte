/* ===================================================================
 * ImageIOMediaRequestHandler.java
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
 * $Id: ImageIOMediaRequestHandler.java,v 1.1 2006/06/03 22:26:19 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.image.iio;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.apache.log4j.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MediaRequestHandlerParams;
import magoffin.matt.ma.image.AbstractImageMediaRequestHandler;
import magoffin.matt.ma.util.Geometry;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.util.StringUtil;

/**
 * Abstract base class for image media handlers utilizing the Image IO API.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:19 $
 */
public abstract class ImageIOMediaRequestHandler
extends AbstractImageMediaRequestHandler 
{
	/** The Image IO format name for the 1.0 "standard" format. */
	public static final String FORMAT_NAME_STANDARD_1_0 = "javax_imageio_1.0";
	
	private static final Logger LOG = Logger.getLogger(ImageIOMediaRequestHandler.class);

/**
 * Get an ImageReader for the given InputStream for the given MediaItem.
 * 
 * <p>This method will create an ImageInputStream from the given InputStream.</p>
 * 
 * @param in the InputStream of the image to decode
 * @param item the MediaItem to get an ImageReader for
 * @return an ImageReader set to the InputStream <em>in</em>
 * @throws MediaAlbumException
 */
protected ImageReader getReaderForItem(InputStream in, MediaItem item) throws MediaAlbumException
{
	Iterator readers = ImageIO.getImageReadersByMIMEType(item.getMime());
	if ( !readers.hasNext() ) {
		throw new MediaAlbumException("MIME type '" +item.getMime() +"' not registered for reading in Image IO");
	}
	ImageReader reader = (ImageReader)readers.next();
	try {
		ImageInputStream iis = ImageIO.createImageInputStream(in);
		reader.setInput(iis);
		return reader;
	} catch ( IOException e ) {
		throw new MediaAlbumException("IOException reading image input stream for " +item.getPath(),e);
	}
}

protected ImageReader getReaderForFile(File f) throws MediaAlbumException
{
	Iterator readers = ImageIO.getImageReadersBySuffix(StringUtil.substringAfter(
			f.getName(),'.'));
	if ( !readers.hasNext() ) {
		throw new MediaAlbumException("File type '" +f.getName() +"' not registered for reading in Image IO");
	}
	ImageReader reader = (ImageReader)readers.next();
	try {
		ImageInputStream iis = ImageIO.createImageInputStream(
				new BufferedInputStream(new FileInputStream(f)));
		reader.setInput(iis);
		return reader;
	} catch ( IOException e ) {
		LOG.error("IOException getting ImageIO reader for file " +f.getAbsolutePath());
		throw new MediaAlbumException("IOException reading image input stream for " 
				+f.getName(),e);
	}
}

/**
 * Get an ImageWriter for the given OutputStream for the given MediaItem.
 * 
 * <p>This method will create an ImageOutputStream from the given OutputStream.</p>
 * 
 * @param out the OutputStream to encode the image to
 * @param item the MediaItem to get an ImageWriter for
 * @return an ImageWriter set to the OutputStream <em>in</em>
 * @throws MediaAlbumException
 */
protected final ImageWriter getWriterForItem(OutputStream out, MediaItem item) throws MediaAlbumException
{
	ImageWriter writer = this.getWriterForItem(item);
	if ( writer == null ) {
		throw new MediaAlbumException("Can't get ImageWriter for item " +item.getItemId() +" (MIME "
			+item.getMime() +")");
	}
	try {
		ImageOutputStream ios = ImageIO.createImageOutputStream(out);
		writer.setOutput(ios);
		return writer;
	} catch ( IOException e ) {
		throw new MediaAlbumException("IOException reading image output stream for " +item.getPath(),e);
	}
}

/**
 * Get an image writer for a specific item.
 * 
 * <p>This method will be called by {@link #getWriterForItem(OutputStream, MediaItem)}
 * and will return an ImageWriter registered for <var>item</var>'s MIME type, as 
 * defined by {@link MediaItem#getMime()}. Media handler implementations that wish
 * to generate an output type different than their native type can override this method.</p>
 * 
 * @param item the item to get a writer for
 * @return an ImageWriter, or <em>null</em> if none available
 */
protected ImageWriter getWriterForItem(MediaItem item)
{
	Iterator writers = ImageIO.getImageWritersByMIMEType(item.getMime());
	if ( !writers.hasNext() ) {
		return null;
	}
	return (ImageWriter)writers.next();
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
	ImageReader reader = getReaderForFile(f);
	try {
		return reader.read(0);
	} catch (IOException e) {
		throw new MediaAlbumException("IOException reading image data from file "
				+f.getName(),e);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.image.ImageMediaRequestHandler#getBufferedImage(magoffin.matt.ma.xsd.MediaItem, java.io.InputStream, magoffin.matt.ma.MediaRequestHandlerParams)
 */
public BufferedImage getBufferedImage(MediaItem item, InputStream in, MediaRequestHandlerParams params)
throws MediaAlbumException {
	Geometry geometry = getGeometry(params);
	
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Reading buffered image: ID = "
			+item.getItemId() 
			+", dimensions = " +item.getWidth() +"x" +item.getHeight() 
			+", output dimensions = " +geometry.getWidth() +"x" +geometry.getHeight() );
	}
	
	int itemWidth = item.getWidth().intValue();
	int itemHeight = item.getHeight().intValue();
	int width;// = geometry.getWidth();
	int height;// = geometry.getHeight();
	
	if ( params.hasParamSet(ROTATE_DEGREES_KEY) ) {
		width = geometry.getHeight();
		height = geometry.getWidth();
	} else {
		width = geometry.getWidth();
		height = geometry.getHeight();
	}
	
	// read in a reduced number of pixels if possible to conserve memory
	ImageReader reader = null;
	BufferedImage image = null;

	try {
		reader = this.getReaderForItem(in,item);
		ImageReadParam param = reader.getDefaultReadParam();
		if ( width == itemWidth && height == itemHeight ) {
			// not changing size
			image = reader.read(0);
		} else {
			// changing size
			if (param.canSetSourceRenderSize() ) {
				param.setSourceRenderSize(new Dimension(width, height));
				return reader.read(0,param);
			}
			
			// read in sub-sampled image if size < 1/2 original
			int periodX = (int)Math.floor(itemWidth / (double)width / 2.0d );
			int periodY = (int)Math.floor(itemHeight / (double)height / 2.0d );
			if ( periodX < 1 ) {
				periodX = 1;
			}
			if ( periodY < 1 ) {
				periodY = 1;
			}
			if ( LOG.isDebugEnabled() ) {
				LOG.debug("Source sampling = " +periodX +"x" +periodY);
			}
			param.setSourceSubsampling(periodX,periodY,0,0);
			image = reader.read(0,param);
		}
	} catch ( IOException e ) {
		throw new MediaAlbumException("IOException reading image data for item "
			+item.getPath(),e);
	} finally {
		if ( reader != null ) {
			reader.dispose();
		}
	}
		
	return image;
}

protected String debugMetadata(Node root) {
	StringBuffer buf = new StringBuffer();
    debugMetadata(root, buf, 0);
    return buf.toString();
}

private void indent(int level, StringBuffer buf) {
    for (int i = 0; i < level; i++) {
            buf.append("  ");
    }
} 

private void debugMetadata(Node node, StringBuffer buf, int level) {
    indent(level,buf); // emit open tag
    buf.append("<").append(node.getNodeName());
    NamedNodeMap map = node.getAttributes();
    if (map != null) { // print attribute values
            int length = map.getLength();
            for (int i = 0; i < length; i++) {
                    Node attr = map.item(i);
                    buf.append(" ").append(attr.getNodeName()).append(
                                     "=\"").append( attr.getNodeValue()).append("\"");
            }
    }

    Node child = node.getFirstChild();
    if (child != null) {
            buf.append(">\n"); // close current tag
            while (child != null) { // emit child tags recursively
                    debugMetadata(child, buf, level + 1);
                    child = child.getNextSibling();
            }
            indent(level,buf); // emit close tag
            buf.append("</").append(node.getNodeName()).append(">\n");
    } else {
            buf.append("/>\n");
    }
}

}
