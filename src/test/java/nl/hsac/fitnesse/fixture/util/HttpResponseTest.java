package nl.hsac.fitnesse.fixture.util;

import nl.hsac.fitnesse.fixture.UnitTestHelper;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

/**
 * Tests HttpResponse.
 */
public class HttpResponseTest {
    @Before
    public void setUp() {
        HttpResponse.clearInstances();
    }

    @Test
    public void testParseLalResponse() {
        XmlHttpResponse lalResponse = new XmlHttpResponse();
        UnitTestHelper.fillResponse(lalResponse, "leanapps/getPolicyCheckResponse.xml");
        String key = lalResponse.toString();
        assertSame(lalResponse, HttpResponse.parse(key));
    }

    @Test
    public void testValidResponseOk() {
        HttpResponse resp = new HttpResponse();
        resp.setStatusCode(HttpStatus.SC_OK);
        resp.validResponse();

        resp.setStatusCode(201);
        resp.validResponse();

        resp.setStatusCode(299);
        resp.validResponse();

        resp.setStatusCode(301);
        resp.validResponse();

        resp.setStatusCode(302);
        resp.validResponse();

        resp.setStatusCode(100);
        resp.validResponse();
    }

    @Test
    public void testValidResponseNoResponse() {
        HttpResponse resp = new HttpResponse();
        resp.setStatusCode(0);
        assertThrows(RuntimeException.class, () -> resp.validResponse());
    }

    @Test
    public void testValidResponseTooLowResponse() {
        HttpResponse resp = new HttpResponse();
        resp.setStatusCode(99);
        assertThrows(RuntimeException.class, () -> resp.validResponse());
    }

    @Test
    public void testValidResponseServerErrorNotImplemented() {
        HttpResponse resp = new HttpResponse();
        resp.setStatusCode(HttpStatus.SC_NOT_IMPLEMENTED);
        assertThrows(RuntimeException.class, () -> resp.validResponse());
    }

    @Test
    public void testValidResponseServerErrorGeneric() {
        HttpResponse resp = new HttpResponse();
        resp.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertThrows(RuntimeException.class, () -> resp.validResponse());
    }

    @Test
    public void testValidResponseClientErrorGeneric() {
        HttpResponse resp = new HttpResponse();
        resp.setStatusCode(HttpStatus.SC_BAD_REQUEST);
        assertThrows(RuntimeException.class, () -> resp.validResponse());
    }

    @Test
    public void testValidResponseClientErrorNotFound() {
        HttpResponse resp = new HttpResponse();
        resp.setStatusCode(HttpStatus.SC_NOT_FOUND);
        assertThrows(RuntimeException.class, () -> resp.validResponse());
    }

    @Test
    public void testValidResponseClientErrorForbidden() {
        HttpResponse resp = new HttpResponse();
        resp.setStatusCode(HttpStatus.SC_FORBIDDEN);
        assertThrows(RuntimeException.class, () -> resp.validResponse());
    }
}
