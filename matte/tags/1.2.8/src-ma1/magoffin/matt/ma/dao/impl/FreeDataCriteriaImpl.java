/* ===================================================================
 * FreeDataCriteriaImpl.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Mar 24, 2004 11:26:33 AM.
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
 * $Id: FreeDataCriteriaImpl.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao.impl;

import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.dao.FreeDataCriteria;
import magoffin.matt.util.ArrayUtil;

/**
 * Implementation of FreeDataCriteria utilizing GeRDAL.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public class FreeDataCriteriaImpl extends AbstractGeRDALCriteria
implements FreeDataCriteria 
{
	public static final String SEARCH_EQUAL_ALIAS_ALBUM = "album";
	public static final String SEARCH_EQUAL_ALIAS_COLLECTION = "collection";
	public static final String SEARCH_EQUAL_ALIAS_ITEM = "item";
	public static final String SEARCH_EQUAL_ALIAS_OWNER = "owner";
	public static final String SEARCH_EQUAL_ALIAS_USER = "user";
	public static final String SEARCH_EQUAL_ALIAS_DATA_TYPE = "type";
	
	/** Search equal keys for finding user-assigned free data */
	public static final String[] SEARCH_EQUALS_FOR_OWNER = new String[] {
			SEARCH_EQUAL_ALIAS_OWNER, SEARCH_EQUAL_ALIAS_ALBUM,
			SEARCH_EQUAL_ALIAS_COLLECTION, SEARCH_EQUAL_ALIAS_ITEM
	};
	
	public static final String[] SEARCH_EQUALS_FOR_WATCHING_USER = new String[] {
			SEARCH_EQUAL_ALIAS_DATA_TYPE, SEARCH_EQUAL_ALIAS_USER
	};
	
	/**
	 * The custom SQL alias for finding free data that match multiple 
	 * media item IDs.
	 */
	public static final String CUSTOM_SQL_ALIAS_MULTI_ITEMS = "fdata-for-items";

	/**
	 * The custom SQL alias for finding free data that match multiple 
	 * album IDs.
	 */
	public static final String CUSTOM_SQL_ALIAS_MULTI_ALBUMS = "fdata-for-albums";

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.impl.AbstractGeRDALCriteria#getTableReferenceKey()
 */
protected String getTableReferenceKey() {
	return DAOImplConstants.FREE_DATA_TABLE_REFERENCE_KEY;
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
		case FREE_DATA_FOR_OWNER:
			if ( query != null ) {
				Object[] params = new Object[] {
						query, 
						null,
						null,
						null
				};
				setValues(params);
				setSearchAliases(SEARCH_EQUALS_FOR_OWNER);
			}
			break;
			
		case FREE_DATA_FOR_ITEMS:
			if ( query != null ) {
				Object[] array = (Object[])query;
				String str = ArrayUtil.join(array,',',-1);
				setCustomSqlDynamicParams(new Object[] {str});
				setCustomSqlAlias(CUSTOM_SQL_ALIAS_MULTI_ITEMS);
			}
			break;
			
		case FREE_DATA_FOR_ITEM:
			if ( query != null ) {
				setValue(query);
				setSearchAlias(SEARCH_EQUAL_ALIAS_ITEM);
			}
			break;

		case FREE_DATA_FOR_ALBUMS:
			if ( query != null ) {
				Object[] array = (Object[])query;
				String str = ArrayUtil.join(array,',',-1);
				setCustomSqlDynamicParams(new Object[] {str});
				setCustomSqlAlias(CUSTOM_SQL_ALIAS_MULTI_ALBUMS);
			}
			break;
			
		case FREE_DATA_FOR_ALBUM:
			if ( query != null ) {
				setValue(query);
				setSearchAlias(SEARCH_EQUAL_ALIAS_ALBUM);
			}
			break;
		
		case FREE_DATA_FOR_WATCHING_USER:
			if ( query != null ) {
				Object[] params = new Object[] {
						ApplicationConstants.FREE_DATA_TYPE_EMAIL_NOTIFICATION, 
						query
				};
				setValues(params);
				setSearchAliases(SEARCH_EQUALS_FOR_WATCHING_USER);
			}
			break;
}
}

}
