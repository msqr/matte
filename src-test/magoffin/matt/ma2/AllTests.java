/* ===================================================================
 * AllTests.java
 * 
 * Created Jan 23, 2007 5:28:56 PM
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
 * $Id: AllTests.java,v 1.7 2007/09/29 07:55:34 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma2;

import magoffin.matt.ma2.biz.impl.AbstractSearchBizTest;
import magoffin.matt.ma2.biz.impl.IOBizImplTest;
import magoffin.matt.ma2.biz.impl.JAXBDomainObjectFactoryTest;
import magoffin.matt.ma2.biz.impl.MediaBizImplTest;
import magoffin.matt.ma2.biz.impl.SystemBizImplTest;
import magoffin.matt.ma2.biz.impl.UserBizImplTest;
import magoffin.matt.ma2.biz.impl.WorkBizImplTest;
import magoffin.matt.ma2.dao.AlbumDaoTest;
import magoffin.matt.ma2.dao.CollectionDaoTest;
import magoffin.matt.ma2.dao.MediaItemDaoTest;
import magoffin.matt.ma2.dao.ThemeDaoTest;
import magoffin.matt.ma2.dao.TimeZoneDaoTest;
import magoffin.matt.ma2.dao.UserDaoTest;
import magoffin.matt.ma2.dao.support.AlbumsByDateBrowseModePluginTest;
import magoffin.matt.ma2.dao.support.PopularityBrowseModePluginTest;
import magoffin.matt.ma2.dao.support.RatingAverageBrowseModePluginTest;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite for all unit tests.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.7 $ $Date: 2007/09/29 07:55:34 $
 */
public class AllTests {

	/**
	 * Get a test suite.
	 * @return suite
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for magoffin.matt.ma2");
		// $JUnit-BEGIN$
		suite.addTestSuite(AlbumDaoTest.class);
		suite.addTestSuite(CollectionDaoTest.class);
		suite.addTestSuite(MediaItemDaoTest.class);
		suite.addTestSuite(ThemeDaoTest.class);
		suite.addTestSuite(TimeZoneDaoTest.class);
		suite.addTestSuite(UserDaoTest.class);
		suite.addTestSuite(AlbumsByDateBrowseModePluginTest.class);
		suite.addTestSuite(PopularityBrowseModePluginTest.class);
		suite.addTestSuite(RatingAverageBrowseModePluginTest.class);
		suite.addTestSuite(AbstractSearchBizTest.class);
		suite.addTestSuite(IOBizImplTest.class);
		suite.addTestSuite(JAXBDomainObjectFactoryTest.class);
		suite.addTestSuite(MediaBizImplTest.class);
		suite.addTestSuite(SystemBizImplTest.class);
		suite.addTestSuite(WorkBizImplTest.class);
		suite.addTestSuite(UserBizImplTest.class);
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
