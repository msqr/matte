/* ===================================================================
 * UserDao.java
 * 
 * Created Oct 22, 2005 9:07:39 AM
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
 * $Id: UserDao.java,v 1.8 2007/03/22 08:13:31 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.dao;

import java.util.List;

import magoffin.matt.dao.BatchableDao;
import magoffin.matt.dao.GenericDao;
import magoffin.matt.ma2.domain.User;

/**
 * DAO for User objects.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.8 $ $Date: 2007/03/22 08:13:31 $
 */
public interface UserDao extends GenericDao<User, Long>, BatchableDao<User> {

	/** The batch processing name to index MediaItem objects. */
	static final String BATCH_NAME_INDEX = "batch.index";
	
	/**
	 * Get a user by a login name.
	 * 
	 * @param login the login name
	 * @return the user, or <em>null</em> if not found
	 */
	User getUserByLogin(String login);
	
	/**
	 * Get a user by an email.
	 * 
	 * @param email the email
	 * @return the user, or <em>null</em> if not found
	 */
	User getUserByEmail(String email);
	
	/**
	 * Get a user by anonymous key.
	 * @param key the user key
	 * @return the user, or <em>null</em> if not found
	 */
	User getUserByKey(String key);
	
	/**
	 * Find all users for a given access level.
	 * 
	 * @param accessLevel the access level
	 * @return the access level
	 */
	List<User> findUsersForAccess(Integer accessLevel);
	
	/**
	 * Delete registered users that are unconfirmed and created longer
	 * than the specified number of days ago.
	 * 
	 * @param minDaysOld the minimum number of days old from the current time
	 * the registration must be in order to delete
	 * @return the users that were deleted
	 */
	List<User> deleteUnconfirmedRegistrations(int minDaysOld);
	
}
