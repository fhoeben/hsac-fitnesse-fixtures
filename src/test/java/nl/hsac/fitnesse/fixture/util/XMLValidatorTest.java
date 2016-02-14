package nl.hsac.fitnesse.fixture.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests XMLValidator.isWellFormed.
 */
public class XMLValidatorTest {
    private final XMLValidator validator = new XMLValidator();

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
}
