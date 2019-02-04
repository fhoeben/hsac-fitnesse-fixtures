package nl.hsac.fitnesse.fixture.util;

import nl.hsac.fitnesse.fixture.slim.SlimFixtureException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests XMLValidator.isWellFormed.
 */
public class XMLValidatorTest {
    private final XMLValidator validator = new XMLValidator();
    private final String curDir = Paths.get("").toAbsolutePath().toString();
    private final String testResourcesDir = curDir + "/src/test/resources/".replace('/', File.separatorChar);

    /**
     * All is well.
     */
    @Test
    public void testOk() {
        String xml = "<root><child/><child2>a</child2></root>";
        assertTrue(validator.checkWelformed(xml));
        validator.isWelformed(xml);
    }

    /**
     * Child element not closed.
     */
    @Test
    public void testChildNotClosed() {
        String xml = "<root><child/><child2>a</root>";
        String msg = "child2";
        checkNotOk(xml, msg);
    }

    /**
     * Root not closed.
     */
    @Test
    public void testRootNotClosed() {
        String xml = "<root><child/><child2>a</child2></root";
        int endPos = xml.length() + 1;
        checkNotOk(xml, String.valueOf(endPos));
    }

    private void checkNotOk(String xml, String msgPart) {
        assertFalse(validator.checkWelformed(xml));
        try {
            validator.isWelformed(xml);
            fail("expected exception");
        } catch (RuntimeException e) {
            String message = e.getMessage();
            assertTrue(message, message.contains(msgPart));
        }
    }

    @Test
    public void validXmlValidationReturnsTrue() {
        String xml = FileUtil.loadFile("valid_shiporder.xml");
        String schemaFile = testResourcesDir + "shiporder_schema.xsd";
        assertTrue(validator.validateAgainst(xml, schemaFile));
    }

    @Rule
    public final ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void invalidXmlValidationReturnsDescriptiveException() {
        expectedEx.expect(SlimFixtureException.class);
        expectedEx.expectMessage("Attribute 'orderid' must appear on element 'shiporder'");

        String xml = FileUtil.loadFile("invalid_shiporder.xml");
        String schema = testResourcesDir + "shiporder_schema.xsd";
        validator.validateAgainst(xml, schema);
    }
}
