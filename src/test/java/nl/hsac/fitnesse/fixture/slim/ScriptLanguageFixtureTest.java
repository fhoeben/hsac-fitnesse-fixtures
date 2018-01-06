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
    public void jsExpressionWithError() {
        checkSlimFixtureExceptionThrown(
                () -> fixture.evaluate("doesnotexist.a"),
                "<eval>:1 ReferenceError: \"doesnotexist\" is not defined");
    }

    @Test
    public void multiLineExpressionWithError() {
        checkSlimFixtureExceptionThrown(
                () -> fixture.evaluate("<pre>var b = new Object();\nvar c = a+b; doesnotexist.a</pre>"),
                "<eval>:2 ReferenceError: \"a\" is not defined");
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
    public void invokeFunctionWithError() {
        fixture.evaluate("function hello(name) { return 'Hello, ' + a.n; }");
        checkSlimFixtureExceptionThrown(
                () -> fixture.invokeFunction("hello","Boo"),
                "<eval>:1 ReferenceError: \"a\" is not defined");
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
    public void invokeMethodWithError() {
        fixture.evaluate("var obj = new Object(); obj.hello = function hello(name) { return 'Hello, ' + c.name; }");
        checkSlimFixtureExceptionThrown(
                () -> fixture.invokeMethod("obj","hello","Boo"),
                "<eval>:1 ReferenceError: \"c\" is not defined");
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
    public void getNestedVariable() {
        fixture.evaluate("var d = new Object(); d.name = 'my name'; d.i = [1,2]; var c = new Object(); c.a = 'a'; d.c = c; e = Java.asJSONCompatible(d);");
        assertEquals("my name", fixture.value("d.name"));
        assertEquals("a", fixture.value("d.c.a"));

        assertEquals(1, fixture.value("d.i[0]"));
        assertEquals(2, fixture.value("d.i[1]"));

        assertEquals("my name", fixture.value("e.name"));
        assertEquals("a", fixture.value("e.c.a"));
        assertEquals(1, fixture.value("e.i[0]"));
        assertEquals(2, fixture.value("e.i[1]"));
    }

    @Test
    public void getUnknownVariable() {
        assertNull(fixture.value("x"));
    }

    @Test
    public void getNestedVariableViaGet() {
        fixture.evaluate("var d = new Object(); d.name = 'my name'; d.i = [1,2]; var c = new Object(); c.a = 'a'; d.c = c;");
        assertEquals("my name", fixture.get("d.name"));
        assertEquals("a", fixture.get("d.c.a"));
        assertEquals(1, fixture.get("d.i[0]"));
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