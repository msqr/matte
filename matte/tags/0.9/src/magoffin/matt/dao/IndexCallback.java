/* ===================================================================
 * IndexCallback.java
 * 
 * Created May 28, 2006 10:32:31 AM
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
 * $Id: IndexCallback.java,v 1.2 2006/10/08 04:32:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.dao;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Interface to allow for indexing DAO-based data.
 * 
 * @param <PK> the primary key type
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.2 $ $Date: 2006/10/08 04:32:18 $
 */
public interface IndexCallback<PK extends Serializable> {
	
	/**
	 * Representation of a "row" of data for the purposes of indexing.
	 * 
	 * <p>While processing row data via the {@link IndexCallback}, the 
	 * callback implementation should not assume a new IndexData object 
	 * is created for each row returned, and thus should not hold any 
	 * reference to the IndexData object past the method invocation.</p>
	 * 
	 * @param <PK> the primary key type
	 */
	public static interface IndexData<PK extends Serializable> {
		
		/** The default <code>dataMap</code> key for a domain object. */
		public static final String DOMAIN_OBJECT_KEY = "domainObject";
		
		/** 
		 * The object ID. 
		 * @return the ID of the object represented by this "row" of data
		 */
		PK getId();
		
		/**
		 * Return a Map of data associated with this "row" of data.
		 * @return the row of data
		 */
		Map<String, Object> getDataMap();
		
	}
	
	/**
	 * Handle one "row" of index data.
	 * @param data the current index data
	 */
	void handle(IndexData<PK> data);
	
	/**
	 * Called after processing the last row of data.
	 */
	void finish();
	
	/**
	 * Get a start date to limit the index callback to.
	 * 
	 * <p>Note if a <em>startDate</em> is specified then the 
	 * <em>endDate</em> must also be specified.</p>
	 * 
	 * @return the start date
	 */
	Date getStartDate();
	
	/**
	 * Get a end date to limit the index callback to.
	 * 
	 * <p>Note if a <em>endDate</em> is specified then the 
	 * <em>startDate</em> must also be specified.</p>
	 * 
	 * @return the start date
	 */
	Date getEndDate();
	
}
