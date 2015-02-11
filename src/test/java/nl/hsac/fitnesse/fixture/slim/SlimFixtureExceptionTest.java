package nl.hsac.fitnesse.fixture.slim;

import fitnesse.testsystems.slim.results.SlimExceptionResult;
import org.junit.Test;

import static org.junit.Assert.*;

public class SlimFixtureExceptionTest {

    @Test
    public void testNoStackTrace() {
        SlimFixtureException e = new SlimFixtureException(false, "Hallo");
        SlimExceptionResult exceptionResult = new SlimExceptionResult("key", e.getMessage());
        assertTrue(exceptionResult.hasMessage());
        assertEquals("Hallo", exceptionResult.getMessage());
    }

    @Test
    public void testStackTrace() {
        SlimFixtureException e = new SlimFixtureException("Hallo");
        SlimExceptionResult exceptionResult = new SlimExceptionResult("key", e.getMessage());
        assertFalse(exceptionResult.hasMessage());
    }
}
