/* ===================================================================
 * AbstractMediaHandler.java
 * 
 * Created Mar 5, 2006 5:20:16 PM
 * 
 * Copyright (c) 2006 Matt Magoffin.
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

package magoffin.matt.ma2.support;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import magoffin.matt.ma2.MediaEffect;
import magoffin.matt.ma2.MediaHandler;
import magoffin.matt.ma2.MediaMetadata;
import magoffin.matt.ma2.MediaQuality;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.MediaResponse;
import magoffin.matt.ma2.MediaSize;
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.biz.MediaBiz;
import magoffin.matt.ma2.biz.UserBiz;
import magoffin.matt.ma2.domain.Collection;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.Metadata;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.image.EmbeddedImageMetadata;
import magoffin.matt.ma2.util.BizContextUtil;
import org.apache.commons.lang.math.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;

/**
 * Base class for {@link MediaHandler} implementations.
 * 
 * <p>
 * This class is a good starting point for {@link MediaHandler} implementations
 * to extend. It provides many useful methods common to most implementations.
 * </p>
 * 
 * <p>
 * The {@link #handleMetadata(MediaRequest, Resource, MediaItem)} method makes
 * use of {@link SmetaMediaMetadata} to extract metadata from media resources,
 * so is able to handle any file type supported by the application's
 * configuration of <a href="http://smeta.sourceforge.net/">sMeta</a>.
 * </p>
 * 
 * <p>
 * The configurable properties of this class are:
 * </p>
 * 
 * <dl class="class-properties">
 * <dt>domainObjectFactory</dt>
 * <dd>The {@link magoffin.matt.ma2.biz.DomainObjectFactory} to use for creating
 * new domain objects.</dd>
 * 
 * <dt>mediaBiz</dt>
 * <dd>An instance of the {@link MediaBiz} to use.</dd>
 * 
 * <dt>userBiz</dt>
 * <dd>An instance of the {@link UserBiz} for getting user locale information
 * from.</dd>
 * 
 * <dt>preferredFileExtension</dt>
 * <dd>The file extension to use for the media files processed by this
 * {@code MediaHandler}. In many cases there is a one-to-one mapping of
 * {@code MediaHandler} instances to file types they support, and thus will only
 * need to return a single file extension at all times. This property can be
 * configured with this file extension.</dd>
 * 
 * <dt>mime</dt>
 * <dd>A MIME type to associate with this handler. Similarly to how the
 * {@code preferredFileExtension} is configurable in this class, this MIME type
 * property allows implementations to return a single MIME type for every file
 * handled by each instance of this class.</dd>
 * 
 * <dt>smetaPropertyMap</dt>
 * <dd>An optional Map of JavaBean properties to apply to each
 * {@link SmetaMediaMetadata} instance created in the
 * {@link #getMediaMetadataInstance(MediaRequest, Resource, MediaItem)} method.
 * A Spring {@link BeanWrapper} is used on the newly created objects, so this
 * provides a way to initialize properties on those objects after they are
 * created.</dd>
 * 
 * <dt>noWatermarkSizes</dt>
 * <dd>A Set of {@link MediaSize} instances which should <em>not</em> have a
 * watermark applied, if a user has a watermark configured. This defaults to all
 * thumbnail sizes.</dd>
 * 
 * </dl>
 * 
 * @author matt.magoffin
 * @version 1.1
 */
public abstract class AbstractMediaHandler implements MediaHandler {

	/**
	 * An {@link MediaRequest} parameter key for a cached {@link MediaMetadata}
	 * object.
	 */
	public static final String METADATA_PARAMETER_KEY = "abstractmediahandler.metadata";

	/** A class Logger. */
	protected final Logger log = LoggerFactory.getLogger(getClass());

	private DomainObjectFactory domainObjectFactory;
	private MediaBiz mediaBiz;
	private UserBiz userBiz;
	private String preferredFileExtension;
	private String mime;
	private Map<String, Object> smetaPropertyMap;
	private Set<MediaSize> noWatermarkSizes = EnumSet.of(MediaSize.THUMB_BIGGER, MediaSize.THUMB_BIG,
			MediaSize.THUMB_NORMAL, MediaSize.THUMB_SMALL);

	/**
	 * Construct with MIME type.
	 * 
	 * @param mime
	 *        the MIME type
	 */
	public AbstractMediaHandler(String mime) {
		this.mime = mime;
	}

	/**
	 * Create a new List of Metadata instances from a Map.
	 * 
	 * <p>
	 * This will return a list of newly-constructed {@link Metadata} objects,
	 * using the keys of the map to call {@link Metadata#setKey(String)} and the
	 * corresponding map values to call {@link Metadata#setValue(String)}. This
	 * can be useful for converting metadata properties returned by sMeta into
	 * {@link Metadata} objects for persisting on a {@link MediaItem} (i.e. by
	 * adding the list to {@link MediaItem#getMetadata()}.
	 * </p>
	 * 
	 * @param map
	 *        the map of properties to turn into Metadata
	 * @return the list of newly created Metadata objects
	 */
	protected List<Metadata> createMetadataList(Map<String, String> map) {
		List<Metadata> results = new ArrayList<Metadata>(map.size());
		for ( Map.Entry<String, String> me : map.entrySet() ) {
			Metadata meta = domainObjectFactory.newMetadataInstance();
			meta.setKey(me.getKey());
			meta.setValue(me.getValue());
			results.add(meta);
		}
		return results;
	}

	/**
	 * Extract metadata from a resource and replace item metadata with all
	 * extracted data.
	 * 
	 * <p>
	 * This method provides a simple way to populate the {@link Metadata} list
	 * of a newly created {@link MediaItem}. It will generate the list of
	 * {@link Metadata} and then add all of them into the List returned by
	 * {@link MediaItem#getMetadata()}.
	 * </p>
	 * 
	 * <p>
	 * This method first calls
	 * {@link #getMediaMetadataInstance(MediaRequest, Resource, MediaItem)} to
	 * create a new instance of {@link MediaMetadata} and then calls
	 * {@link MediaMetadata#setMediaResource(Resource)}.
	 * </p>
	 * 
	 * <p>
	 * If {@link MediaMetadata#getCreationDate()} returns a non-null value then
	 * a new {@link Calendar} instance will be created from it and used to set
	 * the <code>creationDate</code> property of the MediaItem.
	 * </p>
	 * 
	 * <p>
	 * If the MediaMetadata is an instance of {@link EmbeddedImageMetadata} then
	 * {@link EmbeddedImageMetadata#getEmbeddedImage()} will be called, and the
	 * width/height of the returned image will be used to set the width/height
	 * properties of the MediaItem provided.
	 * </p>
	 * 
	 * @param request
	 *        the reqeust (may be <em>null</em>)
	 * @param mediaResource
	 *        the media resource to extract the metadata from
	 * @param item
	 *        the item to replace the extracted metadata in
	 * @return the resulting metadata instance
	 */
	@SuppressWarnings("unchecked")
	protected MediaMetadata handleMetadata(MediaRequest request, Resource mediaResource, MediaItem item) {
		MediaMetadata resultMeta = getMediaMetadataInstance(request, mediaResource, item);
		resultMeta = resultMeta.setMediaResource(mediaResource);
		List<Metadata> metadata = createMetadataList(resultMeta.getMetadataMap());
		item.getMetadata().clear();
		item.getMetadata().addAll(metadata);

		if ( resultMeta.getCreationDate() != null ) {
			// set creation date from metadata date
			Calendar cal = Calendar.getInstance();
			cal.setTime(resultMeta.getCreationDate());
			item.setItemDate(cal);
		}

		if ( resultMeta instanceof EmbeddedImageMetadata ) {
			EmbeddedImageMetadata embed = (EmbeddedImageMetadata) resultMeta;
			BufferedImage image = embed.getEmbeddedImage();
			if ( image != null ) {
				item.setWidth(image.getWidth());
				item.setHeight(image.getHeight());
			}
		}
		return resultMeta;
	}

	/**
	 * Get a {@link MediaMetadata} instance for the given resource.
	 * 
	 * <p>
	 * This implementation returns a new {@link SmetaMediaMetadata} instance.
	 * Extending classes may want to change this behavior or set properties onto
	 * the returned instance.
	 * </p>
	 * 
	 * <p>
	 * If the {@link #getSmetaPropertyMap()} is configured, then the created
	 * instance will have all the properties defined in that Map set onto it
	 * before being returned. This allows the instance to be easily configured.
	 * The properties are set via a Spring {@link BeanWrapper} so should follow
	 * those naming conventions.
	 * </p>
	 * 
	 * <p>
	 * If the {@code request} parameter is not <em>null</em> then the
	 * {@code SmetaMediaMetadata} instance will also be placed into the
	 * parameter Map of the {@link MediaRequest}, using the key
	 * {@link #METADATA_PARAMETER_KEY}.
	 * </p>
	 * 
	 * @param request
	 *        the reqeust (may be <em>null</em>)
	 * @param mediaResource
	 *        the resource
	 * @param item
	 *        the item
	 * @return a new MediaMetadata instance
	 */
	protected MediaMetadata getMediaMetadataInstance(MediaRequest request, Resource mediaResource,
			MediaItem item) {
		if ( request != null && request.getParameters().containsKey(METADATA_PARAMETER_KEY) ) {
			return (MediaMetadata) request.getParameters().get(METADATA_PARAMETER_KEY);
		}
		Locale locale = null;
		if ( userBiz != null ) {
			// we assume this is only called on new items, so the acting user 
			// is the owner and we get their locale here
			BizContext context = BizContextUtil.getBizContext();
			if ( context != null ) {
				User user = context.getActingUser();
				if ( user != null ) {
					locale = userBiz.getUserLocale(user, context);
				}
			}
		}
		if ( locale == null ) {
			locale = Locale.getDefault();
		}

		SmetaMediaMetadata result = new SmetaMediaMetadata(locale);
		if ( !CollectionUtils.isEmpty(getSmetaPropertyMap()) ) {
			BeanWrapper wrapper = new BeanWrapperImpl(result);
			wrapper.setPropertyValues(getSmetaPropertyMap());
		}
		if ( request != null ) {
			request.getParameters().put(METADATA_PARAMETER_KEY, result);
		}
		return result;
	}

	/**
	 * Get the file extension.
	 * 
	 * <p>
	 * This method simply returns {@link #getPreferredFileExtension()}.
	 * Extending class may override this for request-specific handling.
	 * </p>
	 */
	public String getFileExtension(MediaItem item, MediaRequest request) {
		return getPreferredFileExtension();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see magoffin.matt.ma2.MediaHandler#getEffect(java.lang.String,
	 * java.util.Map)
	 */
	public MediaEffect getEffect(String key, Map<String, ?> effectParameters) {
		return null;
	}

	/**
	 * Get a list of effects to apply to a request.
	 * 
	 * <p>
	 * This method provides a way to create a list of standard effects useful
	 * for every request:
	 * </p>
	 * 
	 * <ol>
	 * <li>Resizing: scaling to the size requested by
	 * {@link MediaRequest#getSize()}. If the requested width/height are not the
	 * same as those returned by {@link MediaItem#getWidth()} and
	 * {@link MediaItem#getHeight()} then this method will call
	 * {@link MediaHandler#getEffect(String, Map)} using the key
	 * {@link MediaEffect#KEY_SCALE}.</li>
	 * 
	 * <li>Rotation: if the {@link MediaRequest#getParameters()} contains the
	 * key {@link MediaEffect#MEDIA_REQUEST_PARAM_ROTATE_DEGREES}, then this
	 * method will call {@link MediaHandler#getEffect(String, Map)} using the
	 * key {@link MediaEffect#KEY_ROTATE}.</li>
	 * </ol>
	 * 
	 * <p>
	 * Extending classes that wish to support additional effects can override
	 * this method, adding any additional effects to the returned list as
	 * desired.
	 * </p>
	 * 
	 * @param item
	 *        the item the effects are to be applied to
	 * @param request
	 *        the current request
	 * @return list of effects (or empty list of no effects appropriate for this
	 *         request)
	 */
	protected List<MediaEffect> getRequestEffects(MediaItem item, MediaRequest request) {
		List<MediaEffect> effects = new LinkedList<MediaEffect>();

		// handle resize
		Geometry geo = getMediaBiz().getGeometry(request.getSize());
		if ( geo.getWidth() != item.getWidth() || geo.getHeight() != item.getHeight() ) {
			MediaEffect effect = getEffect(MediaEffect.KEY_SCALE, request.getParameters());
			if ( effect == null ) {
				throw new RuntimeException("Effect not configured: " + MediaEffect.KEY_SCALE);
			}
			effects.add(effect);
		}

		// handle rotate
		if ( request.getParameters().containsKey(MediaEffect.MEDIA_REQUEST_PARAM_ROTATE_DEGREES) ) {
			MediaEffect rotate = getEffect(MediaEffect.KEY_ROTATE, request.getParameters());
			if ( rotate != null ) {
				effects.add(rotate);
			} else {
				log.warn("Effect [" + MediaEffect.KEY_ROTATE + "] not configured, skipping");
			}
		}

		// handle watermark
		if ( CollectionUtils.isEmpty(this.noWatermarkSizes)
				|| !this.noWatermarkSizes.contains(request.getSize()) ) {
			Resource watermark = (Resource) request.getParameters().get(
					MediaEffect.MEDIA_REQUEST_PARAM_WATERMARK_RESOURCE);
			if ( watermark == null ) {
				Collection collection = mediaBiz.getMediaItemCollection(item);
				if ( collection != null && collection.getOwner() != null ) {
					User owner = collection.getOwner();
					watermark = userBiz.getUserWatermark(owner.getUserId());
				}
			}
			if ( watermark != null && watermark.exists() ) {
				MediaEffect watermarkEffect = getEffect(MediaEffect.KEY_WATERMARK,
						request.getParameters());
				if ( watermarkEffect != null ) {
					request.getParameters().put(MediaEffect.MEDIA_REQUEST_PARAM_WATERMARK_RESOURCE,
							watermark);
					effects.add(watermarkEffect);
				} else {
					log.warn("Effect [" + MediaEffect.KEY_WATERMARK + "] not configured, skipping");
				}
			}
		}

		effects.addAll(request.getEffects());
		request.getEffects().clear();
		request.getEffects().addAll(effects);
		return request.getEffects();
	}

	/**
	 * Apply the effects for a request.
	 * 
	 * <p>
	 * This method provides a default way to apply effects to a media request.
	 * </p>
	 * 
	 * <p>
	 * This method will call {@link #getRequestEffects(MediaItem, MediaRequest)}
	 * and the call
	 * {@link MediaEffect#apply(MediaItem, MediaRequest, MediaResponse)} for
	 * each effect returned. Note the MediaHandler implementation must have set
	 * up any required parameters needed by the effects prior to calling this
	 * method.
	 * </p>
	 * 
	 * @param item
	 *        the item to apply the effects to
	 * @param request
	 *        the current request
	 * @param response
	 *        the current response
	 */
	protected void applyEffects(MediaItem item, MediaRequest request, MediaResponse response) {
		List<MediaEffect> effectList = getRequestEffects(item, request);
		if ( log.isDebugEnabled() ) {
			log.debug("Applying " + effectList.size() + " effects to item [" + item.getItemId() + "]");
		}
		for ( MediaEffect effect : effectList ) {
			effect.apply(item, request, response);
		}
		if ( log.isDebugEnabled() ) {
			log.debug("Effects complete for item [" + item.getItemId() + "]");
		}
	}

	/**
	 * Test if the current request needs to alter the original media item in
	 * some way or not.
	 * 
	 * <p>
	 * This method provides a way test if there are any alterations needed to
	 * satifsy the request, including:
	 * </p>
	 * 
	 * <ol>
	 * <li>Resize (scale): if the request is for a size different from the
	 * {@link MediaItem#getWidth()} or {@link MediaItem#getHeight()}.</li>
	 * 
	 * <li>Recompress: if the request is not for {@link MediaQuality#HIGHEST}.</li>
	 * 
	 * <li>Effects: if {@link MediaRequest#getEffects()} has at least one effect
	 * in it.</li>
	 * 
	 * <li>Rotate: if {@link #needToRotate(MediaItem, MediaRequest)} returns
	 * <em>true</em>.</li>
	 * </ol>
	 * 
	 * <p>
	 * If any of these are found to be <em>true</em> then this method will also
	 * return <em>true</em>.
	 * </p>
	 * 
	 * @param item
	 *        the item being tested
	 * @param request
	 *        the current request
	 * @return boolean
	 */
	protected boolean needToAlter(MediaItem item, MediaRequest request) {
		if ( request.isOriginal() ) {
			return false;
		}

		MediaSize size = request.getSize();
		Geometry geometry = getMediaBiz().getGeometry(size);
		MediaQuality quality = request.getQuality();
		if ( !MediaQuality.HIGHEST.equals(quality) || geometry.getWidth() != item.getWidth()
				|| geometry.getHeight() != item.getHeight() ) {
			return true;
		}

		// check for other effects already added...
		if ( request.getEffects() != null && request.getEffects().size() > 0 ) {
			return true;
		}

		// need to rotate?
		if ( needToRotate(item, request) )
			return true;

		// looks like no changing
		return false;
	}

	/**
	 * Check if rotation needs to be performed for a given media item.
	 * 
	 * <p>
	 * This method will return <em>true</em> if any of the following are found
	 * to be <em>true</em>:
	 * </p>
	 * 
	 * <ol>
	 * <li>The {@link MediaRequest#getParameters()} Map contains a key
	 * {@link MediaEffect#MEDIA_REQUEST_PARAM_ROTATE_DEGREES}.</li>
	 * 
	 * <li>The {@link MediaRequest#getEffects()} contains an effect where
	 * {@link MediaEffect#getKey()} <em>ends with</em>
	 * {@link MediaEffect#KEY_ROTATE}.</li>
	 * </ol>
	 * 
	 * @param item
	 *        the item
	 * @param request
	 *        the request
	 * @return boolean
	 */
	protected boolean needToRotate(MediaItem item, MediaRequest request) {
		if ( request.getParameters().containsKey(MediaEffect.MEDIA_REQUEST_PARAM_ROTATE_DEGREES) ) {
			return true;
		}

		if ( request.getEffects().size() > 0 ) {
			for ( MediaEffect effect : request.getEffects() ) {
				if ( effect.getKey().endsWith(MediaEffect.KEY_ROTATE) ) {
					// rotate effect already on request, so yes we are rotating
					return true;
				}
			}
		}

		// don't think we need to rotate
		return false;
	}

	/**
	 * Handle the original media data, without altering.
	 * 
	 * <p>
	 * This method will call {@link MediaResponse#setMimeType(String)} with the
	 * value of {@link #getMime()}. Then it will call
	 * {@link MediaResponse#setMediaLength(long)} with the value returned by the
	 * {@code itemResource}'s File {@link java.io.File#length()}. It will also
	 * call {@link MediaResponse#setModifiedDate(long)} with the value returned
	 * by {@link java.io.File#lastModified()}.
	 * </p>
	 * 
	 * <p>
	 * Finally, it will copy the file to the
	 * {@link MediaResponse#getOutputStream()}.
	 * </p>
	 * 
	 * @param item
	 *        the item to handle
	 * @param itemResource
	 *        the item's Resource
	 * @param request
	 *        the request
	 * @param response
	 *        the response
	 */
	protected void defaultHandleRequestOriginal(MediaItem item, Resource itemResource,
			MediaRequest request, MediaResponse response) {
		try {
			response.setMimeType(getMime());
			response.setModifiedDate(itemResource.getFile().lastModified());
		} catch ( IOException e ) {
			// ignore
			return;
		}

		if ( !response.hasOutputStream() ) {
			// happens on last-modified requests
			return;
		}

		long fileLength = -1;
		try {
			fileLength = itemResource.getFile().length();
		} catch ( IOException e ) {
			// ignore
		}
		if ( request.getPartialContentByteRange() != null ) {
			Range range = request.getPartialContentByteRange();
			long start = range.getMinimumLong();
			if ( start < 0 ) {
				start = 0;
			}
			long end = range.getMaximumLong();
			if ( end < fileLength ) {
				end += 1;
			} else {
				end = fileLength;
			}
			byte[] buf = new byte[4096];
			if ( log.isDebugEnabled() ) {
				log.debug("Returning stream byte range {}-{} {}", start, end, item.getPath());
			}
			response.setPartialResponse(start, end - 1, fileLength);
			OutputStream out = response.getOutputStream();
			RandomAccessFile file = null;
			try {
				file = new RandomAccessFile(itemResource.getFile(), "r");
				file.seek(start);
				while ( start < end ) {
					int max = Math
							.min(4096, start + buf.length > end ? (int) (end - start) : buf.length);
					int len = file.read(buf, 0, max);
					out.write(buf, 0, len);
					start += len;
				}
				out.flush();
				out.close();
			} catch ( IOException e ) {
				log.info("IOException returning stream byte range {}-{}: {}", start, end, e);
			} finally {
				if ( file != null ) {
					try {
						file.close();
					} catch ( IOException e ) {
						// ignore this
					}
				}
			}
		} else {
			if ( log.isDebugEnabled() ) {
				log.debug("Returning unaltered stream " + item.getPath());
			}
			try {
				if ( fileLength > 0 ) {
					response.setMediaLength(fileLength);
				}
				FileCopyUtils.copy(itemResource.getInputStream(), response.getOutputStream());
			} catch ( IOException e ) {
				// not much we can do, lets just log a message
				if ( log.isDebugEnabled() ) {
					log.debug("IOException sending media response", e);
				} else if ( log.isInfoEnabled() ) {
					log.info("IOException sending media response: " + e.getMessage());
				}
			}
		}
	}

	/**
	 * @return Returns the domainObjectFactory.
	 */
	public DomainObjectFactory getDomainObjectFactory() {
		return domainObjectFactory;
	}

	/**
	 * @param domainObjectFactory
	 *        The domainObjectFactory to set.
	 */
	public void setDomainObjectFactory(DomainObjectFactory domainObjectFactory) {
		this.domainObjectFactory = domainObjectFactory;
	}

	/**
	 * @return Returns the mediaBiz.
	 */
	public MediaBiz getMediaBiz() {
		return mediaBiz;
	}

	/**
	 * @param mediaBiz
	 *        The mediaBiz to set.
	 */
	public void setMediaBiz(MediaBiz mediaBiz) {
		this.mediaBiz = mediaBiz;
	}

	/**
	 * @return Returns the preferredFileExtension.
	 */
	public String getPreferredFileExtension() {
		return preferredFileExtension;
	}

	/**
	 * @param preferredFileExtension
	 *        The preferredFileExtension to set.
	 */
	public void setPreferredFileExtension(String preferredFileExtension) {
		this.preferredFileExtension = preferredFileExtension;
	}

	/**
	 * @return the mime
	 */
	public String getMime() {
		return mime;
	}

	/**
	 * @param mime
	 *        the mime to set
	 */
	public void setMime(String mime) {
		this.mime = mime;
	}

	/**
	 * @return the smetaPropertyMap
	 */
	public Map<String, Object> getSmetaPropertyMap() {
		return smetaPropertyMap;
	}

	/**
	 * @param smetaPropertyMap
	 *        the smetaPropertyMap to set
	 */
	public void setSmetaPropertyMap(Map<String, Object> smetaPropertyMap) {
		this.smetaPropertyMap = smetaPropertyMap;
	}

	/**
	 * @return the userBiz
	 */
	public UserBiz getUserBiz() {
		return userBiz;
	}

	/**
	 * @param userBiz
	 *        the userBiz to set
	 */
	public void setUserBiz(UserBiz userBiz) {
		this.userBiz = userBiz;
	}

	/**
	 * @return the noWatermarkSizes
	 */
	public Set<MediaSize> getNoWatermarkSizes() {
		return noWatermarkSizes;
	}

	/**
	 * @param noWatermarkSizes
	 *        the noWatermarkSizes to set
	 */
	public void setNoWatermarkSizes(Set<MediaSize> noWatermarkSizes) {
		this.noWatermarkSizes = noWatermarkSizes;
	}

}
