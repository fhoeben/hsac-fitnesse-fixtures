package nl.hsac.fitnesse.fixture;

import nl.hsac.fitnesse.junit.FitnesseFromJUnitRunner;
import org.junit.Test;

/**
 * Test class to allow fixture code to be debugged.
 */
public class FixtureDebugTest {
    @Test
    public void testWithFitnesse() throws Exception {
        new FitnesseFromJUnitRunner().assertSuitePasses("MyTests");
    }
}
