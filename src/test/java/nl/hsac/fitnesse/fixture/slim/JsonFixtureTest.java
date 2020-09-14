package nl.hsac.fitnesse.fixture.slim;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class JsonFixtureTest {
    private JsonFixture fixture = new JsonFixture();

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
    public void testElementOfJsonPathNoResult() {
        fixture.load(JSON);
        Object result = fixture.elementOfJsonPath(0, "$.store.book[?(@.category==\"test\")].author");
        assertNull(result);

        result = fixture.elementOfJsonPath(3, "$.store.book[?(@.category==\"fiction\")].author");
        assertNull(result);
    }

    @Test
    public void testElementOfJsonPathResults() {
        fixture.load(JSON);
        Object result = fixture.elementOfJsonPath(0, "$.store.book[?(@.category==\"fiction\")].author");
        assertEquals("Evelyn Waugh", result);
    }
}
