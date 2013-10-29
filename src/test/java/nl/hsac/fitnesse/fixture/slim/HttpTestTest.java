package nl.hsac.fitnesse.fixture.slim;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
    }
}
