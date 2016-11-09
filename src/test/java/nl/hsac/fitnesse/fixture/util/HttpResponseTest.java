package nl.hsac.fitnesse.fixture.util;

import nl.hsac.fitnesse.fixture.UnitTestHelper;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertSame;

/**
 * Tests HttpResponse.
 */
public class HttpResponseTest {
    @Rule
    public ExpectedException expect = ExpectedException.none();

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
    public void testValidResponseServerErrorNotImplemented() {
        expect.expect(RuntimeException.class);
        HttpResponse resp = new HttpResponse();
        resp.setStatusCode(HttpStatus.SC_NOT_IMPLEMENTED);
        resp.validResponse();
    }

    @Test
    public void testValidResponseServerErrorGeneric() {
        expect.expect(RuntimeException.class);
        HttpResponse resp = new HttpResponse();
        resp.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        resp.validResponse();
    }

    @Test
    public void testValidResponseClientErrorGeneric() {
        expect.expect(RuntimeException.class);
        HttpResponse resp = new HttpResponse();
        resp.setStatusCode(HttpStatus.SC_BAD_REQUEST);
        resp.validResponse();
    }

    @Test
    public void testValidResponseClientErrorNotFound() {
        expect.expect(RuntimeException.class);
        HttpResponse resp = new HttpResponse();
        resp.setStatusCode(HttpStatus.SC_NOT_FOUND);
        resp.validResponse();
    }

    @Test
    public void testValidResponseClientErrorForbidden() {
        expect.expect(RuntimeException.class);
        HttpResponse resp = new HttpResponse();
        resp.setStatusCode(HttpStatus.SC_FORBIDDEN);
        resp.validResponse();
    }
}
