/* ===================================================================
 * AbstractGeRDALDAO.java
 * 
 * Created Dec 8, 2003 7:27:53 PM
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
 * $Id: AbstractGeRDALDAO.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao;

import magoffin.matt.biz.BizInitializer;
import magoffin.matt.dao.Criteria;
import magoffin.matt.dao.DAOException;
import magoffin.matt.dao.DAOInitializer;
import magoffin.matt.dao.DAORuntimeException;
import magoffin.matt.dao.DataObject;
import magoffin.matt.dao.DuplicateKeyException;
import magoffin.matt.gerdal.dao.BaseRdbDAO;
import magoffin.matt.gerdal.dao.BaseRdbDataObject;
import magoffin.matt.gerdal.dataobjects.TableMetaData;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.biz.MediaAlbumBizInitializer;
import magoffin.matt.ma.dao.impl.DAOImplConstants;
import magoffin.matt.ma.util.MediaAlbumConfigUtil;

/**
 * Base DAO implementation using GeRDAL.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public abstract class AbstractGeRDALDAO extends BaseRdbDAO {
	
	private String myTableAlias = null;
	private Class myValueObjectClass = null;

protected abstract String getTableReferenceKey();

/* (non-Javadoc)
 * @see magoffin.matt.gerdal.dao.PrimaryKey#init(magoffin.matt.gerdal.dao.DAOInitializer)
 */
public void init(DAOInitializer init) throws DAOException {
	super.init(init);
	if ( myTableAlias == null ) {
		doInit(init);
	}
}

private synchronized void doInit( DAOInitializer daoInit ) {
	BizInitializer init = daoInit.getBizInitializer();	
	if ( !(init instanceof MediaAlbumBizInitializer) ) {
		throw new DAORuntimeException(
				"Expecting " +MediaAlbumBizInitializer.class
				+" but found " +init);
	}
	MediaAlbumBizInitializer maInit = (MediaAlbumBizInitializer)init;
	try {
		myTableAlias = MediaAlbumConfigUtil.getTableReference(maInit.getAppConfig(),
				getTableReferenceKey()).getAlias();
		
		TableMetaData meta = this.getConfiguredTable(myTableAlias);
		
		if ( meta == null ) {
			throw new DAORuntimeException(
				"Table alias '" +myTableAlias +"' is not defined");
		}
		
		myValueObjectClass = meta.getDataObjectImpl();
		
	} catch ( MediaAlbumException e ) {
		throw new DAORuntimeException(
				"Unable to get user table alias from table reference '" 
				+getTableReferenceKey() +"'",e);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.gerdal.dao.DataAccessObject#getNewInstance()
 */
public DataObject getNewInstance() throws DAOException {
	try {
		BaseRdbDataObject td = (BaseRdbDataObject)myValueObjectClass.newInstance();
		td.setTableAlias(myTableAlias);
		return td;
	} catch ( Exception e ) {
		throw new DAOException("Unable to create new instance of "
			+myValueObjectClass,e);
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.dao.DAO#create(magoffin.matt.dao.DataObject)
 */
public void create(DataObject dataObject) throws DAOException 
{
	if ( dataObject != null ) {
		BaseRdbDataObject bDataObject = (BaseRdbDataObject)dataObject;
		if ( bDataObject.getTableAlias() == null ) {
			bDataObject.setTableAlias(myTableAlias);
		}
	}
	try {
		super.create(dataObject);
	} catch ( DAOException e ) {
		if ( e.getMessage() != null && e.getMessage().indexOf(
				DAOImplConstants.SQL_DUPLICATE_KEY_ERROR_MATCH) >= 0 ) {
			throw new DuplicateKeyException(e.getMessage());
		}
		throw e;
	}
}

/* (non-Javadoc)
 * @see magoffin.matt.gerdal.dao.DataAccessObject#create(magoffin.matt.gerdal.dataobjects.TableData[])
 */
public void create(DataObject[] dataObjects) throws DAOException {
	if ( dataObjects != null && dataObjects.length > 0 ) {
		BaseRdbDataObject bDataObject = (BaseRdbDataObject)dataObjects[0];
		if ( bDataObject.getTableAlias() == null ) {
			bDataObject.setTableAlias(myTableAlias);
		}
	}
	super.create(dataObjects);
}

/* (non-Javadoc)
 * @see magoffin.matt.dao.DAO#update(magoffin.matt.dao.DataObject)
 */
public void update(DataObject dataObject) throws DAOException {
	if ( dataObject != null ) {
		BaseRdbDataObject bDataObject = (BaseRdbDataObject)dataObject;
		if ( bDataObject.getTableAlias() == null ) {
			bDataObject.setTableAlias(myTableAlias);
		}
	}
	super.update(dataObject);
}

/* (non-Javadoc)
 * @see magoffin.matt.gerdal.dao.BaseRdbDAO#update(magoffin.matt.gerdal.dataobjects.TableMetaData, magoffin.matt.dao.DataObject[])
 */
public void update(TableMetaData meta, DataObject[] dataObjects)
throws DAOException {
	if ( dataObjects != null && dataObjects.length > 0 ) {
		BaseRdbDataObject bDataObject = (BaseRdbDataObject)dataObjects[0];
		if ( bDataObject.getTableAlias() == null ) {
			bDataObject.setTableAlias(myTableAlias);
		}
	}
	super.update(meta, dataObjects);
}

/* (non-Javadoc)
 * @see magoffin.matt.dao.DAO#update(magoffin.matt.dao.DataObject, magoffin.matt.dao.Criteria)
 */
public int update(DataObject dataObject, Criteria criteria)
throws DAOException 
{
	if ( dataObject != null ) {
		BaseRdbDataObject bDataObject = (BaseRdbDataObject)dataObject;
		if ( bDataObject.getTableAlias() == null ) {
			bDataObject.setTableAlias(myTableAlias);
		}
	}
	return super.update(dataObject, criteria);
}

}
