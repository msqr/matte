/* ===================================================================
 * SearchBiz.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Mar 29, 2004 10:48:13 AM.
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
 * $Id: SearchBiz.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.biz;

import magoffin.matt.biz.Biz;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.search.IndexParams;
import magoffin.matt.ma.search.MediaItemQuery;
import magoffin.matt.ma.search.MediaItemResults;
import magoffin.matt.ma.xsd.MediaItem;
import magoffin.matt.ma.xsd.User;

/**
 * Biz interface for indexing/searching.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public interface SearchBiz extends Biz 
{
	
/**
 * Search for media items.
 * 
 * @param query the query
 * @param actingUser the acting user
 * @return the results
 * @throws MediaAlbumException if an error occurs
 */
public MediaItemResults search(MediaItemQuery query, User actingUser) 
throws MediaAlbumException;

/**
 * Index a MediaItem using an IndexParam object.
 * 
 * <p>To index associated FreeData, ItemComment, and ItemRating objects 
 * they must be populated in <var>item</var> before calling this method.</p>
 * 
 * <p><b>Note:</b> this method is generally only called from within SearchBiz
 * implementations. Use the {@link #index(Integer)} method to index
 * MediaItem object.</p>
 * 
 * @param item the item to index
 * @param params the index parameters
 * @throws MediaAlbumException if an error occurs
 * @see #index(Integer)
 */
public void index(MediaItem item, IndexParams params) 
throws MediaAlbumException;

/**
 * Index a MediaItem.
 * 
 * <p>This method should handle both indexing a Media Item that hasn't been
 * indexed before as well as updating an item that is already indexed.</p>
 * 
 * @param itemId the ID of the item to index
 * @throws MediaAlbumException if an error occurs
 */
public void index(Integer itemId) throws MediaAlbumException;

/**
 * Remove a MediaItem from the index.
 * 
 * @param itemId the ID of the media item to remove
 * @throws MediaAlbumException if an error occurs
 */
public void removeMediaItem(Integer itemId) throws MediaAlbumException;

/**
 * Recreate the entire set of application search indicies.
 * 
 * @throws MediaAlbumException if an error occurs
 */
public void recreateEntireIndex() throws MediaAlbumException;
}
