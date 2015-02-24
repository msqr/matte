/* ===================================================================
 * BasicAlbumSearchCriteria.java
 * 
 * Created Jul 7, 2007 1:18:49 PM
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
 */

package magoffin.matt.ma2.support;

import magoffin.matt.ma2.biz.SearchBiz.AlbumSearchCriteria;

/**
 * Basic implementation of {@link AlbumSearchCriteria}.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.1
 */
public class BasicAlbumSearchCriteria implements AlbumSearchCriteria {

	private Long albumId;
	private String anonymousKey;

	/**
	 * Default constructor.
	 */
	public BasicAlbumSearchCriteria() {
		super();
	}

	/**
	 * Construct with an album ID.
	 * 
	 * @param albumId
	 *        the ID to search for
	 */
	public BasicAlbumSearchCriteria(Long albumId) {
		this.albumId = albumId;
	}

	/**
	 * Construct with an anonymous key.
	 * 
	 * @param anonymousKey
	 *        the anonymous key to search for
	 */
	public BasicAlbumSearchCriteria(String anonymousKey) {
		this.anonymousKey = anonymousKey;
	}

	public Long getAlbumId() {
		return this.albumId;
	}

	public void setAlbumId(Long albumId) {
		this.albumId = albumId;
	}

	public String getAnonymousKey() {
		return anonymousKey;
	}

	public void setAnonymousKey(String anonymousKey) {
		this.anonymousKey = anonymousKey;
	}

}
