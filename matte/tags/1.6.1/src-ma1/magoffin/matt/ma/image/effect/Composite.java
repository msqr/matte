/* ===================================================================
 * Watermark.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Apr 12, 2004 8:45:34 AM.
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
 * $Id: Composite.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.image.effect;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.io.File;
import java.util.List;

import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MediaRequestHandlerParams;
import magoffin.matt.ma.image.ImageEffect;
import magoffin.matt.ma.image.ImageMediaRequestHandler;
import magoffin.matt.ma.image.ImageMediaRequestHandlerParams;
import magoffin.matt.ma.util.Geometry;

import org.apache.log4j.Logger;

/**
 * Apply watermark effect to image.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class Composite implements ImageEffect {
	
	private static final Logger LOG = Logger.getLogger(Composite.class);
	
	/**
	 * BufferedImageOp to perform the composite operation.
	 * 
	 * @author Matt Magoffin (spamsqr@msqr.us)
	 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
	 */
	private static class CompositeOp implements BufferedImageOp {
		
		private BufferedImage image;
		private AlphaComposite comp;
		private int x;
		private int y;
		
		public CompositeOp(BufferedImage image, int x, int y, AlphaComposite comp) {
			this.image = image;
			this.comp = comp;
			this.x = x;
			this.y = y;
		}
		
		/* (non-Javadoc)
		 * @see java.awt.image.BufferedImageOp#createCompatibleDestImage(java.awt.image.BufferedImage, java.awt.image.ColorModel)
		 */
		public BufferedImage createCompatibleDestImage(BufferedImage src,
				ColorModel destCM) {
			return src;
		}
		
		/* (non-Javadoc)
		 * @see java.awt.image.BufferedImageOp#filter(java.awt.image.BufferedImage, java.awt.image.BufferedImage)
		 */
		public BufferedImage filter(BufferedImage src, BufferedImage dest) {
			LOG.debug("Applying composite...");
			BufferedImage outImage = dest;
			if ( dest == null ) {
				outImage = new BufferedImage(src.getWidth(),src.getHeight(),
						src.getType() > 0 ? src.getType() : BufferedImage.TYPE_INT_RGB);
			}
			Graphics2D g = outImage.createGraphics();
			if ( dest == null ) {
				g.drawImage(src,0,0,null);
			}
			g.setComposite(comp);
			g.drawImage(image,x,y,null);
			g.dispose();
			LOG.debug("Composite applied");
			return outImage;
		}
		
		/* (non-Javadoc)
		 * @see java.awt.image.BufferedImageOp#getBounds2D(java.awt.image.BufferedImage)
		 */
		public Rectangle2D getBounds2D(BufferedImage src) {
			return new Rectangle(src.getWidth(),src.getHeight());
		}
		
		/* (non-Javadoc)
		 * @see java.awt.image.BufferedImageOp#getPoint2D(java.awt.geom.Point2D, java.awt.geom.Point2D)
		 */
		public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
			if ( dstPt != null ) {
				dstPt.setLocation(srcPt);
			}
			return srcPt;
		}
		
		/* (non-Javadoc)
		 * @see java.awt.image.BufferedImageOp#getRenderingHints()
		 */
		public RenderingHints getRenderingHints() {
			return null;
		}
}
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.image.ImageEffect#applyEffect(magoffin.matt.ma.image.ImageMediaRequestHandler, magoffin.matt.ma.image.ImageMediaRequestHandlerParams, java.awt.image.BufferedImage, java.util.List)
 */
public BufferedImage applyEffect(ImageMediaRequestHandler handler,
		ImageMediaRequestHandlerParams params, BufferedImage source,
		List bufferedOpList) throws MediaAlbumException 
{
	if ( !params.hasParamSet(MediaRequestHandlerParams.WATERMARK) ) return source;
	File watermarkFile = (File)params.getParam(MediaRequestHandlerParams.WATERMARK);
	BufferedImage watermark = handler.getBufferedImage(watermarkFile);
	Geometry geometry = handler.getGeometry(params);
	int wmX = geometry.getWidth() - watermark.getWidth();
	int wmY = geometry.getHeight() - watermark.getHeight();
	BufferedImageOp op = new CompositeOp(watermark,wmX,wmY,
			AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
	bufferedOpList.add(op);
	return source;
}

/* (non-Javadoc)
 * @see magoffin.matt.util.ResetableObject#reset()
 */
public void reset() {
	// nada
}

}
