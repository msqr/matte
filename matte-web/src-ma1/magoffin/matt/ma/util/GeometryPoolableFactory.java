/* ===================================================================
 * GeometryPoolableFactory.java
 * 
 * Copyright (c) 2002 Matt Magoffin.
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
 * $Id: GeometryPoolableFactory.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.util;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.log4j.Logger;

/**
 * Poolable factory implementation for Geometry class.
 * 
 * <p>Created Oct 16, 2002 8:02:38 PM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public class GeometryPoolableFactory extends BasePoolableObjectFactory 
{

	private static final Logger log = Logger.getLogger(GeometryPoolableFactory.class);

/**
 * @see org.apache.commons.pool.PoolableObjectFactory#makeObject()
 */
public Object makeObject() throws Exception 
{
	log.debug("Creating new Geometry instance");
	return new Geometry();
}

/**
 * @see org.apache.commons.pool.PoolableObjectFactory#passivateObject(java.lang.Object)
 */
public void passivateObject(Object o) throws Exception 
{
	((Geometry)o).reset();
}

} // class GeometryPoolableFactory
