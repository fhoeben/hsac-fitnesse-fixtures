package nl.hsac.fitnesse.fixture;

import fitnesse.junit.FitNesseRunner;
import nl.hsac.fitnesse.junit.HsacFitNesseRunner;
import org.junit.runner.RunWith;

/**
 * Test class to allow fixture code to be debugged.
 */
@RunWith(HsacFitNesseRunner.class)
@FitNesseRunner.SuiteFilter()
@HsacFitNesseRunner.ExcludeSuiteFilter()
@FitNesseRunner.Suite()
public class FixtureDebugTest {
}
