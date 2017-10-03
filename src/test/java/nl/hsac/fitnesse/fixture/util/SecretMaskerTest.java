package nl.hsac.fitnesse.fixture.util;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

public class SecretMaskerTest {
    private final SecretMasker masker = new SecretMasker();
    private final Map<String, Object> map = new LinkedHashMap<>();

    @Before
    public void setUp() {
        LinkedHashMap<String, Object> nested = new LinkedHashMap<>();
        nested.put("a", "A");
        nested.put("one", true);
        nested.put("two", false);

        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);
        map.put("nested", nested);
    }

    @Test
    public void testNoOp() {
        assertSame(map, masker.replaceSecrets(Collections.emptyList(), map));
    }

    @Test
    public void testNotPresent() {
        assertSame(map, masker.replaceSecrets(Arrays.asList("bye", "hi"), map));
    }

    @Test
    public void testTopLevel() {
        Map<String, Object> result = masker.replaceSecrets(Collections.singleton("three"), map);
        assertNotSame(map, result);
        assertEquals("{one=1, two=2, three=*****, nested={a=A, one=true, two=false}}", result.toString());
    }

    @Test
    public void testNestedLevel() {
        Map<String, Object> result = masker.replaceSecrets(Collections.singleton("a"), map);
        assertNotSame(map, result);
        assertEquals("{one=1, two=2, three=3, nested={a=*****, one=true, two=false}}", result.toString());
    }

    @Test
    public void testBothLevel() {
        Map<String, Object> result = masker.replaceSecrets(Arrays.asList("two", "one"), map);
        assertNotSame(map, result);
        assertEquals("{one=*****, two=*****, three=3, nested={a=A, one=*****, two=*****}}", result.toString());
    }

    @Test
    public void testSubMap() {
        Map<String, Object> result = masker.replaceSecrets(Arrays.asList("nested", "one"), map);
        assertNotSame(map, result);
        assertEquals("{one=*****, two=2, three=3, nested=*****}", result.toString());
    }

}
