/* ===================================================================
 * BasicIconBasedMediaHandler.java
 * 
 * Created Jan 30, 2007 6:25:35 PM
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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2.support;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import magoffin.matt.ma2.MediaEffect;
import magoffin.matt.ma2.MediaHandler;
import magoffin.matt.ma2.MediaHandlerDelegate;
import magoffin.matt.ma2.MediaMetadata;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.MediaResponse;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.Metadata;
import magoffin.matt.ma2.image.EmbeddedImageMetadata;
import magoffin.matt.ma2.image.ImageConstants;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

/**
 * Base handler for media types that are icon-based.
 * 
 * <p>This handler provides support for metadata-based images if available, 
 * otherwise an icon is used. For icon resolution, the item's MIME type will
 * be used to search for a PNG image icon using the {@link #getIconResourcePathPrefix()}
 * path as the prefix, <code>.png</code> as a suffix, and the following 
 * search paths will be tried:</p>
 * 
 * <ol>
 *   <li>mime</li>
 *   <li>mime prefix (part of MIME before the / character)</li>
 *   <li>file extension</li>
 * </ol>
 * 
 * <p>For example, for an item with a file name <code>my-video.mpg</code> and with a 
 * MIME type of <code>video/mpeg</code> and the default 
 * <code>iconResourcePathPrefix</code>, the following paths will be tried:</p>
 * 
 * <ol>
 *   <li>classpath:META-INF/icons/video/mpeg.png</li>
 *   <li>classpath:META-INF/icons/video.png</li>
 *   <li>classpath:META-INF/icons/mpg.png</li>
 * </ol>
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl class="class-properties">
 *   <dt>imageDelegateMediaHandler</dt>
 *   <dd>A {@link MediaHandler} implementation to handle the embedded image
 *   of the metadata, if available.</dd>
 *   
 *   <dt>iconResourcePathPrefix</dt>
 *   <dd>A resource prefix path to use for resolving icon images.</dd>
 *   
 *   <dt>resourceLoader</dt>
 *   <dd>A {@link ResourceLoader} to use for loading icon resources. Defaults
 *   to a {@link DefaultResourceLoader} instance.</dd>
 *   
 *   <dt>fallbackIconName</dt>
 *   <dd>The name of an icon resource to use if searching for a media-specific 
 *   version fails. Defaults to {@link #DEFAULT_FALLBACK_ICON_NAME}.</dd>
 * </dl>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class BasicIconBasedMediaHandler extends AbstractMediaHandler {

	/** The {@link MediaMetadata} key for the embedded image MIME type. */
	public static final String EMBEDDED_IMAGE_MIME_TYPE_METADATA_KEY = "EMBEDDED_IMAGE_MIME_TYPE";
	
	/** The default value for the <code>iconResourcePathPrefix</code> property. */
	public static final String DEFAULT_ICON_RESOURCE_PATH_PREFIX = "classpath:META-INF/icons/";
	
	/** The default value for the <code>fallbackIconName</code> property. */
	public static final String DEFAULT_FALLBACK_ICON_NAME = "unknown";
	
	private MediaHandlerDelegate imageMediaRequestDelegate = null;
	private String iconResourcePathPrefix = DEFAULT_ICON_RESOURCE_PATH_PREFIX;
	private String fallbackIconName = DEFAULT_FALLBACK_ICON_NAME;
	private ResourceLoader resourceLoader = new DefaultResourceLoader();
	private Map<String, byte[]> iconCache = new WeakHashMap<String, byte[]>();

	/**
	 * Construct with a MIME type.
	 * 
	 * @param mime the MIME
	 * @param preferredFileExtension the preferred file extension
	 */
	public BasicIconBasedMediaHandler(String mime, String preferredFileExtension) {
		super(mime);
		setPreferredFileExtension(preferredFileExtension);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaHandler#createNewMediaItem(java.io.File)
	 */
	@SuppressWarnings("unchecked")
	public MediaItem createNewMediaItem(File inputFile) {
		MediaItem item = getDomainObjectFactory().newMediaItemInstance();
		item.setMime(getMime());
		
		FileSystemResource mediaResource = new FileSystemResource(inputFile);
		MediaMetadata meta = handleMetadata(null, mediaResource, item);
		
		// look for image, if if available store image MIME as metadata,
		// for performance in handling media requests later
		if ( meta instanceof EmbeddedImageMetadata ) {
			EmbeddedImageMetadata embed = (EmbeddedImageMetadata)meta;
			if ( embed.hasEmbeddedImage() ) {
				String embedMime = embed.getEmbeddedImageMimeType();
				if ( StringUtils.hasText(embedMime) ) {
					Metadata itemMeta = getDomainObjectFactory().newMetadataInstance();
					itemMeta.setKey(EMBEDDED_IMAGE_MIME_TYPE_METADATA_KEY);
					itemMeta.setValue(embedMime);
					item.getMetadata().add(itemMeta);
				}
				
				// also set width/height from embedded image
				int width = embed.getEmbeddedImageWidth();
				if ( width > 0 ) {
					item.setWidth(width);
				}
				
				int height = embed.getEmbeddedImageHeight();
				if ( height > 0 ) {
					item.setHeight(height);
				}
			} else {
				item.setUseIcon(true);
			}
		} else {
			item.setUseIcon(true);
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
	
	@SuppressWarnings("unchecked")
	@Override
	public String getFileExtension(MediaItem item, MediaRequest request) {
		if ( request.isOriginal() ) {
			return super.getFileExtension(item, request);
		}
		if ( item.isUseIcon() || getImageMediaRequestDelegate() == null ) {
			return ImageConstants.DEFAULT_PNG_FILE_EXTENSION;
		}
		
		// use delegate file extension
		Resource itemResource = getMediaBiz().getMediaItemResource(item);

		// look for MIME on item metadata
		for ( Metadata meta : (List<Metadata>)item.getMetadata() ) {
			if ( EMBEDDED_IMAGE_MIME_TYPE_METADATA_KEY.equals(meta.getKey()) ) {
				return getImageMediaRequestDelegate().getDelegateFileExtension(itemResource, 
						meta.getValue(), item, request);
				
			}
		}
		
		MediaMetadata metadata = getMediaMetadataInstance(request, itemResource, item)
			.setMediaResource(itemResource);
		if ( !(metadata instanceof EmbeddedImageMetadata) ) {
			throw new RuntimeException("EmbeddedImageMetadata expected, but have ["
					+metadata +"]");
		}
		
		// add MIME to Item metadata so used in future requests...
		EmbeddedImageMetadata imageMeta = (EmbeddedImageMetadata)metadata;
		String embedMime = imageMeta.getEmbeddedImageMimeType();
		Metadata itemMeta = getDomainObjectFactory().newMetadataInstance();
		itemMeta.setKey(EMBEDDED_IMAGE_MIME_TYPE_METADATA_KEY);
		itemMeta.setValue(embedMime);
		item.getMetadata().add(itemMeta);
		
		return getImageMediaRequestDelegate().getDelegateFileExtension(itemResource, 
				embedMime, item, request);
	}

	@Override
	public MediaEffect getEffect(String key, Map<String, ?> effectParameters) {
		return null;
	}

	/**
	 * Default handler for media requests.
	 * 
	 * @param item the item
	 * @param request the request
	 * @param response the response
	 */
	protected void defaultHandleRequest(MediaItem item, MediaRequest request,
			MediaResponse response) {
		Resource itemResource = getMediaBiz().getMediaItemResource(item);
		if ( request.isOriginal() ) {
			defaultHandleRequestOriginal(item, itemResource, request, response);
			return;
		}
		if ( item.isUseIcon() || getImageMediaRequestDelegate() == null ) {
			defaultHandleIconResponse(item, request, response);
			return;
		}
		
		// delegate to image handler
		MediaMetadata metadata = getMediaMetadataInstance(request, itemResource, item)
			.setMediaResource(itemResource);
		if ( !(metadata instanceof EmbeddedImageMetadata) ) {
			throw new RuntimeException("EmbeddedImageMetadata expected, but have ["
					+metadata +"]");
		}
		EmbeddedImageMetadata imageMeta = (EmbeddedImageMetadata)metadata;
		getImageMediaRequestDelegate().handleDelegateMediaRequest(
				imageMeta.getEmbeddedImageResource(), 
				imageMeta.getEmbeddedImageMimeType(),
				item, request, response);
	}
	
	/**
	 * Default handler for returning an icon response.
	 * 
	 * @param item the item
	 * @param request the request
	 * @param response the response
	 */
	protected void defaultHandleIconResponse(MediaItem item, 
			MediaRequest request, 
			MediaResponse response) {
		String itemMime = item.getMime();
		byte[] icon = iconCache.get(itemMime);
		if ( icon == null ) {
			Resource iconResource = null;
			String path = iconResourcePathPrefix +itemMime 
				+'.' +ImageConstants.DEFAULT_PNG_FILE_EXTENSION;
			iconResource = resourceLoader.getResource(path);
			if ( !iconResource.exists() ) {
				int slashIdx = itemMime.indexOf('/');
				path = iconResourcePathPrefix +itemMime.substring(0, slashIdx)
					+'.' +ImageConstants.DEFAULT_PNG_FILE_EXTENSION;
				iconResource = resourceLoader.getResource(path);
				if ( !iconResource.exists() ) {
					String itemPath = item.getPath();
					int extensionIdx = itemPath.lastIndexOf('.');
					path = iconResourcePathPrefix +itemPath.substring(extensionIdx+1)
						+'.' +ImageConstants.DEFAULT_PNG_FILE_EXTENSION;
					iconResource = resourceLoader.getResource(path);
					if ( !iconResource.exists() ) {
						iconResource = resourceLoader.getResource(iconResourcePathPrefix
							+fallbackIconName +ImageConstants.DEFAULT_PNG_FILE_EXTENSION);
					}
				}
			}
			if ( iconResource == null || !iconResource.exists() ) {
				throw new RuntimeException("Unable to resolve icon for item [" +item.getItemId()
						+"] with MIME [" +itemMime +"]");
			}
			try {
				icon = FileCopyUtils.copyToByteArray(iconResource.getInputStream());
				iconCache.put(itemMime, icon);
			} catch ( IOException e ) {
				throw new RuntimeException(e);
			}
		}
		response.setMimeType(ImageConstants.PNG_MIME);
		response.setMediaLength(icon.length);
		response.setItem(item);
		try {
			FileCopyUtils.copy(icon, response.getOutputStream());
		} catch ( IOException e ) {
			log.warn("IOException writing icon to response: " +e);
		}
	}

	/**
	 * @return the imageMediaRequestDelegate
	 */
	public MediaHandlerDelegate getImageMediaRequestDelegate() {
		return imageMediaRequestDelegate;
	}
	
	/**
	 * @param imageMediaRequestDelegate the imageMediaRequestDelegate to set
	 */
	public void setImageMediaRequestDelegate(
			MediaHandlerDelegate imageMediaRequestDelegate) {
		this.imageMediaRequestDelegate = imageMediaRequestDelegate;
	}
	
	/**
	 * @return the iconResourcePathPrefix
	 */
	public String getIconResourcePathPrefix() {
		return iconResourcePathPrefix;
	}
	
	/**
	 * @param iconResourcePathPrefix the iconResourcePathPrefix to set
	 */
	public void setIconResourcePathPrefix(String iconResourcePathPrefix) {
		this.iconResourcePathPrefix = iconResourcePathPrefix;
	}
	
	/**
	 * @return the fallbackIconName
	 */
	public String getFallbackIconName() {
		return fallbackIconName;
	}
	
	/**
	 * @param fallbackIconName the fallbackIconName to set
	 */
	public void setFallbackIconName(String fallbackIconName) {
		this.fallbackIconName = fallbackIconName;
	}
	
	/**
	 * @return the resourceLoader
	 */
	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}
	
	/**
	 * @param resourceLoader the resourceLoader to set
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}
	
}
