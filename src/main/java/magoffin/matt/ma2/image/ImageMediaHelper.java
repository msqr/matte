/* ===================================================================
 * ImageMediaHelper.java
 * 
 * Created Jan 12, 2007 4:33:01 PM
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

package magoffin.matt.ma2.image;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.apache.log4j.Logger;

import magoffin.matt.ma2.domain.MediaItem;

/**
 * Helper class for dealing with ImageIO-based images.
 * 
 * <p>This class exists so the functionality can be shared between other 
 * MediaHandler implementations outside the .image package, which may need
 * to render images from their media. Video handlers, for example, can use
 * this class to render image previews for their video files.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public class ImageMediaHelper {
	
	private final Logger log = Logger.getLogger(getClass());

	/**
	 * Get a BufferedImage from an Image.
	 * 
	 * @param image the image to get a BufferedImage for
	 * @return BufferedImage
	 */
	public BufferedImage getBufferedImage(Image image) {
		
		if (image instanceof BufferedImage) {
			return (BufferedImage)image;
		}
		
		// ASSUME ALL PIXELS IN IMAGE ARE LOADED

		// Determine if the image has transparent pixels; for this method's
		// implementation, see e665 Determining If an Image Has Transparent Pixels
		boolean hasAlpha = hasAlpha(image);

		// Create a buffered image with a format that's compatible with the screen
		BufferedImage bimage = null;

		// Create a buffered image using the default color model
		int type = BufferedImage.TYPE_INT_RGB;
		
		if (hasAlpha) {
			type = BufferedImage.TYPE_INT_ARGB;
		}
		bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);


		// Copy image to buffered image
		Graphics g = bimage.createGraphics();

		// Paint the image onto the buffered image
		g.drawImage(image, 0, 0, null);
		g.dispose();

		return bimage;
	}
	
	/**
	 * Returns true if the specified image has transparent pixels.
	 * 
	 * @param image the Image to test
	 * @return true if the image has transparent pixels
	 */ 
	public boolean hasAlpha(Image image) {
		// If buffered image, the color model is readily available
		if (image instanceof BufferedImage) {
			BufferedImage bimage = (BufferedImage)image;
			return bimage.getColorModel().hasAlpha();
		}

		// Use a pixel grabber to retrieve the image's color model;
		// grabbing a single pixel is usually sufficient
		PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			// ignore
		}

		// Get the image's color model
		ColorModel cm = pg.getColorModel();
		return (cm != null ? cm.hasAlpha() : false);
	}
	
	/**
	 * Get an image writer for a specific MIME type.
	 * 
	 * <p>This method will be called by {@link #getWriterForMIME(OutputStream, String)}
	 * and will return an ImageWriter registered for <var>item</var>'s MIME type, as 
	 * defined by {@link MediaItem#getMime()}. Media handler implementations that wish
	 * to generate an output type different than their native type can override this method.</p>
	 * 
	 * @param mime the MIME type to get a ImageWriter for
	 * @return an ImageWriter, or <em>null</em> if none available
	 */
	public ImageWriter getWriterForMIME(String mime) {
		Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType(mime);
		if ( !writers.hasNext() ) {
			return null;
		}
		return writers.next();
	}

	/**
	 * Get an ImageWriter for the given OutputStream for the given MIME type.
	 * 
	 * <p>This method will create an ImageOutputStream from the given OutputStream.</p>
	 * 
	 * @param out the OutputStream to encode the image to
	 * @param mime the MIME type to get an ImageWriter for
	 * @return an ImageWriter set to the OutputStream <em>in</em>
	 */
	public ImageWriter getWriterForMIME(OutputStream out, String mime) {
		ImageWriter writer = this.getWriterForMIME(mime);
		if ( writer == null ) {
			throw new RuntimeException("Can't get ImageWriter for MIME type [" 
					+mime +"]"); 
		}
		try {
			ImageOutputStream ios = ImageIO.createImageOutputStream(out);
			writer.setOutput(ios);
			return writer;
		} catch ( IOException e ) {
			throw new RuntimeException("IOException creating image output stream for [" 
					+mime +"]",e);
		}
	}

	/**
	 * Get an ImageReader for a File (based on file extension).
	 * 
	 * @param file the file to get the reader for
	 * @return the ImageReader
	 */
	public ImageReader getReaderForFile(File file) {
		String extension = file.getName().substring(file.getName().lastIndexOf('.')+1);
		Iterator<ImageReader> readers = ImageIO.getImageReadersBySuffix(extension);
		if ( !readers.hasNext() ) {
			throw new RuntimeException("File type '" +file.getName() 
					+"' not registered for reading in Image IO");
		}
		ImageReader reader = readers.next();
		try {
			ImageInputStream iis = ImageIO.createImageInputStream(
					new BufferedInputStream(new FileInputStream(file)));
			reader.setInput(iis);
			return reader;
		} catch ( IOException e ) {
			log.error("IOException getting ImageIO reader for file " +file.getAbsolutePath());
			throw new RuntimeException("IOException reading image input stream for " 
					+file.getName(),e);
		}
	}
	
	/**
	 * Get an ImageReader for the given InputStream for the given MIME type.
	 * 
	 * <p>This method will create an ImageInputStream from the given InputStream.</p>
	 * 
	 * @param mime the MIME to get an ImageReader for
	 * @param in the InputStream of the image to decode
	 * @return an ImageReader set to the InputStream <em>in</em>
	 */
	public ImageReader getReaderForMIME(String mime, InputStream in) {
		Iterator<ImageReader> readers = ImageIO.getImageReadersByMIMEType(mime);
		if ( !readers.hasNext() ) {
			throw new RuntimeException("MIME type [" +mime 
					+"] not registered for reading in ImageIO");
		}
		ImageReader reader = readers.next();
		try {
			ImageInputStream iis = ImageIO.createImageInputStream(in);
			reader.setInput(iis);
			return reader;
		} catch ( IOException e ) {
			throw new RuntimeException("IOException reading image input stream for [" 
					+mime +"]",e);
		}
	}

	/**
	 * Encode a JPEG image to an OutputStream with the specified quality.
	 * 
	 * @param image the image to encode as a JPEG stream
	 * @param itemId the media item ID being written (for logging only)
	 * @param quality integer between 0 and 100, with 100 being the higest quality
	 * @param out the output stream
	 */
	public void writeJpegStream(BufferedImage image, Long itemId, int quality, OutputStream out) {
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
			log.debug("Writing JPEG stream: ID = " +itemId
				+", quality = " +quality
				+", dimensions = " +image.getWidth() +"x" +image.getHeight());
		}
		ImageWriter writer = null;
		try {
			writer =  this.getWriterForMIME(out, ImageConstants.JPEG_MIME);
			ImageWriteParam param = writer.getDefaultWriteParam();
			param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			param.setCompressionQuality(quality / 100.0f);
			
			IIOImage iioi = new IIOImage(image,null,null);
			writer.write(null,iioi,param);
		} catch ( IOException e ) {
			throw new RuntimeException(e);
		} finally {
			if ( writer != null ) {
				writer.dispose();
			}
		} 
		if ( log.isDebugEnabled() ) {
			log.debug( "Finished JPEG stream: ID = " +itemId);
		}
	}

}
