/* ===================================================================
 * FriendDAO.java
 * 
 * Created Dec 2, 2003 8:21:03 AM
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
 * $Id: FriendDAO.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao;

import magoffin.matt.ma.dao.impl.DAOImplConstants;

/**
 * DAO implementation for friend table using GeRDAL.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class FriendDAO extends AbstractGeRDALDAO {
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.AbstractGeRDALDAO#getTableReferenceKey()
 */
protected String getTableReferenceKey() {
	return DAOImplConstants.FRIEND_TABLE_REFERENCE_KEY;
}

}
