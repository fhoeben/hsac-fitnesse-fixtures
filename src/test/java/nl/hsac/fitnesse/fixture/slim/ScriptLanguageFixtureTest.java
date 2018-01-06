package nl.hsac.fitnesse.fixture.slim;

import org.junit.Before;
import org.junit.Test;

import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class ScriptLanguageFixtureTest {
    private ScriptLanguageFixture fixture;

    @Before
    public void setUp() {
        fixture = new ScriptLanguageFixture();
    }

    @Test
    public void jsExpression() {
        Object result = fixture.evaluate("40+2");

        assertEquals(42, result);
    }

    @Test
    public void cleanUpExpression() {
        Object result = fixture.evaluate("<pre>'a' + 'b'</pre>");

        assertEquals("ab", result);
    }

    @Test
    public void useCurrentValues() {
        fixture.set("a", 2);
        fixture.set("b", 40);
        Object result = fixture.evaluate("a+b");

        assertEquals(42.0, result);
    }

    @Test
    public void invokeFunction() {
        fixture.set("a", 21);
        Object result = fixture.evaluate("function hello(name) { return 'Hello, ' + name + a; }");
        assertNotEquals(Boolean.FALSE, result);
        result = fixture.invokeFunction("hello", "Scripting!!");
        assertEquals("Hello, Scripting!!21", result);
    }

    @Test
    public void invokeBadFunction() {
        checkSlimFixtureExceptionThrown(
                () -> fixture.invokeFunction("unknown", "1"),
                "No function found for this name and these arguments");
    }

    @Test
    public void invokeMethod() {
        fixture.set("c", 12);
        Object result = fixture.evaluate("var obj = new Object(); obj.hello = function hello(name) { return 'Hello, ' + name + c; }");
        assertNotEquals(Boolean.FALSE, result);
        result = fixture.invokeMethod("obj","hello", "Scripting!!");
        assertEquals("Hello, Scripting!!12", result);
    }

    @Test
    public void invokeMethodNoObject() {
        checkSlimFixtureExceptionThrown(
                () -> fixture.invokeMethod("b","unknown1", false),
                "No object found called: b");
    }

    @Test
    public void invokeBadMethod() {
        fixture.evaluate("var a = new Object()");
        checkSlimFixtureExceptionThrown(
                () -> fixture.invokeMethod("a","unknown2", 1),
                "No method found for this name and these arguments");
    }

    @Test
    public void getVariable() {
        fixture.evaluate("var d = new Object()");
        assertNotNull(fixture.value("d"));

        fixture.evaluate("var e = 1");
        assertEquals(1, fixture.value("e"));
    }

    @Test
    public void getUnknownVariable() {
        assertNull(fixture.value("x"));
    }

    private static void checkSlimFixtureExceptionThrown(Supplier<Object> supplier, String expectedMsg) {
        try {
            Object result = supplier.get();
            fail("Expected exception, got: " + result);
        } catch (SlimFixtureException e) {
            assertEquals("message:<<" + expectedMsg + ">>", e.getMessage());
        }
    }
}