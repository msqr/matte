/* ===================================================================
 * MediaItemLuceneSearchCriteria.java
 * 
 * Created Feb 25, 2007 1:55:28 PM
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
 * $Id: MediaItemLuceneSearchCriteria.java,v 1.3 2007/03/07 03:46:36 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.lucene;

import magoffin.matt.lucene.SearchCriteria;
import magoffin.matt.ma2.biz.SearchBiz;
import magoffin.matt.ma2.domain.PaginationCriteria;

/**
 * Lucene search criteria for media items.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.3 $ $Date: 2007/03/07 03:46:36 $
 */
public class MediaItemLuceneSearchCriteria implements SearchCriteria {

	private int maxResults = -1;
	private int pageSize = -1;
	private int page = 1;
	private boolean countOnly = false;
	private SearchBiz.MediaItemSearchCriteria mediaItemCriteria;
	
	/**
	 * Construct with a MediaItemSearchCriteria.
	 * @param mediaItemCriteria the criteria
	 */
	public MediaItemLuceneSearchCriteria(SearchBiz.MediaItemSearchCriteria mediaItemCriteria) {
		this(mediaItemCriteria, null);
	}
	
	/**
	 * Construct with a MediaItemSearchCriteria and pagination.
	 * @param mediaItemCriteria the criteria
	 * @param pagination the pagination
	 */
	public MediaItemLuceneSearchCriteria(SearchBiz.MediaItemSearchCriteria mediaItemCriteria, 
			PaginationCriteria pagination) {
		this.mediaItemCriteria = mediaItemCriteria;
		this.countOnly = mediaItemCriteria.isCountOnly();
		if ( pagination != null ) {
			if ( pagination.getMaxResults() != null ) {
				this.maxResults = pagination.getMaxResults().intValue();
			}
			if ( pagination.getPageOffset() != null ) {
				this.page = pagination.getPageOffset().intValue();
			}
			if ( pagination.getPageSize() != null ) {
				this.pageSize = pagination.getPageSize().intValue();
			}
		}
	}
	
	/**
	 * @return the maxResults
	 */
	public int getMaxResults() {
		return maxResults;
	}
	
	/**
	 * @param maxResults the maxResults to set
	 */
	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}
	
	/**
	 * @return the mediaItemCriteria
	 */
	public SearchBiz.MediaItemSearchCriteria getMediaItemCriteria() {
		return mediaItemCriteria;
	}
	
	/**
	 * @param mediaItemCriteria the mediaItemCriteria to set
	 */
	public void setMediaItemCriteria(
			SearchBiz.MediaItemSearchCriteria mediaItemCriteria) {
		this.mediaItemCriteria = mediaItemCriteria;
	}
	
	/**
	 * @return the page
	 */
	public int getPage() {
		return page;
	}
	
	/**
	 * @param page the page to set
	 */
	public void setPage(int page) {
		this.page = page;
	}
	
	/**
	 * @return the pageSize
	 */
	public int getPageSize() {
		return pageSize;
	}
	
	/**
	 * @param pageSize the pageSize to set
	 */
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	
	/**
	 * @return the countOnly
	 */
	public boolean isCountOnly() {
		return countOnly;
	}
	
	/**
	 * @param countOnly the countOnly to set
	 */
	public void setCountOnly(boolean countOnly) {
		this.countOnly = countOnly;
	}
	
}
