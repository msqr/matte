/* ===================================================================
 * AbstractGeRDALCriteria.java
 * 
 * Created Dec 2, 2003 8:49:19 PM
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
 * $Id: AbstractGeRDALCriteria.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao.impl;

import magoffin.matt.biz.BizInitializer;
import magoffin.matt.dao.DAOInitializer;
import magoffin.matt.dao.DAORuntimeException;
import magoffin.matt.gerdal.dao.BaseRdbCriteria;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.biz.MediaAlbumBizInitializer;
import magoffin.matt.ma.util.MediaAlbumConfigUtil;

/**
 * Base criteria implementation for GeRDAL.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public abstract class AbstractGeRDALCriteria extends BaseRdbCriteria {

	/** The type of search to perform, defaults to <code>-1</code>. */
	protected int searchType = -1;
	
	/** The search query data. */
	protected Object query = null;
	
	/** My table alias, obtained from BizInitializer. */
	private String myTableAlias = null;
	
/**
 * Default constructor.
 */
public AbstractGeRDALCriteria() {
	super();
}

/**
 * Construct with a table alias.
 * 
 * @param alias
 */
public AbstractGeRDALCriteria(String alias) {
	super(alias);
}

/* (non-Javadoc)
 * @see magoffin.matt.gerdal.dao.Criteria#reset()
 */
public void reset() {
	super.reset();
	query = null;
	searchType = getDefaultSearchType();
	setAlias(myTableAlias);
}

protected void handleSearchDataChange() {
	super.reset();
	setAlias(myTableAlias);
}

/**
 * Get the GeRDAL table reference key for this criteria.
 * @return the table reference key
 */
protected abstract String getTableReferenceKey();

protected abstract int getDefaultSearchType();

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.UserCriteria#getQuery()
 */
public Object getQuery() {
	return query;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.UserCriteria#getSearchType()
 */
public int getSearchType() {
	return searchType;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.UserCriteria#setQuery(java.lang.Object)
 */
public void setQuery(Object query) {
	this.query = query;
	handleSearchDataChange();
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.dao.UserCriteria#setSearchType(int)
 */
public void setSearchType(int searchType) {
	this.searchType = searchType;
	handleSearchDataChange();
}

/* (non-Javadoc)
 * @see magoffin.matt.gerdal.dao.Criteria#init(magoffin.matt.gerdal.dao.DAOInitializer)
 */
public void init(DAOInitializer init) {
	if ( myTableAlias == null ) {
		BizInitializer bizInit = init.getBizInitializer();
		init(bizInit);
	}
}

private synchronized void init( BizInitializer init ) {
	if ( !(init instanceof MediaAlbumBizInitializer) ) {
		throw new DAORuntimeException(
				"Expecting " +MediaAlbumBizInitializer.class
				+" but found " +init);
	}
	MediaAlbumBizInitializer maInit = (MediaAlbumBizInitializer)init;
	try {
		myTableAlias = MediaAlbumConfigUtil.getTableReference(maInit.getAppConfig(),
				getTableReferenceKey()).getAlias();
		setAlias(myTableAlias);
	} catch ( MediaAlbumException e ) {
		throw new DAORuntimeException(
				"Unable to get table alias from table reference '" 
				+getTableReferenceKey() +"'",e);
	}
}

}
