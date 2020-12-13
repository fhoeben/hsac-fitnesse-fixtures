package nl.hsac.fitnesse.fixture.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class JsonFormatterTest {
    private JsonFormatter formatter = new JsonFormatter();

    @Test
    public void testFormatNull() {
        assertNull(formatter.format(null));
    }

    @Test
    public void testFormatSimple() {
        assertEquals("{\n" +
                        "    \"price\": 8.95,\n" +
                        "    \"category\": \"reference\"\n" +
                        "}",
                formatter.format("{\"price\": 8.95,\"category\": \"reference\"}"));
    }

    @Test
    public void testFormatArray() {
        assertEquals("[\n" +
                        "    {\n" +
                        "        \"price\": 8.95,\n" +
                        "        \"category\": \"reference\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "        \"a\": 1\n" +
                        "    }\n" +
                        "]",
                formatter.format("[{\"price\": 8.95,\"category\": \"reference\"}, {\"a\":1}]"));
    }
}
