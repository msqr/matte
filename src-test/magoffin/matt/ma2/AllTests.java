package magoffin.matt.ma2;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ magoffin.matt.ma2.audio.AllTests.class, magoffin.matt.ma2.biz.impl.AllTests.class,
		magoffin.matt.ma2.dao.AllTests.class,
 magoffin.matt.ma2.dao.support.AllTests.class,
		magoffin.matt.ma2.image.AllTests.class, magoffin.matt.ma2.image.iio.AllTests.class,
		magoffin.matt.ma2.image.im4java.AllTests.class,
		magoffin.matt.ma2.lucene.AllTests.class, magoffin.matt.ma2.util.AllTests.class,
		magoffin.matt.ma2.video.AllTests.class,
		magoffin.matt.ma2.web.service.AllTests.class })
public class AllTests {
	// nothing
}
