package nl.hsac.fitnesse.fixture.slim;

import freemarker.template.Template;
import nl.hsac.fitnesse.fixture.util.XMLFormatter;
import nl.hsac.fitnesse.fixture.util.XmlHttpResponse;
import nl.hsac.fitnesse.fixture.web.SlimFixture;

import java.util.HashMap;
import java.util.Map;

/**
 * Fixture to send SOAP calls, and check XPaths against responses, using Slim scripts.
 */
public class SOAPClient extends SlimFixture {
    private final XMLFormatter xmlFormatter = new XMLFormatter();
    private final Map<String, String> currentValues = new HashMap<String, String>();
    private String template;
    private XmlHttpResponse response = new XmlHttpResponse();

    /**
     * Sets template to use.
     * @param aTemplate name of template file
     * @return true if template could be found.
     */
    public boolean template(String aTemplate) {
        boolean result = false;
        Template t = getEnvironment().getTemplate(aTemplate);
        if (t != null) {
            template = aTemplate;
            result = true;
        }
        return result;
    }

    /**
     * Stores value to be passed to template.
     * @param value value to be passed.
     * @param name placeholder in template to use this value for.
     */
    public void setValueFor(String value, String name) {
        currentValues.put(name, value);
    }

    /**
     * Sends HTTP POST template with current values to service endpoint.
     * @param serviceUrl service endpoint to send XML to.
     * @return true if call could be made and response did not indicate error.
     */
    public boolean postTo(String serviceUrl) {
        boolean result = false;
        response = new XmlHttpResponse();
        if (template != null) {
            getEnvironment().callService(serviceUrl, template, currentValues, response);
            try {
                response.validResponse();
                result = true;
            } catch (RuntimeException e) {
                result = false;
            }
        }
        return result;
    }

    /**
     * @return request sent last time postTo() was called.
     */
    public String request() {
        return xmlFormatter.format(response.getRequest());
    }

    /**
     * @return response received last time postTo() was called.
     */
    public String response() {
        return xmlFormatter.format(response.getResponse());
    }

    /**
     * @return http status received last time postTo() was called.
     */
    public int responseStatus() {
        return response.getStatusCode();
    }

    /**
     * @param xPathExpr expression to evaluate.
     * @return result of expression evaluation against last response received.
     */
    public String xPath(String xPathExpr) {
        return response.getXPath(xPathExpr);
    }

    /**
     * @param xPathExpr expression to evaluate.
     * @return result of expression evaluation against last response received.
     */
    public Double xPathDouble(String xPathExpr) {
        return response.getXPathDouble(xPathExpr);
    }

    /**
     * @param xPathExpr expression to evaluate.
     * @return result of expression evaluation against last response received.
     */
    public Double xPathInt(String xPathExpr) {
        return response.getXPathDouble(xPathExpr);
    }
}
