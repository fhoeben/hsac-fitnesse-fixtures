package nl.hsac.fitnesse.slim.converter;

import fitnesse.slim.converters.ConverterRegistry;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class NumberedListConverterTest {
    private final NumberedListConverter converter = new NumberedListConverter();

    @AfterClass
    public static void cleanup() {
        ConverterRegistry.resetToStandardConverters();
    }

    @Test
    public void testToStringNull() {
        assertNull(converter.toString(null));
    }

    @Test
    public void testToStringEmpty() {
        assertEquals("<ol start=\"0\"></ol>", converter.toString(new ArrayList<Object>()));
    }

    @Test
    public void testToStringFilled() {
        ArrayList<Object> list = new ArrayList<Object>(Arrays.asList("b", "c", "d", null));
        assertEquals("<ol start=\"0\"><li>b</li><li>c</li><li>d</li><li>null</li></ol>", converter.toString(list));
    }

    @Test
    public void testFromStringEmpty() {
        assertEquals(new ArrayList<Object>(), converter.fromString("<ol> </ol>"));
    }

    @Test
    public void testFromStringFilled() {
        List<String> expected = Arrays.asList("b", "c", "d");
        assertEquals(expected, converter.fromString("<ol start=\"0\"> <li>   b </li> <li>   c </li><li>d</li></ol>"));
        assertEquals(expected, converter.fromString("<ol> <li>   b </li> <li>   c </li><li>d</li></ol>"));
    }

    @Test
    public void testFromStringStandardList() {
        List<String> expected = Arrays.asList("b", "c", "d");
        assertEquals(expected, converter.fromString(expected.toString()));
    }

    @Test
    public void testViaRegistry() {
        NumberedListConverter.register();

        Map<String, List<String>> map = new HashMap<String, List<String>>();
        map.put("test", new LinkedList<String>(Arrays.asList("b", "c", null)));
        assertEquals(
                "<table class=\"hash_table\">\n" +
                "\t<tr class=\"hash_row\">\n" +
                "\t\t<td class=\"hash_key\">test</td>\n" +
                "\t\t<td class=\"hash_value\"><ol start=\"0\"><li>b</li><li>c</li><li>null</li></ol></td>\n" +
                "\t</tr>\n" +
                "</table>",
                ConverterRegistry.getConverterForClass(Map.class).toString(map).replace("\r", ""));

        Map<String, List<Integer>> map2 = new HashMap<String, List<Integer>>();
        map2.put("test", new LinkedList<Integer>(Arrays.asList(1, null, 3)));
        assertEquals(
                "<table class=\"hash_table\">\n" +
                        "\t<tr class=\"hash_row\">\n" +
                        "\t\t<td class=\"hash_key\">test</td>\n" +
                        "\t\t<td class=\"hash_value\"><ol start=\"0\"><li>1</li><li>null</li><li>3</li></ol></td>\n" +
                        "\t</tr>\n" +
                        "</table>",
                ConverterRegistry.getConverterForClass(Map.class).toString(map2).replace("\r", ""));

        Map<String, ArrayList<Object>> map3 = new HashMap<String, ArrayList<Object>>();
        map3.put("test", new ArrayList<Object>(Arrays.asList(1, null, "test")));
        assertEquals(
                "<table class=\"hash_table\">\n" +
                        "\t<tr class=\"hash_row\">\n" +
                        "\t\t<td class=\"hash_key\">test</td>\n" +
                        "\t\t<td class=\"hash_value\"><ol start=\"0\"><li>1</li><li>null</li><li>test</li></ol></td>\n" +
                        "\t</tr>\n" +
                        "</table>",
                ConverterRegistry.getConverterForClass(Map.class).toString(map3).replace("\r", ""));
    }
}
