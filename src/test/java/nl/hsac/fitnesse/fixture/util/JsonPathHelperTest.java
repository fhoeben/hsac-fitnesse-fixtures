package nl.hsac.fitnesse.fixture.util;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class JsonPathHelperTest {
    private JsonPathHelper helper = new JsonPathHelper();

    private static String JSON =
            "{\n" +
            "    \"store\": {\n" +
            "        \"book\": [\n" +
            "            {\n" +
            "                \"category\": \"reference\",\n" +
            "                \"author\": \"Nigel Rees\",\n" +
            "                \"title\": \"Sayings of the Century\",\n" +
            "                \"price\": 8.95\n" +
            "            },\n" +
            "            {\n" +
            "                \"category\": \"fiction\",\n" +
            "                \"author\": \"Evelyn Waugh\",\n" +
            "                \"title\": \"Sword of Honour\",\n" +
            "                \"price\": 12.99\n" +
            "            },\n" +
            "            {\n" +
            "                \"category\": \"fiction\",\n" +
            "                \"author\": \"Herman Melville\",\n" +
            "                \"title\": \"Moby Dick\",\n" +
            "                \"isbn\": \"0-553-21311-3\",\n" +
            "                \"price\": 8.99\n" +
            "            },\n" +
            "            {\n" +
            "                \"category\": \"fiction\",\n" +
            "                \"author\": \"J. R. R. Tolkien\",\n" +
            "                \"title\": \"The Lord of the Rings\",\n" +
            "                \"isbn\": \"0-395-19395-8\",\n" +
            "                \"price\": 22.99\n" +
            "            }\n" +
            "        ],\n" +
            "        \"bicycle\": {\n" +
            "            \"color\": \"red\",\n" +
            "            \"price\": 19.95\n" +
            "        }\n" +
            "    },\n" +
            "    \"expensive\": 10\n" +
            "}\n";

    @Test
    public void testJsonPathNoResult() {
        Object result = helper.getJsonPath(JSON, "$.store.book[2].author.name");
        assertEquals(null, result);

        List<Object> results = helper.getAllJsonPath(JSON, "$..book[2].author.name");
        assertEquals(Collections.emptyList(), results);
    }

    @Test
    public void testJsonPath() {
        Object result = helper.getJsonPath(JSON, "$.store.book[2].author");
        assertEquals("Herman Melville", result);
    }

    @Test
    public void testJsonPathException() {
        try {
            Object result = helper.getJsonPath(JSON, "$..book[2].author");
            fail("Expected exception, got: " + result);
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("$..book[2].author"));
        }
    }

    @Test
    public void testJsonPathMultiple() {
        List<Object> result = helper.getAllJsonPath(JSON, "$..book[2].author");
        assertEquals(1, result.size());
        assertEquals("Herman Melville", result.get(0));
    }

    @Test
    public void testJsonPathMultipleOne() {
        List<Object> result = helper.getAllJsonPath(JSON, "$.store.book[2].author");
        assertEquals(1, result.size());
        assertEquals("Herman Melville", result.get(0));
    }

    @Test
    public void testJsonPathMultipleNone() {
        List<Object> results = helper.getAllJsonPath(JSON, "$.store.book[2].author.name");
        assertEquals(Collections.emptyList(), results);
    }
}
