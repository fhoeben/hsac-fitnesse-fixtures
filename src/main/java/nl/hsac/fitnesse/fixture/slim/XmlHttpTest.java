package nl.hsac.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.util.XMLFormatter;
import nl.hsac.fitnesse.fixture.util.XmlHttpResponse;

/**
 * Fixture to make HTTP requests returning XNL using Slim scripts and/or scenarios.
 * Example use: make SOAP calls using #postTo().
 */
public class XmlHttpTest extends HttpTest {
    private final XMLFormatter xmlFormatter = new XMLFormatter();

    /**
     * Register a prefix to use in XPath expressions.
     * @param prefix prefix to be used in xPath expressions.
     * @param namespace XML namespace the prefix should point to.
     */
    public void registerPrefixForNamespace(String prefix, String namespace) {
        getEnvironment().registerNamespace(prefix, getUrl(namespace));
    }

    /**
     * @return request sent last time postTo() was called.
     */
    @Override
    public String request() {
        return getEnvironment().getHtmlForXml(super.request());
    }

    /**
     * @return response received last time postTo() was called.
     */
    @Override
    public String response() {
        String response = super.response();
        String result;
        try {
            result = getEnvironment().getHtmlForXml(response);
        } catch (Exception e) {
            result = response;
        }
        return result;
    }

    /**
     * Gets XPath value, without ensuring response was valid.
     * @param xPathExpr expression to evaluate.
     * @return result of expression evaluation against last response received.
     */
    public String rawXPath(String xPathExpr) {
        return getResponse().getRawXPath(xPathExpr);
    }

    /**
     * @param xPathExpr expression to evaluate.
     * @return result of expression evaluation against last response received.
     * @throws RuntimeException if no valid response was available or XPath could not be evaluated.
     */
    public String xPath(String xPathExpr) {
        return getResponse().getXPath(xPathExpr);
    }

    /**
     * @param xPathExpr expression to evaluate.
     * @return result of expression evaluation against last response received.
     * @throws RuntimeException if no valid response was available or XPath could not be evaluated.
     */
    public Double xPathDouble(String xPathExpr) {
        return getResponse().getXPathDouble(xPathExpr);
    }

    /**
     * @param xPathExpr expression to evaluate.
     * @return result of expression evaluation against last response received.
     * @throws RuntimeException if no valid response was available or XPath could not be evaluated.
     */
    public Double xPathInt(String xPathExpr) {
        return getResponse().getXPathDouble(xPathExpr);
    }

    @Override
    protected boolean postProcessResponse() {
        // always called after post or get, so easy place to ensure namespaces are registered
        getResponse().setNamespaceContext(getEnvironment().getNamespaceContext());
        boolean result = super.postProcessResponse();
        return result;
    }

    @Override
    protected XmlHttpResponse getResponse() {
        return (XmlHttpResponse) super.getResponse();
    }

    @Override
    protected XmlHttpResponse createResponse() {
        return new XmlHttpResponse();
    }
}
