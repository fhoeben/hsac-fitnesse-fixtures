package nl.hsac.fitnesse.fixture;

import nl.hsac.fitnesse.junit.FitnesseFromJUnitRunner;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

/**
 * Test class to allow fixture code to be debugged.
 */
public class FixtureDebugTest {
    private final static String suiteOverrideVariableName = "fitnesseSuiteToRun";

    @Test
    public void testWithFitnesse() throws Exception {
        String suiteName = "HsacExamples.SlimTests.HttpTests";

        // allow suite to execute to be overriden via system property (e.g. for use by build server)
        String propSuite = System.getProperty(suiteOverrideVariableName);
        if (!StringUtils.isEmpty(propSuite)) {
            suiteName = propSuite;
        }

        new FitnesseFromJUnitRunner().assertSuitePasses(suiteName);
    }
}
