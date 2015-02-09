/* ===================================================================
 * PoolFactory.java
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
 * $Id: PoolFactory.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.util;

import magoffin.matt.xsd.ObjectPoolConfig;
import magoffin.matt.xsd.StackObjectPoolConfig;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.ObjectPoolFactory;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.StackObjectPoolFactory;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Manages object pools.
 * 
 * <p>Created Mar 3, 2003 8:44:20 AM.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public class PoolFactory 
{
	private static final Logger log = Logger.getLogger(PoolFactory.class);
	
	private Map objectPools;
	private Map poolableFactoryMap;
	private ObjectPoolFactory opFactory;

/**
 * Create a PoolFactory instance.
 * 
 * <p>All object pools created by this instance will use the same 
 * parameters and be of the same type as defined by <var>config</var>.
 * 
 * @param config
 */	
public PoolFactory(ObjectPoolConfig config) 
{
	objectPools = new HashMap();
	if ( !(config instanceof StackObjectPoolConfig) ) {
		throw new IllegalArgumentException("Config type " +config.getClass().getName()
			+" not supported.");
	}
	StackObjectPoolConfig sConfig = (StackObjectPoolConfig)config;
	opFactory = new StackObjectPoolFactory(sConfig.getMax(),sConfig.getInitial());
	poolableFactoryMap = new HashMap();
}

public ObjectPool getPoolInstance(Class poolableObjectClass) 
throws IllegalArgumentException
{
	if ( !objectPools.containsKey(poolableObjectClass) ) {
		 if ( !poolableFactoryMap.containsKey(poolableObjectClass) ) {
		 	throw new IllegalArgumentException("Class " +poolableObjectClass.getName()
		 		+" is not registered.");
		 }
		if ( log.isDebugEnabled() ) {
			log.debug("Creating new object pool for " +poolableObjectClass);
		}
		 ObjectPool pool = opFactory.createPool();
		 pool.setFactory((PoolableObjectFactory)poolableFactoryMap.get(poolableObjectClass));
		 objectPools.put(poolableObjectClass,pool);
	}
	return (ObjectPool)objectPools.get(poolableObjectClass);
}

public void registerPoolableObjectFactory(Class poolableObjectClass, PoolableObjectFactory pof)
{
	if ( log.isDebugEnabled() ) {
		log.debug("Registering " +poolableObjectClass +" for object pool " +pof.getClass().getName());
	}
	poolableFactoryMap.put(poolableObjectClass,pof);
}

public Class[] getRegisteredObjectPools() {
	Class[] result = new Class[objectPools.size()];
	int i = 0;
	for ( Iterator itr = objectPools.keySet().iterator(); itr.hasNext(); i++ ) {
		result[i] = (Class)itr.next();
	}
	return result;
}

} // class PoolFactory
