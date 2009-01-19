/* ===================================================================
 * AllTests.java
 * 
 * Created Mar 22, 2006 4:54:15 PM
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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.ma2.dao;

import junit.framework.Test;
import junit.framework.TestSuite;
import magoffin.matt.ma2.AbstractSpringEnabledTransactionalTest;
import magoffin.matt.ma2.dao.support.AlbumsByDateBrowseModePluginTest;
import magoffin.matt.ma2.dao.support.PopularityBrowseModePluginTest;
import magoffin.matt.ma2.dao.support.RatingAverageBrowseModePluginTest;

/**
 * Test suite for all package tests.
 * @author matt
 */
public class AllTests {
	
	/**
	 * Return a suite of tests.
	 * @return suite
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for magoffin.matt.ma2.dao");
		//$JUnit-BEGIN$
		suite.addTestSuite(AlbumDaoTest.class);
		suite.addTestSuite(CollectionDaoTest.class);
		suite.addTestSuite(MediaItemDaoTest.class);
		suite.addTestSuite(ThemeDaoTest.class);
		suite.addTestSuite(TimeZoneDaoTest.class);
		suite.addTestSuite(UserDaoTest.class);
		suite.addTestSuite(AlbumsByDateBrowseModePluginTest.class);
		suite.addTestSuite(PopularityBrowseModePluginTest.class);
		suite.addTestSuite(RatingAverageBrowseModePluginTest.class);
		//$JUnit-END$
		return suite;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
		AbstractSpringEnabledTransactionalTest.shutdown();
	}

}
