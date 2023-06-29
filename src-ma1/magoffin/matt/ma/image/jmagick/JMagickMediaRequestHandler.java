/* ===================================================================
 * JMagickMediaRequestHandler.java
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
 * $Id: JMagickMediaRequestHandler.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.image.jmagick;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import magick.ImageInfo;
import magick.MagickException;
import magick.MagickImage;
import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.MediaRequestHandlerParams;
import magoffin.matt.ma.image.AbstractImageMediaRequestHandler;
import magoffin.matt.ma.image.ImageMediaRequestHandlerParams;
import magoffin.matt.ma.image.jmagick.effect.BumpMap;
import magoffin.matt.ma.image.jmagick.effect.Composite;
import magoffin.matt.ma.image.jmagick.effect.Rotate;
import magoffin.matt.ma.image.jmagick.effect.Zoom;
import magoffin.matt.ma.util.Geometry;
import magoffin.matt.ma.util.WorkScheduler;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.util.FileUtil;

import org.apache.log4j.Logger;

/**
 * Abstract base class for image media handlers utilizing the JMagick API.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public abstract class JMagickMediaRequestHandler
extends AbstractImageMediaRequestHandler 
{
	private static final Logger LOG = Logger.getLogger(JMagickMediaRequestHandler.class);
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.MediaRequestHandler#useStreamsForWrite()
 */
public boolean useStreamsForWrite() {
	return false;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.image.ImageMediaRequestHandler#getBufferedImage(java.io.File)
 */
public BufferedImage getBufferedImage(File f) throws MediaAlbumException {
	throw new UnsupportedOperationException();
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.image.ImageMediaRequestHandler#getBufferedImage(magoffin.matt.ma.xsd.MediaItem, java.io.InputStream, magoffin.matt.ma.MediaRequestHandlerParams)
 */
public BufferedImage getBufferedImage(MediaItem item, InputStream in,
		MediaRequestHandlerParams params) throws MediaAlbumException 
{
	throw new UnsupportedOperationException();
}

/*
protected void doJmagick(MediaItem item, File inFile, File outFile, 
		ImageMediaRequestHandlerParams params, ImageEffect[] effects)
throws MediaAlbumException
{
	Geometry geometry = getGeometry(params);
	int quality = getQuality(params);
	int filterType = isThumbnail(params)
		? FilterType.TriangleFilter 	// for thumbnails this will do
		: FilterType.SincFilter;			// for larger go for better quality
		
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Generating Magick: ID = " +item.getItemId()
				+", quality = " +quality
				+", dimensions = " +geometry
				+", filter = " +FILTER_NAMES[filterType]);
	}
	
	try {
		ImageInfo inInfo = new ImageInfo(inFile.getAbsolutePath());
		inInfo.setQuality(quality);
		
		// read image into memory
		MagickImage image = new MagickImage(inInfo);
		
		// scale image via zoom which uses the defined filter while scaling
		image.setFilter(filterType); // set filter type
		MagickImage scaledImage = image.zoomImage(
			geometry.getWidth(),geometry.getHeight());
		
		// set filename for output
		scaledImage.setFileName(outFile.getAbsolutePath());
		LOG.debug("Quality: " +inInfo.getQuality());
		// write output image
		scaledImage.writeImage(inInfo);
	} catch ( MagickException e ) {
		throw new MediaAlbumException("MagickException writing media: " +e,e);
	} finally {
		// force GC to run otherwise memory can get trashed
		Runtime runtime = Runtime.getRuntime();
		runtime.runFinalization();
		runtime.gc();
	}

	if ( LOG.isDebugEnabled() ) {
		LOG.debug( "Finished Magick: ID = " +item.getItemId() +", " +outFile.getAbsolutePath() );
	}
}
*/

/**
 * Provides a basic way to handle JMagick requests.
 * 
 * <p>This method will perform the following steps:</p>
 * 
 * <ol>
 * <li>Call {@link #needToAlter(MediaItem, MediaRequestHandlerParams)}</li>
 * <li>If that returns <em>false</em> then copy the input file to the 
 * output file and return.</li>
 * <li>Otherwise, schedule work via {@link MediaRequestHandlerParams#getWorkBiz()}</li>
 * <li>Call {@link magoffin.matt.ma.image.ImageMediaRequestHandler#hasEffects(MediaRequestHandlerParams)}</li>
 * <li>If that returns <em>true</em> then call 
 * {@link magoffin.matt.ma.image.ImageMediaRequestHandler#getEffects(MediaRequestHandlerParams)}.</li>
 * <li>Call the {@link #applyEffects(File, File, ImageMediaRequestHandlerParams, JMagickImageEffect[])}
 * method with the requet's input file, output file, and effects array.</li>
 * </ol>
 * 
 * @param item the current media item
 * @param params the current request params
 * @throws MediaAlbumException if an error occurs
 */
protected final void jmagickHandleRequest( MediaItem item, 
		MediaRequestHandlerParams params)
throws MediaAlbumException
{
	File inFile = (File)params.getParam(MediaRequestHandlerParams.INPUT_FILE);
	File outFile = (File)params.getParam(MediaRequestHandlerParams.OUTPUT_FILE);	
	if ( inFile == null || outFile == null ) {
		throw new MediaAlbumException("Input or output file not supplied.");
	}	
	try {
		if ( needToAlter(item,params) ) {
			setEffectsForRequest(item,params);
			
			WorkScheduler scheduler = null;
			try {
				ImageMediaRequestHandlerParams iParams = (ImageMediaRequestHandlerParams)
						params;
		
				scheduler = params.getWorkBiz().schedule(iParams);
				
				JMagickImageEffect[] effects = null;
				if ( hasEffects(params) ) {
					effects = (JMagickImageEffect[])getEffects(params);
				}
				
				// DO JMagick work here
				if ( effects != null ) {
					applyEffects(inFile,outFile,iParams,effects);
				}
				
			} finally {
				if ( scheduler != null ) {
					params.getWorkBiz().done(scheduler);
				}
			}
			
		} else {
			
			// no altering needed, simply copy the original file back, no need to alter
			if ( LOG.isDebugEnabled() ) {
				LOG.debug("Copying unaltered stream " +item.getPath());
			}
			FileUtil.slurp(inFile, new BufferedOutputStream(
					new FileOutputStream(outFile)));
		}
		
	} catch (Exception e) {
		throw new MediaAlbumException(e.getMessage(),e);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.image.AbstractImageMediaRequestHandler#setEffectsForRequest(magoffin.matt.ma.xsd.MediaItem, magoffin.matt.ma.MediaRequestHandlerParams)
 */
protected void setEffectsForRequest(MediaItem item,
		MediaRequestHandlerParams params) 
{
	List effectList = borrowArrayList();
	try {
		Geometry geo = getGeometry(params);
		if ( geo.getWidth() != item.getWidth().intValue() || geo.getHeight() != 
			item.getHeight().intValue() ) {
			effectList.add(EFFECT_POOL_FACTORY.borrowEffect(Zoom.class));
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
			JMagickImageEffect[] effects = (JMagickImageEffect[])effectList.toArray(
					new JMagickImageEffect[effectList.size()]);
			params.setParam(EFFECTS_KEY,effects);
		}
	} finally {
		returnArrayList(effectList);
	}
}

protected void applyEffects(
		File inFile, File outFile,
		ImageMediaRequestHandlerParams params, 
		JMagickImageEffect[] effects)
throws MediaAlbumException
{
	if ( LOG.isDebugEnabled() ) {
		LOG.debug("Applying " +effects.length + " effects...");
	}
	
	int quality = getQuality(params);
	Geometry geometry = getGeometry(params);
	try {
		ImageInfo inInfo = new ImageInfo(inFile.getAbsolutePath());
		inInfo.setQuality(quality);
		inInfo.setSize(geometry.toString());
		
		// read image into memory
		MagickImage image = new MagickImage(inInfo);
		MagickImage result = image;
		
		for ( int i = 0; i < effects.length; i++ ) {
			result = effects[i].applyEffect(this,inInfo,result,params);
		}
		
		// set filename for output
		result.setFileName(outFile.getAbsolutePath());
		LOG.debug("Quality: " +inInfo.getQuality());
		// write output image
		result.writeImage(inInfo);
	} catch ( MagickException e ) {
		throw new MediaAlbumException("MagickException writing media: " +e,e);
	} /*finally { going to try without this... depend on ImageMagick limit
		// force GC to run otherwise memory can get trashed
		Runtime runtime = Runtime.getRuntime();
		runtime.runFinalization();
		runtime.gc();
	}*/
	
	LOG.debug("Effects complete.");
}


}
