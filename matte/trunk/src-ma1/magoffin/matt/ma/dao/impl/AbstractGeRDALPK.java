/* ===================================================================
 * AbstractGeRDALPK.java
 * 
 * Created Dec 2, 2003 9:43:47 PM
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
 * $Id: AbstractGeRDALPK.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao.impl;

import magoffin.matt.biz.BizInitializer;
import magoffin.matt.dao.DAOInitializer;
import magoffin.matt.dao.DAORuntimeException;
import magoffin.matt.gerdal.dao.BaseRdbPK;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.biz.MediaAlbumBizInitializer;
import magoffin.matt.ma.dao.MediaAlbumDAOInitializer;
import magoffin.matt.ma.util.MediaAlbumConfigUtil;

/**
 * Base PrimaryKey implementation using GeRDAL.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public abstract class AbstractGeRDALPK extends BaseRdbPK {
	
	/** My table alias, obtained from BizInitializer. */
	protected String myTableAlias = null;
	
	/** My DAOInitializer. */
	protected MediaAlbumDAOInitializer initializer = null;
	
/**
 * Default constructor.
 */
public AbstractGeRDALPK() {
	super();
}

/**
 * Construct with table alias and key value.
 * @param alias
 * @param key
 */
public AbstractGeRDALPK(String alias, Object key) {
	super(alias, key);
}

/* (non-Javadoc)
 * @see magoffin.matt.gerdal.dao.Criteria#reset()
 */
public void reset() {
	super.reset();
	setAlias(myTableAlias);
}

protected abstract String getTableReferenceKey();

/* (non-Javadoc)
 * @see magoffin.matt.gerdal.dao.PrimaryKey#init(magoffin.matt.gerdal.dao.DAOInitializer)
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
		initializer = maInit.getDAOInitializer();
	} catch ( MediaAlbumException e ) {
		throw new DAORuntimeException(
				"Unable to get user table alias from table reference '" 
				+getTableReferenceKey() +"'",e);
	}
}

}
