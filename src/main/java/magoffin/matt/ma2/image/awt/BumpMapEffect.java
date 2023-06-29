/* ===================================================================
 * BumpMapEffect.java
 * 
 * Created Sep 10, 2007 2:03:24 PM
 * 
 * Copyright (c) 2007 Matt Magoffin.
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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageReader;

import magoffin.matt.ma2.MediaEffect;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.image.ImageMediaHelper;

import org.springframework.core.io.Resource;

import com.sun.glf.goodies.DirectionalLight;
import com.sun.glf.goodies.ElevationMap;
import com.sun.glf.goodies.LightOp;
import com.sun.glf.goodies.LitSurface;

/**
 * A watermark effect for AWT based processing, that creates a 3D bump map
 * from the watermark image.
 *
 * @author matt
 * @version 1.0
 */
public class BumpMapEffect extends BaseAwtMediaEffect {
	
	/**
	 * The key for this effect.
	 */
	public static final String BUMP_MAP_KEY = "image.awt.bump." +MediaEffect.KEY_WATERMARK;

	/** Default value for the {@code light} property. */
	public static final double[] DEFAULT_LIGHT = {-1,-1,1};
	
	/** Default value for the {@code intensity} property. */
	public static final float DEFAULT_INTENSITY = 1.0f;
	
	/** Default value for the {@code color} property. */
	public static final Color DEFAULT_COLOR = Color.WHITE;
	
	/** Default value for the {@code elevationScale} property. */
	public static final int DEFAULT_ELEVATION_SCALE = 1;

	private ImageMediaHelper imageMediaHelper = null;
	private Color color = DEFAULT_COLOR;
	private Integer elevationScale = DEFAULT_ELEVATION_SCALE;
	private Float intensity = DEFAULT_INTENSITY;
	private double[] light = DEFAULT_LIGHT;
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.image.awt.AwtMediaEffect#applyEffect(magoffin.matt.ma2.domain.MediaItem, magoffin.matt.ma2.MediaRequest, java.awt.image.BufferedImage)
	 */
	public BufferedImage applyEffect(MediaItem item, MediaRequest request,
			BufferedImage source) {
		Resource watermarkResource = (Resource)request.getParameters().get(
				MediaEffect.MEDIA_REQUEST_PARAM_WATERMARK_RESOURCE);
		if ( watermarkResource == null || !watermarkResource.exists() ) {
			return source;
		}
		File watermarkFile = null;
		BufferedImage watermark = null;
		try {
			watermarkFile = watermarkResource.getFile();
			ImageReader reader = imageMediaHelper.getReaderForFile(watermarkFile);
			watermark = reader.read(0);
		} catch ( IOException e ) {
			throw new RuntimeException(e);
		}
		int wmX = source.getWidth() - watermark.getWidth();
		int wmY = source.getHeight() - watermark.getHeight();

		if ( log.isDebugEnabled() ) {
			log.debug("Setting bump map watermark for item [" +request.getMediaItemId() +']');
		}
		
		// create a new buffered image the same size as out output image
		BufferedImage tmp = new BufferedImage(source.getWidth(),
				source.getHeight(), source.getType());
		
		// paint the watermark image into tmp image
		Graphics2D tmp2D = tmp.createGraphics();
		tmp2D.drawImage(watermark,wmX,wmY,null);
		tmp2D.dispose();
		
		// create bump map op
		DirectionalLight dirLight = new DirectionalLight(light, intensity, color);
		ElevationMap texture = new ElevationMap(tmp, true, elevationScale);
		LitSurface litSurface = new LitSurface(0);
		litSurface.addLight(dirLight);
		litSurface.setElevationMap(texture);
		LightOp lightOp = new LightOp(litSurface);
		
		BufferedImage result = lightOp.filter(source, null);
		
		if ( log.isDebugEnabled() ) {
			log.debug("Bump map watermark complete for item [" +request.getMediaItemId() +']');
		}
		
		return result;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaEffect#getKey()
	 */
	public String getKey() {
		return BUMP_MAP_KEY;
	}

	/**
	 * @return the imageMediaHelper
	 */
	public ImageMediaHelper getImageMediaHelper() {
		return imageMediaHelper;
	}

	/**
	 * @param imageMediaHelper the imageMediaHelper to set
	 */
	public void setImageMediaHelper(ImageMediaHelper imageMediaHelper) {
		this.imageMediaHelper = imageMediaHelper;
	}

	/**
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * @return the elevationScale
	 */
	public Integer getElevationScale() {
		return elevationScale;
	}

	/**
	 * @param elevationScale the elevationScale to set
	 */
	public void setElevationScale(Integer elevationScale) {
		this.elevationScale = elevationScale;
	}

	/**
	 * @return the intensity
	 */
	public Float getIntensity() {
		return intensity;
	}

	/**
	 * @param intensity the intensity to set
	 */
	public void setIntensity(Float intensity) {
		this.intensity = intensity;
	}

	/**
	 * @return the light
	 */
	public double[] getLight() {
		return light;
	}

	/**
	 * @param light the light to set
	 */
	public void setLight(double[] light) {
		this.light = light;
	}

}
