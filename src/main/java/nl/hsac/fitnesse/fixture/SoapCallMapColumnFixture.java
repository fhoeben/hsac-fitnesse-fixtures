package nl.hsac.fitnesse.fixture;

import nl.hsac.fitnesse.fixture.util.XmlHttpResponse;

/**
 * Base class for Fixtures making SOAP calls, where checks are also performed using SOAP.
 */
public abstract class SoapCallMapColumnFixture<Response extends XmlHttpResponse>
                    extends TemplateBasedMapColumnFixture<Response, XmlHttpResponse> {

    /**
     * Registers a namespace in the environment, so the prefix can be used in
     * XPath expressions.
     * @param prefix prefix to be used in XPath expressions.
     * @param url namespace URI the prefix should map to.
     */
    protected static void registerNs(String prefix, String url) {
        Environment.getInstance().registerNamespace(prefix, url);
    }

    /**
     * Creates new.
     * @param aResponseClass class of response (for original call to SOAP service) expected.
     */
    public SoapCallMapColumnFixture(Class<? extends Response> aResponseClass) {
        super(aResponseClass, XmlHttpResponse.class);
    }

    @Override
    protected void executeCheckCall() {
        try {
            super.executeCheckCall();
        } catch (RuntimeException e) {
            // ignore, no checkResponse will be available
            XmlHttpResponse checkResponse = getRawCheckResponse();
            checkResponse.setResponse("Unable to perform check. Error: " + e.getMessage());
        }
    }


    /**
     * @return request to be sent formatted to be displayed
     */
    public String request() {
        return xmlAsHtml(getRequest());
    }

    /**
     * @return response formatted to be displayed
     */
    public String response() {
        return xmlAsHtml(getResponse());
    }

    /**
     * @return request that was sent
     */
    public String getRequest() {
        return getRawResponse().getRequest();
    }

    /**
     * @return response as received
     */
    public String getResponse() {
        return getRawResponse().getResponse();
    }

    /**
     * @return response formatted to be displayed
     */
    public String checkResponse() {
        return xmlAsHtml(getRawCheckResponse().getResponse());
    }

    /**
     * @return check request to be sent formatted to be displayed
     */
    public String checkRequest() {
        return xmlAsHtml(getRawCheckResponse().getRequest());
    }

    protected String xmlAsHtml(String xml) {
        return getEnvironment().getHtmlForXml(xml);
    }

}
