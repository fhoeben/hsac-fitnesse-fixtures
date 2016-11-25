package nl.hsac.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.Environment;
import nl.hsac.fitnesse.fixture.util.ReflectionHelper;
import nl.hsac.fitnesse.fixture.util.XMLFormatter;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests XmlHttpTest.
 */
public class XmlHttpTestTest {
    private XmlHttpTest xmlHttpTest = new XmlHttpTest();

    @Test
    public void testFormatValueXml() {
        String expected = "<pre>&lt;hello/&gt;\n</pre>";

        assertEquals(expected, xmlHttpTest.safeFormatValue("<hello/>").replace("\r", ""));
        assertEquals(expected, xmlHttpTest.safeFormatValue(" <hello/>").replace("\r", ""));
    }

    @Test
    public void testFormatValueNoXml() {
        // IBM JVM has transformer that converts non-xml string into empty string
        // we ensure this does not prevent us from showing request URLs
        ReflectionHelper helper = new ReflectionHelper();
        Object originalFormatter = helper.getField(Environment.getInstance(), "xmlFormatter");
        assertNotNull(originalFormatter);

        String expected = "http://localhost/hello?abc=xyz&abf=76";
        try {
            helper.setField(Environment.getInstance(), "xmlFormatter", new XMLFormatter() {
                @Override
                public String format(String value) {
                    return "";
                }
            });

            assertEquals(expected, xmlHttpTest.safeFormatValue(expected));

        } finally {
            helper.setField(Environment.getInstance(), "xmlFormatter", originalFormatter);
        }
    }
}
