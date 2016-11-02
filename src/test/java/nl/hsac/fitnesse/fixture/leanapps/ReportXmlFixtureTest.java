package nl.hsac.fitnesse.fixture.leanapps;

import nl.hsac.fitnesse.fixture.UnitTestHelper;
import nl.hsac.fitnesse.fixture.util.XmlHttpResponse;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests ReportXmlFixture.
 */
public class ReportXmlFixtureTest {
    private ReportXmlFixture fixture = new ReportXmlFixture();

    /**
     * Tests XPath to get report trigger value.
     */
    @Test
    public void testReportTrigger() {
        XmlHttpResponse resp = new XmlHttpResponse();
        UnitTestHelper.fillResponse(resp, "leanapps/report.xml");
        resp.setStatusCode(200);
        fixture.setResponse(resp);
        assertEquals("NEW_POLICY_NEW_CUSTOMER", fixture.reportTrigger());
    }
    /**
     * Tests filename generated.
     */
    @Test
    public void testFilename() {
        fixture.setPolicyNr("1234");
        assertEquals("policy_1234_1_1.xml", fixture.getReportXmlFilename());

        fixture.setVersionNr("2");
        assertEquals("policy_1234_1_2.xml", fixture.getReportXmlFilename());
        
        fixture.setTermination(true);
        assertEquals("policy_laa004_1234.xml", fixture.getReportXmlFilename());
    }
}
