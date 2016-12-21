package nl.hsac.fitnesse.fixture.slim;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests JsonHttpTest.
 */
public class JsonHttpTestTest {
    private JsonHttpTest fixture = new JsonHttpTest();

    @Test
    public void testFormatJson() {
        String expected = "<pre>{\n" +
                "    &quot;category&quot;: &quot;reference&quot;,\n" +
                "    &quot;price&quot;: 8.95\n" +
                "}</pre>";

        assertEquals(expected,
                fixture.safeFormatValue("{\"category\": \"reference\",\"price\": 8.95}").replace("\r", ""));
        assertEquals(expected,
                fixture.safeFormatValue(" {\"category\": \"reference\",\"price\": 8.95}").replace("\r", ""));
    }

    @Test
    public void testFormatUrl() {
        String expected = "http://myhost.com/get?has=a&hg=9223s";

        assertEquals(expected, fixture.safeFormatValue(expected));
    }

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
