/* ===================================================================
 * TestImageScale.java
 * 
 * Created Dec 27, 2003 5:54:36 PM
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
 * $Id: TestImageScale.java,v 1.1 2006/06/03 22:26:19 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.test;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

/**
 * Test IIO image scaling.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:19 $
 */
public class TestImageScale {
	
	private static final Object[] RENDER_VALUES = new Object[] {
		   null,
		   RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR,
		   RenderingHints.VALUE_INTERPOLATION_BILINEAR,
		   RenderingHints.VALUE_INTERPOLATION_BICUBIC
			};

	private static final Map RENDER_DISPLAY_NAMES = new HashMap(6);
	static {
		RENDER_DISPLAY_NAMES.put(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR,"VALUE_INTERPOLATION_NEAREST_NEIGHBOR");
		RENDER_DISPLAY_NAMES.put(RenderingHints.VALUE_INTERPOLATION_BILINEAR,"VALUE_INTERPOLATION_BILINEAR");
		RENDER_DISPLAY_NAMES.put(RenderingHints.VALUE_INTERPOLATION_BICUBIC,"VALUE_INTERPOLATION_BICUBIC");
	}

private static ImageReader getJpegReader(InputStream in) throws Exception
{
	Iterator readers = ImageIO.getImageReadersByMIMEType("image/jpeg");
	if ( !readers.hasNext() ) {
		throw new Exception("MIME type 'image/jpeg' not registered for reading in Image IO");
	}
	ImageReader reader = (ImageReader)readers.next();
	ImageInputStream iis = ImageIO.createImageInputStream(in);
	reader.setInput(iis);
	return reader;
}
	
private static ImageWriter getJpegWriter() throws Exception
{
	Iterator writers = ImageIO.getImageWritersByMIMEType("image/jpeg");
	if ( !writers.hasNext() ) {
		throw new Exception("MIME type 'image/jpeg' not registered for writing in Image IO");
	}
	return (ImageWriter)writers.next();
}

private static void writeJpegStream(BufferedImage image, int quality, OutputStream out)
throws Exception
{
	System.out.println("Writing JPEG stream: quality = " +quality
				+", dimensions = " +image.getWidth() +"x" +image.getHeight());
	ImageWriter writer = null;
	try {
		writer =  getJpegWriter();
		ImageOutputStream ios = ImageIO.createImageOutputStream(out);
		writer.setOutput(ios);
		
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
	System.out.println( "Finished JPEG stream");
}

private static BufferedImage scaleImage(BufferedImage image, int width, int height, Object renderHint ) 
{
	BufferedImage alteredImage = new BufferedImage(width, height, image.getType());
	
	Graphics2D graphics2D = alteredImage.createGraphics();
	
	if ( renderHint != null ) {
		System.out.println("Render hint = " +RENDER_DISPLAY_NAMES.get(renderHint));
		graphics2D.setRenderingHint(
				RenderingHints.KEY_INTERPOLATION,
				renderHint);
	}
	
	AffineTransform tx = new AffineTransform();
	double scale = (double)width / (double)image.getWidth();
	if ( scale < 1.0d ) {
		tx.scale(scale,scale);
	}
	
	System.out.println("Resizing image to " +width +"x"	+height +" (" +scale +")");

	long start = System.currentTimeMillis();
	graphics2D.drawImage(image,tx,null);
	long diff = System.currentTimeMillis() - start;
	System.out.println("Image scale complete in " +diff +"ms");
	
	graphics2D.dispose();
	
	return alteredImage;
}

private static BufferedImage getBufferedImageForSize(
		InputStream in, 
		int inputWidth, 
		int inputHeight, 
		int width, 
		int height, 
		Object renderHint) 
throws Exception
{
	System.out.println("Reading buffered image: dimensions = "
				+inputWidth +"x" +inputHeight 
				+", output dimensions = " +width +"x" +height );
	
	// read in a reduced number of pixels if possible to conserve memory
	ImageReader reader = null;
	BufferedImage image = null;
	
	try {
		reader = getJpegReader(in);
		ImageReadParam param = reader.getDefaultReadParam();
		if ( width != inputWidth || height != inputHeight ) {
			// changing size
			if (param.canSetSourceRenderSize() ) {
				param.setSourceRenderSize(new Dimension(width, height));
				return reader.read(0,param);
			}
			
			// read in sub-sampled image if size < 1/2 original
			int periodX = (int)Math.floor(inputWidth / (double)width / 2.0d );
			int periodY = (int)Math.floor(inputHeight / (double)height / 2.0d );
			if ( periodX < 1 ) {
				periodX = 1;
			}
			if ( periodY < 1 ) {
				periodY = 1;
			}
			System.out.println("Source sampling = " +periodX +"x" +periodY);

			param.setSourceSubsampling(periodX,periodY,0,0);
			
			image = reader.read(0,param);
			
			image = scaleImage(image,width,height,renderHint);			
		}
	} finally {
		if ( reader != null ) {
			reader.dispose();
		}
	}
	
	return image;
}

private static final String[][] DEFAULT_ARGS = {
	{"slow1.jpg","1608","980","640","390"},
	{"fast1.jpg","1944","2592","360","480"},
	{"slow2.jpg","1944","2592","360","480"},
	{"fast2.jpg","1600","1200","640","480"},
};
	
public static void main(String[] args) {
	String[][] data = null;
	
	if ( args.length < 5 ) {
		data = DEFAULT_ARGS;
	} else {
		data = new String[][] {args};
	}
	
	try {
		for ( int i = 0; i < data.length; i++ ) {
			String path = data[i][0];
			int inWidth = Integer.parseInt(data[i][1]);
			int inHeight = Integer.parseInt(data[i][2]);
			int outWidth = Integer.parseInt(data[i][3]);
			int outHeight = Integer.parseInt(data[i][4]);
			
			System.out.println("Scaling image " +path);
			
			for ( int j = 0; j < RENDER_VALUES.length; j++ ) {
			
				InputStream in = new BufferedInputStream(new FileInputStream(path));
				BufferedImage image = getBufferedImageForSize(
						in,inWidth,inHeight,outWidth,outHeight,
						RENDER_VALUES[j]);
				
				String outPath = "s"+j+"-"+path;
				
				System.out.println("Writing scaled image to " +outPath);
				
				OutputStream out = new BufferedOutputStream(new FileOutputStream(outPath));
				
				writeJpegStream(image,100,out);
				
				System.out.println("");
			
			}
		}
	} catch ( Exception e ) {
		System.err.println("Exception:  " +e);
		e.printStackTrace(System.err);
		System.exit(1);
	}
}
}
