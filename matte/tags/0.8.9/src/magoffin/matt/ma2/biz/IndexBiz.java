/* ===================================================================
 * IndexBiz.java
 * 
 * Created May 26, 2006 3:42:58 PM
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
 * $Id: IndexBiz.java,v 1.2 2006/10/08 20:23:05 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.biz;

import magoffin.matt.ma2.biz.WorkBiz.WorkInfo;

/**
 * API for search index indexing methods.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.2 $ $Date: 2006/10/08 20:23:05 $
 */
public interface IndexBiz {
	
	/**
	 * Index a user object based on its ID.
	 * 
	 * <p>This method must support indexing a user that may 
	 * or may not have been indexed before.</p>
	 * 
	 * @param userId the ID of the user to index
	 */
	public void indexUser(Long userId);
	
	/**
	 * Remove a user from the index.
	 * 
	 * @param userId the ID of the user to remove
	 */
	public void removeUserFromIndex(Long userId);
	
	/**
	 * Rebuild the entire user index.
	 * 
	 * @param context the current context
	 * @return the work info
	 */
	public WorkInfo recreateUserIndex(BizContext context);

	/**
	 * Index a MediaItem object based on its ID.
	 * 
	 * <p>This method must support indexing an item that may 
	 * or may not have been indexed before.</p>
	 * 
	 * @param itemId the ID of the item to index
	 */
	public void indexMediaItem(Long itemId);
	
	/**
	 * Remove a MediaItem from the index.
	 * 
	 * @param itemId the ID of the item to remove
	 */
	public void removeMediaItemFromIndex(Long itemId);
	
	/**
	 * Rebuild the entire MediaItem index.
	 * 
	 * @param context the current context
	 * @return the work info
	 */
	public WorkInfo recreateMediaItemIndex(BizContext context);

}
