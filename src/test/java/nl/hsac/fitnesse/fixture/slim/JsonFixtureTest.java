package nl.hsac.fitnesse.fixture.slim;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class JsonFixtureTest {
    private JsonFixture fixture = new JsonFixture();
    private final String jsonPathNoResults = "$.store.book[?(@.category==\"test\")].author";
    private final String jsonPathWithResults = "$.store.book[?(@.category==\"fiction\")].author";
    private final String expectedResult = "Evelyn Waugh";


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
        JsonFixture cont = new JsonFixture();
        cont.load(JSON);
        Object result = cont.elementOfJsonPath(0, jsonPathNoResults);
        assertNull(result);
    }

    @Test
    public void testElementOfJsonPathResults(){
        JsonFixture cont = new JsonFixture();
        cont.load(JSON);
        Object result = cont.elementOfJsonPath(0, "$.store.book[?(@.category==\"fiction\")].author");
        assertEquals(expectedResult, "Evelyn Waugh");
    }
}
