package nl.hsac.fitnesse.fixture.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests MapHelper.
 */
public class MapHelperTest {
    private MapHelper helper = new MapHelper();

    @Test
    public void testGetValue() {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> nestedMap = new HashMap<>();
        Map<String, Object> nestedNestedMap = new HashMap<>();
        map.put("list", Arrays.asList(nestedMap));
        nestedMap.put("nestedList", Arrays.asList("", nestedNestedMap));
        nestedNestedMap.put("node", 12);

        assertEquals(Arrays.asList(nestedMap), helper.getValue(map, "list"));
        assertEquals(nestedMap, helper.getValue(map, "list[0]"));
        assertEquals(nestedNestedMap, helper.getValue(map, "list[0].nestedList[1]"));
        assertEquals(12, helper.getValue(map, "list[0].nestedList[1].node"));
    }

    @Test
    public void testSetValue() {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> nestedMap = new HashMap<>();
        Map<String, Object> nestedNestedMap = new HashMap<>();
        map.put("list", new ArrayList<Object>(Arrays.asList(nestedMap)));
        nestedMap.put("nestedList", Arrays.asList("", nestedNestedMap));
        nestedNestedMap.put("node", 12);

        helper.setValueForIn(11, "intNode", map);
        assertEquals(11, map.get("intNode"));

        helper.setValueForIn("13", "node", map);
        assertEquals("13", map.get("node"));

        helper.setValueForIn("13", "list[0].nestedList[1].node", map);
        assertEquals("13", nestedNestedMap.get("node"));

        helper.setValueForIn("15", "list[0].newnode", map);
        assertEquals("15", nestedMap.get("newnode"));

        helper.setValueForIn("14", "list[0]", map);
        assertEquals("14", ((List) map.get("list")).get(0));
        helper.setValueForIn("11", "list[1]", map);
        assertEquals("11", ((List) map.get("list")).get(1));

        helper.setValueForIn("16", "list[2][0]", map);
        assertEquals("16", ((List) ((List) map.get("list")).get(2)).get(0));

        Map<String, Object> nested2 = new HashMap<>();
        List<String> aliases = new ArrayList<>();
        aliases.add("Pete");
        aliases.add("John");
        nested2.put("aliases", aliases);
        map.put("map", nested2);
        helper.setValueForIn("Joe", "map.aliases[1]", map);
        assertEquals("Joe", aliases.get(1));
    }

    @Test
    public void testSetValueCreatesNestedMaps() {
        Map<String, Object> map = new HashMap<>();

        helper.setValueForIn("NewNestedMapKey", "nested3.a", map);
        Object nested3 = map.get("nested3");
        assertNotNull(nested3);
        assertEquals("NewNestedMapKey", ((Map<String, Object>)nested3).get("a"));

        // multiple levels of nesting
        helper.setValueForIn("2LevelDeep", "nested4.b.c", map);
        Object nested4 = map.get("nested4");
        assertNotNull(nested4);
        Object nested4b = ((Map<String, Object>)nested4).get("b");
        assertNotNull(nested4b);
        assertEquals("2LevelDeep", ((Map<String, Object>)nested4b).get("c"));
    }

    @Test
    public void testSetValueCreatingList() {
        Map<String, Object> map = new HashMap<>();
        helper.setValueForIn("1", "list[0]", map);
        List list = (List) map.get("list");
        assertEquals("1", list.get(0));

        helper.setValueForIn("2", "list[1].nested", map);
        Map<String, Object> secondListElement = (Map<String, Object>) list.get(1);
        assertEquals("2", secondListElement.get("nested"));

        helper.setValueForIn("3", "list[1].nested", map);
        assertEquals("3", secondListElement.get("nested"));

        helper.setValueForIn("4", "list[1].nested2[0].nested3", map);
        assertEquals("4", ((Map) ((List) secondListElement.get("nested2")).get(0)).get("nested3"));

        helper.setValueForIn("5", "list2[0].nested", map);
        List list2 = (List) map.get("list2");
        Map<String, Object> thirdListElement = (Map<String, Object>) list2.get(0);
        assertEquals("5", thirdListElement.get("nested"));

        assertEquals("first element changed","1", list.get(0));
    }
}
