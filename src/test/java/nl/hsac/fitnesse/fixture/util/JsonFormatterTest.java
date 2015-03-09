package nl.hsac.fitnesse.fixture.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class JsonFormatterTest {
    private JsonFormatter formatter = new JsonFormatter();

    @Test
    public void testFormatNull() {
        assertNull(formatter.format(null));
    }

    @Test
    public void testFormatSimple() {
        assertEquals(
                "{\n" +
                "    \"category\": \"reference\",\n" +
                "    \"price\": 8.95\n" +
                "}",
                formatter.format("{\"category\": \"reference\",\"price\": 8.95}"));
    }
}
