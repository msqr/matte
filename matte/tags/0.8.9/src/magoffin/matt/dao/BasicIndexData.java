/* ===================================================================
 * BasicIndexData.java
 * 
 * Created May 28, 2006 10:41:28 AM
 * 
 * Copyright (c) 2006 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: BasicIndexData.java,v 1.1 2006/07/13 09:09:56 matt Exp $
 * ===================================================================
 */

package magoffin.matt.dao;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import magoffin.matt.dao.IndexCallback.IndexData;

/**
 * Basic implementation of IndexData.
 * 
 * @param <PK> the primary key type
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/07/13 09:09:56 $
 */
public class BasicIndexData<PK extends Serializable> implements IndexData<PK> {
	
	private PK id;
	private Map<String,Object> dataMap;
	
	/**
	 * Default constructor.
	 */
	public BasicIndexData() {
		this.id = null;
		this.dataMap = new LinkedHashMap<String,Object>();
	}
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.dao.IndexCallback.IndexData#getDataMap()
	 */
	public Map<String, Object> getDataMap() {
		return this.dataMap;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.dao.IndexCallback.IndexData#getId()
	 */
	public PK getId() {
		return this.id;
	}
	
	@Override
	public String toString() {
		return "BasicIndexData{id="+id+",dataMap="+dataMap+"}";
	}

	/**
	 * @param id the id to set
	 */
	public void setId(PK id) {
		this.id = id;
	}
	
	/**
	 * @param dataMap the dataMap to set
	 */
	public void setDataMap(Map<String, Object> dataMap) {
		this.dataMap = dataMap;
	}

}
