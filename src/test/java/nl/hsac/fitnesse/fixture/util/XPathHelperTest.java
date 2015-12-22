package nl.hsac.fitnesse.fixture.util;

import fit.exception.FitFailureException;
import nl.hsac.fitnesse.fixture.Environment;
import nl.hsac.fitnesse.fixture.leanapps.LalPolicyXPaths;
import org.junit.Test;

import javax.xml.namespace.NamespaceContext;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
            assertTrue("Bad message start: " + e.getMessage(), e.getMessage().startsWith("Unable to evaluate xpath: \\status\n"));
            assertTrue("Bad message end: " + e.getMessage(), e.getMessage().endsWith("A location path was expected, but the following token was encountered:  \\"));
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
        List<String> all = XPathHelper.getAllXPath(NS_CONTEXT, responseString, "//*/@xsi:type");

        assertEquals(13, all.size());
        assertEquals("ns:PostalAddress", all.get(1));
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
