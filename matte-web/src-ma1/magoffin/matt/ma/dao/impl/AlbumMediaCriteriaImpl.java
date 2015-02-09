/* ===================================================================
 * AlbumMediaCriteriaImpl.java
 * 
 * Created Jun 10, 2004 9:48:33 AM
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
 * $Id: AlbumMediaCriteriaImpl.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao.impl;

import magoffin.matt.util.ArrayUtil;


/**
 * Implementation of AlbumMediaCriteria utilizing GeRDAL.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public class AlbumMediaCriteriaImpl extends AbstractGeRDALCriteria
implements magoffin.matt.ma.dao.AlbumMediaCriteria 
{
	/** The search equal alias for finding album media for an album: <code>album</code> */
	public static final String SEARCH_ALBUM_MEDIA_FOR_ALBUM = "album";
	
	/** 
	 * The custom SQL alias for finding a subset of media for an album:
	 * <code>item-subset</code>.
	 */
	public static final String CUSTOM_SQL_MEDIA_SUBSET = "item-subset";
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.impl.AbstractGeRDALCriteria#getTableReferenceKey()
 */
protected String getTableReferenceKey() {
	return DAOImplConstants.ALBUM_MEDIA_TABLE_REFERENCE_KEY;
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
		case ALBUM_MEDIA_FOR_ALBUM:
			if ( query != null ) {
				setValue(query.toString());
			}
			setSearchAlias(SEARCH_ALBUM_MEDIA_FOR_ALBUM);
			break;
		
		case ALBUM_MEDIA_SUBSET:
			if ( query != null ) {
				Integer[] data = (Integer[])getQuery();
				Integer[] mediaIds = new Integer[data.length - 1];
				System.arraycopy(data,1,mediaIds,0,mediaIds.length);
				String mediaIdParam = ArrayUtil.join(mediaIds,',',-1);
				
				setCustomSqlParams(new Object[] {data[0]});
				setCustomSqlDynamicParams(new Object[] {mediaIdParam});
				setCustomSqlAlias(CUSTOM_SQL_MEDIA_SUBSET);
			}
			break;
	}
}

}
