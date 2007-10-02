/* ===================================================================
 * HibernateTimeZoneDao.java
 * 
 * Created Feb 3, 2006 11:31:55 AM
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
 * $Id: HibernateTimeZoneDao.java,v 1.6 2006/11/19 06:57:22 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.dao.hbm;

import java.util.List;

import magoffin.matt.dao.hbm.GenericIndexableHibernateDao;
import magoffin.matt.ma2.dao.TimeZoneDao;
import magoffin.matt.ma2.domain.TimeZone;

/**
 * Hibernate implementation of {@link magoffin.matt.ma2.dao.TimeZoneDao}.
 * 
 * <p>info</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.6 $ $Date: 2006/11/19 06:57:22 $
 */
public class HibernateTimeZoneDao extends GenericIndexableHibernateDao<TimeZone,String> 
implements TimeZoneDao {

	/** Find all time zones. */
	public static final String QUERY_TIME_ZONE_ALL = "TimeZoneAll";
	
	/**
	 * Default constructor.
	 */
	public HibernateTimeZoneDao() {
		super(TimeZone.class);
	}

	@Override
	protected String getPrimaryKey(TimeZone domainObject) {
		if ( domainObject == null ) return null;
		return domainObject.getCode();
	}
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.dao.TimeZoneDao#findAllTimeZones()
	 */
	public List<TimeZone> findAllTimeZones() {
		return findByNamedQuery(QUERY_TIME_ZONE_ALL);
	}

}
