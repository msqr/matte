/* ===================================================================
 * ScaleEffect.java
 * 
 * Created Mar 20, 2006 4:42:41 PM
 * 
 * Copyright (c) 2006 Matt Magoffin (spamsqr@msqr.us)
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

package magoffin.matt.ma2.image.awt;

import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import magoffin.matt.ma2.MediaEffect;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.support.Geometry;

/**
 * Effect that scales an image to the size specified on the request.
 * 
 * <p>Note this effect assumes a rotate effect has not been applied
 * before this effect is applied.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public class ScaleEffect extends BaseAwtMediaEffect {
	
	/**
	 * The default <code>RenderingHints</code> applied to the scale operation.
	 * 
	 * <p>The default hins contain:</p>
	 * 
	 *  <dl class="class-properties">
	 *    <dt><code>RenderingHints.KEY_INTERPOLATION</code></dt>
	 *    <dd><code>RenderingHints.VALUE_INTERPOLATION_BICUBIC</code></dd>
	 *  </dl>
	 */
	public static final RenderingHints DEFAULT_RENDERING_HINTS = new RenderingHints(
			RenderingHints.KEY_INTERPOLATION,
			RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	
	/**
	 * The key for this effect.
	 */
	public static final String SCALE_KEY = "image.awt." +MediaEffect.KEY_SCALE;
	
	private RenderingHints renderingHints = DEFAULT_RENDERING_HINTS;
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.image.awt.AwtMediaEffect#applyEffect(magoffin.matt.ma2.domain.MediaItem, magoffin.matt.ma2.MediaRequest, java.awt.image.BufferedImage)
	 */
	public BufferedImage applyEffect(MediaItem item, MediaRequest request, BufferedImage source) {
		Geometry geometry = getMediaBiz().getScaledGeometry(item, request);
		
		// this assumes rotate has NOT been applied yet!
		
		int width = geometry.getWidth();
		int height = geometry.getHeight();
		
		if ( width != source.getWidth() || height != source.getHeight() ) {	
			double sx = (double)width / (double)source.getWidth();
			double sy = (double)height / (double)source.getHeight();
			
			AffineTransformOp op = new AffineTransformOp(
					AffineTransform.getScaleInstance(sx,sy), renderingHints);
			if ( log.isDebugEnabled() ) {
				log.debug("Applying scale effect on item [" +item.getItemId() +"] from " 
						+item.getWidth() +"x" +item.getHeight()
						+" to " +width +"x" +height);
			}
			BufferedImage result = op.filter(source,null);
			if ( log.isDebugEnabled() ) {
				log.debug("Scale effect complete on item [" +item.getItemId() 
						+"] to " 
						+result.getWidth() +"x" +result.getHeight());
			}
			return result;
		}
		return source;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaEffect#getKey()
	 */
	public String getKey() {
		return SCALE_KEY;
	}

	/**
	 * @return Returns the renderingHints.
	 */
	public RenderingHints getRenderingHints() {
		return renderingHints;
	}
	
	/**
	 * @param renderingHints The renderingHints to set.
	 */
	public void setRenderingHints(RenderingHints renderingHints) {
		this.renderingHints = renderingHints;
	}

}
