package nl.hsac.fitnesse.fixture.slim;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class StopTestFixtureTest {
    private final StopTestFixture fixture = new StopTestFixture();
    
    @Test
    public void testStopTestIfIs() {
        assertTrue(fixture.stopTestIfIs("Hallo", "Bye"));
        assertTrue(fixture.stopTestIfIs(null, "Bye"));
        assertTrue(fixture.stopTestIfIs("Hallo", null));

        try {
            fixture.stopTestIfIs("Hallo", "Hallo");
            fail();
        } catch (StopTestException e) {
            assertTrue(e.getMessage().contains(" Hallo"));
        }

        try {
            fixture.stopTestIfIs(null, null);
            fail();
        } catch (StopTestException e) {
            assertTrue(e.getMessage().contains(" null"));
        }
    }

    @Test
    public void testStopTestIfIsNot() {
        assertTrue(fixture.stopTestIfIsNot("Hallo", "Hallo"));
        assertTrue(fixture.stopTestIfIsNot(null, null));

        try {
            fixture.stopTestIfIsNot("Hallo", "Bye");
            fail();
        } catch (StopTestException e) {
            assertTrue(e.getMessage().contains(" Hallo"));
        }

        try {
            fixture.stopTestIfIsNot(null, "Bye");
            fail();
        } catch (StopTestException e) {
            assertTrue(e.getMessage().contains(" null"));
        }

        try {
            fixture.stopTestIfIsNot("Hallo", null);
            fail();
        } catch (StopTestException e) {
            assertTrue(e.getMessage().contains(" Hallo"));
        }
    }

    @Test
    public void testStopTestIfIsDifferentType() {
        try {
            fixture.stopTestIfIs(1, "1");
            fail();
        } catch (StopTestException e) {
            assertTrue(e.getMessage().contains(" 1"));
        }

        try {
            fixture.stopTestIfIs(2.0, "2.0");
            fail();
        } catch (StopTestException e) {
            assertTrue(e.getMessage().contains(" 2.0"));
        }
    }

    @Test
    public void testStopTestIfIsNotDifferentType() {
        assertTrue(fixture.stopTestIfIsNot("1", 1));
        assertTrue(fixture.stopTestIfIsNot(2.0, "2.0"));
    }
}
