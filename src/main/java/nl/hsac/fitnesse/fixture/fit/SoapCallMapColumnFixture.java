package nl.hsac.fitnesse.fixture.fit;

import nl.hsac.fitnesse.fixture.Environment;
import nl.hsac.fitnesse.fixture.util.XmlHttpResponse;

import java.util.Collections;
import java.util.Map;

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
            XmlHttpResponse checkResponse = getEnvironment().createInstance(getCheckResponseClass());
            checkResponse.setResponse("Unable to perform check. Error: " + e.getMessage());
            setRawCheckResponse(checkResponse);
        }
    }

    @Override
    protected XmlHttpResponse callCheckService() {
        return getEnvironment().createInstance(getCheckResponseClass());
    }

    /**
     * Creates response, calls service using configured template and current row's values and calls SOAP service.
     * @param urlSymbolKey key of symbol containing service's URL.
     * @param soapAction SOAPAction header value (null if no header is required).
     * @return filled response
     */
    protected Response callServiceImpl(String urlSymbolKey, String soapAction) {
        String url = getSymbol(urlSymbolKey).toString();
        Response response = getEnvironment().createInstance(getResponseClass());
        callSoapService(url, getTemplateName(), soapAction, response);
        return response;
    }

    /**
     * Creates check response, calls service using configured check template and current row's values and calls SOAP service.
     * @param urlSymbolKey key of symbol containing check service's URL.
     * @param soapAction SOAPAction header value (null if no header is required).
     * @return filled check response
     */
    protected XmlHttpResponse callCheckServiceImpl(String urlSymbolKey, String soapAction) {
        String url = getSymbol(urlSymbolKey).toString();
        XmlHttpResponse response = getEnvironment().createInstance(getCheckResponseClass());
        callSoapService(url, getCheckTemplateName(), soapAction, response);
        return response;
    }

    /**
     * Calls SOAP service using template and current row's values.
     * @param url url of service to call.
     * @param templateName name of template to use to create POST body.
     * @param soapAction SOAPAction header value (null if no header is required).
     * @param response response to fill based on call.
     */
    protected void callSoapService(String url, String templateName, String soapAction, XmlHttpResponse response) {
        Map<String, Object> headers = soapAction != null ? Collections.singletonMap("SOAPAction", (Object) soapAction) : null;
        getEnvironment().callService(url, templateName, getCurrentRowValues(), response, headers);
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
