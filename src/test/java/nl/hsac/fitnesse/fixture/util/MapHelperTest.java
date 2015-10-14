package nl.hsac.fitnesse.fixture.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Tests MapHelper.
 */
public class MapHelperTest {
    private MapHelper helper = new MapHelper();

    @Test
    public void testValueOf() {
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> nestedMap = new HashMap<String, Object>();
        Map<String, Object> nestedNestedMap = new HashMap<String, Object>();
        map.put("list", Arrays.asList(nestedMap));
        nestedMap.put("nestedList", Arrays.asList("", nestedNestedMap));
        nestedNestedMap.put("node", 12);

        assertEquals(12, helper.getValue(map, "list[0].nestedList[1].node"));
    }
}
