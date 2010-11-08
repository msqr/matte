/* ===================================================================
 * FreeDataPK.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Mar 25, 2004 5:42:05 PM.
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
 * $Id: FreeDataPK.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao;

import magoffin.matt.dao.PrimaryKey;

/**
 * PrimaryKey for FreeData objects.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public interface FreeDataPK extends PrimaryKey {
	
public void setUserId(Integer userId);

public Integer getUserId();

public void setItemId(Integer itemId);

public Integer getItemId();

/**
 * Get the FreeData ID value.
 * @return id
 */
public Integer getDataId();

/**
 * Set the FreeData ID value.
 * @param id the ID value
 */
public void setDataId(Integer id);

public Integer getDataTypeId();

public void setDataTypeId(Integer id);

}
