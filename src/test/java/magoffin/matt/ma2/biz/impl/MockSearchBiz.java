/* ===================================================================
 * MockSearchBiz.java
 * 
 * Created Jul 7, 2007 1:11:33 PM
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

package magoffin.matt.ma2.biz.impl;

import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.domain.PaginationCriteria;
import magoffin.matt.ma2.domain.SearchResults;

/**
 * Extension of {@link AbstractSearchBiz} to enable unit testing.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public class MockSearchBiz extends AbstractSearchBiz {

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.SearchBiz#findMediaItems(magoffin.matt.ma2.biz.SearchBiz.MediaItemSearchCriteria, magoffin.matt.ma2.domain.PaginationCriteria, magoffin.matt.ma2.biz.BizContext)
	 */
	public SearchResults findMediaItems(MediaItemSearchCriteria criteria,
			PaginationCriteria pagination, BizContext context) {
		return null;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.biz.SearchBiz#findUsersForIndex(magoffin.matt.ma2.domain.PaginationCriteria, magoffin.matt.ma2.biz.BizContext)
	 */
	public SearchResults findUsersForIndex(PaginationCriteria pagination,
			BizContext context) {
		return null;
	}

}
