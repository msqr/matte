/* ===================================================================
 * ThemeDaoTest.java
 * 
 * Created May 20, 2006 9:01:26 PM
 * 
 * Copyright (c) 2006 Matt Magoffin.
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
 * $Id: ThemeDaoTest.java,v 1.3 2007/01/23 06:43:11 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.dao;

import java.util.Calendar;
import java.util.List;

import magoffin.matt.ma2.AbstractSpringEnabledTransactionalTest;
import magoffin.matt.ma2.TestConstants;
import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.domain.Theme;
import magoffin.matt.ma2.domain.TimeZone;
import magoffin.matt.ma2.domain.User;

/**
 * Test case for ThemeDao.
 * 
 * @author matt.magoffin
 * @version $Revision: 1.3 $ $Date: 2007/01/23 06:43:11 $
 */
public class ThemeDaoTest extends AbstractSpringEnabledTransactionalTest {
	
	/** The ThemeDao to test. */
	protected ThemeDao themeDao;
	
	/** The DomainObjectFactory instance. */
	protected DomainObjectFactory domainObjectFactory;

	/** The TimeZoneDao to test with. */
	protected TimeZoneDao timeZoneDao;
	
	/** The UserDao to test. */
	protected UserDao userDao;
	
	/**
	 * Default constructor.
	 */
	public ThemeDaoTest() {
		super();
		setPopulateProtectedVariables(true);
	}

	@Override
	protected void onSetUpInTransaction() throws Exception {
		super.onSetUpInTransaction();
		deleteFromTables(TestConstants.ALL_TABLES_FOR_CLEAR);
	}

	// save a new album
	private Long saveNewUser() {
		User user = domainObjectFactory.newUserInstance();
		user.setAnonymousKey("123");
		user.setEmail("foo@no.where");
		user.setName("Foo Bar");
		user.setPassword("password");
		user.setLogin("foobar");
		user.setCreationDate(Calendar.getInstance());
		
		List<TimeZone> tzList = timeZoneDao.findAllTimeZones();
		user.setTz(tzList.get(0));
		
		return userDao.store(user);
	}
	
	private Theme saveNewTheme() {
		Long savedUserId = saveNewUser();
		User user = userDao.get(savedUserId);

		Theme theme = domainObjectFactory.newThemeInstance();
		theme.setAuthor("Test Author");
		theme.setAuthorEmail("Test Author Email");
		theme.setBasePath("base/path");
		theme.setCreationDate(Calendar.getInstance());
		theme.setDescription("Description here.");
		theme.setModifyDate(Calendar.getInstance());
		theme.setName("Theme Name");
		theme.setOwner(user);
		Long savedThemeId = themeDao.store(theme);
		return themeDao.get(savedThemeId);
	}
	
	/**
	 * Test persisting a new Theme instance.
	 */
	public void testCreate() {
		Theme theme = saveNewTheme();
		assertNotNull("Returned theme ID should not be null",theme.getThemeId());
	}
	
	/**
	 * Test getting a Theme by its primary key.
	 */
	public void testGetByPrimaryKey() {
		Theme theme = saveNewTheme();
		Theme theme2 = themeDao.get(theme.getThemeId());
		assertNotNull("Returned user should not be null",theme2);
		assertEquals("Returned ID should be the same as the saved ID",
				theme.getThemeId(), theme2.getThemeId());
	}
	
	/**
	 * Test persisting an existing User instance.
	 */
	public void testSave() {
		Theme theme = saveNewTheme();
		theme.setName("My Updated Theme");
		theme.setAuthor("updated auth");
		
		Long updatedThemeId = themeDao.store(theme);
		assertNotNull("Updated ID should not be null", updatedThemeId);
		assertEquals(theme.getThemeId(), updatedThemeId);
	}
	
	/**
	 * Test finding by name.
	 *
	 */
	public void testFindByName() {
		Theme theme = saveNewTheme();
		Theme foundTheme = themeDao.getThemeForName(theme.getName());
		
		assertNotNull("The returned Theme should never be null", foundTheme);
	}

}
