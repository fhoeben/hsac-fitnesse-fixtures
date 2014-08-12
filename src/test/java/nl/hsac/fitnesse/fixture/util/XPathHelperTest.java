package nl.hsac.fitnesse.fixture.util;

import fit.exception.FitFailureException;
import nl.hsac.fitnesse.fixture.Environment;
import nl.hsac.fitnesse.fixture.leanapps.LalPolicyXPaths;
import org.junit.Test;

import javax.xml.namespace.NamespaceContext;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests XPathHelper.
 */
public class XPathHelperTest {
    private static final NamespaceContext NS_CONTEXT = Environment.getInstance().getNamespaceContext();

    @Test
    public void testXPathWithNamespace() {
        LalPolicyXPaths.registerNamespace();
        String responseString = FileUtil.loadFile("leanapps/getPolicyCheckResponse.xml");
        assertEquals("OK", XPathHelper.getXPath(NS_CONTEXT, responseString, "//lal:status/lal:status"));
    }

    @Test
    public void testBadXPath() {
        String responseString = FileUtil.loadFile("leanapps/getPolicyCheckResponse.xml");
        assertEquals("", XPathHelper.getXPath(null, responseString, "//status"));
        try {
            XPathHelper.getXPath(null, responseString, "\\status");
            fail("expected exception");
        } catch (FitFailureException e) {
            assertEquals("Unable to evaluate xpath: \\status", e.getMessage());
        }
    }

    @Test
    public void testBadXml() {
        try {
            XPathHelper.getXPath(null, "bla", "\\status");
            fail("expected exception");
        } catch (FitFailureException e) {
            assertEquals("Cannot perform XPATH on non-xml: bla", e.getMessage());
        }
    }

    @Test
    public void testAllXmlNoText() {
        LalPolicyXPaths.registerNamespace();
        String responseString = FileUtil.loadFile("leanapps/getPolicyCheckResponse.xml");
        List<String> all = XPathHelper.getAllXPath(NS_CONTEXT, responseString, "//lal:oid");

        assertEquals(28, all.size());
        assertEquals("11600596", all.get(1));
    }

    @Test
    public void testAllXmlWithText() {
        LalPolicyXPaths.registerNamespace();
        String responseString = FileUtil.loadFile("leanapps/getPolicyCheckResponse.xml");
        List<String> all = XPathHelper.getAllXPath(NS_CONTEXT, responseString, "//lal:key/text()");

        assertEquals(17, all.size());
        assertEquals("20000541", all.get(0));
    }
}
