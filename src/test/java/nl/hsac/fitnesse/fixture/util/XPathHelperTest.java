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
    private XPathHelper xPathHelper = new XPathHelper();

    @Test
    public void testXPathWithNamespace() {
        LalPolicyXPaths.registerNamespace();
        String responseString = FileUtil.loadFile("leanapps/getPolicyCheckResponse.xml");
        assertEquals("OK", xPathHelper.getXPath(NS_CONTEXT, responseString, "//lal:status/lal:status"));
    }

    @Test
    public void testBadXPath() {
        String responseString = FileUtil.loadFile("leanapps/getPolicyCheckResponse.xml");
        assertEquals("", xPathHelper.getXPath(null, responseString, "//status"));
        try {
            xPathHelper.getXPath(null, responseString, "\\status");
            fail("expected exception");
        } catch (FitFailureException e) {
            String message = e.getMessage();
            assertTrue("Bad message start: " + message, message.startsWith("Unable to compile xpath: \\status\n"));
            assertTrue("Bad message end: " + message, message.endsWith("A location path was expected, but the following token was encountered:  \\"));
        }
    }

    @Test
    public void testBadXml() {
        try {
            xPathHelper.getXPath(null, "bla", "//status");
            fail("expected exception");
        } catch (FitFailureException e) {
            assertEquals("Cannot perform XPATH on non-xml: bla", e.getMessage());
        }
    }

    @Test
    public void testBadXml2() {
        try {
            xPathHelper.getXPath(null, "<bla", "//status");
            fail("expected exception");
        } catch (FitFailureException e) {
            String message = e.getMessage();
            assertTrue("Bad message start: " + message, message.startsWith("Unable to evaluate xpath: //status\n"));
            assertTrue("Bad message end: " + message, message.endsWith("XML document structures must start and end within the same entity."));
        }
    }

    @Test
    public void testAllXmlNoText() {
        LalPolicyXPaths.registerNamespace();
        String responseString = FileUtil.loadFile("leanapps/getPolicyCheckResponse.xml");
        List<String> all = xPathHelper.getAllXPath(NS_CONTEXT, responseString, "//*/@xsi:type");

        assertEquals(13, all.size());
        assertEquals("ns:PostalAddress", all.get(1));
    }

    @Test
    public void testAllXmlWithText() {
        LalPolicyXPaths.registerNamespace();
        String responseString = FileUtil.loadFile("leanapps/getPolicyCheckResponse.xml");
        List<String> all = xPathHelper.getAllXPath(NS_CONTEXT, responseString, "//lal:key/text()");

        assertEquals(17, all.size());
        assertEquals("20000541", all.get(0));
    }
}
