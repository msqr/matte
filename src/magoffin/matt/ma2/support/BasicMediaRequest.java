/* ===================================================================
 * BasicMediaRequest.java
 * 
 * Created Mar 16, 2006 10:16:58 PM
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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2.support;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.Range;

import magoffin.matt.ma2.MediaEffect;
import magoffin.matt.ma2.MediaQuality;
import magoffin.matt.ma2.MediaRequest;
import magoffin.matt.ma2.MediaSize;

/**
 * Basic implementation of MediaRequest.
 * 
 * @author matt.magoffin
 * @version $Revision$ $Date$
 */
public class BasicMediaRequest implements MediaRequest {
	
	private Long mediaItemId = null;
	private boolean original = false;
	private MediaSize size = MediaSize.NORMAL;
	private MediaQuality quality = MediaQuality.GOOD;
	private Map<String,Object> parameters = new LinkedHashMap<String,Object>();
	private List<MediaEffect> effects = new LinkedList<MediaEffect>();
	private Range partialContentByteRange = null;
	
	/**
	 * Default constructor.
	 */
	public BasicMediaRequest() {
		// nothing to do
	}
	
	/**
	 * Copy constructor.
	 * @param request the request to copy
	 */
	public BasicMediaRequest(MediaRequest request) {
		this.mediaItemId = request.getMediaItemId();
		this.original = request.isOriginal();
		this.size = request.getSize();
		this.quality = request.getQuality();
		this.parameters.putAll(request.getParameters());
		this.effects = request.getEffects();
	}
	
	/**
	 * Construct with some parameters.
	 * 
	 * @param id the media item ID
	 */
	public BasicMediaRequest(Long id) {
		mediaItemId = id;
	}

	/**
	 * Construct with some parameters.
	 * 
	 * @param id the media item ID
	 * @param size the size
	 * @param quality the quality
	 */
	public BasicMediaRequest(Long id, MediaSize size, MediaQuality quality) {
		mediaItemId = id;
		this.size = size;
		this.quality = quality;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaRequest#getMediaItemId()
	 */
	public Long getMediaItemId() {
		return mediaItemId;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaRequest#isOriginal()
	 */
	public boolean isOriginal() {
		return original;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaRequest#getSize()
	 */
	public MediaSize getSize() {
		return size;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaRequest#getQuality()
	 */
	public MediaQuality getQuality() {
		return quality;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaRequest#getParameters()
	 */
	public Map<String, Object> getParameters() {
		return parameters;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaRequest#partialContentByteRange()
	 */
	public Range getPartialContentByteRange() {
		return partialContentByteRange;
	}

	/**
	 * Generates a cache key based on the item's ID, size, and quality.
	 */
	public String getCacheKey() {
		StringBuilder sb = new StringBuilder(getMediaItemId().toString());
		sb.append("_");
		sb.append( size == null ? "#" : size.toString());
		sb.append("_");
		sb.append(quality == null ? "#" : quality.toString());
		// possibly parameters, effects as part of key in future?
		return sb.toString();
	}

	/**
	 * @param mediaItemId The mediaItemId to set.
	 */
	public void setMediaItemId(Long mediaItemId) {
		this.mediaItemId = mediaItemId;
	}

	/**
	 * @param original The original to set.
	 */
	public void setOriginal(boolean original) {
		this.original = original;
	}

	/**
	 * @param parameters The parameters to set.
	 */
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	/**
	 * @param quality The quality to set.
	 */
	public void setQuality(MediaQuality quality) {
		this.quality = quality;
	}

	/**
	 * @param size The size to set.
	 */
	public void setSize(MediaSize size) {
		this.size = size;
	}

	/**
	 * @return Returns the effects.
	 */
	public List<MediaEffect> getEffects() {
		return effects;
	}
	
	/**
	 * @param effects The effects to set.
	 */
	public void setEffects(List<MediaEffect> effects) {
		this.effects = effects;
	}

	/**
	 * @param partialContentByteRange the partialContentByteRange to set
	 */
	public void setPartialContentByteRange(Range partialContentByteRange) {
		this.partialContentByteRange = partialContentByteRange;
	}

}
