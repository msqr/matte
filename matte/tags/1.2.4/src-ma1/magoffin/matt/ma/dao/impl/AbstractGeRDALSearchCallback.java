/* ===================================================================
 * AbstractGeRDALSearchCallback.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Mar 29, 2004 11:40:13 AM.
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
 * $Id: AbstractGeRDALSearchCallback.java,v 1.1 2006/06/03 22:26:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao.impl;

import java.util.Map;

import magoffin.matt.dao.Criteria;
import magoffin.matt.dao.DAOSearchCallback;
import magoffin.matt.gerdal.dao.BaseRdbCriteria;

/**
 * Base implementation of DAOSearchCallback using GeRDAL.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:16 $
 */
public abstract class AbstractGeRDALSearchCallback implements DAOSearchCallback 
{
	protected BaseRdbCriteria criteria = null;
	protected Map connAttr = null;
	
/* (non-Javadoc)
 * @see magoffin.matt.dao.DAOSearchCallback#getCriteria()
 */
public Criteria getCriteria() {
	return criteria;
}

/* (non-Javadoc)
 * @see magoffin.matt.dao.DAOSearchCallback#getConnectionAttributes()
 */
public Map getConnectionAttributes() {
	return connAttr;
}

/**
 * @param connAttr The connAttr to set.
 */
public void setConnAttr(Map connAttr) {
	this.connAttr = connAttr;
}

/**
 * @param criteria The criteria to set.
 */
public void setCriteria(BaseRdbCriteria criteria) {
	this.criteria = criteria;
}
}
