package magoffin.matt.ma2.biz.impl;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/** Package test suite. */
@RunWith(Suite.class)
@SuiteClasses({ AbstractSearchBizTest.class, IOBizImplTest.class, JAXBDomainObjectFactoryTest.class,
		MediaBizImplTest.class, SystemBizImplTest.class, UserBizImplTest.class, WorkBizImplTest.class })
public class AllTests {
	// nothing here
}
