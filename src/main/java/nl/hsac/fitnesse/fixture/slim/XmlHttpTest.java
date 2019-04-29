package nl.hsac.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.util.XMLValidator;
import nl.hsac.fitnesse.fixture.util.XmlHttpResponse;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Fixture to make HTTP requests returning XNL using Slim scripts and/or scenarios.
 * Example use: make SOAP calls using #postTo().
 */
public class XmlHttpTest extends HttpTest {
    /**
     * Creates new.
     */
    public XmlHttpTest() {
        setContentType(XmlHttpResponse.CONTENT_TYPE_XML_TEXT_UTF8);
    }

    /**
     * Register a prefix to use in XPath expressions.
     * @param prefix prefix to be used in xPath expressions.
     * @param namespace XML namespace the prefix should point to.
     */
    public void registerPrefixForNamespace(String prefix, String namespace) {
        getEnvironment().registerNamespace(prefix, getUrl(namespace));
    }

    @Override
    protected String formatValue(String value) {
        String formatted = super.formatValue(value);
        if (value != null && value.trim().startsWith("<")) {
            formatted = getEnvironment().getHtmlForXml(value);
        }
        return formatted;
    }

    /**
     * Gets a HTML list with all matches to the supplied XPath.
     * @param xPathExpr expression to evaluate.
     * @return list containing all results of expression evaluation against last response received, null if there were no matches.
     * @throws RuntimeException if no valid response was available or XPath could not be evaluated.
     */
    public String allXPathMatches(String xPathExpr) {
        String result = null;
        List<String> allXPath = listXPathMatches(xPathExpr);
        if (allXPath != null && !allXPath.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("<div><ul>");
            for (String match : allXPath) {
                sb.append("<li>");
                sb.append(match);
                sb.append("</li>");
            }
            sb.append("</ul></div>");
            result = sb.toString();
        }
        return result;
    }

    public ArrayList<String> listXPathMatches(String xPathExpr) {
        List<String> results = getResponse().getAllXPath(xPathExpr);
        return results instanceof ArrayList? (ArrayList<String>) results : new ArrayList<>(results);
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

    /**
     * @param baseName base of filename to generate (a number might be added to the name to make it unique).
     * @param xPath expression to evaluate.
     * @return link to created file.
     */
    public String createFileFromBase64ContentOf(String baseName, String xPath) {
        String base64Content = xPath(xPath);
        if (StringUtils.isEmpty(base64Content)) {
            throw new SlimFixtureException(false, "No content from xPath: " + xPath);
        } else {
            return createFileFromBase64(baseName, base64Content);
        }
    }

    /**
     * Validate the loaded xml against a schema in file xsdFileName
     * @param xsdFileName filename of the xsd to use
     * @return true if the response xml validates against the schema. Throws a descriptive exception otherwise
     */
    public boolean validateResponseAgainstXsdFile(String xsdFileName) {
        String xsdContent = new FileFixture().textIn(xsdFileName);
        return new XMLValidator().validateAgainst(getResponse().getResponse(), xsdContent);
    }

    /**
     * Validate the loaded xml against a schema provided from the wiki page
     * @param xsdSchema xsd schema to use
     * @return true if the response xml validates against the schema. Throws a descriptive exception otherwise
     */
    public boolean validateResponseAgainstXsd(String xsdSchema) {
        String xsdContent = cleanupValue(xsdSchema);
        return new XMLValidator().validateAgainst(getResponse().getResponse(), xsdContent);
    }

    @Override
    protected boolean postProcessResponse() {
        // always called after post or get, so easy place to ensure namespaces are registered
        getEnvironment().setContext(getResponse());
        boolean result = super.postProcessResponse();
        return result;
    }

    public boolean repeatUntilXPathIs(final String xPath, final String expectedValue) {
        RepeatCompletion completion;
        if (expectedValue == null) {
            completion = new RepeatLastCall() {
                @Override
                public boolean isFinished() {
                    return xPath(xPath) == null;
                }
            };
        } else {
            completion = new RepeatLastCall() {
                @Override
                public boolean isFinished() {
                    Object actual = xPath(xPath);
                    return compareActualToExpected(expectedValue, actual);
                }
            };
        }
        return repeatUntil(completion);
    }

    @Override
    public XmlHttpResponse getResponse() {
        return (XmlHttpResponse) super.getResponse();
    }

    @Override
    protected XmlHttpResponse createResponse() {
        return new XmlHttpResponse();
    }
}
