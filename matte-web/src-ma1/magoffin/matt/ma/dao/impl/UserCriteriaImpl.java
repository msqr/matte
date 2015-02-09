/* ===================================================================
 * UserCriteriaImpl.java
 * 
 * Created Dec 2, 2003 7:27:58 PM
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
 * $Id: UserCriteriaImpl.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao.impl;

import magoffin.matt.ma.MediaAlbumRuntimeException;
import magoffin.matt.ma.dao.UserCriteria;

/**
 * Implementation of UserCriteria utilizing GeRDAL.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public class UserCriteriaImpl extends AbstractGeRDALCriteria implements UserCriteria 
{
	/** The search equal alias for finding a user by their anonymous key: <code>key</code> */
	public static final String SEARCH_USERS_BY_ANONYMOUS_KEY = "key";

	/** The GeRDAL join alias for friends of user: <code>user-friends</code> */
	public static final String USER_FRIENDS_JOIN_ALIAS = "user-friends";

	/** The GeRDAL join alias for users of group: <code>group-users</code> */
	public static final String GROUP_USERS_JOIN_ALIAS = "group-users";
	
	/** The search like alias for finding a user by their email: <code>email</code> */
	public static final String SEARCH_USERS_BY_EMAIL = "email";

	/** The search like equal for finding a user by their email: <code>email</code> */
	public static final String SEARCH_USERS_BY_EMAIL_EXACT = "email";

	/** The search like alias for finding a user by their name: <code>name</code> */
	public static final String SEARCH_USERS_BY_NAME = "name";

	/** The search like alias for finding a user by their email: <code>username</code> */
	public static final String SEARCH_USERS_BY_USERNAME = "username";

	/** The search equal alias for finding a user by their browse theme ID: <code>theme</code>. */
	public static final String SEARCH_THEME_ID = "theme";

	/** 
	 * The custom SQL alias for finding users using the default browse theme:
	 * <code>user-theme-default</code>.
	 */
	public static final String CUSTOM_SQL_USERS_USING_DEFAULT_THEME = "user-theme-default";

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.impl.AbstractGeRDALCriteria#handleSearchDataChange()
 */
protected void handleSearchDataChange() {
	super.handleSearchDataChange();
	switch ( searchType ) {
		case USERNAME_SEARCH:
			if ( query != null ) {
				setValue(query.toString());
			}
			break;
			
		case FRIENDS_OF_USER_SEARCH:
			if ( query != null ) {
				setValue(query.toString());
			}
			setJoinSearchAlias(USER_FRIENDS_JOIN_ALIAS);
			break;
			
		case USERS_IN_GROUP_SEARCH:
			if ( query != null ) {
				setValue(query.toString());
			}
			setJoinSearchAlias(GROUP_USERS_JOIN_ALIAS);
			break;
			
		case ANONYMOUS_KEY_SEARCH:
			if ( query != null ) {
				setValue(query.toString());
			}
			setSearchAlias(SEARCH_USERS_BY_ANONYMOUS_KEY);
			break;
			
		case EMAIL_SEARCH:
			if ( query != null ) {
				setLike(query.toString());
			}
			setSearchAlias(SEARCH_USERS_BY_EMAIL);
			break;
			
		case EMAIL_SEARCH_EXACT:
			if ( query != null ) {
				setValue(query.toString());
			}
			setSearchAlias(SEARCH_USERS_BY_EMAIL_EXACT);
			break;
			
		case NAME_SEARCH:
			if ( query != null ) {
				setLike(query.toString());
			}
			setSearchAlias(SEARCH_USERS_BY_NAME);
			break;
			
		case USERNAME_SUB_SEARCH:
			if ( query != null ) {
				setLike(query.toString());
			}
			setSearchAlias(SEARCH_USERS_BY_USERNAME);
			break;
			
		case USERS_USING_DEFAULT_THEME_COUNT:
			if ( query != null ) {
				setCustomSqlAlias(CUSTOM_SQL_USERS_USING_DEFAULT_THEME);
				setCustomSqlParams(new Object[]{query});
				setCountOnly(true);
			}
			break;
			
		case USERS_USING_THEME_COUNT:
			if ( query != null ) {
				setSearchAlias(SEARCH_THEME_ID);
				setValue(query);
				setCountOnly(true);
			}
			break;
			
		default:
			throw new MediaAlbumRuntimeException("Unsupported search type");
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.impl.AbstractGeRDALCriteria#getTableReferenceKey()
 */
protected String getTableReferenceKey() {
	return DAOImplConstants.USER_TABLE_REFERENCE_KEY;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.impl.AbstractGeRDALCriteria#getDefaultSearchType()
 */
protected int getDefaultSearchType() {
	return UNDEFINED_SEARCH;
}

}
