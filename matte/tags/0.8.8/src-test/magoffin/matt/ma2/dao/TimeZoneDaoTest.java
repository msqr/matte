/* ===================================================================
 * AlbumDaoTest.java
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
 * $Id: TimeZoneDaoTest.java,v 1.6 2007/01/23 06:43:11 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2.dao;

import java.util.List;

import magoffin.matt.ma2.AbstractSpringEnabledTransactionalTest;
import magoffin.matt.ma2.TestConstants;
import magoffin.matt.ma2.biz.DomainObjectFactory;
import magoffin.matt.ma2.domain.TimeZone;

/**
 * Test case for TimeZoneDao.
 * 
 * @author matt.magoffin
 * @version $Revision: 1.6 $ $Date: 2007/01/23 06:43:11 $
 */
public class TimeZoneDaoTest extends AbstractSpringEnabledTransactionalTest {

	/** The album DAO to test. */
	protected TimeZoneDao timeZoneDao;
	
	/** The DomainObjectFactory instance. */
	protected DomainObjectFactory domainObjectFactory;

	/**
	 * Default constructor.
	 */
	public TimeZoneDaoTest() {
		super();
		setPopulateProtectedVariables(true);
	}

	@Override
	protected void onSetUpInTransaction() throws Exception {
		super.onSetUpInTransaction();
		deleteFromTables(TestConstants.ALL_TABLES_FOR_CLEAR);
		deleteFromTables(new String[] {TestConstants.TABLE_TIME_ZONE});
	}

	// save a new time zone
	private String saveNewTimeZone() {
		TimeZone tz = domainObjectFactory.newTimeZoneInstance();
		tz.setCode("test/code");
		tz.setName("Test Code");
		tz.setOffset(8000);
		tz.setOrdering(1);
		
		return timeZoneDao.store(tz);
	}
	
	/**
	 * Test persisting a new Album instance.
	 */
	public void testCreate() {
		String savedTimeZoneCode = saveNewTimeZone();
		assertNotNull("Returned TimeZone code should not be null",savedTimeZoneCode);
	}
	
	/**
	 * Test getting an album by it's primary key.
	 */
	public void testGetByPrimaryKey() {
		String savedTimeZoneCode = saveNewTimeZone();
		
		TimeZone tz = timeZoneDao.get(savedTimeZoneCode);
		assertNotNull("Returned TimeZone should not be null",tz);
		assertEquals("Returned TimeZone code should be the same as the saved album ID",
				savedTimeZoneCode, tz.getCode());
	}

	/**
	 * Test persisting an existing Album instance.
	 */
	public void testSave() {
		String savedTimeZoneCode = saveNewTimeZone();
		TimeZone savedTimeZone = timeZoneDao.get(savedTimeZoneCode);
		savedTimeZone.setName("My Updated Album");
		savedTimeZone.setOffset(-5000);
		
		String updatedTimeZoneCode = timeZoneDao.store(savedTimeZone);
		assertNotNull("Updated TimeZone code should not be null",updatedTimeZoneCode);
		assertEquals(savedTimeZoneCode, updatedTimeZoneCode);
	}
	
	/**
	 * Test finding all TimeZone instances.
	 *
	 */
	public void testFindAll() {
		String savedTimeZoneCode = saveNewTimeZone();
		List<TimeZone> tzList = timeZoneDao.findAllTimeZones();
		
		assertNotNull(tzList);
		assertEquals("The TimeZone list should have 1 item", 1, tzList.size());
		assertEquals("The TimeZone should be the saved one", savedTimeZoneCode, tzList.get(0).getCode());
	}
	
	
}
