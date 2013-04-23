package nl.hsac.fitnesse.fixture.util;

import nl.hsac.fitnesse.fixture.Environment;

import javax.xml.namespace.NamespaceContext;
import java.util.Map;
import java.util.Map.Entry;


/**
 * Wrapper around XML HTTP response (and request).
 */
public class XmlHttpResponse extends HttpResponse {
    private NamespaceContext namespaceContext;

    @Override
    public void validResponse() {
        super.validResponse();

        if (response != null && response.contains("<faultcode>soapenv:Server</faultcode>")) {
            Environment.handleErrorResponse("SOAP fault received: ", response);
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

        return getRawXPath(response, xPathExpr, params);
    }

    protected String getRawXPath(String soapResponse, String xPathExpr, Object... params) {
        String expr = String.format(xPathExpr, params);
        String xPathValue = XPathHelper.getXPath(namespaceContext, soapResponse, expr);
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
        XPathCheckResult result = new XPathCheckResult();
        if (response == null) {
            result.setMismatchDetail("NOK: no response available.");
            return result;
        }
        validResponse();

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
            String xpathValue = String.valueOf(getRawXPath(response, xpath)).trim();
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
     * @param aNamespaceContext the namespaceContext to set
     */
    public void setNamespaceContext(NamespaceContext aNamespaceContext) {
        namespaceContext = aNamespaceContext;
    }
}
