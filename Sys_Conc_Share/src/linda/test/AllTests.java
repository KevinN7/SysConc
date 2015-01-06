package linda.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ TestRead.class, TestTryRead.class, TestTryTake.class })
public class AllTests {

}
