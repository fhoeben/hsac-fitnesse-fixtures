package nl.hsac.fitnesse.fixture.util;

import nl.hsac.fitnesse.fixture.slim.SlimFixtureException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ThrowingFunctionTest {
    @Test
    public void testNoThrow() {
        Object result = callFunction(x -> callThatMightThrow(x), true);
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testThrowChecked() {
        try {
            Object result = callFunction(x -> callThatMightThrow(x), null);
            fail("expected exception, got: " + result);
        } catch (SlimFixtureException e) {
            assertEquals(IOException.class, e.getCause().getClass());
        }
    }

    @Test
    public void testThrowRuntime() {
        try {
            Object result = callFunction(x -> callThatMightThrow(x), false);
            fail("expected exception, got: " + result);
        } catch (IllegalArgumentException e) {
            assertEquals("false", e.getMessage());
        }
    }

    private static Object callFunction(ThrowingFunction<Boolean, Object, IOException> f, Boolean input) {
        return f.applyWrapped(input, SlimFixtureException::new);
    }

    private static Object callThatMightThrow(Boolean val) throws IOException {
        if (val == null) {
            throw new IOException("Oops");
        } else if (!val) {
            throw new IllegalArgumentException("false");
        } else {
            return val;
        }
    }
}