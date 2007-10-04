/* ===================================================================
 * MediaItemDAO.java
 * 
 * Copyright (c) 2002-2003 Matt Magoffin.
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
 * $Id: MediaItemDAO.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao;

import magoffin.matt.dao.DAOException;
import magoffin.matt.dao.DAOSearchCallback;
import magoffin.matt.dao.UnsupportedObjectException;
import magoffin.matt.ma.dao.impl.DAOImplConstants;
import magoffin.matt.ma.dao.impl.MediaItemIndexCallback;
import magoffin.matt.ma.dao.impl.SizeCallback;

/**
 * DAO for MediaItem objects.
 * 
 * <p>Created Oct 9, 2002 8:42:56 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public final class MediaItemDAO extends AbstractGeRDALDAO
{
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.AbstractGeRDALDAO#getTableReferenceKey()
 */
protected String getTableReferenceKey() 
{
	return DAOImplConstants.MEDIA_ITEM_TABLE_REFERENCE_KEY;
}

/* (non-Javadoc)
 * @see magoffin.matt.dao.DAO#getCallbackInstance(java.lang.String)
 */
public DAOSearchCallback getCallbackInstance(String key)
throws DAOException 
{
	if ( DAOConstants.SEARCH_CALLBACK_MEDIA_ITEM.equals(key) ) {
		return new MediaItemIndexCallback();
	} else if ( DAOConstants.SEARCH_CALLBACK_SIZE.equals(key) ) {
		return new SizeCallback();
	}
	throw new UnsupportedObjectException(key);
}

}
