/* ===================================================================
 * AlbumThemePKImpl.java
 * 
 * Created Dec 28, 2003 11:00:55 AM
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
 * $Id: AlbumThemePKImpl.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao.impl;

import magoffin.matt.ma.dao.AlbumThemePK;

/**
 * Album theme primary key implementation using GeRDAL.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public class AlbumThemePKImpl
	extends AbstractGeRDALPK
	implements AlbumThemePK 
{

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.impl.AbstractGeRDALPK#getTableReferenceKey()
 */
protected String getTableReferenceKey() {
	return DAOImplConstants.ALBUM_THEME_TABLE_REFERENCE_KEY;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.AlbumThemePK#getId()
 */
public Object getId() {
	return getKey();
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.AlbumThemePK#setId(java.lang.Object)
 */
public void setId(Object id) {
	setKey(id);
}

}
