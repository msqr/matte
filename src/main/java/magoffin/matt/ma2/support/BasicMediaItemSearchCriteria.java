/* ===================================================================
 * BasicMediaItemSearchCriteria.java
 * 
 * Created Feb 27, 2007 9:48:38 PM
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

import java.util.Calendar;
import magoffin.matt.ma2.biz.SearchBiz.MediaItemSearchCriteria;
import magoffin.matt.ma2.domain.MediaItem;

/**
 * Basic implementation of {@link MediaItemSearchCriteria}.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.1
 */
public class BasicMediaItemSearchCriteria implements MediaItemSearchCriteria {

	private Calendar endDate = null;
	private Calendar startDate = null;
	private MediaItem mediaItemTemplate = null;
	private String quickSearch = null;
	private boolean countOnly = false;
	private Long userId = null;
	private String userAnonymousKey = null;
	private boolean sharedOnly = false;

	/**
	 * Default constructor.
	 */
	public BasicMediaItemSearchCriteria() {
		super();
	}

	/**
	 * Construct with a quick search.
	 * 
	 * @param quickSearch
	 */
	public BasicMediaItemSearchCriteria(String quickSearch) {
		super();
		this.quickSearch = quickSearch;
	}

	@Override
	public String toString() {
		return "BasicMediaItemSearchCriteria{quickSearch=" + this.quickSearch + ",startDate="
				+ (this.startDate == null ? "" : this.startDate.getTime()) + ",endDate="
				+ (this.endDate == null ? "" : this.endDate.getTime()) + ",countOnly=" + countOnly
				+ ",userId=" + userId + ",userKey=" + userAnonymousKey + "}";
	}

	@Override
	public Calendar getEndDate() {
		return endDate;
	}

	@Override
	public MediaItem getMediaItemTemplate() {
		return mediaItemTemplate;
	}

	@Override
	public String getQuickSearch() {
		return quickSearch;
	}

	@Override
	public Calendar getStartDate() {
		return startDate;
	}

	@Override
	public boolean isCountOnly() {
		return countOnly;
	}

	@Override
	public Long getUserId() {
		return userId;
	}

	@Override
	public boolean isSharedOnly() {
		return sharedOnly;
	}

	/**
	 * @param endDate
	 *        the endDate to set
	 */
	public void setEndDate(Calendar endDate) {
		this.endDate = endDate;
	}

	/**
	 * @param mediaItemTemplate
	 *        the mediaItemTemplate to set
	 */
	public void setMediaItemTemplate(MediaItem mediaItemTemplate) {
		this.mediaItemTemplate = mediaItemTemplate;
	}

	/**
	 * @param quickSearch
	 *        the quickSearch to set
	 */
	public void setQuickSearch(String quickSearch) {
		this.quickSearch = quickSearch;
	}

	/**
	 * @param startDate
	 *        the startDate to set
	 */
	public void setStartDate(Calendar startDate) {
		this.startDate = startDate;
	}

	/**
	 * @param countOnly
	 *        the countOnly to set
	 */
	public void setCountOnly(boolean countOnly) {
		this.countOnly = countOnly;
	}

	/**
	 * @param sharedOnly
	 *        the sharedOnly to set
	 */
	public void setSharedOnly(boolean sharedOnly) {
		this.sharedOnly = sharedOnly;
	}

	/**
	 * @param userId
	 *        the userId to set
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Override
	public String getUserAnonymousKey() {
		return userAnonymousKey;
	}

	/**
	 * @param userAnonymousKey
	 *        the userAnonymousKey to set
	 */
	public void setUserAnonymousKey(String userAnonymousKey) {
		this.userAnonymousKey = userAnonymousKey;
	}

}
