/* ===================================================================
 * TimeZoneDao.java
 * 
 * Created Feb 3, 2006 11:29:38 AM
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
 * $Id: TimeZoneDao.java,v 1.3 2006/07/13 09:09:56 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.dao;

import java.util.List;

import magoffin.matt.dao.GenericDao;
import magoffin.matt.ma2.domain.TimeZone;

/**
 * DAO for TimeZone objects.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.3 $ $Date: 2006/07/13 09:09:56 $
 */
public interface TimeZoneDao extends GenericDao<TimeZone, String> {

	/**
	 * Get all available time zones.
	 * @return the available time zones
	 */
	List<TimeZone> findAllTimeZones();
	
}