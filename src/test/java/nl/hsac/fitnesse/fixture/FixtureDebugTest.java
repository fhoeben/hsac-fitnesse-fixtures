package nl.hsac.fitnesse.fixture;

import nl.hsac.fitnesse.junit.HsacFitNesseRunner;
import nl.hsac.fitnesse.junit.patchFor486.FitNesseRunner;
import org.junit.runner.RunWith;

/**
 * Test class to allow fixture code to be debugged.
 */
@RunWith(HsacFitNesseRunner.class)
@FitNesseRunner.Suite("HsacExamples.SlimTests.HttpTests")
public class FixtureDebugTest {
}
