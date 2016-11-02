package nl.hsac.fitnesse.fixture.util;

import nl.hsac.fitnesse.fixture.fit.SoapCallMapColumnFixture;
import org.apache.http.HttpStatus;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests XmlHttpResponse.
 */
public class XmlHttpResponseTest {
    final static String OK_RESP = "<?xml version='1.0' encoding='UTF-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body><ns2:yarOpportunityResponse xmlns:ns2=\"http://www.openuri.org/\" xmlns=\"http://www.leanapps.com/life/wslife\"><opportunityReturn><errorStatus>OK</errorStatus><calculatedResult>13.44</calculatedResult><amountPremiumYear>158.86</amountPremiumYear></opportunityReturn></ns2:yarOpportunityResponse></soapenv:Body></soapenv:Envelope>";
    final static String MULTIPLY_NODES_RESP = "<?xml version='1.0' encoding='UTF-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body><ns2:yarOpportunityResponse xmlns:ns2=\"http://www.openuri.org/\" xmlns=\"http://www.leanapps.com/life/wslife\"><opportunityReturns><errorStatus>OK</errorStatus><opportunityReturn><calculatedResult>125.14</calculatedResult><amountPremiumYear>125.14</amountPremiumYear></opportunityReturn><opportunityReturn><calculatedResult>13.44</calculatedResult><amountPremiumYear>158.86</amountPremiumYear></opportunityReturn><opportunityReturn><calculatedResult>19.37</calculatedResult><amountPremiumYear>139.24</amountPremiumYear></opportunityReturn></opportunityReturns></ns2:yarOpportunityResponse></soapenv:Body></soapenv:Envelope>";


    @Rule
    public ExpectedException expect = ExpectedException.none();

    /**
     * Checks validResponse when statusCode is good.
     */
    @Test
    public void testValidResponse() {
        HttpResponse resp = new XmlHttpResponse();
        resp.setStatusCode(HttpStatus.SC_ACCEPTED);
        resp.validResponse();
    }

    /**
     * Checks validResponse when statusCode is bad.
     */
    @Test
    public void testValidResponseWrongStatus() {
        expect.expect(RuntimeException.class);
        HttpResponse resp = new XmlHttpResponse();
        resp.setStatusCode(HttpStatus.SC_NOT_IMPLEMENTED);
        resp.validResponse();
    }

    /**
     * Checks getting of XPath when statusCode is bad.
     */
    @Test
    public void testGetXPathWrongStatus() {
        expect.expect(RuntimeException.class);
        XmlHttpResponse resp = new XmlHttpResponse();
        resp.setStatusCode(HttpStatus.SC_NOT_IMPLEMENTED);

        resp.getXPath("/");
    }

    /**
     * Tests getting of XPath
     */
    @Test
    public void testGetXPath() {
        XmlHttpResponse resp = getOKResponse();
        String xpathResult = resp.getXPath("//*[local-name()='amountPremiumYear']");
        assertEquals("158.86", xpathResult);
    }

    /**
     * Tests getting of XPath
     */
    @Test
    public void testGetXPathList() {
        XmlHttpResponse resp = createResponse();
        resp.setResponse(MULTIPLY_NODES_RESP);

        List<String> xpathResult =
                resp.getAllXPath("//*[local-name()='amountPremiumYear']/text()");
        assertEquals(3, xpathResult.size());
        assertEquals("125.14", xpathResult.get(0));
        assertEquals("158.86", xpathResult.get(1));
        assertEquals("139.24", xpathResult.get(2));

        xpathResult = resp.getAllXPath("//*[local-name()='calculatedResult']/text()");
        assertEquals(3, xpathResult.size());
        assertEquals("125.14", xpathResult.get(0));
        assertEquals("13.44", xpathResult.get(1));
        assertEquals("19.37", xpathResult.get(2));
    }

    /**
     * Tests getting of XPath as double
     */
    @Test
    public void testGetXPathDouble() {
        XmlHttpResponse resp = getOKResponse();
        double xpathResult = resp.getXPathDouble("//*[local-name()='amountPremiumYear']");
        assertEquals(158.86, xpathResult, 0d);
    }

    private XmlHttpResponse getOKResponse() {
        XmlHttpResponse resp = createResponse();
        resp.setStatusCode(HttpStatus.SC_ACCEPTED);
        resp.setResponse(OK_RESP);
        return resp;
    }

    private XmlHttpResponse createResponse() {
        XmlHttpResponse resp = new XmlHttpResponse();
        resp.setXPathHelper(new XPathHelper());
        return resp;
    }

    @Test
    public void testCheckXPathsCorrect() {
        XmlHttpResponse resp = getOKResponse();

        Map<String, Object> values = new HashMap<String, Object>();
        values.put("amount", "158.86");
        values.put("result", "13.44");

        Map<String, String> expressionsToCheck = createExprToCheck();

        XPathCheckResult checkResult = resp.checkXPaths(values, expressionsToCheck);
        assertEquals("OK", checkResult.getResult());
        assertNull(checkResult.getMismatchDetail());
    }

    @Test
    public void testCheckXPathsCorrectWithDoubleHandling() {
        XmlHttpResponse resp = getOKResponse();

        Map<String, Object> values = new HashMap<String, Object>();
        values.put("amount", "158.860");
        values.put("result", "013.440000");

        Map<String, String> expressionsToCheck = createExprToCheck();

        XPathCheckResult checkResult = resp.checkXPaths(values, expressionsToCheck);
        assertEquals("OK", checkResult.getResult());
        assertNull(checkResult.getMismatchDetail());
    }

    @Test
    public void testCheckXPathsIncorrect() {
        XmlHttpResponse resp = getOKResponse();

        Map<String, Object> values = new HashMap<String, Object>();
        values.put("amount", "12");
        values.put("result", "1");
        values.put("status", "OK");

        Map<String, String> expressionsToCheck = createExprToCheck();
        expressionsToCheck.put("//*[local-name()='errorStatus']", "unknownKey");
        expressionsToCheck.put("//noMatchXPath", "status");

        String expected = SoapCallMapColumnFixture.NO_ESCAPE_PREFIX
                            + "NOK:\n<ul>\n"
                            + " <li>result: 1 <> 13.44</li>\n"
                            + " <li>status: OK <> null</li>\n"
                            + " <li>unknownKey: null <> OK</li>\n"
                            + " <li>amount: 12 <> 158.86</li>\n"
                            + "</ul>";
        XPathCheckResult checkResult = resp.checkXPaths(values, expressionsToCheck);
        assertEquals("NOK", checkResult.getResult());
        assertEquals(expected, checkResult.getMismatchDetail());
    }

    private Map<String, String> createExprToCheck() {
        Map<String, String> expressionsToCheck = new HashMap<String, String>();
        expressionsToCheck.put("//*[local-name()='amountPremiumYear']", "amount");
        expressionsToCheck.put("//*[local-name()='calculatedResult']", "result");
        return expressionsToCheck;
    }

    @Test
    public void testAllXPath() {
        XmlHttpResponse resp = getOKResponse();
        List<String> xpathResults = resp.getAllXPath("//*/text()");
        assertEquals(3, xpathResults.size());
    }
}
