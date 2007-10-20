/* ===================================================================
 * AbstractMediaMetadata.java
 * 
 * Created Mar 5, 2006 11:06:24 AM
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

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

import magoffin.matt.ma2.MediaMetadata;

/**
 * Basic implementation of {@link MediaMetadata}.
 * 
 * @author matt.magoffin
 * @version $Revision$ $Date$
 */
public abstract class AbstractMediaMetadata implements MediaMetadata {
	
	private Date creationDate;
	private Map<String,String> metadataMap = new LinkedHashMap<String,String>();

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaMetadata#getCreationDate()
	 */
	public Date getCreationDate() {
		return this.creationDate;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.MediaMetadata#getMetadataMap()
	 */
	public Map<String, String> getMetadataMap() {
		return this.metadataMap;
	}

	/**
	 * Add a non-null value to the metadata Map.
	 * 
	 * <p>If the <code>value</code> is <em>null</em> then nothing
	 * will be added to the Map. The {@link Object#toString()} method
	 * is called on the <code>key</code> to add to the Map.</p>
	 * 
	 * @param key the Map key
	 * @param value the Map value
	 */
	protected void addToMap(Object key, String value) {
		if ( !StringUtils.hasText(value) || key == null ) return;
		getMetadataMap().put(key.toString(),value);
	}

	/**
	 * @param creationDate The creationDate to set.
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @param metadataMap The metadataMap to set.
	 */
	public void setMetadataMap(Map<String, String> metadataMap) {
		this.metadataMap = metadataMap;
	}

}
