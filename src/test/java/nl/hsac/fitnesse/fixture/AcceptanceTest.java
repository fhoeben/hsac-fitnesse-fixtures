package nl.hsac.fitnesse.fixture;

import fitnesse.junit.FitNesseRunner;
import nl.hsac.fitnesse.junit.HsacFitNesseRunner;
import org.junit.runner.RunWith;

/**
 * Test class to run HsacAcceptanceTests wiki suite.
 */
@RunWith(HsacFitNesseRunner.class)
@FitNesseRunner.Suite("HsacAcceptanceTests")
public class AcceptanceTest {
}
