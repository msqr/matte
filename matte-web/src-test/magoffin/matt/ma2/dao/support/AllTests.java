package magoffin.matt.ma2.dao.support;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({ AlbumsByDateBrowseModePluginTest.class, PopularityBrowseModePluginTest.class,
		RatingAverageBrowseModePluginTest.class })
public class AllTests {
	// nothing
}
