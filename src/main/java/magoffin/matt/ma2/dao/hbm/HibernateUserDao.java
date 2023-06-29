/* ===================================================================
 * HibernateUserDao.java
 * 
 * Created Oct 22, 2005 9:08:54 AM
 * 
 * Copyright (c) 2005 Matt Magoffin (spamsqr@msqr.us)
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
 */

package magoffin.matt.ma2.dao.hbm;

import java.util.Calendar;
import java.util.List;

import magoffin.matt.dao.BasicBatchResult;
import magoffin.matt.dao.hbm.CriteriaBuilder;
import magoffin.matt.dao.hbm.GenericHibernateDao;
import magoffin.matt.ma2.dao.UserDao;
import magoffin.matt.ma2.domain.User;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;

/**
 * Hibernate implementation of {@link magoffin.matt.ma2.dao.UserDao}.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public class HibernateUserDao extends GenericHibernateDao<User, Long>
implements UserDao {

	/** Find a user by its username. */
	public static final String QUERY_USER_BY_LOGIN = "UserForLogin";
	
	/** Find a user by its email. */
	public static final String QUERY_USER_BY_EMAIL = "UserForEmail";

	/** Find a user by its key. */
	public static final String QUERY_USER_BY_KEY = "UserForKey";

	/** Find users by access level. */
	public static final String QUERY_USERS_BY_ACCESS_LEVEL = "UsersForAccessLevel";

	/** Find users not confirmed in a long time. */
	public static final String QUERY_USERS_UNCONFIRMED_FOR_LONG_TIME 
		= "UsersUnconfirmedForLongTime";

	/**
	 * Default constructor.
	 */
	public HibernateUserDao() {
		super(User.class);
	}

	@Override
	protected Long getPrimaryKey(User domainObject) {
		if ( domainObject == null ) return null;
		return domainObject.getUserId();
	}
	
	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.dao.UserDao#findUsersForAccess(java.lang.Integer)
	 */
	public List<User> findUsersForAccess(Integer accessLevel) {
		return findByNamedQuery(QUERY_USERS_BY_ACCESS_LEVEL, new Object[]{accessLevel});
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.dao.UserDao#getUserByEmail(java.lang.String)
	 */
	public User getUserByEmail(String email) {
		List<User> results = findByNamedQuery(QUERY_USER_BY_EMAIL, new Object[]{email});
		if ( results.size() < 1 ) return null;
		return results.get(0);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.dao.UserDao#getUserByLogin(java.lang.String)
	 */
	public User getUserByLogin(String login) {
		List<User> results = findByNamedQuery(QUERY_USER_BY_LOGIN, new Object[]{login});
		if ( results.size() < 1 ) return null;
		return results.get(0);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.dao.BatchableDao#batchProcess(magoffin.matt.dao.BatchableDao.BatchCallback, magoffin.matt.dao.BatchableDao.BatchOptions)
	 */
	public BatchResult batchProcess(BatchCallback<User> callback, BatchOptions options) {
		if ( callback == null || options == null ) {
			throw new IllegalArgumentException("Batch parameters and options are required");
		}
		BatchResult result = null;
		if ( BATCH_NAME_INDEX.equals(options.getName()) ) {
			result = handleBatchIndex(callback, options);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.dao.UserDao#getUserByKey(java.lang.String)
	 */
	public User getUserByKey(String key) {
		List<User> results = findByNamedQuery(QUERY_USER_BY_KEY, new Object[]{key});
		if ( results.size() < 1 ) return null;
		return results.get(0);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.ma2.dao.UserDao#deleteUnconfirmedRegistrations(int)
	 */
	public List<User> deleteUnconfirmedRegistrations(int minDaysOld) {
		Calendar now = Calendar.getInstance();
		now.add(Calendar.DATE, -minDaysOld);
		final List<User> results = findByNamedQuery(QUERY_USERS_UNCONFIRMED_FOR_LONG_TIME, 
				new Object[]{now});
		getHibernateTemplate().deleteAll(results);
		if ( log.isDebugEnabled() ) {
			log.debug("Deleted " +results.size() +" stale user registrations");
		}
		return results;
	}

	private BatchResult handleBatchIndex(BatchCallback<User> callback,
			BatchOptions batchOptions) {
		Integer numProcessed = executeLiveCriteriaBatchCallback(
				new CriteriaBuilder() {
					public void buildCriteria(Criteria criteria) {
						criteria.addOrder(Order.asc("id"));
					}
				}, callback, batchOptions);
		return new BasicBatchResult(numProcessed);
	}

}
