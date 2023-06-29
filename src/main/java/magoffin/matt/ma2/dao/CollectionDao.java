/* ===================================================================
 * CollectionDao.java
 * 
 * Created Mar 2, 2006 7:14:27 PM
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
 */

package magoffin.matt.ma2.dao;

import java.util.List;

import magoffin.matt.dao.GenericDao;
import magoffin.matt.ma2.domain.Collection;

/**
 * DAO for Collection objects.
 * 
 * @author matt.magoffin
 * @version 1.0
 */
public interface CollectionDao extends GenericDao<Collection, Long> {

	/**
	 * Find all collections owned by a given user.
	 * 
	 * @param userId the ID of the user to find the collections for
	 * @return list of found collections, or empty List if none found
	 */
	List<Collection> findCollectionsForUser(Long userId);
	
	/**
	 * Get the collection a MediaItem is in.
	 * 
	 * @param mediaItemId the ID of the media item
	 * @return the collection
	 */
	Collection getCollectionForMediaItem(Long mediaItemId);
	
	/**
	 * Get an Collection with it's items fully populated.
	 * 
	 * @param collectionId the ID of the collection
	 * @return the Collection, or <em>null</em> if not found
	 */
	Collection getCollectionWithItems(Long collectionId);

}
