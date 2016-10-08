package nl.hsac.fitnesse.fixture.slim;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests JsonHttpTest.
 */
public class JsonHttpTestTest {
    private JsonHttpTest fixture = new JsonHttpTest();

    @Test
    public void testJsonPathHandleEmptyResponse() {
        try {
            Object result = fixture.jsonPath("summary");
            fail("Expected exception. Got: " + result);
        } catch (SlimFixtureException e) {
            checkException(e);
        }
    }

    @Test
    public void testJsonPathCountHandleEmptyResponse() {
        try {
            Object result = fixture.jsonPathCount("summary");
            fail("Expected exception. Got: " + result);
        } catch (SlimFixtureException e) {
            checkException(e);
        }
    }

    private void checkException(SlimFixtureException e) {
        String msg = e.getMessage();
        assertTrue("Did not expect a stacktrace for wiki" + msg, msg.startsWith("message:<<"));
    }
}
