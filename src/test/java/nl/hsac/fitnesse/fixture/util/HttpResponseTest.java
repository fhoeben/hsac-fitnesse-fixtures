package nl.hsac.fitnesse.fixture.util;

import nl.hsac.fitnesse.fixture.UnitTestHelper;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertSame;

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

}
