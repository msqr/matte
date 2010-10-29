/* ===================================================================
 * ElevationMap.java
 * 
 * Created Apr 12, 2004 1:27:00 PM
 * 
 * Copyright (c) 2004 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: BumpMap.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.image.effect;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MediaRequestHandlerParams;
import magoffin.matt.ma.image.ImageEffect;
import magoffin.matt.ma.image.ImageMediaRequestHandler;
import magoffin.matt.ma.image.ImageMediaRequestHandlerParams;
import magoffin.matt.ma.util.Geometry;

import org.apache.log4j.Logger;

import com.sun.glf.goodies.DirectionalLight;
import com.sun.glf.goodies.ElevationMap;
import com.sun.glf.goodies.LightOp;
import com.sun.glf.goodies.LitSurface;

/**
 * Use a grayscale image to render an lighting effect elevation map
 * onto the source image.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class BumpMap implements ImageEffect
{
	public static final double[] DEFAULT_LIGHT = {-1,-1,1};
	
	public static final float DEFAULT_INTENSITY = 1.0f;
	
	public static final Color DEFAULT_COLOR = Color.WHITE;
	
	public static final int DEFAULT_ELEVATION_SCALE = 1;
	
	private static final Logger LOG = Logger.getLogger(BumpMap.class);
	
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

	LOG.debug("Setting bump map watermark...");
	
	// create a new buffered image the same size as out output image
	BufferedImage tmp = new BufferedImage(geometry.getWidth(),
			geometry.getHeight(), source.getType());
	
	// paint the watermark image into tmp image
	Graphics2D tmp2D = tmp.createGraphics();
	tmp2D.drawImage(watermark,wmX,wmY,null);
	tmp2D.dispose();
	
	// create bump map op
	DirectionalLight light = new DirectionalLight(DEFAULT_LIGHT,DEFAULT_INTENSITY,
			DEFAULT_COLOR);
	ElevationMap texture = new ElevationMap(tmp,true,
			DEFAULT_ELEVATION_SCALE);
	LitSurface litSurface = new LitSurface(0);
	litSurface.addLight(light);
	litSurface.setElevationMap(texture);
	LightOp lightOp = new LightOp(litSurface);
	
	bufferedOpList.add(lightOp);
	return source;
}


/* (non-Javadoc)
 * @see magoffin.matt.util.ResetableObject#reset()
 */
public void reset()
{
	// nada
}

}
