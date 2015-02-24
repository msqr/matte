/* ===================================================================
 * MemberDAO.java
 * 
 * Copyright (c) 2002 Matt Magoffin.
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
 * $Id: MemberDAO.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao;

import magoffin.matt.ma.dao.impl.DAOImplConstants;

/**
 * DAO for group member items.
 * 
 * <p> Created on Nov 6, 2002 9:41:10 AM.</p>
 *
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class MemberDAO extends AbstractGeRDALDAO 
{

/**
 * Constructor for MemberDAO.
 */
public MemberDAO()
{
	super();
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.impl.AbstractGeRDALDAO#getTableReferenceKey()
 */
protected String getTableReferenceKey() {
	return DAOImplConstants.MEMBER_TABLE_REFERENCE_KEY;
}

}
