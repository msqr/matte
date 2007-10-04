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
 * $Id: BasicAlbumSearchCriteria.java,v 1.1 2007/07/07 03:44:06 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.support;

import magoffin.matt.ma2.biz.SearchBiz.AlbumSearchCriteria;

/**
 * Basic implementation of {@link AlbumSearchCriteria}.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2007/07/07 03:44:06 $
 */
public class BasicAlbumSearchCriteria implements AlbumSearchCriteria {
	
	private Long albumId;
	
	/**
	 * Default constructor.
	 */
	public BasicAlbumSearchCriteria() {
		this(null);
	}
	
	/**
	 * Construct with an album ID.
	 * @param albumId the ID to search for
	 */
	public BasicAlbumSearchCriteria(Long albumId) {
		this.albumId = albumId;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.SearchBiz.AlbumSearchCriteria#getAlbumId()
	 */
	public Long getAlbumId() {
		return this.albumId;
	}
	
	/**
	 * @param albumId the albumId to set
	 */
	public void setAlbumId(Long albumId) {
		this.albumId = albumId;
	}
	
}
