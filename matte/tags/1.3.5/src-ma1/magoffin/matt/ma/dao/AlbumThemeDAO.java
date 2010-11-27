/* ===================================================================
 * AlbumThemeDAO.java
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
 * $Id: AlbumThemeDAO.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao;

import magoffin.matt.ma.dao.impl.DAOImplConstants;

/**
 * DAO for album theme data objects.
 * 
 * <p> Created on Jan 28, 2003 3:12:08 PM.</p>
 *
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class AlbumThemeDAO extends AbstractGeRDALDAO {

/**
 * Constructor for AlbumThemeDAO.
 */
public AlbumThemeDAO() {
	super();
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.AbstractGeRDALDAO#getTableReferenceKey()
 */
protected String getTableReferenceKey() {
	return DAOImplConstants.ALBUM_THEME_TABLE_REFERENCE_KEY;
}

}
