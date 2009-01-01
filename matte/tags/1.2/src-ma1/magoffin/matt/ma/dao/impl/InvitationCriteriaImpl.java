/* ===================================================================
 * InvitationCriteriaImpl.java
 * 
 * Created Jan 21, 2004 3:16:54 PM
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
 * $Id: InvitationCriteriaImpl.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao.impl;

import magoffin.matt.ma.dao.InvitationCriteria;

/**
 * Implementation of InvitationCriteria utilizing GeRDAL.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public class InvitationCriteriaImpl
extends AbstractGeRDALCriteria
implements InvitationCriteria 
{
	/** The GeRDAL search equal alias for user ID: <code>userid</code> */
	public static final String EQUAL_ALIAS_USERID = "userid";

	/** The GeRDAL search equal alias for email: <code>email</code> */
	public static final String EQUAL_ALIAS_EMAIL = "email";

	/** The search equal aliases for user ID and email: <code>[userid,email]</code> */
	public static final String[] EQUAL_ALIASES_USERID_EMAIL = 
		new String[] { EQUAL_ALIAS_USERID, EQUAL_ALIAS_EMAIL };

	
/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.impl.AbstractGeRDALCriteria#getTableReferenceKey()
 */
protected String getTableReferenceKey() {
	return DAOImplConstants.INVITATION_TABLE_REFERENCE_KEY;
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
		case PENDING_INVITATION_SEARCH:
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
			setSearchAliases(EQUAL_ALIASES_USERID_EMAIL);
			break;
		
	}
}

}
