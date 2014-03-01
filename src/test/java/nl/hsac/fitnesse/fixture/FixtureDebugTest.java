package nl.hsac.fitnesse.fixture;

import fitnesse.junit.JUnitHelper;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

/**
 * Test class to allow fixture code to be debugged.
 */
public class FixtureDebugTest {
    private JUnitHelper helper = UnitTestHelper.createFitnesseHelper();

    @Test
    public void testWithFitnesse() throws Exception {
        String suite = "MyTests";
        // allow suite to execute to be overriden via system property
        String propSuite = System.getProperty("fitnesseSuiteToRun");
        if (!StringUtils.isEmpty(propSuite)) {
            suite = propSuite;
        }
        helper.assertSuitePasses(suite);
    }
}
