/* ===================================================================
 * RegisterCriteriaImpl.java
 * 
 * Created Jan 22, 2004 8:37:54 AM
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
 * $Id: RegistrationCriteriaImpl.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao.impl;

import magoffin.matt.ma.dao.RegistrationCriteria;

/**
 * Implementation of RegistrationCriteria utilizing GeRDAL.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public class RegistrationCriteriaImpl
extends AbstractGeRDALCriteria
implements RegistrationCriteria 
{
	/** The GeRDAL search equal alias for username: <code>username</code> */
	public static final String EQUAL_ALIAS_USERNAME = "username";

	/** The GeRDAL search equal alias for email: <code>email</code> */
	public static final String EQUAL_ALIAS_EMAIL = "email";

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.impl.AbstractGeRDALCriteria#getTableReferenceKey()
 */
protected String getTableReferenceKey() {
	return DAOImplConstants.REGISTRATION_TABLE_REFERENCE_KEY;
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
		case SEARCH_USERNAME:
			if ( query != null ) {
				setValue(query.toString());
			}
			setSearchAlias(EQUAL_ALIAS_USERNAME);
			break;
		
		case SEARCH_EMAIL:
			if ( query != null ) {
				setValue(query.toString());
			}
			setSearchAlias(EQUAL_ALIAS_EMAIL);
			break;
	}
}

}
