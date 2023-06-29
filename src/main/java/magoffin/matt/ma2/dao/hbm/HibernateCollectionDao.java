/* ===================================================================
 * HibernateCollectionDao.java
 * 
 * Created Sep 19, 2005 7:19:59 PM
 * 
 * Copyright (c) 2005 Matt Magoffin.
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

package magoffin.matt.ma2.dao.hbm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import magoffin.matt.dao.BasicIndexData;
import magoffin.matt.dao.hbm.GenericIndexableHibernateDao;
import magoffin.matt.ma2.dao.CollectionDao;
import magoffin.matt.ma2.domain.Collection;

/**
 * Hibernate implementation of {@link magoffin.matt.ma2.dao.CollectionDao}.
 * 
 * @author matt.magoffin
 * @version 1.2
 */
public class HibernateCollectionDao extends GenericIndexableHibernateDao<Collection, Long>
		implements CollectionDao {

	/** Find all Collections for a User ID. */
	public static final String QUERY_COLLECTIONS_FOR_USER_ID = "CollectionsForUserId";

	/** Find a Collection for an Item ID. */
	public static final String QUERY_COLLECTION_FOR_ITEM_ID = "CollectionForItemId";

	/**
	 * Default constructor.
	 */
	public HibernateCollectionDao() {
		super(Collection.class);
	}

	@Override
	protected Long getPrimaryKey(Collection domainObject) {
		if ( domainObject == null )
			return null;
		return domainObject.getCollectionId();
	}

	@Override
	protected void populateIndexDataId(BasicIndexData<Long> callbackData, ResultSet rs)
			throws SQLException {
		callbackData.setId(new Long(rs.getLong(getIndexObjectIdColumnName())));
	}

	@Override
	public List<Collection> findCollectionsForUser(Long userId) {
		return findByNamedQuery(QUERY_COLLECTIONS_FOR_USER_ID, new Object[] { userId });
	}

	@Override
	public Collection getCollectionForMediaItem(Long mediaItemId) {
		List<Collection> results = findByNamedQuery(QUERY_COLLECTION_FOR_ITEM_ID,
				new Object[] { mediaItemId });
		if ( results.size() < 1 )
			return null;
		return results.get(0);
	}

	@Override
	public Collection getCollectionWithItems(Long collectionId) {
		Collection c = get(collectionId);
		if ( c != null ) {
			fillInCollectionItems(c);
		}
		return c;
	}

	private void fillInCollectionItems(Collection c) {
		// FIXME why does this not work: getHibernateTemplate().initialize(a.getItem());
		c.getItem().size();
	}

}
