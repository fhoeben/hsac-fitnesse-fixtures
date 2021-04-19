package nl.hsac.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.util.XmlHttpResponse;
import org.apache.http.entity.ContentType;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static nl.hsac.fitnesse.fixture.slim.HttpTestTest.checkCall;
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
                "    &quot;price&quot;: 8.95,\n" +
                "    &quot;category&quot;: &quot;reference&quot;\n" +
                "}</pre>";

        assertEquals(expected,
                fixture.safeFormatValue("{\"price\": 8.95,\"category\": \"reference\"}").replace("\r", ""));
        assertEquals(expected,
                fixture.safeFormatValue(" {\"price\": 8.95,\"category\": \"reference\"}").replace("\r", ""));
    }

    @Test
    public void testFormatJsonArray() {
        String expected = "<pre>[\n" +
                "    {\n" +
                "        &quot;category&quot;: &quot;reference&quot;,\n" +
                "        &quot;nested&quot;: {\n" +
                "            &quot;price&quot;: 8.95,\n" +
                "            &quot;category&quot;: &quot;reference&quot;\n" +
                "        }\n" +
                "    }\n" +
                "]</pre>";

        assertEquals(expected,
                fixture.safeFormatValue("[{\"category\": \"reference\",\"nested\": {\"price\": 8.95,\"category\": \"reference\"}}]").replace("\r", ""));
        assertEquals(expected,
                fixture.safeFormatValue(" [{\"category\": \"reference\",\"nested\": {\"price\": 8.95,\"category\": \"reference\"}}] ").replace("\r", ""));
        assertEquals("<pre>[\n    []\n]</pre>",
                fixture.safeFormatValue(" [[]] ").replace("\r", ""));
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

    @Test
    public void testDefaultContentType() {
        String contentType = fixture.getContentType();
        assertEquals(HttpTest.DEFAULT_POST_CONTENT_TYPE, contentType);
    }

    @Test
    public void testDefaultJsonContentType() {
        String contentType = fixture.getContentTypeForJson();
        assertEquals(ContentType.APPLICATION_JSON.toString(), contentType);

        String typeSet = "application/json";
        fixture.setContentType(typeSet);
        contentType = fixture.getContentTypeForJson();
        assertEquals(typeSet, contentType);
    }

    @Test
    public void testPostValuesAsJson() {
        JsonHttpTest jsonHttpTestTest = new JsonHttpTest();
        jsonHttpTestTest.setValueFor("3", "C");
        jsonHttpTestTest.setValueFor("1", "A");
        jsonHttpTestTest.setValueFor("2", "B");
        XmlHttpResponse req1 = checkCall(url -> jsonHttpTestTest.postValuesAsJsonTo(url));
        assertEquals("POST", jsonHttpTestTest.getResponse().getMethod());
        assertEquals("POST", req1.getMethod());
        assertEquals("{\"C\":\"3\",\"A\":\"1\",\"B\":\"2\"}", req1.getRequest());
    }

    @Test
    public void testPutValuesAsJson() {
        JsonHttpTest jsonHttpTestTest = new JsonHttpTest();
        jsonHttpTestTest.setValueFor("g", "G");
        jsonHttpTestTest.setValueFor("s", "S");
        XmlHttpResponse req1 = checkCall(url -> jsonHttpTestTest.putValuesAsJsonTo(url));
        assertEquals("PUT", jsonHttpTestTest.getResponse().getMethod());
        assertEquals("PUT", req1.getMethod());
        assertEquals("{\"G\":\"g\",\"S\":\"s\"}", req1.getRequest());
    }

    @Test
    public void testDeleteValuesAsJson() {
        JsonHttpTest jsonHttpTestTest = new JsonHttpTest();
        jsonHttpTestTest.setValueFor("3", "C");
        jsonHttpTestTest.setValueFor("4", "d");
        XmlHttpResponse req1 = checkCall(url -> jsonHttpTestTest.deleteWithValuesAsJson(url));
        assertEquals("DELETE", jsonHttpTestTest.getResponse().getMethod());
        assertEquals("DELETE", req1.getMethod());
        assertEquals("{\"C\":\"3\",\"d\":\"4\"}", req1.getRequest());
    }
}
