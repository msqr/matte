package magoffin.matt.ma2.web.service;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({ AddMediaEndpointTest.class, AddMediaStaxEndpointTest.class,
		GetCollectionListEndpointTest.class })
public class AllTests {
	// nothing
}
