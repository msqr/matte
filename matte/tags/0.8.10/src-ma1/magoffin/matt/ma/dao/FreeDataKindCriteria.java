/* ===================================================================
 * FreeDataKindCriteria.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Mar 24, 2004 10:31:24 AM.
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
 * $Id: FreeDataKindCriteria.java,v 1.1 2006/06/03 22:26:18 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.dao;

/**
 * Criteria interface for FreeDataTypes DAO.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:18 $
 */
public interface FreeDataKindCriteria extends AbstractCriteria 
{
	/** 
	 * Search type for finding all free data types. 
	 */
	public static final int ALL_FREE_DATA_TYPES = 1;

}