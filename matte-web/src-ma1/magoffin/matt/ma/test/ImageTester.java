/* ===================================================================
 * ImageTester.java
 * 
 * Copyright (c) 2002 Matt Magoffin.
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
 * $Id: ImageTester.java,v 1.1 2006/06/03 22:26:19 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.test;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * @author mmagoffi
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ImageTester {

  public static void main(String[] args) throws Exception {
    if (args.length != 5) {
      System.err.println("Usage: java Thumbnail INFILE " +
        "OUTFILE WIDTH HEIGHT QUALITY");
      System.exit(1);
    }
    // load image from INFILE
    BufferedInputStream input = new BufferedInputStream(new FileInputStream(args[0]));
    JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(input);
    BufferedImage image = decoder.decodeAsBufferedImage();
    /*Image image = Toolkit.getDefaultToolkit().getImage(args[0]);
    MediaTracker mediaTracker = new MediaTracker(new Frame());
    mediaTracker.addImage(image, 0);
    mediaTracker.waitForID(0);
    */
    // determine thumbnail size from WIDTH and HEIGHT
    int thumbWidth = Integer.parseInt(args[2]);
    int thumbHeight = Integer.parseInt(args[3]);
    double thumbRatio = (double)thumbWidth / (double)thumbHeight;
    int imageWidth = image.getWidth();
    int imageHeight = image.getHeight();
    double imageRatio = (double)imageWidth / (double)imageHeight;
    if (thumbRatio < imageRatio) {
      thumbHeight = (int)(thumbWidth / imageRatio);
    } else {
      thumbWidth = (int)(thumbHeight * imageRatio);
    }
    // draw original image to thumbnail image object and
    // scale it to the new size on-the-fly
    BufferedImage thumbImage = new BufferedImage(thumbWidth, 
      thumbHeight, BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics2D = thumbImage.createGraphics();
    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
      RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);
    // save thumbnail image to OUTFILE
    BufferedOutputStream out = new BufferedOutputStream(new
      FileOutputStream(args[1]));
    JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
    JPEGEncodeParam param = encoder.
      getDefaultJPEGEncodeParam(thumbImage);
    int quality = Integer.parseInt(args[4]);
    quality = Math.max(0, Math.min(quality, 100));
    param.setQuality(quality / 100.0f, false);
    encoder.setJPEGEncodeParam(param);
    encoder.encode(thumbImage);
    System.out.println("Done.");
    System.exit(0);
  }
}
