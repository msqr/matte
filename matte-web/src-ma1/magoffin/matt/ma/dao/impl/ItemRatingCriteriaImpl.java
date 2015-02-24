/* ===================================================================
 * ItemRatingCriteriaImpl.java
 * 
 * Created Feb 23, 2004 2:35:56 PM
 * 
 * Copyright (c) 2004 Matt Magoffin.
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
 * $Id: ItemRatingCriteriaImpl.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao.impl;

import magoffin.matt.ma.dao.ItemRatingCriteria;
import magoffin.matt.util.StringUtil;

/**
 * Implementation of ItemRatingCriteria utilizing GeRDAL.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public class ItemRatingCriteriaImpl extends AbstractGeRDALCriteria
implements ItemRatingCriteria
{
	
	/** The search equal alias for finding by item ID: <code>item</code> */
	public static final String SEARCH_EQUAL_ALIAS_ITEM = "item";
	
	/**
	 * The custom SQL alias for finding ratings that match multiple 
	 * media item IDs.
	 */
	public static final String CUSTOM_SQL_ALIAS_MULTI_ITEMS = "ratings-for-items";
	
	public static final String[] SEARCH_EQUALS_ITEM_USER = 
			new String[] {"item","user"};

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.impl.AbstractGeRDALCriteria#getTableReferenceKey()
 */
protected String getTableReferenceKey()
{
	return DAOImplConstants.MEDIA_ITEM_RATING_TABLE_REFERENCE_KEY;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.impl.AbstractGeRDALCriteria#getDefaultSearchType()
 */
protected int getDefaultSearchType()
{
	return UNDEFINED_SEARCH;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.impl.AbstractGeRDALCriteria#handleSearchDataChange()
 */
protected void handleSearchDataChange() 
{
	super.handleSearchDataChange();
	switch ( searchType ) {
		case RATINGS_FOR_ITEM_SEARCH:
			if ( query != null ) {
				setValue(query.toString());
			}
			setSearchAlias(SEARCH_EQUAL_ALIAS_ITEM);
			break;
		
		case RATINGS_FOR_ITEMS_SEARCH:
			if ( query != null ) {
				Object[] array = (Object[])query;
				String str = StringUtil.valueOf(array,",",null,null);
				setCustomSqlDynamicParams(new Object[] {str});
			}
			setCustomSqlAlias(CUSTOM_SQL_ALIAS_MULTI_ITEMS);
			break;
			
		case RATING_FOR_USER:
			if ( query != null ) {
				Object[] ids = (Object[])query;
				String[] values = new String[] {
						ids[0].toString(),
						ids[1].toString()
				};
				setValues(values);
			}
			setSearchAliases(SEARCH_EQUALS_ITEM_USER);
			break;
	}
}

}
