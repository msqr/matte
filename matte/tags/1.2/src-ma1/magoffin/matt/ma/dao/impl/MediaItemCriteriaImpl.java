/* ===================================================================
 * MediaItemCriteriaImpl.java
 * 
 * Created Dec 22, 2003 1:18:14 PM
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
 * $Id: MediaItemCriteriaImpl.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao.impl;

import magoffin.matt.ma.dao.MediaItemCriteria;

/**
 * Implementation of MediaItemCriteria utilizing GeRDAL.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public class MediaItemCriteriaImpl
	extends AbstractGeRDALCriteria
	implements MediaItemCriteria 
{
	/** The search equal alias for media items in a collection: <code>dir</code> */
	public static final String ITEMS_FOR_COLLECTION_ALIAS = "dir";

	/** The search equal aliases for media items in a collection with a path: <code>[dir,path]</code> */
	public static final String[] ITEM_FOR_COLLECTION_AND_PATH = 
			new String[] { ITEMS_FOR_COLLECTION_ALIAS, "path" };

	/** The join alias for media items in an album: <code>album-media</code> */
	public static final String ITEMS_FOR_ALBUM_ALIAS = "album-media";
	
	/**
	 * The update alias for hit data: <code>hits</code>
	 */
	public static final String UPDATE_ALIAS_HITS = "hits";

	/**
	 * The update alias for timezone data: <code>timezone</code>
	 */
	public static final String UPDATE_ALIAS_TIMEZONE = "timezone";
	
	/**
	 * The update alias for custom type: <code>type</code>
	 */
	public static final String UPDATE_ALIAS_CUSTOM_TYPE = "type";
	
	/**
	 * The custom SQL alias for items for user: <code>items-user</code>
	 */
	public static final String CUSTOM_SQL_ITEMS_FOR_USER = "items-user";

	/**
	 * The custom SQL alias for item for user: <code>item-user</code>
	 */
	public static final String CUSTOM_SQL_ITEM_FOR_USER = "item-user";

	/**
	 * The custom SQL alias for items for index: <code>items-index</code>
	 */
	public static final String CUSTOM_SQL_ITEMS_FOR_INDEX = "items-index";
	
	/** The custom SQL alias for the total size of items for a user: <code>total-size</code>. */
	public static final String CUSTOM_SQL_TOTAL_SIZE = "total-size";

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.impl.AbstractGeRDALCriteria#handleSearchDataChange()
 */
protected void handleSearchDataChange() {
	super.handleSearchDataChange();
	switch ( searchType ) {
		case ITEMS_FOR_COLLECTION:
			if ( query != null ) {
				setValue(query.toString());
			}
			setSearchAlias(ITEMS_FOR_COLLECTION_ALIAS);
			break;
			
		case ITEMS_FOR_ALBUM:
			if ( query != null ) {
				setValue(query.toString());
			}
			setJoinSearchAlias(ITEMS_FOR_ALBUM_ALIAS);
			break;
			
		case ITEM_FOR_PATH:
			if ( query != null ) {
				if ( query instanceof String[] ) {
					setValues((String[])query);
				} else {
					Object[] query = (Object[])this.query;
					String[] values = new String[2];
					values[0] = query[0].toString();
					values[1] = query[1].toString();
					setValues(values);
				}
			}
			setSearchAliases(ITEM_FOR_COLLECTION_AND_PATH);
			break;
			
		case UPDATE_FOR_HITS:
			setUpdateAlias(UPDATE_ALIAS_HITS);
			break;

		case UPDATE_FOR_TIMEZONE:
			setUpdateAlias(UPDATE_ALIAS_TIMEZONE);
			break;

		case ITEMS_VIEWABLE_FOR_USER:
			setCustomSqlAlias(CUSTOM_SQL_ITEMS_FOR_USER);
			if ( query != null ) {
				Integer[] ids = (Integer[])query;
				Object[] params = new Object[8];
				params[0] = params[2] = ids[0];
				params[1] = params[3] = params[5] = params[6] = ids[1];
				params[4] = params[7] = Boolean.TRUE;
				setCustomSqlParams(params);
			}
			break;
			
		case ITEMS_COUNT_VIEWABLE_FOR_USER:
			setCustomSqlAlias(CUSTOM_SQL_ITEMS_FOR_USER);
			setCountOnly(true);
			if ( query != null ) {
				Integer[] ids = (Integer[])query;
				Object[] params = new Object[8];
				params[0] = params[2] = ids[0];
				params[1] = params[3] = params[5] = params[6] = ids[1];
				params[4] = params[7] = Boolean.TRUE;
				setCustomSqlParams(params);
			}
			break;
			
		case IS_ITEM_VIEWABLE_FOR_USER:
			setCustomSqlAlias(CUSTOM_SQL_ITEM_FOR_USER);
			setCountOnly(true);
			if ( query != null ) {
				Integer[] ids = (Integer[])query;
				Object[] params = new Object[9];
				params[0] = ids[0];
				params[1] = params[3] = ids[1];
				params[2] = params[4] = params[6] = params[7] = ids[2];
				params[5] = params[8] = Boolean.TRUE;
				setCustomSqlParams(params);
			}
			break;
			
		case ALL_MEDIA_ITEMS:
			// nothing to do
			break;
			
		case ALL_MEDIA_ITEMS_INDEX:
			setCustomSqlAlias(CUSTOM_SQL_ITEMS_FOR_INDEX);
			break;

		case UPADTE_FOR_CUSTOM_TYPE:
			setUpdateAlias(UPDATE_ALIAS_CUSTOM_TYPE);
			break;
			
		case TOTAL_SIZE_FOR_OWNER:
			if ( query != null ) {
				setCustomSqlAlias(CUSTOM_SQL_TOTAL_SIZE);
				Object[] params = new Object[1];
				params[0] = query;
				setCustomSqlParams(params);
			}
			break;

	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.impl.AbstractGeRDALCriteria#getTableReferenceKey()
 */
protected String getTableReferenceKey() {
	return DAOImplConstants.MEDIA_ITEM_TABLE_REFERENCE_KEY;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.impl.AbstractGeRDALCriteria#getDefaultSearchType()
 */
protected int getDefaultSearchType() {
	return UNDEFINED_SEARCH;
}

}
