/* ===================================================================
 * FreeDataPKImpl.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Mar 25, 2004 5:51:05 PM.
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
 * $Id: FreeDataPKImpl.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao.impl;

import magoffin.matt.ma.dao.FreeDataPK;

/**
 * FreeData primary key implementation using GeRDAL.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public class FreeDataPKImpl extends AbstractGeRDALPK implements FreeDataPK 
{
	public static final String USER_COL_ALIAS = "owner";
	public static final String ALBUM_COL_ALIAS = "albumId";
	public static final String COLLECTION_COL_ALIAS = "collectionId";
	public static final String ITEM_COL_ALIAS = "itemId";
	public static final String ALT_USER_COL_ALIAS = "userId";
	public static final String DATA_TYPE_COL_ALIAS = "dataTypeId";
	
	private Integer userId = null;
	private Integer itemId = null;
	private Integer dataTypeId = null;
	
/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.impl.AbstractGeRDALPK#getTableReferenceKey()
 */
protected String getTableReferenceKey() {
	return DAOImplConstants.FREE_DATA_TABLE_REFERENCE_KEY;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.FreeDataPK#setUserId(java.lang.Integer)
 */
public void setUserId(Integer userId) {
	this.userId = userId;
	addKey(USER_COL_ALIAS,userId);
	addKey(ALBUM_COL_ALIAS,null);
	addKey(COLLECTION_COL_ALIAS,null);
	addKey(ITEM_COL_ALIAS,null);
	addKey(ALT_USER_COL_ALIAS,null);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.FreeDataPK#getUserId()
 */
public Integer getUserId() {
	return userId;
}

/* (non-Javadoc)
 * @see magoffin.matt.dao.PrimaryKey#reset()
 */
public void reset() {
	super.reset();
	userId = null;
	itemId = null;
	dataTypeId = null;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.FreeDataPK#getItemId()
 */
public Integer getItemId() {
	return itemId;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.FreeDataPK#setItemId(java.lang.Integer)
 */
public void setItemId(Integer itemId) {
	this.itemId = itemId;
	addKey(ITEM_COL_ALIAS,itemId);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.FreeDataPK#getId()
 */
public Integer getDataId()
{
	return (Integer)getKey();
}
/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.FreeDataPK#setId(java.lang.Integer)
 */
public void setDataId(Integer id)
{
	setKey(id);
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.FreeDataPK#getDataTypeId()
 */
public Integer getDataTypeId() {
	return dataTypeId;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.FreeDataPK#setDataTypeId(java.lang.Integer)
 */
public void setDataTypeId(Integer id) {
	this.dataTypeId = id;
	addKey(DATA_TYPE_COL_ALIAS,dataTypeId);
}

}
