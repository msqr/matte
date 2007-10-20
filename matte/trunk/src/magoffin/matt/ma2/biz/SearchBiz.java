/* ===================================================================
 * SearchBiz.java
 * 
 * Created May 25, 2006 7:01:57 PM
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

package magoffin.matt.ma2.biz;

import java.util.Calendar;

import magoffin.matt.ma2.domain.MediaItem;
import magoffin.matt.ma2.domain.PaginationCriteria;
import magoffin.matt.ma2.domain.SearchResults;
import magoffin.matt.ma2.support.BrowseAlbumsCommand;

/**
 * API for search actions.
 * 
 * @author matt.magoffin
 * @version $Revision$ $Date$
 */
public interface SearchBiz {
	
	/**
	 * Search criteria for MediaItem searches.
	 */
	static interface MediaItemSearchCriteria {
		
		/**
		 * Get a quick search query.
		 * @return the quick search
		 */
		public String getQuickSearch();
		
		/**
		 * Get a minimum date.
		 * @return date
		 */
		public Calendar getStartDate();
		
		/**
		 * Get a maximum date.
		 * @return date
		 */
		public Calendar getEndDate();
		
		/**
		 * Get a search object template.
		 * @return the template
		 */
		public MediaItem getMediaItemTemplate();
		
		/**
		 * If <em>true</em> then return only the count of matches,
		 * but not the matches themselves.
		 * @return boolean
		 */
		public boolean isCountOnly();
		
		/**
		 * Limit the results to those owned by the given user ID.
		 * @return the owner user ID
		 */
		public Long getUserId();
		
		/**
		 * Limit the results to those owned by the given user's 
		 * anonymous key.
		 * @return the owner anonymous key
		 */
		public String getUserAnonymousKey();
		
		/**
		 * If <em>true</em> then limit results to only those that
		 * are part of shared albums.
		 * @return boolean
		 */
		public boolean isSharedOnly();
	}
	
	/**
	 * Search criteria for albums.
	 */
	static interface AlbumSearchCriteria {
		
		/**
		 * Find a specific album based on ID.
		 * @return the album ID
		 */
		public Long getAlbumId();
		
	}
	
	/**
	 * Get an index of UserSearchResult objects.
	 * 
	 * @param pagination the index pagination criteria
	 * @param context the current context
	 * @return search results with the pagination index and user results
	 */
	public SearchResults findUsersForIndex(PaginationCriteria pagination, BizContext context);
	
	/**
	 * Search for media items.
	 * 
	 * @param criteria the criteria
	 * @param pagination the pagination criteria
	 * @param context the current context
	 * @return search results
	 */
	public SearchResults findMediaItems(MediaItemSearchCriteria criteria, 
			PaginationCriteria pagination, BizContext context);
	
	/**
	 * Search for albums.
	 * 
	 * @param criteria the criteria
	 * @param pagination the pagination criteria
	 * @param context the current context
	 * @return search results
	 */
	public SearchResults findAlbums(AlbumSearchCriteria criteria, 
			PaginationCriteria pagination, BizContext context);
	
	/**
	 * Search for shared albums for browsing.
	 * 
	 * @param command the browse criteria
	 * @param pagination the pagination criteria
	 * @param context the current context
	 * @return search results
	 */
	public SearchResults findAlbumsForBrowsing(BrowseAlbumsCommand command, 
			PaginationCriteria pagination, BizContext context);
	
}
