package nl.hsac.fitnesse.fixture.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static nl.hsac.fitnesse.fixture.util.StreamUtil.lastOneWinsMerger;
import static nl.hsac.fitnesse.fixture.util.StreamUtil.toLinkedMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class StreamUtilTest {
    @Test
    public void toLinkedMapNormal() throws Exception {
        List<String> list = Arrays.asList("a", "b");

        Map<String, Object> m = list.stream().collect(toLinkedMap(k -> k, v -> v));

        assertEquals(2, m.size());
        assertEquals(list, new ArrayList<>(m.keySet()));
        assertEquals("a", m.get("a"));
        assertEquals("b", m.get("b"));
        assertEquals(LinkedHashMap.class, m.getClass());
    }

    @Test
    public void toLinkedMapDuplicateKey() throws Exception {
        try {
            List<String> list = Arrays.asList("a", "b");
            Map<String, Object> m = list.stream().collect(toLinkedMap(k -> "a", v -> "c"));
            fail("Expected exception, got: " + m);
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void toLinkedMapDuplicatesAllowed() throws Exception {
        List<String> list = Arrays.asList("a", "b");

        Map<String, Object> m = list.stream().collect(toLinkedMap(k -> "k", v -> v, lastOneWinsMerger()));

        assertEquals(1, m.size());
        assertEquals("b", m.get("k"));
        assertEquals(LinkedHashMap.class, m.getClass());
    }
}