package magoffin.matt.ma2.dao;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({ AlbumDaoTest.class, CollectionDaoTest.class, MediaItemDaoTest.class, ThemeDaoTest.class,
		TimeZoneDaoTest.class, UserDaoTest.class })
public class AllTests {
	// nothing
}
