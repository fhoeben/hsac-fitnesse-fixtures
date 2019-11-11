package nl.hsac.fitnesse.fixture.util;

import fit.exception.FitFailureException;
import nl.hsac.fitnesse.fixture.Environment;

import javax.xml.namespace.NamespaceContext;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * Wrapper around XML HTTP response (and request).
 */
public class XmlHttpResponse extends HttpResponse {
    /** Content type for requests. */
    public final static String CONTENT_TYPE_XML_TEXT_UTF8 = "text/xml; charset=UTF-8";

    private NamespaceContext namespaceContext;
    private XPathHelper xPathHelper;

    @Override
    public void validResponse() {
        super.validResponse();

        String response = getResponse();
        if (response != null) {
            boolean ableToXPath = false;
            try {
                String faultCode = getRawXPath(response, "/env:Envelope/env:Body/env:Fault/faultcode");
                ableToXPath = true;
                if (faultCode != null) {
                    Environment.handleErrorResponse("SOAP fault received: ", response);
                }
            } catch (FitFailureException e) {
                if (ableToXPath) {
                    throw e;
                } else {
                    Environment.handleErrorResponse("Unable to check for SOAP fault, is the result XML? Response was:", response);
                }
            }
        }
    }

    /**
     * @param xPathExpr expression to apply to response.
     * @param params values to put inside expression before evaluation
     * @return result of xpath expression.
     * @throws RuntimeException if no valid response was available or XPath could not be evaluated.
     */
    public String getXPath(String xPathExpr, Object... params) {
        validResponse();

        return getRawXPath(xPathExpr, params);
    }

    /**
     * Gets XPath value without checking whether response is valid.
     * @param xPathExpr expression to apply to response.
     * @param params values to put inside expression before evaluation
     * @return result of xpath expression.
     */
    public String getRawXPath(String xPathExpr, Object... params) {
        return getRawXPath(getResponse(), xPathExpr, params);
    }

    protected String getRawXPath(String soapResponse, String xPathExpr, Object... params) {
        String expr = String.format(xPathExpr, params);
        String xPathValue = getXPathHelper().getXPath(namespaceContext, soapResponse, expr);
        if ("".equals(xPathValue)) {
            xPathValue = null;
        }
        return xPathValue;
    }

    /**
     * @param xPathExpr expression to apply to response.
     * @param params values to put inside expression before evaluation
     * @return result of xpath expression.
     * @throws RuntimeException if no valid response was available or XPath could not be evaluated.
     */
    public Double getXPathDouble(String xPathExpr, Object... params) {
        Double result = null;
        String xPathValue = getXPath(xPathExpr, params);
        if (xPathValue != null) {
            result = Double.valueOf(xPathValue);
        }
        return result;
    }

    /**
     * @param xPathExpr expression to apply to response.
     * @param params values to put inside expression before evaluation
     * @return result of xpath expression.
     * @throws RuntimeException if no valid response was available or XPath could not be evaluated.
     */
    public Integer getXPathInt(String xPathExpr, Object... params) {
        Integer result = null;
        String xPathValue = getXPath(xPathExpr, params);
        if (xPathValue != null) {
            result = Integer.valueOf(xPathValue);
        }
        return result;
    }

    /**
     * @param xPathExpr expression to apply to response.
     * @param params values to put inside expression before evaluation
     * @return result of xpath expression.
     * @throws RuntimeException if no valid response was available or XPath could not be evaluated.
     */
    public Double getXPathAmount(String xPathExpr, Object... params) {
        Double result = getXPathDouble(xPathExpr, params);
        if (result != null) {
            result = Math.round(result * 100) / 100d;
        }
        return result;
    }

    /**
     * @param xPathExpr expression to apply to response.
     * @param params values to put inside expression before evaluation
     * @return result of xpath expression, without timezone part.
     * @throws RuntimeException if no valid response was available or XPath could not be evaluated.
     */
    public String getXPathDate(String xPathExpr, Object... params) {
        String result = getXPath(xPathExpr, params);
        if (result != null) {
            // take only date part (i.e. YYYY-MM-DD)
            result = result.substring(0, 10);
        }
        return result;
    }

    /**
     * Checks whether input values are present at correct locations in the response.
     * @param values keyName -> value, input parameters supplied to get request.
     * @param expressionsToCheck xpath -> keyName, each xpath in this map is expected
     *        to evaluate to the value present in values for that key (i.e. values[keyName])
     * @return OK if all xpath expressions evaluated to the correct value. A description
     *        of the mismatches otherwise.
     */
    public XPathCheckResult checkXPaths(Map<String, Object> values, Map<String, String> expressionsToCheck) {
        XPathCheckResult result;
        String content = getResponse();
        if (content == null) {
            result = new XPathCheckResult();
            result.setMismatchDetail("NOK: no response available.");
        } else {
            validResponse();
            result = checkRawXPaths(content, expressionsToCheck, values);
        }

        return result;
    }

    protected XPathCheckResult checkRawXPaths(String content, Map<String, String> expressionsToCheck, Map<String, Object> values) {
        XPathCheckResult result = new XPathCheckResult();
        for (Entry<String, String> exprEntry : expressionsToCheck.entrySet()) {
            String xpath = exprEntry.getKey();
            String keyName = exprEntry.getValue();
            Object value = null;
            String valueStr = null;
            if (keyName.contains(".") && !values.containsKey(keyName)) {
                value = getNestedValue(values, keyName);
            } else {
                value = values.get(keyName);
            }
            valueStr = String.valueOf(value);
            String xpathValue = String.valueOf(getRawXPath(content, xpath)).trim();
            if (!valueStr.equals(xpathValue) && !equalsDates(valueStr, xpathValue)
                    && !equalsAmounts(valueStr, xpathValue)
                    && !("".equals(valueStr) && "null".equals(xpathValue))) {
                result.addMisMatch(keyName, valueStr, xpathValue);
            }
        }

        return result;
    }

    private Object getNestedValue(Map<String, Object> values, String keyName) {
        String[] keyPath = keyName.split("\\.");
        String parentKey = keyPath[0];
        String valueKey = keyPath[1];
        @SuppressWarnings("unchecked")
        Map<String, ?> firstValue = (Map<String, ?>) values.get(parentKey);
        return firstValue.get(valueKey);
    }

    private boolean equalsDates(String aValue, String aXpathValue) {
        boolean result = false;
        if (aValue.length() == 10 && aXpathValue.startsWith(aValue)) {
            String timeZone = aXpathValue.substring(10);
            if (timeZone.matches("[+-]\\d\\d:\\d\\d")) {
                result = true;
            }
        }
        return result;
    }

    private boolean equalsAmounts(String aValue, String aXpathValue) {
        boolean result = false;
        try {
            Double value = Double.parseDouble(aValue);
            Double xpath = Double.parseDouble(aXpathValue);
            result = value.equals(xpath);
        } catch (Exception e) {
            // ignore
        }
        return result;
    }

    /**
     * Gets the value xsi:type attribute will have for provided type.
     * @param type sub type wanted.
     * @return type name including namespace prefix as present in response.
     */
    public String getXsiTypeValue(String type) {
        return type;
    }

    /**
     * @param xPathExpr expression to apply to response.
     * @param params values to put inside expression before evaluation
     * @return all results of xpath expression.
     * @throws RuntimeException if no valid response was available or XPath could not be evaluated.
     */
    public List<String> getAllXPath(String xPathExpr, Object... params) {
        validResponse();
        return getRawAllXPath(response, xPathExpr, params);
    }

    protected List<String> getRawAllXPath(String soapResponse, String xPathExpr, Object... params) {
        String expr = String.format(xPathExpr, params);
        return getXPathHelper().getAllXPath(namespaceContext, soapResponse, expr);
    }

    /**
     * @param aNamespaceContext the namespaceContext to set
     */
    public void setNamespaceContext(NamespaceContext aNamespaceContext) {
        namespaceContext = aNamespaceContext;
    }

    public void setXPathHelper(XPathHelper xPathHelper) {
        this.xPathHelper = xPathHelper;
    }

    public XPathHelper getXPathHelper() {
        return xPathHelper;
    }
}
