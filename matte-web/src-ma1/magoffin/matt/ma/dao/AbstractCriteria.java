/* ===================================================================
 * AbstractCriteria.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Mar 24, 2004 10:39:16 AM.
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
 * $Id: AbstractCriteria.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao;

import magoffin.matt.dao.Criteria;

/**
 * Basic criteria interface.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public interface AbstractCriteria extends Criteria 
{

	/** Default search type, which is unspecified. */
	public static final int UNDEFINED_SEARCH = 0;
	
/**
 * Get the query data.
 * 
 * @return Returns the query.
 */
public Object getQuery();

/**
 * Set the query data.
 * 
 * @param query The query to set.
 */
public void setQuery(Object query);

/**
 * Get the search type.
 * 
 * @return Returns the searchType.
 */
public int getSearchType();

/**
 * Set the search type.
 * 
 * @param searchType The searchType to set.
 */
public void setSearchType(int searchType);

}
