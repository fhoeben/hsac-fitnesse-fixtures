package nl.hsac.fitnesse.fixture;

import fitnesse.junit.FitNesseRunner;
import nl.hsac.fitnesse.junit.HsacFitNesseRunner;
import org.junit.runner.RunWith;

/**
 * Test class to allow fixture code to be debugged.
 */
@RunWith(HsacFitNesseRunner.class)
@FitNesseRunner.SuiteFilter(systemProperty = "suiteFilter")
@HsacFitNesseRunner.ExcludeSuiteFilter(systemProperty = "excludeSuiteFilter")
@FitNesseRunner.Suite(systemProperty = "fitnesseSuiteToRun")
public class FixtureDebugTest {
}
