package nl.hsac.fitnesse.slim.converter;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ArrayListConverterTest {
    private final ArrayListConverter converter = new ArrayListConverter();

    @Test
    public void testToStringNull() {
        assertNull(converter.toString(null));
    }

    @Test
    public void testToStringEmpty() {
        assertEquals("<ol></ol>", converter.toString(new ArrayList<Object>()));
    }

    @Test
    public void testToStringFilled() {
        ArrayList<Object> list = new ArrayList<Object>(Arrays.asList("b", "c", "d", null));
        assertEquals("<ol><li>b</li><li>c</li><li>d</li><li>null</li></ol>", converter.toString(list));
    }

    @Test
    public void testFromStringEmpty() {
        assertEquals(new ArrayList<Object>(), converter.fromString("<ol> </ol>"));
    }

    @Test
    public void testFromStringFilled() {
        List<String> expected = Arrays.asList("b", "c", "d");
        assertEquals(expected, converter.fromString("<ol> <li>   b </li> <li>   c </li><li>d</li></ol>"));
    }

    @Test
    public void testFromStringStandardList() {
        List<String> expected = Arrays.asList("b", "c", "d");
        assertEquals(expected, converter.fromString(expected.toString()));
    }
}
