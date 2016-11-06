package nl.hsac.fitnesse.fixture.slim;


import nl.hsac.fitnesse.fixture.util.XmlHttpResponse;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests HttpTest.
 */
public class HttpTestTest {
    private final HttpTest client = new XmlHttpTest();

    @Test
    public void testUrlCleanUp() {

        String cleanedUrl = client.getUrl("<a href=\"http://mysite.nl/test\">http://mysite.nl/test</a>");
        assertEquals("http://mysite.nl/test", cleanedUrl);

        cleanedUrl = client.getUrl("http://mysite.nl/test");
        assertEquals("http://mysite.nl/test", cleanedUrl);

        cleanedUrl = client.getUrl("https://mysite.nl:8443/test");
        assertEquals("https://mysite.nl:8443/test", cleanedUrl);

        cleanedUrl = client.getUrl("<a href=\"http://mysite.nl/test\">http://mysite.nl/test</a>/test");
        assertEquals("http://mysite.nl/test/test", cleanedUrl);

        cleanedUrl = client.getUrl("<a href=\"https://mysite.nl/test\">https://mysite.nl/test</a>/test");
        assertEquals("https://mysite.nl/test/test", cleanedUrl);
    }

    @Test
    public void testGetUrlWithParams() {
        String getUrl = client.createUrlWithParams("https://mysite.nl/test");
        assertEquals("https://mysite.nl/test", getUrl);

        client.clearValues();
        client.setValueFor("John", "name");
        getUrl = client.createUrlWithParams("https://mysite.nl/test?age=12");
        assertEquals("https://mysite.nl/test?age=12&name=John", getUrl);
        getUrl = client.createUrlWithParams("https://mysite.nl/test?");
        assertEquals("https://mysite.nl/test?name=John", getUrl);

        client.clearValues();
        client.setValueFor("John", "name");
        client.setValueFor("12", "age");
        getUrl = client.createUrlWithParams("http://mysite.nl/test");
        assertEquals("http://mysite.nl/test?name=John&age=12", getUrl);

        client.clearValues();
        client.setValueFor("12", "age");
        client.setValueFor("John&Pete", "name");
        getUrl = client.createUrlWithParams("https://mysite.nl/test");
        assertEquals("https://mysite.nl/test?age=12&name=John%26Pete", getUrl);

        client.clearValues();
        client.setValueFor("12", "één");
        getUrl = client.createUrlWithParams("http://mysite.nl:8080/test");
        assertEquals("http://mysite.nl:8080/test?%C3%A9%C3%A9n=12", getUrl);

        client.clearValues();
        client.setValueFor(null, "param");
        getUrl = client.createUrlWithParams("http://mysite.nl:8080/test");
        assertEquals("http://mysite.nl:8080/test?param", getUrl);

        client.clearValues();
        client.setValueFor("", "param");
        getUrl = client.createUrlWithParams("http://mysite.nl:8080/test");
        assertEquals("http://mysite.nl:8080/test?param=", getUrl);

        client.clearValues();
        client.setValuesFor("one, two,   three", "param");
        getUrl = client.createUrlWithParams("http://mysite.nl:8080/test");
        assertEquals("http://mysite.nl:8080/test?param=one&param=two&param=three", getUrl);
    }

    @Test
    public void testBodyCleanup() {
        String body = "<xml>";
        String cleaned = client.cleanupBody(body);
        assertEquals(body, cleaned);
    }

    @Test
    public void testBodyCleanupPre() {
        String cleaned = client.cleanupBody("<pre> \n" +
                "&lt;MyContent&gt;\n" +
                "  &lt;content a='c'/&gt;\n" +
                "&lt;/MyContent&gt;\n" +
                " </pre>");

        assertEquals("<MyContent>\n  <content a='c'/>\n</MyContent>", cleaned);
    }

    /**
     * Tests url redirects with follow redirects (default setting)
     */
    @Test
    public void testGetFromFollowRedirect() throws Exception {
        MockXmlServerSetup mockXmlServerSetup = new MockXmlServerSetup();

        try {
            String serverUrl = setupRedirectResponse(mockXmlServerSetup);

            HttpTest httpTest = new HttpTest();
            boolean result = httpTest.getFrom(serverUrl);
            String resp = httpTest.htmlResponse();
            assertNotNull(resp);
            assertEquals(200, httpTest.getResponse().getStatusCode());
            assertEquals("<div><hello/></div>", resp);
            assertTrue(result);

            assertTrue(mockXmlServerSetup.verifyAllResponsesServed());
        } finally {
            mockXmlServerSetup.stop();
        }
    }

    /**
     * Test url redirects without following redirect
     */
    @Test
    public void testGetFromNoRedirect() throws Exception {
        MockXmlServerSetup mockXmlServerSetup = new MockXmlServerSetup();

        try {
            String serverUrl = setupRedirectResponse(mockXmlServerSetup);

            HttpTest httpTest = new HttpTest();
            boolean result = httpTest.getFromNoRedirect(serverUrl);
            String resp = httpTest.htmlResponse();
            assertNotNull(resp);
            assertEquals(301, httpTest.getResponse().getStatusCode());
            assertTrue(result);
        } finally {
            mockXmlServerSetup.stop();
        }
    }

    private String setupRedirectResponse(MockXmlServerSetup mockXmlServerSetup) {
        String serverUrl = mockXmlServerSetup.getMockServerUrl();
        XmlHttpResponse redirectResp = new XmlHttpResponse();
        redirectResp.setStatusCode(301);
        redirectResp.getResponseHeaders().put("Location", serverUrl + "/a");

        List<XmlHttpResponse> list = (List<XmlHttpResponse>) mockXmlServerSetup.getResponseList();
        list.add(redirectResp);

        mockXmlServerSetup.addResponse("<hello/>");
        return serverUrl;
    }
}
