/* ===================================================================
 * CollectionCriteriaImpl.java
 * 
 * Created Dec 15, 2003 8:36:49 AM
 * 
 * Copyright (c) 2003 Matt Magoffin.
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
 * $Id: CollectionCriteriaImpl.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao.impl;

import magoffin.matt.ma.dao.CollectionCriteria;
import magoffin.matt.util.ArrayUtil;

/**
 * Implementation of CollectionCriteria utilizing GeRDAL.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public class CollectionCriteriaImpl
extends AbstractGeRDALCriteria
implements CollectionCriteria {

	/** The search equal alias for finding collections owned by a user: <code>user</code> */
	public static final String SEARCH_COLLECTIONS_FOR_USER = "user";
	
	/** 
	 * The custom SQL alias for finding collections for items: 
	 * <code>collections-for-items</code> 
	 */
	public static final String CUSTOM_SQL_COLLECTIONS_FOR_ITEMS = "collections-for-items";

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.impl.AbstractGeRDALCriteria#getTableReferenceKey()
 */
protected String getTableReferenceKey() {
	return DAOImplConstants.COLLECTION_TABLE_REFERENCE_KEY;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.impl.AbstractGeRDALCriteria#getDefaultSearchType()
 */
protected int getDefaultSearchType() {
	return UNDEFINED_SEARCH;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.impl.AbstractGeRDALCriteria#handleSearchDataChange()
 */
protected void handleSearchDataChange() {
	super.handleSearchDataChange();
	switch ( searchType ) {
		case COLLECTIONS_FOR_USER_SEARCH:
			if ( query != null ) {
				setValue(query.toString());
			}
			setSearchAlias(SEARCH_COLLECTIONS_FOR_USER);
			break;
			
		case COLLECTIONS_CONTAINING_ITEMS:
			if ( query != null ) {
				setCustomSqlAlias(CUSTOM_SQL_COLLECTIONS_FOR_ITEMS);
				Integer[] ids = (Integer[])query;
				String in  = ArrayUtil.join(ids,',',-1);
				setCustomSqlDynamicParams(new Object[]{in});
			}
			break;
	}
}

}
