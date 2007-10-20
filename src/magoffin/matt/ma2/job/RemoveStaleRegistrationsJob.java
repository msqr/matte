/* ===================================================================
 * RemoveStaleRegistrationsJob.java
 * 
 * Created Mar 22, 2007 3:14:21 PM
 * 
 * Copyright (c) 2007 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2.job;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import magoffin.matt.ma2.dao.UserDao;
import magoffin.matt.ma2.domain.User;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * Job to clean out old unconfirmed registrations.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision$ $Date$
 */
public class RemoveStaleRegistrationsJob extends QuartzJobBean {
	
	/** The default value for the <code>minDaysOld</code> property. */
	public static final int DEFAULT_MIN_DAYS_OLD = 30;
	
	private int minDaysOld = DEFAULT_MIN_DAYS_OLD;
	private UserDao userDao;
	
	private final Logger log = Logger.getLogger(getClass());

	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {
		List<User> deleted = userDao.deleteUnconfirmedRegistrations(minDaysOld);
		if ( log.isDebugEnabled() ) {
			Set<String> logins = new TreeSet<String>();
			for ( User u : deleted ) {
				logins.add(u.getLogin());
			}
			log.debug("Deleted " +logins.size() +" users: " +logins);
		}
	}
	
	/**
	 * @return the minDaysOld
	 */
	public int getMinDaysOld() {
		return minDaysOld;
	}
	
	/**
	 * @param minDaysOld the minDaysOld to set
	 */
	public void setMinDaysOld(int minDaysOld) {
		this.minDaysOld = minDaysOld;
	}
	
	/**
	 * @return the userDao
	 */
	public UserDao getUserDao() {
		return userDao;
	}
	
	/**
	 * @param userDao the userDao to set
	 */
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

}
