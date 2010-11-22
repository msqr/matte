/* ===================================================================
 * AlbumThemeCriteriaImpl.java
 * 
 * Created Dec 28, 2003 11:02:59 AM
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
 * $Id: AlbumThemeCriteriaImpl.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao.impl;

import magoffin.matt.ma.dao.AlbumThemeCriteria;

/**
 * Implementation of AlbumThemeCriteria utilizing GeRDAL.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public class AlbumThemeCriteriaImpl
	extends AbstractGeRDALCriteria
	implements AlbumThemeCriteria 
{

	/** The search key for searching by owner. */
	public static final String SEARCH_BY_OWNER_KEY = "owner";

	/** The search key for searching by  global status. */
	public static final String SEARCH_BY_GLOBAL_KEY = "global";
	
	/** The custom SQL key for searching by viewable for user. */
	public static final String CUSTOM_SQL_VIEWABLE_FOR_USER = "user-view";

	/** The custom SQL key for searching by global except for an owner. */
	public static final String CUSTOM_SQL_GLOBAL_EXCEPT_USER = "global-user";

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.impl.AbstractGeRDALCriteria#getTableReferenceKey()
 */
protected String getTableReferenceKey() {
	return DAOImplConstants.ALBUM_THEME_TABLE_REFERENCE_KEY;
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
protected void handleSearchDataChange() 
{
	super.handleSearchDataChange();
	switch ( searchType ) {
		case SEARCH_BY_GLOBAL:
			setValue("true");
			setSearchAlias(SEARCH_BY_GLOBAL_KEY);
			break;
			
		case SEARCH_BY_OWNER:
			if ( query != null ) {
				setValue(query.toString());
			}
			setSearchAlias(SEARCH_BY_OWNER_KEY);
			break;
			
		case SEARCH_VIEWABLE_FOR_USER:
			if ( query != null ) {
				Object[] params = new Object[2];
				params[0] = query;
				params[1] = Boolean.TRUE;
				setCustomSqlParams(params);
			}
			setCustomSqlAlias(CUSTOM_SQL_VIEWABLE_FOR_USER);
			break;
			
		case SEARCH_BY_GLOBAL_EXCEPT_USER:
			if ( query != null ) {
				Object[] params = new Object[2];
				params[0] = query;
				params[1] = Boolean.TRUE;
				setCustomSqlParams(params);
			}
			setCustomSqlAlias(CUSTOM_SQL_GLOBAL_EXCEPT_USER);
			break;
			
		case SEARCH_ALL:
			// nothing to do
			break;
					
	}
}

}
