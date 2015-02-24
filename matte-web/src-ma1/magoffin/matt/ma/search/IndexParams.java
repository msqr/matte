/* ===================================================================
 * IndexParams.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Mar 29, 2004 2:33:39 PM.
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
 * $Id: IndexParams.java,v 1.1 2006/06/03 22:26:17 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.search;

import magoffin.matt.ma.biz.SearchBiz;

/**
 * Interface for index parameter data.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:17 $
 */
public interface IndexParams {

/**
 * Set the SearchBiz implementation to use.
 * 
 * @param searchBiz the search biz
 */
public void setSearchBiz(SearchBiz searchBiz);

public SearchBiz getSearchBiz();

public void setParam(String key, Object param);

public Object getParam(String key);

}
