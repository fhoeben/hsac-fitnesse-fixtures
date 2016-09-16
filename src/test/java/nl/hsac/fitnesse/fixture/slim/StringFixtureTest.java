package nl.hsac.fitnesse.fixture.slim;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests StringFixture.
 */
public class StringFixtureTest {
    private final StringFixture fixture = new StringFixture();

    @Test
    public void testLength() {
        assertEquals("null", 0, fixture.lengthOf(null));
        assertEquals("hello", 5, fixture.lengthOf("hello"));
    }

    @Test
    public void testValue() {
        assertEquals("null", null, fixture.valueOf(null));
        assertEquals("hello", "hello", fixture.valueOf("hello"));
    }

    @Test
    public void testValueDiffersFrom() {
        assertFalse("null - null", fixture.valueDiffersFrom(null, null));
        assertFalse("hello - hello", fixture.valueDiffersFrom("hello", "hello"));
        assertTrue("null - hello", fixture.valueDiffersFrom(null, "hello"));
        assertTrue("hello - null", fixture.valueDiffersFrom("hello", null));
        assertTrue("hello - hela", fixture.valueDiffersFrom("hello", "hela"));
    }

    @Test
    public void testValueEquals() {
        assertTrue("null - null", fixture.valueEquals(null, null));
        assertTrue("hello - hello", fixture.valueEquals("hello", "hello"));
        assertFalse("null - hello", fixture.valueEquals(null, "hello"));
        assertFalse("hello - null", fixture.valueEquals("hello", null));
        assertFalse("hello - hela", fixture.valueEquals("hello", "hela"));
    }

    @Test
    public void testTextContains() {
        assertFalse("null value", fixture.textContains(null, "Hello"));
        assertFalse("bad value", fixture.textContains("World", "Hello"));
        assertTrue("good value", fixture.textContains("I said 'Hello' to the world", "Hello"));
    }

    @Test
    public void testConvertToInt() {
        assertNull("null", fixture.convertToInt(null));
        assertEquals("10", Integer.valueOf(10), fixture.convertToInt("10"));
    }

    @Test
    public void testConvertToDouble() {
        assertNull("null", fixture.convertToDouble(null));
        assertEquals("10.23", Double.valueOf(10.23), fixture.convertToDouble("10.23"));
    }

    @Test
    public void testNormalizeWhitespace() {
        assertNull("null", fixture.normalizeWhitespace(null));
        assertEquals("abc", "abc", fixture.normalizeWhitespace("abc"));
        assertEquals(" 10.23  ", "10.23", fixture.normalizeWhitespace(" 10.23  "));
        assertEquals(" 1 ==  2  ", "1 == 2", fixture.normalizeWhitespace(" 1 ==  2  "));
        assertEquals("\t1\t\t ==  2", "1 == 2", fixture.normalizeWhitespace("\t1\t\t ==  2 "));
        assertEquals(" 1 ==\n\t2\n\n", "1 == 2", fixture.normalizeWhitespace(" 1 ==\n\t2\n\n"));
    }

    @Test
    public void testReplaceAllWithRegExIn() {
        assertNull("null", fixture.replaceAllInWith("b", null, "a"));
        assertEquals("$1", "23", fixture.replaceAllInWith("\\((\\d+)\\)", "(23)", "$1"));
        assertEquals("$1$0", "23(23)", fixture.replaceAllInWith("\\((\\d+)\\)", "(23)", "$1$0"));
        assertEquals("dot matches newline", "23\na", fixture.replaceAllInWith("\\((.*?)\\)", "(23\na)", "$1"));
        assertEquals("null replace value is empty string", "hallo", fixture.replaceAllInWith(" user", "hallo user", null));
    }

    @Test
    public void testExtractIntFromUsingGroup() {
        assertNull("null", fixture.extractIntFromUsingGroup(null, "(\\d+)", 1));
        assertEquals("A023", Integer.valueOf(23), fixture.extractIntFromUsingGroup("A023", "A(\\d+)", 1));
        assertEquals("12A023", Integer.valueOf(23), fixture.extractIntFromUsingGroup("12A023", "(\\d+)[A-Z](\\d+)", 2));
    }

    @Test
    public void testConvertToUpperCase() {
        assertNull("null", fixture.convertToUpperCase(null));
        assertEquals("abC1", "ABC1", fixture.convertToUpperCase("abC1"));
    }

    @Test
    public void testConvertToLowerCase() {
        assertNull("null", fixture.convertToLowerCase(null));
        assertEquals("abC1", "abc1", fixture.convertToLowerCase("abC1"));
    }
}
