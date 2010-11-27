/* ===================================================================
 * AlbumCriteriaImpl.java
 * 
 * Created Dec 16, 2003 8:18:17 PM
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
 * $Id: AlbumCriteriaImpl.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao.impl;

import magoffin.matt.ma.dao.AlbumCriteria;
import magoffin.matt.util.ArrayUtil;

/**
 * Implementation of AlbumCriteria utilizing GeRDAL.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public class AlbumCriteriaImpl
extends AbstractGeRDALCriteria
implements AlbumCriteria {

	/** The search equal alias for finding albums owned by a user: <code>owner</code> */
	public static final String SEARCH_ALBUMS_FOR_USER = "owner";

	/** The search equal alias for finding an album by key: <code>key</code> */
	public static final String SEARCH_ALBUMS_FOR_KEY = "key";

	/** The search equal alias for finding an children albums of an album: <code>parent</code> */
	public static final String SEARCH_CHILDREN_OF_ALBUM = "parent";

	/** 
	 * The join search alias for finding an album by key containing 
	 * anitem of a given ID.
	 */
	public static final String SEARCH_ITEM_ID_ALBUM_KEY = "item-key";
	
	public static final String JOIN_SEARCH_VIEWABLE_USER = "albums-viewable-user";
	
	public static final String JOIN_SEARCH_VIEWABLE_GROUP = "albums-viewable-group";
	
	public static final String CUSTOM_SQL_ITEM_COUNT_FOR_USER = "item-count-user";
	
	public static final String CUSTOM_SQL_ALBUMS_FOR_USR = "albums-user";

	public static final String CUSTOM_SQL_ALBUMS_FOR_ITEMS = "albums-items";
	
	public static final String CUSTOM_SQL_ALBUMS_USING_THEME = "album-theme-count";

	public static final String CUSTOM_SQL_ALBUMS_USING_DEFAULT_THEME = "album-theme-count-default";

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.impl.AbstractGeRDALCriteria#getTableReferenceKey()
 */
protected String getTableReferenceKey() {
	return DAOImplConstants.ALBUM_TABLE_REFERENCE_KEY;
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
		case ALBUMS_FOR_OWNER:
			if ( query != null ) {
				setValue(query);
			}
			setSearchAlias(SEARCH_ALBUMS_FOR_USER);
			break;
			
		case ALBUM_FOR_KEY_SEARCH:
			if ( query != null ) {
				setValue(query);
			}
			setSearchAlias(SEARCH_ALBUMS_FOR_KEY);
			break;
			
		case CHILDREN_OF_ALBUM_SEARCH:
			if ( query != null ) {
				setValue(query);
			}
			setSearchAlias(SEARCH_CHILDREN_OF_ALBUM);
			break;
			
			
		case ALBUM_FOR_ITEM_KEY:
			if ( query != null ) {
				if ( query instanceof String[] ) {
					setValues((String[])query);
				} else {
					Object[] query = (Object[])this.query;
					Object[] values = new Object[2];
					values[0] = query[0];
					values[1] = query[1];
					setValues(values);
				}
			}
			setJoinSearchAlias(SEARCH_ITEM_ID_ALBUM_KEY);
			break;
			
		case VIEWABLE_BY_USER:
			if ( query != null ) {
				setValues(new Object[] {query,Boolean.TRUE});
			}
			setJoinSearchAlias(JOIN_SEARCH_VIEWABLE_USER);
			break;
			
		case VIEWABLE_BY_GROUP:
			if ( query != null ) {
				setValues(new Object[] {query,Boolean.TRUE});
			}
			setJoinSearchAlias(JOIN_SEARCH_VIEWABLE_GROUP);
			break;
					
		case ITEM_COUNT_FOR_USER:
			setCustomSqlAlias(CUSTOM_SQL_ITEM_COUNT_FOR_USER);
			if ( query != null ) {
				Integer[] ids = (Integer[])query;
				Object[] params = new Object[6];
				params[0] = ids[0];
				params[2] = params[5] = Boolean.TRUE;
				params[1] = params[3] = params[4] = ids[1];
				setCustomSqlParams(params);
			}
			setCountOnly(true);
			break;
			
		case ALBUMS_FOR_USER:
			setCustomSqlAlias(CUSTOM_SQL_ALBUMS_FOR_USR);
			if ( query != null ) {
				Integer[] ids = (Integer[])query;
				Object[] params = new Object[6];
				params[0] = ids[0];
				params[2] = params[5] = Boolean.TRUE;
				params[1] = params[3] = params[4] = ids[1];
				setCustomSqlParams(params);
			}
			break;
			
		case ALBUMS_CONTAINING_ITEMS:
			if ( query != null ) {
				setCustomSqlAlias(CUSTOM_SQL_ALBUMS_FOR_ITEMS);
				Integer[] ids = (Integer[])query;
				String in  = ArrayUtil.join(ids,',',-1);
				setCustomSqlDynamicParams(new Object[] {in});
			}
			break;
			
		case ALBUMS_USING_DEFAULT_THEME_COUNT:
			if ( query != null ) {
				Integer id = (Integer)query;
				setCustomSqlAlias(CUSTOM_SQL_ALBUMS_USING_DEFAULT_THEME);
				setCustomSqlDynamicParams(new Object[]{id});
				setCountOnly(true);
			}
			break;
			
		case ALBUMS_USING_THEME_COUNT:
			if ( query != null ) {
				Integer id = (Integer)query;
				setCustomSqlAlias(CUSTOM_SQL_ALBUMS_USING_THEME);
				setCustomSqlDynamicParams(new Object[]{id});
				setCountOnly(true);
			}
			break;
			
	}
}

}
