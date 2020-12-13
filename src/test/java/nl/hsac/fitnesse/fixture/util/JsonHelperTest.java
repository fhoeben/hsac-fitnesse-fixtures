package nl.hsac.fitnesse.fixture.util;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class JsonHelperTest {
    private JsonHelper helper = new JsonHelper();
    private JsonFormatter formatter = new JsonFormatter();

    @Test
    public void testNullToMap() {
        assertNull(helper.jsonStringToMap(null));
    }

    @Test
    public void testSimpleMap() {
        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put("category", "reference");
        expected.put("price", 8.95);
        assertEquals(expected,
                helper.jsonStringToMap("{\"category\": \"reference\",\"price\": 8.95}"));
    }

    @Test
    public void testSortingOnString() {
        String h = helper.sort("{\n" +
                "  \"extraKey\": 2,\n" +
                "  \"parameters\": [\n" +
                "    {\n" +
                "      \"category\": \"reference\",\n" +
                "      \"price\": 8.95\n" +
                "    },\n" +
                "    {\n" +
                "      \"category\": \"areference\",\n" +
                "      \"price\": 18.95\n" +
                "    }\n" +
                "  ]\n" +
                "}", "$.parameters", "$.category");

        JSONAssert.assertEquals("{\n" +
                        "    \"extraKey\": 2,\n" +
                        "    \"parameters\": [\n" +
                        "        {\n" +
                        "            \"price\": 18.95,\n" +
                        "            \"category\": \"areference\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"price\": 8.95,\n" +
                        "            \"category\": \"reference\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}",
                formatter.format(h), false);
    }

    @Test
    public void testSortingOnNumber() {
        String h = helper.sort("{\n" +
                "  \"extraKey\": 2,\n" +
                "  \"parameters\": [\n" +
                "    {\n" +
                "      \"category\": \"reference\",\n" +
                "      \"price\": 8.95\n" +
                "    },\n" +
                "    {\n" +
                "      \"category\": \"areference\",\n" +
                "      \"price\": 18.95\n" +
                "    }\n" +
                "  ]\n" +
                "}", "$.parameters", "$.price");

        JSONAssert.assertEquals("{\n" +
                        "    \"extraKey\": 2,\n" +
                        "    \"parameters\": [\n" +
                        "        {\n" +
                        "            \"price\": 8.95,\n" +
                        "            \"category\": \"reference\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"price\": 18.95,\n" +
                        "            \"category\": \"areference\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}",
                formatter.format(h), false);
    }

    @Test
    public void testComplexMap() {
        Map<String, Object> mobileEmulation = new LinkedHashMap<>();
        mobileEmulation.put("deviceName", "Google Nexus 5");

        Map<String, Object> chromeOptions = new LinkedHashMap<>();
        chromeOptions.put("mobileEmulation", mobileEmulation);

        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put("browserName", "chrome");
        expected.put("chromeOptions", chromeOptions);

        assertEquals(expected,
                helper.jsonStringToMap("{\"browserName\":\"chrome\",\"chromeOptions\":{\"mobileEmulation\":{\"deviceName\":\"Google Nexus 5\"}}}"));
    }

    @Test
    public void testListedMap() {
        ArrayList<Object> listargs = new ArrayList<>();
        listargs.add("start-maximized");

        Map<String, Object> chromeOptions = new LinkedHashMap<>();
        chromeOptions.put("args", listargs);

        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put("browserName", "chrome");
        expected.put("chromeOptions", chromeOptions);

        assertEquals(expected,
                helper.jsonStringToMap("{\"browserName\":\"chrome\",\"chromeOptions\":{\"args\":[\"start-maximized\"]}}"));
    }
}
