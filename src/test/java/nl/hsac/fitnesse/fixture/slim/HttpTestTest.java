package nl.hsac.fitnesse.fixture.slim;


import nl.hsac.fitnesse.fixture.util.XmlHttpResponse;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests HttpTest.
 */
public class HttpTestTest {
    private final HttpTest client = new HttpTest();

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

        client.clearValues();
        client.setValueFor("1234", "field.key");
        getUrl = client.createUrlWithParams("https://mysite.nl/test");
        assertEquals("https://mysite.nl/test?field.key=1234", getUrl);
        client.setValueFor("f", "fieldkey");
        getUrl = client.createUrlWithParams("https://mysite.nl/test");
        assertEquals("https://mysite.nl/test?field.key=1234&fieldkey=f", getUrl);

        client.clearValues();
        client.setValuesFor("one, two,   three", "param.name");
        client.setValuesFor("one", "name2");
        client.setValuesFor("one, two", "param2.name");
        client.setValueFor("three", "param2.nested");
        getUrl = client.createUrlWithParams("http://mysite.nl:8080/test");
        assertEquals("http://mysite.nl:8080/test?param.name=one&param.name=two&param.name=three" +
                    "&name2=one&param2.name=one&param2.name=two&param2.nested=three", getUrl);
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
        Map<String, Object> headers = new HashMap();
        headers.put("Location", serverUrl + "/a");
        mockXmlServerSetup.addResponseWithStatusAndHeaders("", 301, headers);

        mockXmlServerSetup.addResponseFor("<hello/>", "GET: /FitNesseMock/a");
        return serverUrl;
    }

    /**
     * Test get
     */
    @Test
    public void testGet() {
        HttpTest httpTest = new HttpTest();
        XmlHttpResponse req1 = checkCall(url -> httpTest.getFrom(url));
        assertEquals("GET", httpTest.getResponse().getMethod());
        assertEquals("GET", req1.getMethod());
        assertEquals("GET: /FitNesseMock", req1.getRequest());
    }

    /**
     * Test head
     */
    @Test
    public void testHead() {
        HttpTest httpTest = new HttpTest();
        XmlHttpResponse req1 = checkCall(url -> httpTest.headFrom(url));
        assertEquals("HEAD", httpTest.getResponse().getMethod());
        assertEquals("HEAD", req1.getMethod());
        assertEquals("HEAD: /FitNesseMock", req1.getRequest());
    }

    /**
     * Test post
     */
    @Test
    public void testPost() {
        HttpTest httpTest = new HttpTest();
        XmlHttpResponse req1 = checkCall(url -> httpTest.postTo("a", url));
        assertEquals("POST", httpTest.getResponse().getMethod());
        assertEquals("POST", req1.getMethod());
        assertEquals("a", req1.getRequest());
    }

    /**
     * Test put
     */
    @Test
    public void testPut() {
        HttpTest httpTest = new HttpTest();
        XmlHttpResponse req1 = checkCall(url -> httpTest.putTo("b", url));
        assertEquals("PUT", httpTest.getResponse().getMethod());
        assertEquals("PUT", req1.getMethod());
        assertEquals("b", req1.getRequest());
    }

    /**
     * Test delete with body
     */
    @Test
    public void testDeleteWithBody() {
        HttpTest httpTest = new HttpTest();
        XmlHttpResponse req1 = checkCall(url -> httpTest.deleteWith(url, "a=1"));
        assertEquals("DELETE", httpTest.getResponse().getMethod());
        assertEquals("DELETE", req1.getMethod());
        assertEquals("a=1", req1.getRequest());
    }

    /**
     * Test delete without body
     */
    @Test
    public void testDeleteWithoutBody() {
        HttpTest httpTest = new HttpTest();
        XmlHttpResponse req1 = checkCall(url -> httpTest.delete(url));
        assertEquals("DELETE", httpTest.getResponse().getMethod());
        assertEquals("DELETE", req1.getMethod());
        assertEquals("DELETE: /FitNesseMock", req1.getRequest());
    }

    /**
     * Test post with template
     */
    @Test
    public void testPostWithTemplate() {
        HttpTest httpTest = setupHttpTestWithTemplate();

        XmlHttpResponse req1 = checkCall(url -> httpTest.postTemplateTo(url));
        assertEquals("POST", httpTest.getResponse().getMethod());
        checkTemplateRequestBody(httpTest.getResponse().getMethod(), req1);
    }

    /**
     * Test put with template
     */
    @Test
    public void testPutWithTemplate() {
        HttpTest httpTest = setupHttpTestWithTemplate();

        XmlHttpResponse req1 = checkCall(url -> httpTest.putTemplateTo(url));
        assertEquals("PUT", httpTest.getResponse().getMethod());
        checkTemplateRequestBody(httpTest.getResponse().getMethod(), req1);
    }

    /**
     * Test delete with template
     */
    @Test
    public void testDeleteWithTemplate() {
        HttpTest httpTest = setupHttpTestWithTemplate();

        XmlHttpResponse req1 = checkCall(url -> httpTest.deleteWithTemplate(url));
        assertEquals("DELETE", httpTest.getResponse().getMethod());
        checkTemplateRequestBody(httpTest.getResponse().getMethod(), req1);
    }

    static XmlHttpResponse checkCall(Function<String, Boolean> call) {
        MockXmlServerSetup mockXmlServerSetup = new MockXmlServerSetup();
        mockXmlServerSetup.addResponse("hallo");

        try {
            String serverUrl = mockXmlServerSetup.getMockServerUrl();

            boolean result = call.apply(serverUrl);
            assertTrue(result);

            return mockXmlServerSetup.getResponseList().get(0);
        } finally {
            mockXmlServerSetup.stop();
        }
    }

    private HttpTest setupHttpTestWithTemplate() {
        HttpTest httpTest = new HttpTest();
        httpTest.template("samplePost.ftl.xml");
        httpTest.setValueFor("Oosterhout", "countryName");
        return httpTest;
    }

    private void checkTemplateRequestBody(String method, XmlHttpResponse req1) {
        assertEquals(method, req1.getMethod());

        assertEquals("<s11:Envelope xmlns:s11=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "\t<s11:Body>\n" +
                "\t\t<ns1:GetWeather xmlns:ns1=\"http://www.webserviceX.NET\">\n" +
                "\t\t\t\t\t\t<ns1:CountryName>Oosterhout</ns1:CountryName>\n" +
                "\t\t</ns1:GetWeather>\n" +
                "\t</s11:Body>\n" +
                "</s11:Envelope>\n", req1.getRequest());
    }

    /**
     * Test get, with retry
     */
    @Test
    public void testGetRetry() {
        HttpTest httpTest = new HttpTest();
        XmlHttpResponse req1 = checkCallWithRetry(httpTest, url -> httpTest.getFrom(url));
        assertEquals("GET", httpTest.getResponse().getMethod());
        assertEquals("GET", req1.getMethod());
        assertEquals("GET: /FitNesseMock", req1.getRequest());
    }

    /**
     * Test head, with retry
     */
    @Test
    public void testHeadRetry() {
        HttpTest httpTest = new HttpTest();
        XmlHttpResponse req1 = checkCallWithRetry(httpTest, url -> httpTest.headFrom(url));
        assertEquals("HEAD", httpTest.getResponse().getMethod());
        assertEquals("HEAD", req1.getMethod());
        assertEquals("HEAD: /FitNesseMock", req1.getRequest());
    }

    /**
     * Test post, with retry
     */
    @Test
    public void testPostRetry() {
        HttpTest httpTest = new HttpTest();
        XmlHttpResponse req1 = checkCallWithRetry(httpTest, url -> httpTest.postTo("a", url));
        assertEquals("POST", httpTest.getResponse().getMethod());
        assertEquals("POST", req1.getMethod());
        assertEquals("a", req1.getRequest());
    }

    /**
     * Test put, with retry
     */
    @Test
    public void testPutRetry() {
        HttpTest httpTest = new HttpTest();
        XmlHttpResponse req1 = checkCallWithRetry(httpTest, url -> httpTest.putTo("b", url));
        assertEquals("PUT", httpTest.getResponse().getMethod());
        assertEquals("PUT", req1.getMethod());
        assertEquals("b", req1.getRequest());
    }

    /**
     * Test delete with body, with retry
     */
    @Test
    public void testDeleteWithBodyRetry() {
        HttpTest httpTest = new HttpTest();
        XmlHttpResponse req1 = checkCallWithRetry(httpTest, url -> httpTest.deleteWith(url, "a=1"));
        assertEquals("DELETE", httpTest.getResponse().getMethod());
        assertEquals("DELETE", req1.getMethod());
        assertEquals("a=1", req1.getRequest());
    }

    /**
     * Test delete without body, with retry
     */
    @Test
    public void testDeleteWithoutBodyRetry() {
        HttpTest httpTest = new HttpTest();
        XmlHttpResponse req1 = checkCallWithRetry(httpTest, url -> httpTest.delete(url));
        assertEquals("DELETE", httpTest.getResponse().getMethod());
        assertEquals("DELETE", req1.getMethod());
        assertEquals("DELETE: /FitNesseMock", req1.getRequest());
    }

    static XmlHttpResponse checkCallWithRetry(HttpTest httpTest, Function<String, Boolean> call) {
        MockXmlServerSetup mockXmlServerSetup = new MockXmlServerSetup();
        mockXmlServerSetup.addResponseWithStatus("error1", 500);
        mockXmlServerSetup.addResponseWithStatus("error2", 500);
        mockXmlServerSetup.addResponse("hi");

        try {
            String serverUrl = mockXmlServerSetup.getMockServerUrl();

            boolean result = call.apply(serverUrl);
            assertFalse(result);

            result = httpTest.repeatUntilResponseStatusIs(200);
            assertTrue(result);
            assertEquals(2, httpTest.repeatCount());

            return mockXmlServerSetup.getResponseList().get(2);
        } finally {
            mockXmlServerSetup.stop();
        }
    }
}
