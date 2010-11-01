/* ===================================================================
 * ScaleEffect.java
 * 
 * Created Oct 28, 2010 9:11:38 AM
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

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

import magoffin.matt.ma2.MediaEffect;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.MediaSize;
import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.support.Geometry;

import org.im4java.core.IMOperation;

/**
 * Effect that scales an image to the size specified on the request.
 * 
 * <p>Different scaling algorithms can be specified for thumbnail and
 * non-thumbnail sized images, with the idea that thumbnails could 
 * use a faster (but uglier) algorithm while non-thumbnails could use
 * a slower (but prettier) one. By default the thumbnail algorithm is
 * set to <b>Lanczos</b> and the non-thumbnail algorithm is 
 * <b>Sinc</b>.</p>
 * 
 * <p>Note this effect assumes a rotate effect has not been applied
 * before this effect is applied.</p>
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl class="class-properties">
 *   <dt>filterSet</dt>
 *   <dd>A set of allowed filter names. This has a default value containing
 *   the filters supported by GraphicsMagick.</dd>
 *   
 *   <dt>thumbnailFilterName</dt>
 *   <dd>The filter name to use for thumbnail-sized images. Defaults to
 *   {@link #DEFAULT_THUMBNAIL_FILTER_NAME}</dd>
 *   
 *   <dt>normalFilterName</dt>
 *   <dd>The filter name to use for non-thumbnail-sized images. Defaults 
 *   to {@link #DEFAULT_NORMAL_FILTER_NAME}.</dd>
 *   
 *   <dt>filterThumbnailSizes</dt>
 *   <dd>A set of {@link MediaSize} for which to treat as thumbnail sizes
 *   and scale with the <code>thumbnailFilterName</code> as opposed to 
 *   the <code>normalFilterName</code>. Defaults to a set containing
 *   {@link MediaSize#THUMB_SMALL}, {@link MediaSize#THUMB_NORMAL},
 *   and {@link MediaSize#THUMB_BIG}. The {@link MediaSize#THUMB_BIGGER}
 *   is intentionally left out so that the <code>normalFilterName</code>
 *   will be used for this size to achieve higher quality.</dd>
 *   
 *   <dt>thumbnailUnsharp</dt>
 *   <dd>Parameters to use for applying an unsharp operation after the
 *   scale of thumbnail images, to achieve a higher quality looking image.
 *   Set to <em>null</em> to not apply an unsharp operation to thumbnails.
 *   Accepts up to 4 values, for <em>radius, sigma, amount, and threshold</em>.
 *   Defaults to array with two values: <code>0,1</code>.</dd>
 *   
 *   <dt>normalUnsharp</dt>
 *   <dd>Parameters to use for applying an unsharp operation after the
 *   scale of normal images, to achieve a higher quality looking image.
 *   Set to <em>null</em> to not apply an unsharp operation to normal images.
 *   Accepts up to 4 values, for <em>radius, sigma, amount, and threshold</em>.
 *   Defaults to array with two values: <code>0,1</code>.</dd>
 *   
 *   <dt>unsharpThumbnailSizes</dt>
 *   <dd>A set of {@link MediaSize} for which to treat as thumbnail sizes
 *   and unsharp with the <code>thumbnailUnsharp</code> as opposed to 
 *   the <code>normalUnsharp</code>. Defaults to a set containing
 *   {@link MediaSize#THUMB_SMALL}, {@link MediaSize#THUMB_NORMAL},
 *   and {@link MediaSize#THUMB_BIG} and {@link MediaSize#THUMB_BIGGER}.</dd>
 *   
 * </dl>
 * 
 * @author matt
 * @version $Revision$ $Date$
 */
public class ScaleEffect extends BaseIM4JavaMediaEffect {

	/** The default value for the <code>thumbnailFilterName</code> property. */
	public static final String DEFAULT_THUMBNAIL_FILTER_NAME = "Lanczos";
	
	/** The default value for the <code>normalFilterName</code> property. */
	public static final String DEFAULT_NORMAL_FILTER_NAME = "Sinc";
	
	/** The default value for the <code>thumbnailUnsharp</code> property. */
	private static final Double[] DEFAULT_THUMBNAIL_UNSHARP = new Double[] {0.0, 1.0};
	
	/** The default value for the <code>normalUnsharp</code> property. */
	private static final Double[] DEFAULT_NORMAL_UNSHARP = DEFAULT_THUMBNAIL_UNSHARP;
	
	private Set<String> filterSet = defaultSupportedFilterSet();
	private String thumbnailFilterName = DEFAULT_THUMBNAIL_FILTER_NAME;
	private String normalFilterName = DEFAULT_NORMAL_FILTER_NAME;
	private Set<MediaSize> filterThumbnailSizes = EnumSet.of(
			MediaSize.THUMB_BIG, MediaSize.THUMB_NORMAL, MediaSize.THUMB_SMALL);
	private Double[] thumbnailUnsharp = DEFAULT_THUMBNAIL_UNSHARP;
	private Double[] normalUnsharp = DEFAULT_NORMAL_UNSHARP;
	private Set<MediaSize> unsharpThumbnailSizes = EnumSet.of(
			MediaSize.THUMB_BIGGER, MediaSize.THUMB_BIG, 
			MediaSize.THUMB_NORMAL, MediaSize.THUMB_SMALL);
	
	/**
	 * Default constructor.
	 */
	public ScaleEffect() {
		super(MediaEffect.KEY_SCALE);
	}

	private static Set<String> defaultSupportedFilterSet() {
		// these are ordered in increasing CPU complexity
		Set<String> filters = new LinkedHashSet<String>(15);
		filters.add("Point");
		filters.add("Box");
		filters.add("Triangle");
		filters.add("Hermite");
		filters.add("Hanning");
		filters.add("Hamming");
		filters.add("Blackman");
		filters.add("Gaussian");
		filters.add("Quadratic");
		filters.add("Cubic");
		filters.add("Catrom");
		filters.add("Mitchell");
		filters.add("Lanczos");
		filters.add("Bessel");
		filters.add("Sinc");
		return filters;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.image.im4java.IM4JavaMediaEffect#applyEffect(magoffin.matt.ma2.domain.MediaItem, magoffin.matt.ma2.MediaRequest, org.im4java.core.IMOperation)
	 */
	public ImageCommandAndOperation applyEffect(MediaItem item, MediaRequest request,
			IMOperation baseOperation) {
		Geometry geometry = getMediaBiz().getScaledGeometry(item, request);
		int width = geometry.getWidth();
		int height = geometry.getHeight();
		
		if ( width == item.getWidth() && height == item.getHeight() ) {
			// not scaling, just return
		}
		String filterName = filterThumbnailSizes.contains(request.getSize())
			? thumbnailFilterName : normalFilterName;
		if ( filterName != null ) {
			baseOperation.filter(filterName);
		}
		// we pass these flags: ! ignore aspect ratio and > only shrink
		baseOperation.resize(width, height, "!>");
		
		Double[] unsharp = unsharpThumbnailSizes.contains(request.getSize())
			? thumbnailUnsharp : normalUnsharp;
		if ( unsharp != null && unsharp.length > 0 ) {
			switch ( unsharp.length ) {
			case 1:
				baseOperation.unsharp(unsharp[0]);
				break;
				
			case 2:
				baseOperation.unsharp(unsharp[0], unsharp[1]);
				break;
				
			case 3:
				baseOperation.unsharp(unsharp[0], unsharp[1], unsharp[2]);
				break;
				
			default:
				baseOperation.unsharp(unsharp[0], unsharp[1], unsharp[2], unsharp[3]);
			}
		}
		return null;
	}

	/**
	 * @return the filterSet
	 */
	public Set<String> getFilterSet() {
		return filterSet;
	}

	/**
	 * @param filterSet the filterSet to set
	 */
	public void setFilterSet(Set<String> filterSet) {
		this.filterSet = filterSet;
	}

	/**
	 * @return the thumbnailFilterName
	 */
	public String getThumbnailFilterName() {
		return thumbnailFilterName;
	}

	/**
	 * @param thumbnailFilterName the thumbnailFilterName to set
	 */
	public void setThumbnailFilterName(String thumbnailFilterName) {
		if ( filterSet != null && !filterSet.contains(thumbnailFilterName) ) {
			throw new IllegalArgumentException("[" +thumbnailFilterName 
					+"] is not a supported filter");
		}
		this.thumbnailFilterName = thumbnailFilterName;
	}

	/**
	 * @return the normalFilterName
	 */
	public String getNormalFilterName() {
		return normalFilterName;
	}

	/**
	 * @param normalFilterName the normalFilterName to set
	 */
	public void setNormalFilterName(String normalFilterName) {
		if ( filterSet != null && !filterSet.contains(normalFilterName) ) {
			throw new IllegalArgumentException("[" +normalFilterName 
					+"] is not a supported filter");
		}
		this.normalFilterName = normalFilterName;
	}

	/**
	 * @return the filterThumbnailSizes
	 */
	public Set<MediaSize> getFilterThumbnailSizes() {
		return filterThumbnailSizes;
	}

	/**
	 * @param filterThumbnailSizes the thumbnailSizes to set
	 */
	public void setFilterThumbnailSizes(Set<MediaSize> filterThumbnailSizes) {
		this.filterThumbnailSizes = filterThumbnailSizes;
	}

	/**
	 * @return the thumbnailUnsharp
	 */
	public Double[] getThumbnailUnsharp() {
		return thumbnailUnsharp;
	}

	/**
	 * @param thumbnailUnsharp the thumbnailUnsharp to set
	 */
	public void setThumbnailUnsharp(Double[] thumbnailUnsharp) {
		this.thumbnailUnsharp = thumbnailUnsharp;
	}

	/**
	 * @return the normalUnsharp
	 */
	public Double[] getNormalUnsharp() {
		return normalUnsharp;
	}

	/**
	 * @param normalUnsharp the normalUnsharp to set
	 */
	public void setNormalUnsharp(Double[] normalUnsharp) {
		this.normalUnsharp = normalUnsharp;
	}

	/**
	 * @return the unsharpThumbnailSizes
	 */
	public Set<MediaSize> getUnsharpThumbnailSizes() {
		return unsharpThumbnailSizes;
	}

	/**
	 * @param unsharpThumbnailSizes the unsharpThumbnailSizes to set
	 */
	public void setUnsharpThumbnailSizes(Set<MediaSize> unsharpThumbnailSizes) {
		this.unsharpThumbnailSizes = unsharpThumbnailSizes;
	}

}
