/* ===================================================================
 * AllTests.java
 * 
 * Created Mar 22, 2006 4:53:27 PM
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

package magoffin.matt.ma2.biz.impl;

import magoffin.matt.ma2.AbstractSpringEnabledTransactionalTest;
import junit.framework.Test;
import junit.framework.TestSuite;

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
		TestSuite suite = new TestSuite("Test for magoffin.matt.ma2.biz.impl");
		//$JUnit-BEGIN$
		suite.addTestSuite(AbstractSearchBizTest.class);
		suite.addTestSuite(IOBizImplTest.class);
		suite.addTestSuite(JAXBDomainObjectFactoryTest.class);
		suite.addTestSuite(MediaBizImplTest.class);
		suite.addTestSuite(SystemBizImplTest.class);
		suite.addTestSuite(WorkBizImplTest.class);
		suite.addTestSuite(UserBizImplTest.class);
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
