/* ===================================================================
 * ScaleEffect.java
 * 
 * Created Dec 29, 2006 9:14:27 AM
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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2.image.jmagick;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import magick.FilterType;
import magick.ImageInfo;
import magick.MagickException;
import magick.MagickImage;
import magoffin.matt.ma2.MediaEffect;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.MediaSize;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.support.Geometry;

/**
 * Effect that scales an image to the size specified on the request.
 * 
 * <p>Different scaling algorithms can be specified for thumbnail and
 * non-thumbnail sized images, with the idea that thumbnails could 
 * use a faster (but uglier) algorithm while non-thumbnails could use
 * a slower (but prettier) one. By default the thumbnail algorithm is
 * set to <b>triangle</b> and the non-thumbnail algorithm is 
 * <b>sinc</b>.</p>
 * 
 * <p>Note this effect assumes a rotate effect has not been applied
 * before this effect is applied.</p>
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl class="class-properties">
 *   <dt>filterNameMap</dt>
 *   <dd>A mapping of filter names to {@link FilterType} constant values.
 *   This map is populated by default with each FilterType constant
 *   name, minus the "Filter" ending. For example the 
 *   {@link FilterType#BesselFilter} constant is named <code>Bessel</code>.</dd>
 *   
 *   <dt>thumbnailFilterName</dt>
 *   <dd>The filter name to use for thumbnail-sized images. Defaults to
 *   {@link #DEFAULT_THUMBNAIL_FILTER_NAME}</dd>
 *   
 *   <dt>normalFilterName</dt>
 *   <dd>The filter name to use for non-thumbnail-sized images. Defaults 
 *   to {@link #DEFAULT_NORMAL_FILTER_NAME}.</dd>
 *   
 *   <dt>thumbnailSizes</dt>
 *   <dd>A set of {@link MediaSize} for which to treat as thumbnail sizes
 *   and scale with the <code>thumbnailFilterName</code> as opposed to 
 *   the <code>normalFilterName</code>. Defaults to a set containing
 *   {@link MediaSize#THUMB_SMALL}, {@link MediaSize#THUMB_NORMAL},
 *   and {@link MediaSize#THUMB_BIG}. The {@link MediaSize#THUMB_BIGGER}
 *   is intentionally left out so that the <code>normalFilterName</code>
 *   will be used for this size to achieve higher quality.</dd>
 *   
 * </dl>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class ScaleEffect extends BaseJMagickMediaEffect {
	
	/** The default value for the <code>thumbnailFilterName</code> property. */
	public static final String DEFAULT_THUMBNAIL_FILTER_NAME = "Triangle";
	
	/** The default value for the <code>normalFilterName</code> property. */
	public static final String DEFAULT_NORMAL_FILTER_NAME = "Sinc";
	
	/** The key for this effect. */
	public static final String SCALE_KEY = "image.jmagick." +MediaEffect.KEY_SCALE;
	
	private Map<String, Integer> filterNameMap = new LinkedHashMap<String, Integer>();
	private String thumbnailFilterName = DEFAULT_THUMBNAIL_FILTER_NAME;
	private String normalFilterName = DEFAULT_NORMAL_FILTER_NAME;
	private Set<MediaSize> thumbnailSizes = EnumSet.of(
			MediaSize.THUMB_BIG, MediaSize.THUMB_NORMAL, MediaSize.THUMB_SMALL);
	
	/**
	 * Constructor.
	 */
	public ScaleEffect() {
		setupDefaultFilterNameMap();
	}
	
	private void setupDefaultFilterNameMap() {
		filterNameMap.put("Bessel", FilterType.BesselFilter);
		filterNameMap.put("Blackman", FilterType.BlackmanFilter);
		filterNameMap.put("Box", FilterType.BoxFilter);
		filterNameMap.put("Catrom", FilterType.CatromFilter);
		filterNameMap.put("Cubic", FilterType.CubicFilter);
		filterNameMap.put("Guassian", FilterType.GuassianFilter);
		filterNameMap.put("Hamming", FilterType.HammingFilter);
		filterNameMap.put("Hanning", FilterType.HanningFilter);
		filterNameMap.put("Hermite", FilterType.HermiteFilter);
		filterNameMap.put("Lanczos", FilterType.LanczosFilter);
		filterNameMap.put("Mitchell", FilterType.MitchellFilter);
		filterNameMap.put("Point", FilterType.PointFilter);
		filterNameMap.put("Quadratic", FilterType.QuadraticFilter);
		filterNameMap.put("Sinc", FilterType.SincFilter);
		filterNameMap.put("Triangle", FilterType.TriangleFilter);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.image.jmagick.JMagickMediaEffect#applyEffect(magoffin.matt.ma2.domain.MediaItem, magoffin.matt.ma2.MediaRequest, magick.ImageInfo, magick.MagickImage)
	 */
	public MagickImage applyEffect(MediaItem item, MediaRequest request,
			ImageInfo inInfo, MagickImage image) {
		String filterName = thumbnailSizes.contains(request.getSize())
			? thumbnailFilterName : normalFilterName;
		Integer filterType = filterNameMap.get(filterName);
		if ( filterType == null ) {
			throw new RuntimeException("The filter [" 
				+filterName	+"] is not configured. Available names are: " 
				+filterNameMap.keySet());
		}
		
		// this assumes rotate has NOT been applied yet!
		
		Geometry geometry = getMediaBiz().getScaledGeometry(item, request);	
		int width = geometry.getWidth();
		int height = geometry.getHeight();
		
		try {
			if ( width == image.getDimension().getWidth() 
					&& height == image.getDimension().getHeight() ) {
				// no need to scale
				return image;
			}
			
			if ( log.isDebugEnabled() ) {
				log.debug("Magick zoom: filter = " +filterName +", dimensions = " +geometry);
			}
			
			// scale image via zoom which uses the defined filter while scaling
			image.setFilter(filterType);
			MagickImage result = image.zoomImage(width, height);
			
			// remove profiles
			result.profileImage("*", null);
			return result;
		} catch ( MagickException e ) {
			throw new RuntimeException("MagickException zooming: " +e,e);
		}
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaEffect#getKey()
	 */
	public String getKey() {
		return SCALE_KEY;
	}
	
	/**
	 * @return the filterNameMap
	 */
	protected Map<String, Integer> getFilterNameMap() {
		return filterNameMap;
	}
	
	/**
	 * @param filterNameMap the filterNameMap to set
	 */
	protected void setFilterNameMap(Map<String, Integer> filterNameMap) {
		this.filterNameMap = filterNameMap;
	}
	
	/**
	 * @return the thumbnailFilterName
	 */
	protected String getThumbnailFilterName() {
		return thumbnailFilterName;
	}
	
	/**
	 * @param thumbnailFilterName the thumbnailFilterName to set
	 */
	protected void setThumbnailFilterName(String thumbnailFilterName) {
		this.thumbnailFilterName = thumbnailFilterName;
	}
	
	/**
	 * @return the normalFilterName
	 */
	protected String getNormalFilterName() {
		return normalFilterName;
	}
	
	/**
	 * @param normalFilterName the normalFilterName to set
	 */
	protected void setNormalFilterName(String normalFilterName) {
		this.normalFilterName = normalFilterName;
	}
	
	/**
	 * @return the thumbnailSizes
	 */
	protected Set<MediaSize> getThumbnailSizes() {
		return thumbnailSizes;
	}
	
	/**
	 * @param thumbnailSizes the thumbnailSizes to set
	 */
	protected void setThumbnailSizes(Set<MediaSize> thumbnailSizes) {
		this.thumbnailSizes = thumbnailSizes;
	}

}
