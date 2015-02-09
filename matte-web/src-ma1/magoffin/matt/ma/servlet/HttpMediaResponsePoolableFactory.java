/* ===================================================================
 * HttpMediaResponsePoolableFactory.java
 * 
 * Copyright (c) 2003 Matt Magoffin. Created Mar 3, 2003.
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
 * $Id: HttpMediaResponsePoolableFactory.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.servlet;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.PoolableObjectFactory;

/**
 * Poolable object factory for HttpMediaResponse objects.
 * 
 * <p>Created Mar 3, 2003 8:14:25 AM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class HttpMediaResponsePoolableFactory
extends BasePoolableObjectFactory implements PoolableObjectFactory 
{

/* (non-Javadoc)
 * @see org.apache.commons.pool.PoolableObjectFactory#makeObject()
 */
public Object makeObject() throws Exception {
	return new HttpMediaResponse();
}

/* (non-Javadoc)
 * @see org.apache.commons.pool.PoolableObjectFactory#passivateObject(java.lang.Object)
 */
public void passivateObject(Object o) throws Exception {
	((HttpMediaResponse)o).reset();
}

} // class HttpMediaResponsePoolableFactory
