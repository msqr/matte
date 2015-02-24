/* ===================================================================
 * SizeCallback.java
 * 
 * Created Jul 21, 2004 10:46:26 AM
 * 
 * Copyright (c) 2004 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: SizeCallback.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import magoffin.matt.dao.Criteria;
import magoffin.matt.dao.DAOException;
import magoffin.matt.dao.DAOSearchCallbackMatch;
import magoffin.matt.gerdal.dao.BaseRdbCriteria;
import magoffin.matt.gerdal.dao.BaseRdbDAOCallbackMatch;
import magoffin.matt.ma.dao.LongNumCallback;

/**
 * DAOSearchCallback implementation for summing an integer 'size' column.
 * 
 * <p>For each call to {@link #handleMatch(DAOSearchCallbackMatch)} this 
 * callback will look for a long value in a column named <code>size</code>
 * and add that value to the total size. The {@link #getSize()} method 
 * returns the total size. The {@link #getLongNum()} method also returns
 * the total size.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public class SizeCallback extends AbstractGeRDALSearchCallback 
implements LongNumCallback
{
	private long size = 0;
	
/* (non-Javadoc)
 * @see magoffin.matt.dao.DAOSearchCallback#handleMatch(magoffin.matt.dao.DAOSearchCallbackMatch)
 */
public void handleMatch(DAOSearchCallbackMatch match) throws DAOException 
{
	BaseRdbDAOCallbackMatch bMatch = (BaseRdbDAOCallbackMatch)match;
	try {
		ResultSet rs = bMatch.getResultSet();
		long oneSize = rs.getLong("size");
		size += oneSize;
	} catch ( SQLException e ) {
		throw new DAOException("SQLException summing size",e);
	}
}

/**
 * Get the current size.
 * @return size
 */
public long getSize() {
	return size;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.LongNumCallback#getLongNum()
 */
public long getLongNum() {
	return getSize();
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.search.MediaItemDAOIndexCallback#setCriteria(magoffin.matt.dao.Criteria)
 */
public void setCriteria(Criteria criteria) {
	super.setCriteria((BaseRdbCriteria)criteria);
}

}
