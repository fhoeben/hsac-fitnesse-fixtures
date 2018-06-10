package nl.hsac.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.util.XPathHelper;
import org.apache.commons.lang3.StringUtils;

import javax.xml.namespace.NamespaceContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Fixture to work with XML (strings).
 */
public class XmlFixture extends SlimFixture {
    private NamespaceContext namespaceContext = getEnvironment().getNamespaceContext();
    private XPathHelper xPathHelper = getEnvironment().getXPathHelper();
    private String content;

    /**
     * @param filename XML file to be loaded.
     * @return true
     */
    public boolean loadFile(String filename) {
        content = getFileFixture().textIn(filename);
        return true;
    }

    /**
     * @param xml XML content to be loaded.
     * @return true
     */
    public boolean load(String xml) {
        content = cleanupValue(xml);
        return true;
    }

    /**
     * @return formatted loaded XML content.
     */
    public String document() {
        String formatted = content;
        if (content != null && content.trim().startsWith("<")) {
            formatted = getEnvironment().getHtmlForXml(content);
        }
        return formatted;
    }

    /**
     * Register a prefix to use in XPath expressions.
     * @param prefix prefix to be used in xPath expressions.
     * @param namespace XML namespace the prefix should point to.
     */
    public void registerPrefixForNamespace(String prefix, String namespace) {
        getEnvironment().registerNamespace(prefix, getUrl(namespace));
    }

    /**
     * @param xPathExpr expression to evaluate.
     * @return result of expression evaluation against loaded content.
     * @throws RuntimeException if XPath could not be evaluated.
     */
    public String xPath(String xPathExpr) {
        String xPathValue = xPathHelper.getXPath(namespaceContext, content, xPathExpr);
        if ("".equals(xPathValue)) {
            xPathValue = null;
        }
        return xPathValue;
    }

    /**
     * @param xPathExpr expression to evaluate.
     * @return results of expression evaluation against loaded content.
     * @throws RuntimeException if XPath could not be evaluated.
     */
    public ArrayList<String> listXPathMatches(String xPathExpr) {
        List<String> results = xPathHelper.getAllXPath(namespaceContext, content, xPathExpr);
        return results instanceof ArrayList? (ArrayList<String>) results : new ArrayList<>(results);
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

    protected String getContent() {
        return content;
    }

    protected String createFileFromBase64(String baseName, String base64Content) {
        Base64Fixture base64Fixture = getBase64Fixture();
        return base64Fixture.createFrom(baseName, base64Content);
    }

    protected Base64Fixture getBase64Fixture() {
        return new Base64Fixture();
    }

    protected FileFixture getFileFixture() {
        return new FileFixture();
    }
}
