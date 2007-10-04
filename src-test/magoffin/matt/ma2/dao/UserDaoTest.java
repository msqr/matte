/* ===================================================================
 * UserDaoTest.java
 * 
 * Created Sep 19, 2005 2:41:35 PM
 * 
 * Copyright (c) 2005 Matt Magoffin.
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
 * $Id: UserDaoTest.java,v 1.10 2007/01/23 06:43:11 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.dao;

import java.util.BitSet;
import java.util.Calendar;
import java.util.List;

import magoffin.matt.dao.BasicBatchOptions;
import magoffin.matt.dao.BatchableDao.BatchCallbackResult;
import magoffin.matt.dao.BatchableDao.BatchResult;
import magoffin.matt.ma2.AbstractSpringEnabledTransactionalTest;
import magoffin.matt.ma2.TestConstants;
import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.domain.TimeZone;
import magoffin.matt.ma2.domain.User;

import org.apache.commons.lang.mutable.MutableInt;

/**
 * Test case for UserDao.
 * 
 * @author matt.magoffin
 * @version $Revision: 1.10 $ $Date: 2007/01/23 06:43:11 $
 */
public class UserDaoTest extends AbstractSpringEnabledTransactionalTest {

	/** The UserDao to test. */
	protected UserDao userDao;
	
	/** The TimeZoneDao to test with. */
	protected TimeZoneDao timeZoneDao;
	
	/** The DomainObjectFactory instance. */
	protected DomainObjectFactory domainObjectFactory;
	
	/** The index TimeZone. */
	protected java.util.TimeZone indexTimeZone;

	/**
	 * Default constructor.
	 */
	public UserDaoTest() {
		super();
		setPopulateProtectedVariables(true);
	}

	@Override
	protected void onSetUpInTransaction() throws Exception {
		super.onSetUpInTransaction();
		deleteFromTables(TestConstants.ALL_TABLES_FOR_CLEAR);
	}
	
	private Long saveNewUser() {
		return saveNewUser("foobar","foo@no.where");
	}

	// save a new album
	private Long saveNewUser(String login, String email) {
		User user = domainObjectFactory.newUserInstance();
		user.setAnonymousKey("unit.test.key");
		user.setEmail(email);
		user.setName("Foo Bar");
		user.setPassword("password");
		user.setLogin(login);
		user.setCreationDate(Calendar.getInstance());
		user.setAccessLevel(2);
		
		List<TimeZone> tzList = timeZoneDao.findAllTimeZones();
		user.setTz(tzList.get(0));
		
		return userDao.store(user);
	}
	
	private User saveAndGetNewUser(String login, String email) {
		Long id = saveNewUser(login, email);
		return userDao.get(id);
	}
	
	/**
	 * Test persisting a new User instance.
	 */
	public void testCreate() {
		Long savedUserId = saveNewUser();
		assertNotNull("Returned user ID should not be null",savedUserId);
	}
	
	/**
	 * Test getting a user by it's primary key.
	 */
	public void testGetByPrimaryKey() {
		Long userId = saveNewUser();
		
		User user = userDao.get(userId);
		assertNotNull("Returned user should not be null",user);
		assertEquals("Returned user ID should be the same as the saved user ID",
				userId, user.getUserId());
	}
	
	/**
	 * Test persisting an existing User instance.
	 */
	public void testSave() {
		Long savedUserId = saveNewUser();
		User savedUser = userDao.get(savedUserId);
		savedUser.setName("My Updated User");
		savedUser.setPassword("updated password");
		
		Long updatedUserId = userDao.store(savedUser);
		assertNotNull("Updated user ID should not be null",updatedUserId);
		assertEquals(savedUserId, updatedUserId);
	}
	
	/**
	 * Test find a user by their username.
	 */
	public void testFindByUsername() {
		Long savedUserId = saveNewUser();
		User savedUser = userDao.get(savedUserId);
		
		User foundUser = userDao.getUserByLogin(savedUser.getLogin());
		
		assertNotNull("The returned User should never be null", foundUser);
	}

	/**
	 * Test find a user by their email.
	 */
	public void testFindByEmail() {
		Long savedUserId = saveNewUser();
		User savedUser = userDao.get(savedUserId);
		
		User foundUser = userDao.getUserByEmail(savedUser.getEmail());
		
		assertNotNull("The returned User should never be null", foundUser);
	}
	
	/**
	 * Test find a user by their key.
	 */
	public void testFindByKey() {
		Long savedUserId = saveNewUser();
		User savedUser = userDao.get(savedUserId);
		
		User foundUser = userDao.getUserByKey(savedUser.getAnonymousKey());
		
		assertNotNull("The returned User should not be null", foundUser);
	}
	
	/**
	 * Test find a user by access level.
	 */
	public void testFindByAccessLevel() {
		Long savedUserId = saveNewUser();
		User savedUser = userDao.get(savedUserId);
		
		List<User> foundUser = userDao.findUsersForAccess(savedUser.getAccessLevel());
		
		assertNotNull("The returned List should never be null", foundUser);
		assertTrue(foundUser.size() > 0);
		assertEquals(savedUser.getAccessLevel(), foundUser.get(0).getAccessLevel());
	}
	
	/**
	 * Test can index user data.
	 */
	public void testIndexAllUserData() {
		final User savedUser1 = saveAndGetNewUser("user1","user@1.test");
		final User savedUser2 = saveAndGetNewUser("user2","user@2.test");
		final BitSet itemSet = new BitSet(2);
		final MutableInt numIndexed = new MutableInt(0);
		
		BasicBatchOptions batchOptions = new BasicBatchOptions(
				MediaItemDao.BATCH_NAME_INDEX);
		BatchResult results = userDao.batchProcess(
				new magoffin.matt.dao.BatchableDao.BatchCallback<User>() {
			
			public BatchCallbackResult handle(User domainObject) {
				numIndexed.setValue(numIndexed.intValue()+1);
				if ( logger.isDebugEnabled() ) {
					logger.debug("Got item: " +domainObject.getUserId());
				}
				assertNotNull(domainObject.getUserId());
				if ( domainObject.getUserId().equals(savedUser1.getUserId())) {
					itemSet.set(0);
				} else if ( domainObject.getUserId().equals(savedUser2.getUserId())) {
					itemSet.set(1);
				}
				return BatchCallbackResult.CONTINUE;
			}
			
		}, batchOptions);
		assertNotNull(results);
		assertEquals(2, numIndexed.intValue());
		assertEquals(2, itemSet.cardinality());
		assertEquals(2, results.numProcessed());
	}
		
}