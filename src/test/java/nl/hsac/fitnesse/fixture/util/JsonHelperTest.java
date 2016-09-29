package nl.hsac.fitnesse.fixture.util;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class JsonHelperTest {
    private JsonHelper helper = new JsonHelper();

    @Test
    public void testFormatNull() {
        assertNull(helper.format(null));
    }

    @Test
    public void testFormatSimple() {
        assertEquals(
                "{\n" +
                        "    \"category\": \"reference\",\n" +
                        "    \"price\": 8.95\n" +
                        "}",
                helper.format("{\"category\": \"reference\",\"price\": 8.95}"));
    }

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

        assertEquals("{\n" +
                        "    \"extraKey\": 2,\n" +
                        "    \"parameters\": [\n" +
                        "        {\n" +
                        "            \"category\": \"areference\",\n" +
                        "            \"price\": 18.95\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"category\": \"reference\",\n" +
                        "            \"price\": 8.95\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}",
                helper.format(h));
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

        assertEquals("{\n" +
                        "    \"extraKey\": 2,\n" +
                        "    \"parameters\": [\n" +
                        "        {\n" +
                        "            \"category\": \"reference\",\n" +
                        "            \"price\": 8.95\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"category\": \"areference\",\n" +
                        "            \"price\": 18.95\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}",
                helper.format(h));
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
}
