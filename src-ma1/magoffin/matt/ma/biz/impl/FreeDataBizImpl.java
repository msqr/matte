/* ===================================================================
 * FreeDataBizImpl.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Mar 25, 2004 9:48:39 AM.
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
 * $Id: FreeDataBizImpl.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.biz.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.pool.ObjectPool;

import magoffin.matt.dao.CriteriaObjectPoolFactory;
import magoffin.matt.dao.DAO;
import magoffin.matt.dao.DAOException;
import magoffin.matt.dao.DataObject;
import magoffin.matt.ma.ApplicationConstants;
import magoffin.matt.ma.MediaAlbumException;
import magoffin.matt.ma.biz.FreeDataBiz;
import magoffin.matt.ma.dao.FreeDataKindCriteria;
import magoffin.matt.ma.xsd.FreeData;
import magoffin.matt.ma.xsd.FreeDataKind;

/**
 * Biz implementation for FreeDataBiz.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public class FreeDataBizImpl extends AbstractBiz implements FreeDataBiz 
{
	/** The cache key for array of all FreeDataKind objects. */
	public static final Integer ALL_FREE_DATA_KIND_CACHE_KEY = new Integer(-1);
	public static final Integer ALL_FREE_DATA_KIND_MAP_CACHE_KEY = new Integer(-2);

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.FreeDataBiz#getAllFreeDataKinds(boolean)
 */
public FreeDataKind[] getAllFreeDataKinds(boolean allowCached)
throws MediaAlbumException 
{
	FreeDataKind[] results = (FreeDataKind[])getCachedObject(
			allowCached,ApplicationConstants.CacheFactoryKeys.FREE_DATA,
			ALL_FREE_DATA_KIND_CACHE_KEY);
	
	if ( results != null ) {
		return results;
	}
	
	FreeDataKindCriteria crit = null;
	ObjectPool pool = null;
	try {
		pool = CriteriaObjectPoolFactory.getInstance().getCriteriaObjectPool(
				FreeDataKind.class);
		crit = (FreeDataKindCriteria)borrowObjectFromPool(pool);
		
		crit.setSearchType(FreeDataKindCriteria.ALL_FREE_DATA_TYPES);
		
		DAO dao = initializer.getDAOFactory().getDataAccessObjectInstance(
				FreeDataKind.class);
		
		DataObject[] data = dao.findByCriteria(crit);
		
		if ( data == null ) {
			results = new FreeDataKind[0];
		} else {
			results = (FreeDataKind[])data;
		}
		
	} catch (DAOException e) {
		throw new MediaAlbumException("DAO exception",e);
	} finally {
		returnObjectToPool(pool,crit);
	}

	cacheObject(ApplicationConstants.CacheFactoryKeys.FREE_DATA,ALL_FREE_DATA_KIND_CACHE_KEY,
			results);
	return results;
}

/**
 * Return a Map of Integer FreeDataKind IDs to FreeDataKind objects.
 * 
 * @param allowCached if <em>true</em> then allow returning cached objects
 * @return Map (never <em>null</em>)
 * @throws MediaAlbumException if an error occurs
 */
private Map getFreeDataTypeMap(boolean allowCached) throws MediaAlbumException
{
	Map result = (Map)getCachedObject(
			allowCached,ApplicationConstants.CacheFactoryKeys.FREE_DATA,
			ALL_FREE_DATA_KIND_MAP_CACHE_KEY);
	
	if ( result != null ) {
		return result;
	}
	
	FreeDataKind[] kinds = getAllFreeDataKinds(allowCached);
	
	result = new HashMap((int)(kinds.length*1.25));
	for ( int i = 0; i < kinds.length; i++ ) {
		result.put(kinds[i].getDataTypeId(),kinds[i]);
	}
	
	cacheObject(ApplicationConstants.CacheFactoryKeys.FREE_DATA,ALL_FREE_DATA_KIND_MAP_CACHE_KEY,
			result);
	return result;
}

/* (non-Javadoc)
 * @see magoffin.matt.ma.biz.FreeDataBiz#populateFreeDataTypeNames(magoffin.matt.ma.xsd.FreeData[], boolean)
 */
public void populateFreeDataTypeNames(FreeData[] data, boolean allowCached)
throws MediaAlbumException 
{
	if ( data == null ) return;
	
	Map kindMap = getFreeDataTypeMap(allowCached);
	
	for ( int i = 0; i < data.length; i++ ) {
		FreeData fd = data[i];
		if ( kindMap.containsKey(fd.getDataTypeId()) ) {
			FreeDataKind kind = (FreeDataKind)kindMap.get(fd.getDataTypeId());
			fd.setDataTypeName(kind.getName());
		}
	}
}

}
