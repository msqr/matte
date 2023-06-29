/* ===================================================================
 * AbstractImageMediaRequestHandler.java
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
 * $Id: AbstractImageMediaRequestHandler.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.image;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MediaAlbumRuntimeException;
import magoffin.matt.ma.MediaRequestHandlerParams;
import magoffin.matt.ma.image.effect.BumpMap;
import magoffin.matt.ma.image.effect.Composite;
import magoffin.matt.ma.image.effect.Rotate;
import magoffin.matt.ma.image.effect.Scale;
import magoffin.matt.ma.util.BasicFileMediaRequestHandler;
import magoffin.matt.ma.util.Geometry;
import magoffin.matt.ma.util.MediaSpecUtil;
import magoffin.matt.ma.util.PoolFactory;
import magoffin.matt.ma.util.WorkScheduler;
import magoffin.matt.ma.xsd.MediaAlbumConfig;
import magoffin.matt.ma.xsd.MediaHandlerConfig;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.ma.xsd.MediaItemMetadata;
import magoffin.matt.util.FileUtil;

import org.apache.commons.pool.ObjectPool;
import org.apache.log4j.Logger;

/**
 * Abstract base class for image media handlers.
 * 
 * <p>Created Oct 16, 2002 7:55:46 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public abstract class AbstractImageMediaRequestHandler
extends BasicFileMediaRequestHandler implements ImageMediaRequestHandler
{
	private static final Logger LOG = Logger.getLogger(AbstractImageMediaRequestHandler.class);
	
	private ObjectPool GEOMETRY_POOL = null;
		
	private ObjectPool IMRHP_POOL = null;
	
	private ObjectPool ARRAY_LIST_POOL = null;
	
	protected ImageEffectPoolFactory EFFECT_POOL_FACTORY = null;

	public static final String IMRHP_GEOMETRY_KEY = "_geo";
	
	public static final String EFFECTS_KEY = "_ie";
	
	public static final String THUMBNAIL_KEY = "_t";
	
	public static final String ROTATE_DEGREES_KEY = "_ro";
	
	public static final Integer ROTATE_90_CW = new Integer(90);
	
	public static final Integer ROTATE_90_CCW = new Integer(-90);
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandler#init(magoffin.matt.ma.xsd.MediaHandlerConfig, magoffin.matt.ma.util.PoolFactory)
 */
public void init(MediaHandlerConfig config, PoolFactory pf, MediaAlbumConfig appConfig) throws MediaAlbumException 
{
	super.init(config,pf,appConfig);
	GEOMETRY_POOL = pf.getPoolInstance(Geometry.class);
	IMRHP_POOL = pf.getPoolInstance(ImageMediaRequestHandlerParams.class);
	ARRAY_LIST_POOL = pf.getPoolInstance(ArrayList.class);
	EFFECT_POOL_FACTORY = ImageEffectPoolFactory.getInstance();
}


/**
 * Returns a ImageMediaRequestHandlerParams object.
 * 
 * @see magoffin.matt.ma.MediaRequestHandler#getParamInstance()
 */
public MediaRequestHandlerParams getParamInstance() 
{
	try {
		return (ImageMediaRequestHandlerParams)IMRHP_POOL.borrowObject();
	} catch ( Exception e ) {
		LOG.error("Unable to get pooled ImageMediaRequestHandlerParams object: " +e.getMessage(),e);
	}
	return new ImageMediaRequestHandlerParams();
}


private Geometry borrowGeometry()
{
	try {
		return (Geometry)GEOMETRY_POOL.borrowObject();
	} catch ( Exception e ) {
		LOG.error("Unable to get pooled Geometry object: " +e.getMessage(),e);
	}
	return new Geometry();
}


private void returnGeometry(Geometry geometry)
{
	try {
		GEOMETRY_POOL.returnObject(geometry);
	} catch ( Exception e ) {
		LOG.error("Unable to return Geometry object to pool:" +e.getMessage(),e);
	}
}

protected List borrowArrayList()
{
	try {
		return (List)ARRAY_LIST_POOL.borrowObject();
	} catch ( Exception e ) {
		LOG.error("Unable to get pooled List object: " +e.getMessage(),e);
	}
	return new ArrayList();
}


protected void returnArrayList(List list)
{
	try {
		ARRAY_LIST_POOL.returnObject(list);
	} catch ( Exception e ) {
		LOG.error("Unable to return Geometry object to pool:" +e.getMessage(),e);
	}
}


/**
 * @see magoffin.matt.ma.MediaRequestHandler#getCacheKey(magoffin.matt.ma.xsd.MediaItem, magoffin.matt.ma.MediaRequestHandlerParams)
 */
public String getCacheKey(
	MediaItem item,
	MediaRequestHandlerParams params) 
{
	try {
	
		Geometry geometry = getGeometry(params);
		
		String key = "img" +item.getItemId() + '_' +geometry.getWidth() + "x" +geometry.getHeight()
			+"_" +getQuality(params);
					
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Generated cache key for item " +item.getItemId() +": " +key);
		}
		return key;
	} catch ( Exception e ) {
		LOG.error("Exception getting cache key for item " +item.getItemId() +": " +e.getMessage(),e);
	}
	return null;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandler#isCacheKey(magoffin.matt.ma.xsd.MediaItem, java.lang.String)
 */
public boolean isCacheKey(MediaItem item, String key) {
	String test = "img"+item.getItemId()+"_";
	return key.startsWith(test);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.image.ImageMediaRequestHandler#getQuality(magoffin.matt.ma.MediaRequestHandlerParams)
 */
public int getQuality(MediaRequestHandlerParams params)
{
	int quality = 100;
	
	Object o = params.getParam(MediaRequestHandlerParams.COMPRESSION);
	if ( o != null ) {
		quality = MediaSpecUtil.getCompressionValue(o.toString()).intValue();
	} else {
		o = params.getParam(ImageMediaRequestHandlerParams.QUALITY);
		if ( o != null ) {
			if ( o instanceof Integer ) {
				return ((Integer)o).intValue();
			}
			
			try {
				quality = Integer.parseInt(o.toString());
			} catch ( NumberFormatException e ) {
				if ( LOG.isDebugEnabled() ) {
					LOG.debug("Can't get quality setting from: " +o);
				}
			}
		} else {
			quality = MediaSpecUtil.getCompressionValue(
				MediaSpecUtil.COMPRESS_NORMAL).intValue();
			if ( LOG.isDebugEnabled() ) {
				LOG.debug("Compression not specified, defaulting to " 
					+MediaSpecUtil.COMPRESS_NORMAL);
			}	
		}
	}
	quality = Math.max(0, Math.min(quality, 100));
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Using quality " +quality);
	}
	
	// overwrite the quality with the integer version
	params.setParam(ImageMediaRequestHandlerParams.QUALITY,String.valueOf(quality));
	return quality;
}

/**
 * Set the ImageEffect objects onto the MediaRequestHandlerParams for
 * the current request.
 * 
 * <p>Note this uses pooled ImageEffect objects, which <em>must</em> be
 * returned to their pool when finished. This is accomplised in 
 * the {@link #postProcessParams(MediaItem, MediaRequestHandlerParams)}
 * method, so if you override that method be sure to call it from your 
 * implementation, i.e. <code>super.postProcessParams(...)</code>.</p>
 * 
 * @param item the media item
 * @param params the request parameters
 */
protected void setEffectsForRequest(MediaItem item, MediaRequestHandlerParams params)
{
	List effectList = borrowArrayList();
	
	try {
		Geometry geo = getGeometry(params);
		if ( geo.getWidth() != item.getWidth().intValue() || geo.getHeight() != 
			item.getHeight().intValue() ) {
			effectList.add(EFFECT_POOL_FACTORY.borrowEffect(Scale.class));
		}
		
		if ( params.hasParamSet(ROTATE_DEGREES_KEY) ) {
			Rotate r = (Rotate)EFFECT_POOL_FACTORY.borrowEffect(Rotate.class);
			r.setDegrees((Integer)params.getParam(ROTATE_DEGREES_KEY));
			effectList.add(r);
		}

		// look for watermark
		if ( params.hasParamSet(MediaRequestHandlerParams.WATERMARK) ) {
			if ( !isThumbnail(params) ) {
				// only watermark non-thumbnail images
				Class effectClass = BumpMap.class;
				// check for params
				if ( params.hasParamSet(MediaRequestHandlerParams.WATERMARK_PARAM) ) {
					String[] wmp = (String[])params.getParam(
							MediaRequestHandlerParams.WATERMARK_PARAM);
					for ( int i = 0; i < wmp.length; i++ ) {
						if ( ApplicationConstants.WATERMARK_PARAM_OVERLAY.equals(wmp[i])) {
							effectClass = Composite.class;
						}
					}
				}
				effectList.add(EFFECT_POOL_FACTORY.borrowEffect(effectClass));
			}
		}
		
		// future effects processing here
		
		if ( effectList != null ) {
			ImageEffect[] effects = (ImageEffect[])effectList.toArray(
					new ImageEffect[effectList.size()]);
			params.setParam(EFFECTS_KEY,effects);
		}
	} finally {
		returnArrayList(effectList);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandler#getOutputGeometry(magoffin.matt.ma.xsd.MediaItem, magoffin.matt.ma.MediaRequestHandlerParams)
 */
public Geometry getOutputGeometry(MediaItem item,
		MediaRequestHandlerParams params) 
{
	if ( !params.hasParamSet(IMRHP_GEOMETRY_KEY) ) {
		throw new MediaAlbumRuntimeException("Geometry not availble for item "
				+item.getItemId());
	}
	Geometry geo = (Geometry)params.getParam(IMRHP_GEOMETRY_KEY);
	
	// clone since geo comes from pool
	geo = new Geometry(
			geo.getWidth(),
			geo.getHeight(),
			geo.getMode());
	return geo;
}
	


/**
 * This method generates a Geometry out of the request params and places that
 * object on <var>params</var> at key {@link #IMRHP_GEOMETRY_KEY}.
 * 
 * <p><strong>Note:</strong> the Geometry object will be obtained by calling
 * {@link #borrowGeometry()}, as such it should be released by calling
 * {@link #returnGeometry(Geometry)} later.</p>
 * 
 * <p>In addition, if the desired image size is determined to be 
 * a thumbnail size, the handler param key {@link #THUMBNAIL_KEY} will 
 * be set to {@link Boolean#TRUE}.</p>
 *  * @param item the media item for the current request * @param params the params for the current request */
private void setGeometryForRequest(MediaItem item, MediaRequestHandlerParams params)
{
	int imageWidth;// = item.getWidth().intValue();
	int imageHeight;// = item.getHeight().intValue();

	if ( params.hasParamSet(ROTATE_DEGREES_KEY) ) {
		imageWidth = item.getHeight().intValue();
		imageHeight = item.getWidth().intValue();
	} else {
		imageWidth = item.getWidth().intValue();
		imageHeight = item.getHeight().intValue();
	}

	int desiredWidth = imageWidth;
	int desiredHeight = imageHeight;
	
	int w = desiredWidth;
	int h = desiredHeight;
	
	// get a borrowed geometry object and place onto the params object
	Geometry geometry = borrowGeometry();
	if ( params.hasParamSet(IMRHP_GEOMETRY_KEY) ) {
		// return the old geometry
		returnGeometry((Geometry)params.getParam(IMRHP_GEOMETRY_KEY));
	}
	params.setParam(IMRHP_GEOMETRY_KEY,geometry);
	
	// set up desired w, h
	
	Object o = params.getParam(MediaRequestHandlerParams.SIZE);
	if ( o != null ) {
		// use pre-set width/height
		String sizeName = o.toString();
		int[] size = MediaSpecUtil.getWidthHeightValue(sizeName);
		if ( params.hasParamSet(ROTATE_DEGREES_KEY) ) {
			desiredWidth = size[1];
			desiredHeight = size[0];
		} else {
			desiredWidth = size[0];
			desiredHeight = size[1];
		}
		if ( MediaSpecUtil.isThumbnailSize(sizeName) ) {
			params.setParam(THUMBNAIL_KEY,Boolean.TRUE);
		}
	} else {
		if ( params.getParam(ImageMediaRequestHandlerParams.SET_WIDTH) != null ) {
			// forced width / height
			o = params.getParam(ImageMediaRequestHandlerParams.SET_WIDTH);
			if ( o != null ) {
				Object o2 = params.getParam(ImageMediaRequestHandlerParams.SET_HEIGHT);
				if ( o2 != null ) {
					// got SET width and height, so force to whatever supplied and return
					try {
						if ( params.hasParamSet(ROTATE_DEGREES_KEY) ) {
							geometry.setHeight(Integer.parseInt(o.toString()));
							geometry.setWidth(Integer.parseInt(o2.toString()));
						} else {
							geometry.setWidth(Integer.parseInt(o.toString()));
							geometry.setHeight(Integer.parseInt(o2.toString()));
						}
						geometry.setMode(Geometry.EXACT);
						if ( LOG.isDebugEnabled() ) {
							LOG.debug("Using set WxH: " +geometry.getWidth() +"x" +geometry.getHeight());
						}
						return;
					} catch ( NumberFormatException e) {
						if ( LOG.isDebugEnabled() ) {
							LOG.debug("Bad set-width/set-height: " +o +"x" +o2);
						}
					}
				}
			}
		}
		
		// fall back on max width / height
		o = params.getParam(ImageMediaRequestHandlerParams.MAX_WIDTH); 
		if ( o != null ) {
			try {
				desiredWidth = Integer.parseInt(o.toString());
			} catch ( NumberFormatException e ) {
				if ( LOG.isDebugEnabled() ) {
					LOG.debug("Bad width number: " +o);
				}
			}
		}
		o = null;
		o = params.getParam(ImageMediaRequestHandlerParams.MAX_HEIGHT); 
		if ( o != null ) {
			try {
				desiredHeight = Integer.parseInt(o.toString()); 
			} catch ( NumberFormatException e ) {
				if ( LOG.isDebugEnabled() ) {
					LOG.debug("Bad height number: " +o);
				}
			}
		}
		
		if ( params.hasParamSet(ROTATE_DEGREES_KEY) ) {
			int tmp = desiredHeight;
			desiredHeight = desiredWidth;
			desiredWidth = tmp;
		}
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("desired WxH: " +desiredWidth +"x" +desiredHeight);
		}
	}
	
	if ( desiredWidth > imageWidth ) {
		desiredWidth = imageWidth;
	}
	if ( desiredHeight > imageHeight ) {
		desiredHeight = imageHeight;
	}
	
	// now calculate w, h
	
	if ( desiredWidth != imageWidth || desiredHeight != imageHeight ) {
		
		if ( geometry.getMode() != Geometry.EXACT ) {
		
			double imageRatio = (double)imageWidth / (double)imageHeight;
	
			if ( desiredWidth != imageWidth && desiredHeight != imageHeight ) {
				// determine thumbnail size from WIDTH and HEIGHT
			    double ratio = (double)desiredWidth / (double)desiredHeight;
			    if ( LOG.isDebugEnabled() ) {
			    	LOG.debug("Desired ratio = " +ratio +"; image ratio = " +imageRatio);
			    }
				if ( imageRatio > ratio) {
					h = (int)Math.round(desiredWidth / imageRatio);
				} else {
					w = (int)Math.round(desiredHeight * imageRatio);
				}
			} else {
				if ( desiredHeight == imageHeight )  {
					// only specified width, so scale desired with to ratio of width
					h = (int)Math.round(desiredWidth / imageRatio);
					if ( LOG.isDebugEnabled() ) {
						LOG.debug("Desired height not specified, setting to " +desiredHeight);
					}
				} else {
					// only specified height, so scale desired with to ratio of height
					w = (int)Math.round(desiredHeight * imageRatio);
					if ( LOG.isDebugEnabled() ) {
						LOG.debug("Desired width not specified, setting to " +desiredWidth);
					}
				}
			}
		}
	}
	
	// last check for rounding over
	if ( w > desiredWidth ) {
		w = desiredWidth;
	}
	if ( h > desiredHeight ) {
		h = desiredHeight;
	}
	
	geometry.setWidth(w);
	geometry.setHeight(h);

	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Output dimensions: " +geometry);
	}
}


private void setRotationForRequest(MediaItem item, MediaRequestHandlerParams params)
{
	if ( params.hasParamSet(ROTATE_DEGREES_KEY) ) {
		return;
	}
	int orientation = -1;
	if ( item.getMetadataCount() > 0 ) {
		int size = item.getMetadataCount();
		for ( int i = 0; i < size; i++ ) {
			MediaItemMetadata meta = item.getMetadata(i);
			if ( ImageMetadata.META_ORIENTATION.equals(meta.getKey()) ) {
				try {
					orientation = Integer.parseInt(meta.getContent());
					break;
				} catch ( NumberFormatException e ) {
					LOG.warn("Unable to parse orientation setting: " +meta.getContent());
				}
			}
		}
	} else if ( item.getMeta() != null ) {
		ImageMetadata meta = getImageMetadataInstance(item);
		if ( meta != null ) {
			meta.deserializeFromString(item.getMeta());
			if ( meta.getOrientationSetting() != null ) {
				try {
					orientation = Integer.parseInt(meta.getOrientationSetting());
				} catch ( NumberFormatException e ) {
					LOG.warn("Unable to parse orientation setting: " +meta.getOrientationSetting());
				}
			}
		}
	}
	if ( orientation > 0 ) {
		switch ( orientation ) {
			case 6:
			case 8:
				if ( item.getWidth().intValue() < item.getHeight().intValue() ) {
					// guess already rotated
					orientation = -1;
				} else {
					// stash on params so don't have to calculate again
					params.setParam(ROTATE_DEGREES_KEY, orientation == 6
							? ROTATE_90_CCW : ROTATE_90_CW );
				}
				break;
				
			default:
				// don't change
				orientation = -1;
				break;
		}
	}
}


/**
 * Returns true if the specified image has transparent pixels.
 */ 
protected static boolean hasAlpha(Image image) {
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
	}

	// Get the image's color model
	ColorModel cm = pg.getColorModel();
	return (cm != null ? cm.hasAlpha() : false);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.image.ImageMediaRequestHandler#isThumbnail(magoffin.matt.ma.MediaRequestHandlerParams)
 */
public boolean isThumbnail(MediaRequestHandlerParams params) {
	return params.hasParamSet(THUMBNAIL_KEY);
}

/**
 * Get a BufferedImage from an Image.
 * 
 * @param image
 * @return BufferedImage
 */
protected BufferedImage getBufferedImage(Image image)
{
	
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
 * Method that uses Java2D to scale an image.
 * 
 * @param image
 * @param geometry
 * @return BufferedImage
 */
protected BufferedImage scaleImage(BufferedImage image, Geometry geometry ) 
{
	BufferedImage alteredImage = new BufferedImage(geometry.getWidth(),
		geometry.getHeight(), image.getType());
	Graphics2D graphics2D = alteredImage.createGraphics();
	graphics2D.setRenderingHint(
		RenderingHints.KEY_INTERPOLATION,
		RenderingHints.VALUE_INTERPOLATION_BICUBIC);

	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Resizing image to " +geometry.getWidth() +"x"
				+geometry.getHeight());
	}

	graphics2D.drawImage(image, 0, 0, geometry.getWidth(),
		geometry.getHeight(), null);

	LOG.debug("Image scale complete.");
	
	graphics2D.dispose();
	
	return alteredImage;
}

/**
 * Handle a media request.
 * 
 * <p>This method will operate in the following way:</p>
 * 
 * <ol>
 * <li>Call {@link #needToAlter(MediaItem, MediaRequestHandlerParams)}</li>
 * <li>If that returns <em>false</em> then copy the input stream to the 
 * output stream and return <em>null</em>.</li>
 * <li>Otherwise, schedule work via {@link MediaRequestHandlerParams#getWorkBiz()}</li>
 * <li>Call {@link ImageMediaRequestHandler#getBufferedImage(MediaItem, InputStream, MediaRequestHandlerParams)}</li>
 * <li>Call {@link #hasEffects(MediaRequestHandlerParams)}</li>
 * <li>If that returns <em>false</em> then return buffered image.</li>
 * <li>Otherwise, call {@link #applyEffects(BufferedImage, ImageMediaRequestHandlerParams, ImageEffect[])}
 * and return the result of that.</li>
 * </ol>
 * 
 * @param item the media item
 * @param out the output stream
 * @param in the input stream
 * @param params the request params
 * @return BufferedImage if any changes were processed, <em>null</em> otherwise
 * @throws MediaAlbumException if an error occurs
 */
protected BufferedImage defaultHandleRequest( MediaItem item, OutputStream out,
		InputStream in, MediaRequestHandlerParams params)
throws MediaAlbumException
{
	try {
		if ( needToAlter(item,params) ) {
			setEffectsForRequest(item,params);
			
			WorkScheduler scheduler = null;
			try {
				ImageMediaRequestHandlerParams iParams = (ImageMediaRequestHandlerParams)
						params;
		
				scheduler = params.getWorkBiz().schedule(iParams);
				BufferedImage result = getBufferedImage(item, in, params);
				
				if ( hasEffects(params) ) {
					ImageEffect[] effects = getEffects(params);
					result = applyEffects(result,iParams,effects);
				}
				
				return result;
				
			} finally {
				if ( scheduler != null ) {
					params.getWorkBiz().done(scheduler);
				}
			}
			
		} else {
			
			// no altering needed, simply stream the original file back, no need to alter
			if ( LOG.isDebugEnabled() ) {
				LOG.debug("Returning unaltered stream " +item.getPath());
			}
			FileUtil.copy(in,out,false,false);
			return null;
		}
		
	} catch (Exception e) {
		throw new MediaAlbumException(e.getMessage(),e);
	}

}

protected BufferedImage applyEffects(BufferedImage image, ImageMediaRequestHandlerParams params, ImageEffect[] effects)
throws MediaAlbumException
{
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Applying " +effects.length + " effects...");
	}
	
	List opList = borrowArrayList();
	BufferedImage result = image;
	try {
		for ( int i = 0; i < effects.length; i++ ) {
			result = effects[i].applyEffect(this,params,result,opList);
		}
		if ( opList.size() > 0 ) {
			// apply ops... 
			int numOps = opList.size();
			for ( int i = 0; i < numOps; i++ ) {
				BufferedImageOp op = (BufferedImageOp)opList.get(i);
				if ( LOG.isDebugEnabled() ) {
					LOG.debug("Applying BufferedImageOp " +op.getClass().getName());
				}
				result = op.filter(result,null);
			}
		}
	} finally {
		returnArrayList(opList);
	}
	
	LOG.debug("Effects complete.");
	
	return result;
}

/**
 * @see magoffin.matt.ma.MediaRequestHandler#postProcessParams(magoffin.matt.ma.xsd.MediaItem, magoffin.matt.ma.MediaRequestHandlerParams)
 */
public void preProcessParams(
	MediaItem item,
	MediaRequestHandlerParams params)
{
	setRotationForRequest(item,params);
	setGeometryForRequest(item,params);
	setEffectsForRequest(item,params);
}

/**
 * @see magoffin.matt.ma.MediaRequestHandler#preProcessParams(magoffin.matt.ma.xsd.MediaItem, magoffin.matt.ma.MediaRequestHandlerParams)
 */
public void postProcessParams(
	MediaItem item,
	MediaRequestHandlerParams params) 
{
	try {
		// return the geometry to the pool
		GEOMETRY_POOL.returnObject(params.getParam(IMRHP_GEOMETRY_KEY));
	} catch ( Exception e ) {
		LOG.error("Unable to return Geometry object to pool: " +e.getMessage(),e);
	}
	
	try {
		// return the params to the pool
		IMRHP_POOL.returnObject(params);
	} catch ( Exception e ) {
		LOG.error("Unable to return MediaRequestHandlerParams object to pool:" +e.getMessage(),e);
	}
	
	if ( params.hasParamSet(EFFECTS_KEY) ) {
		// return effects to pool
		ImageEffect[] effects = (ImageEffect[])params.getParam(EFFECTS_KEY);
		for ( int i = 0; i < effects.length; i++ ) {
			EFFECT_POOL_FACTORY.returnEffect(effects[i]);
		}
	}
}


/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandler#getMetadataForItem(magoffin.matt.ma.xsd.MediaItem)
 */
public MediaItemMetadata[] getMetadataForItem(MediaItem item)
{
	if ( item == null || item.getMeta() == null ) return null;
	ImageMetadata metadata = this.getImageMetadataInstance(item);
	metadata.deserializeFromString(item.getMeta());
	List l = new ArrayList(8);

	addMediaItemMetadata(l,ImageMetadata.META_CAMERA_MAKE,metadata.getCameraMake());
	addMediaItemMetadata(l,ImageMetadata.META_CAMERA_MODEL,metadata.getCameraModel());
	addMediaItemMetadata(l,ImageMetadata.META_SHUTTER_SPEED,metadata.getShutterSpeedAsFractionSecs());
	addMediaItemMetadata(l,ImageMetadata.META_EXPOSURE_TIME,metadata.getExposureTimeAsFractionSecs());
	addMediaItemMetadata(l,ImageMetadata.META_EXPOSURE_BIAS,metadata.getExposureBias());
	addMediaItemMetadata(l,ImageMetadata.META_APERTURE,metadata.getApertureAsFstop());
	addMediaItemMetadata(l,ImageMetadata.META_MAX_APERTURE,metadata.getMaxApertureAsFstop());
	addMediaItemMetadata(l,ImageMetadata.META_FOCAL_LENGTH,metadata.getFocalLength());
	addMediaItemMetadata(l,ImageMetadata.META_FOCAL_LENGTH_35_EQUIV,metadata.getFocalLength35mm());
	addMediaItemMetadata(l,ImageMetadata.META_FLASH,metadata.getFlashSetting());
	addMediaItemMetadata(l,ImageMetadata.META_ORIENTATION,metadata.getOrientationSetting());
	addMediaItemMetadata(l,ImageMetadata.META_COMPRESSION,metadata.getCompressionType());
	
	this.addMediaItemMetadata(metadata,item,l);
	
	return l.size() < 1 ? null : (MediaItemMetadata[])
		l.toArray(new MediaItemMetadata[l.size()]);
}

/**
 * Get an ImageMetadata instance for a MediaItem.
 * 
 * @param item
 * @return ImageMetadata
 */
protected abstract ImageMetadata getImageMetadataInstance(MediaItem item);


/**
 * This method will be called form {@link #getMetadataForItem(MediaItem)} to
 * allow for custom MediaItemMetadata objects to be added.
 * 
 * @param meta
 * @param item
 * @param list
 */
protected abstract void addMediaItemMetadata(ImageMetadata meta, MediaItem item, List list);

/* (non-Javadoc)
 * @see magoffin.matt.ma.image.ImageMediaRequestHandler#getGeometry(magoffin.matt.ma.MediaRequestHandlerParams)
 */
public final Geometry getGeometry(MediaRequestHandlerParams params) {
	return (Geometry)params.getParam(IMRHP_GEOMETRY_KEY);
}

/**
 * Get the rotation, in degrees.
 * @param params the params
 * @return rotation, or <em>null</em> if not defined
 */
protected final Integer getRotation(MediaRequestHandlerParams params) {
	return (Integer)params.getParam(ROTATE_DEGREES_KEY);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.image.ImageMediaRequestHandler#needToAlter(magoffin.matt.ma.xsd.MediaItem, magoffin.matt.ma.MediaRequestHandlerParams)
 */
public boolean needToAlter(MediaItem item, MediaRequestHandlerParams params) {
	// check for want original
	if ( params.hasParamSet(MediaRequestHandlerParams.WANT_ORIGINAL) ) {
		return false;
	}
	
	Geometry geometry = getGeometry(params);
	int quality = getQuality(params);
	if ( quality != 100 || geometry.getWidth() != item.getWidth().intValue() || 
		geometry.getHeight() != item.getHeight().intValue() ) {
		return true;
	}
	
	// check for watermark
	if ( params.hasParamSet(MediaRequestHandlerParams.WATERMARK) ) {
		return true;
	}
	
	// looks like no changing
	return false;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.image.ImageMediaRequestHandler#hasEffects(magoffin.matt.ma.MediaRequestHandlerParams)
 */
public boolean hasEffects(MediaRequestHandlerParams params) {
	return params.hasParamSet(EFFECTS_KEY);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.image.ImageMediaRequestHandler#getEffects(magoffin.matt.ma.MediaRequestHandlerParams)
 */
public ImageEffect[] getEffects(MediaRequestHandlerParams params) {
	return (ImageEffect[])params.getParam(EFFECTS_KEY);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.image.ImageMediaRequestHandler#isRotated(magoffin.matt.ma.MediaRequestHandlerParams)
 */
public boolean isRotated(MediaRequestHandlerParams params) {
	return params.hasParamSet(ROTATE_DEGREES_KEY);
}

}
