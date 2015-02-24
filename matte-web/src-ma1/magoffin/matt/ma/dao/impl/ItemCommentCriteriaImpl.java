/* ===================================================================
 * ItemCommentCriteriaImpl.java
 * 
 * Created Feb 18, 2004 6:13:46 PM
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
 * $Id: ItemCommentCriteriaImpl.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao.impl;

import magoffin.matt.ma.dao.ItemCommentCriteria;
import magoffin.matt.util.StringUtil;

/**
 * Implementation of ItemCommentCriteria utilizing GeRDAL.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public class ItemCommentCriteriaImpl extends AbstractGeRDALCriteria 
implements ItemCommentCriteria 
{
	
	/** The search equal alias for finding by item ID: <code>item</code> */
	public static final String SEARCH_EQUAL_ALIAS_ITEM = "item";
	
	/**
	 * The custom SQL alias for finding comments that match multiple 
	 * media item IDs.
	 */
	public static final String CUSTOM_SQL_ALIAS_MULTI_ITEMS = "comments-for-items";

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.impl.AbstractGeRDALCriteria#getTableReferenceKey()
 */
protected String getTableReferenceKey() {
	return DAOImplConstants.MEDIA_ITEM_COMMENT_TABLE_REFERENCE_KEY;
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
		case COMMENTS_FOR_ITEM_SEARCH:
			if ( query != null ) {
				setValue(query.toString());
			}
			setSearchAlias(SEARCH_EQUAL_ALIAS_ITEM);
			break;
		
		case COMMENTS_FOR_ITEMS_SEARCH:
			if ( query != null ) {
				Object[] array = (Object[])query;
				String str = StringUtil.valueOf(array,",",null,null);
				setCustomSqlDynamicParams(new Object[] {str});
			}
			setCustomSqlAlias(CUSTOM_SQL_ALIAS_MULTI_ITEMS);
			break;
	}
}

}
