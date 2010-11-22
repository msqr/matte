/* ===================================================================
 * BaseIM4JavaMediaHandler.java
 * 
 * Created Oct 21, 2010 10:25:38 AM
 * 
 * Copyright (c) 2010 Matt Magoffin.
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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2.image.im4java;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import magoffin.matt.ma2.MediaEffect;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.MediaResponse;
import magoffin.matt.ma2.MediaSize;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.image.BaseImageMediaHandler;
import magoffin.matt.ma2.support.Geometry;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.IdentifyCmd;
import org.im4java.core.ImageCommand;
import org.im4java.process.ArrayListOutputConsumer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

/**
 * Base implementation of {@link magoffin.matt.ma2.MediaHandler} that uses the JMagick
 * for image processing.
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl class="class-properties">
 *   <dt>im4javaMediaEffectMap</dt>
 *   <dd>A Map of effect keys to {@link IM4JavaMediaEffect} implementations.</dd>
 *   
 *   <dt>thumbnailProfile</dt>
 *   <dd>The +profile setting to use for stripping out profile information
 *   from processed thumbnail images. Defaults to <code>!icm,*</code> which strips out
 *   all profiles except for the color (ICM) profile.</dd>
 *   
 *   <dt>normalProfile</dt>
 *   <dd>The +profile setting to use for stripping out profile information
 *   from processed normal images. Defaults to <code>!icm,*</code> which strips out
 *   all profiles except for the color (ICM) profile.</dd>
 *   
 *   <dt>profileThumbnailSizes</dt>
 *   <dd>A set of {@link MediaSize} for which to treat as thumbnail sizes
 *   and profile with the <code>thumbnailProfile</code> as opposed to 
 *   the <code>normalProfile</code>. Defaults to a set containing
 *   {@link MediaSize#THUMB_SMALL}, {@link MediaSize#THUMB_NORMAL},
 *   and {@link MediaSize#THUMB_BIG} and {@link MediaSize#THUMB_BIGGER}.</dd>
 *   
 *   <dt>useSizeHint</dt>
 *   <dd>If <em>true</em> then use the <code>-size</code> operator with a 
 *   geometry value double the desired output, for efficiently reading large
 *   files while producing smaller copies of them. Defaults to <em>true</em>.
 *   This is the parameter GraphicsMagick uses for this hint, as well as 
 *   older ImageMagick versions.</dd>
 *   
 *   <dt>useJpegSizeHint</dt>
 *   <dd>If <em>true</em> then use the <code>-define jpeg:size=</code> operator
 *   with a geometry value bould the desired output, for efficiently reading
 *   large JPEG files while producing smaller copies of them. This is the value
 *   newer versions of ImageMagick use for this hint, but having it enabled
 *   for GraphicsMagick should not cause any effect. Defaults to <em>true</em>.</dd>
 * </dl>
 *
 * @author matt
 * @version $Revision$ $Date$
 */
public abstract class BaseIM4JavaMediaHandler extends BaseImageMediaHandler {

	/** For thumbnails, remove all profiles except color (ICM). */
	public static final String DEFAULT_THUMBNAIL_PROFILE = "!icm,*";
	
	/** For normal images, remove all profiles except color (ICM). */
	public static final String DEFAULT_NORMAL_PROFILE = DEFAULT_THUMBNAIL_PROFILE;
	
	private Map<String, IM4JavaMediaEffect> im4javaMediaEffectMap;
	private Set<MediaSize> profileThumbnailSizes = EnumSet.of(
			MediaSize.THUMB_BIGGER, MediaSize.THUMB_BIG, 
			MediaSize.THUMB_NORMAL, MediaSize.THUMB_SMALL);
	private String thumbnailProfile = DEFAULT_THUMBNAIL_PROFILE;
	private String normalProfile = DEFAULT_NORMAL_PROFILE;
	private boolean useSizeHint = true;
	private boolean useJpegSizeHint = true;
	
	/**
	 * Construct with a MIME type.
	 * @param mime the MIME type
	 */
	public BaseIM4JavaMediaHandler(String mime) {
		super(mime);
	}

	@Override
	public MediaEffect getEffect(String key, Map<String, ?> effectParameters) {
		return im4javaMediaEffectMap.get(key);
	}

	/**
	 * Basic JMagick implementation of createNewMediaItem.
	 * 
	 * <p>This implementation creates a new MediaItem instance and then
	 * calls {@link #setupBaseItemProperties(MediaItem, File)}
	 * followed by {@link #handleMetadata(MediaRequest, Resource, MediaItem)}.</p>
	 */
	public MediaItem createNewMediaItem(File inputFile) {
		MediaItem item = getDomainObjectFactory().newMediaItemInstance();
		try {
			setupBaseItemProperties(item, inputFile);
			handleMetadata(null, new FileSystemResource(inputFile), item);
		} catch ( IM4JavaException e ) {
			throw new RuntimeException("Exception reading image", e);
		} catch ( InterruptedException e ) {
			throw new RuntimeException("Exception reading image", e);
		} catch ( IOException e ) {
			throw new RuntimeException("Exception reading image", e);
		}
		return item;
	}

	/**
	 * Basic JMagick implementation of handleMediaRequest.
	 * 
	 * <p>This implementation simply calls 
	 * {@link #defaultHandleRequest(MediaItem, MediaRequest, MediaResponse)}.</p>
	 */
	public void handleMediaRequest(MediaItem item, MediaRequest request,
			MediaResponse response) {
		defaultHandleRequest(item, request, response);
	}
	
	/**
	 * Setup some basic properties from an ImageReader.
	 * 
	 * <p>This will set up the item's width, height, and MIME type.</p>
	 * 
	 * @param item the MediaItem to setup
	 * @param inputFile the image file
	 * @throws IM4JavaException if an IM4Java error occurs
	 * @throws InterruptedException if interrupted
	 * @throws IOException if an IO error occurs
	 */
	protected void setupBaseItemProperties(MediaItem item, File inputFile)
	throws IM4JavaException, InterruptedException, IOException {
		ArrayListOutputConsumer result = new ArrayListOutputConsumer();
		IdentifyCmd cmd = new IdentifyCmd();
		IMOperation op = new IMOperation();
		op.ping();
		op.addImage();
		cmd.setOutputConsumer(result);
		if ( log.isTraceEnabled() ) {
			StringWriter writer = new StringWriter();
			cmd.createScript(new PrintWriter(writer), op, new Properties());
			log.debug("Ping command: " +writer.toString());
		}
		cmd.run(op, inputFile.getAbsolutePath());

		if ( result.getOutput().size() > 0 ) {
			// format: test.jpg JPEG 1615x1053+0+0 DirectClass 2.3e+02kb
			Pattern pat = Pattern.compile("\\b(\\d+)x(\\d+)");
			Matcher matcher = pat.matcher(result.getOutput().get(0));
			if ( matcher.find() ) {
				item.setWidth(Integer.parseInt(matcher.group(1)));
				item.setHeight(Integer.parseInt(matcher.group(2)));
			}
		}
		
		// set MIME
		item.setMime(getMime());
	}
	
	/**
	 * Default handler for IM4Java requests.
	 * 
	 * <p>This implementation gets a {@link Resource} via 
	 * {@link MediaBiz#getMediaItemResource(MediaItem)} and passes that 
	 * to {@link #defaultHandleResource(MediaItem, MediaRequest, MediaResponse, Resource)}.</p>
	 * 
	 * @param item the item
	 * @param request the request
	 * @param response the response
	 */
	protected void defaultHandleRequest(MediaItem item, MediaRequest request,
			MediaResponse response) {
		Resource itemResource = getMediaBiz().getMediaItemResource(item);
		defaultHandleResource(item, request, response, itemResource);

	}
	
	/**
	 * Get the MIME type to set in the response.
	 * 
	 * <p>This implementation merely calls {@link #getMime()} but extending classes
	 * may need to override this.</p>
	 * 
	 * @param item the MediaItem being processed
	 * @param request the request
	 * @param itemResource the item resource being processed
	 * @return MIME
	 */
	protected String getResponseMime(
			MediaItem item, 
			MediaRequest request, 
			Resource itemResource) {
		return getMime();
	}

	/**
	 * Default handler for JMagick resource request.
	 * 
	 * <p>This can be used to service {@link magoffin.matt.ma2.MediaHandlerDelegate}
	 * requests, if extending classes wish to support that API.</p>
	 * 
	 * @param item the item
	 * @param request the request
	 * @param response the response
	 * @param itemResource the media resource being operated on
	 */
	protected void defaultHandleResource(MediaItem item, MediaRequest request, 
			MediaResponse response, Resource itemResource) {
		try {
			if ( !needToAlter(item, request) ) {
				defaultHandleRequestOriginal(item, itemResource, request, response);
				return;
			}
			
			// set response MIME
			response.setMimeType(getResponseMime(item, request, itemResource));

			IMOperation effectOperation = new IMOperation();
			request.getParameters().put(IM4JavaMediaEffect.IM_OPERATION, effectOperation);
			
			// set up support for effect command operations
			List<ImageCommandAndOperation> secondaryCommands = new ArrayList<ImageCommandAndOperation>();
			request.getParameters().put(IM4JavaMediaEffect.SUB_COMMAND_LIST, secondaryCommands);
			
			int quality = Math.round(getMediaBiz().getQualityValue(request.getQuality()) * 100.0f);
			Geometry geometry = getMediaBiz().getScaledGeometry(item, request);

			if ( log.isDebugEnabled() ) {
				log.debug("Request output size: " +geometry.toString() +", quality: " +quality);
			}
			
			needToRotate(item, request);
			applyEffects(item, request, response);
			
			// set up base convert command operation
			IMOperation baseOperation = new IMOperation();
			
			// if a scale effect has been used, add a size hint to efficiently read large images
			if ( (useSizeHint || useJpegSizeHint) && (geometry.getWidth() < item.getWidth() 
					|| geometry.getHeight() < item.getHeight()) ) {
				int hintWidth = geometry.getWidth() * 2;
				int hintHeight = geometry.getHeight() * 2;
				if ( useSizeHint ) {
					baseOperation.size(hintWidth, hintHeight);
				}
				if ( useJpegSizeHint ) {
					baseOperation.define("jpeg:size=" +hintWidth +'x' +hintHeight);
				}
			}
			
			baseOperation.quality(Double.valueOf(quality));
			
			// create final full convert operation
			IMOperation op = new IMOperation();
			op.addOperation(baseOperation);
			
			op.addImage();	// source image placeholder
			op.addOperation(effectOperation);
			
			String profileValue = profileThumbnailSizes.contains(request.getSize())
				? thumbnailProfile : normalProfile;
			if ( profileValue != null ) {
				op.p_profile(profileValue);
			}

			// add output image placeholder
			op.addImage();
			
			// set filename for output
			File outFile = null;
			if ( request.getParameters().containsKey(MediaRequest.OUTPUT_FILE_KEY)
					&& secondaryCommands.size() == 0 ) {
				outFile = (File)request.getParameters().get(MediaRequest.OUTPUT_FILE_KEY);
			} else {
				// use temp file
				String extension = secondaryCommands.size() == 0
					? getFileExtension(item, request) : "miff";
				outFile = File.createTempFile("IM4JavaTemp-convert-", "."+extension);
			}
			
			ConvertCmd cmd = new ConvertCmd();
			if ( log.isTraceEnabled() ) {
				debugCommandAndOperation(cmd, op);
			}
			cmd.run(op, itemResource.getFile().getAbsolutePath(), outFile.getAbsolutePath());
			
			for ( Iterator<ImageCommandAndOperation> cmdItr = secondaryCommands.iterator(); cmdItr.hasNext(); ) {
				ImageCommandAndOperation secondaryCmd = cmdItr.next();
				
				File secondaryOutFile = null;
				if ( cmdItr.hasNext() ) {
					// we still have other secondary commands to process, so use a
					// temporary MIFF file to hold the intermediate transformation
					secondaryOutFile = File.createTempFile("IM4JavaTemp-"
							+secondaryCmd.getCommand().getCommand()+"-", ".miff");
				} else if ( request.getParameters().containsKey(MediaRequest.OUTPUT_FILE_KEY) ) {
					secondaryOutFile = (File)request.getParameters().get(MediaRequest.OUTPUT_FILE_KEY);
				} else {
					// use temp file with desired output encoding
					secondaryOutFile = File.createTempFile("IM4JavaTemp-encode-", 
							"."+getFileExtension(item, request));
				}
				
				IMOperation secondaryOp = new IMOperation();
				if ( !cmdItr.hasNext() ) {
					// add base operation for quality setting
					secondaryOp.addOperation(baseOperation);
				}
				secondaryOp.addOperation(secondaryCmd.getOp());
				
				if ( log.isTraceEnabled() ) {
					debugCommandAndOperation(secondaryCmd.getCommand(), secondaryOp);
				}
				secondaryCmd.getCommand().run(secondaryCmd.getOp(),
						outFile.getAbsolutePath(),
						secondaryOutFile.getAbsolutePath());
				
				/*if ( !request.getParameters().containsKey(MediaRequest.OUTPUT_FILE_KEY) ) {
					secondaryOutFile = (File)request.getParameters().get(MediaRequest.OUTPUT_FILE_KEY);
				} else {
					// use temp file with desired output encoding
					secondaryOutFile = File.createTempFile("IM4JavaTemp-encode-", 
							"."+getFileExtension(item, request));
				}*/

				if ( outFile != secondaryOutFile ) {
					outFile.delete();
				}
				outFile = secondaryOutFile;
			}
			
			// if used temp file, then copy to output stream and delete now
			if ( !request.getParameters().containsKey(MediaRequest.OUTPUT_FILE_KEY) ) {
				try {
					FileCopyUtils.copy(new FileInputStream(outFile), response.getOutputStream());
				} finally {
					outFile.delete();
				}
			}
		} catch ( Exception e ) {
			throw new RuntimeException("Exception writing media: "+e, e);
		}
	}
	
	private void debugCommandAndOperation(ImageCommand cmd, IMOperation op) {
		StringWriter writer = new StringWriter();
		cmd.createScript(new PrintWriter(writer), op, new Properties());
		log.trace(cmd.getCommand() +" command: " +writer.toString());
	}

	/**
	 * @return the im4javaMediaEffectMap
	 */
	public Map<String, IM4JavaMediaEffect> getIm4javaMediaEffectMap() {
		return im4javaMediaEffectMap;
	}

	/**
	 * @param im4javaMediaEffectMap the im4javaMediaEffectMap to set
	 */
	public void setIm4javaMediaEffectMap(
			Map<String, IM4JavaMediaEffect> im4javaMediaEffectMap) {
		this.im4javaMediaEffectMap = im4javaMediaEffectMap;
	}

	/**
	 * @return the profileThumbnailSizes
	 */
	public Set<MediaSize> getProfileThumbnailSizes() {
		return profileThumbnailSizes;
	}

	/**
	 * @param profileThumbnailSizes the profileThumbnailSizes to set
	 */
	public void setProfileThumbnailSizes(Set<MediaSize> profileThumbnailSizes) {
		this.profileThumbnailSizes = profileThumbnailSizes;
	}

	/**
	 * @return the thumbnailProfile
	 */
	public String getThumbnailProfile() {
		return thumbnailProfile;
	}

	/**
	 * @param thumbnailProfile the thumbnailProfile to set
	 */
	public void setThumbnailProfile(String thumbnailProfile) {
		this.thumbnailProfile = thumbnailProfile;
	}

	/**
	 * @return the normalProfile
	 */
	public String getNormalProfile() {
		return normalProfile;
	}

	/**
	 * @param normalProfile the normalProfile to set
	 */
	public void setNormalProfile(String normalProfile) {
		this.normalProfile = normalProfile;
	}
	
}
