package nl.hsac.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.util.MapHelper;

import java.util.*;

/**
 * Fixture to manipulate and generate map values. Generated values can be stored in variables so the can
 * be passed as arguments to methods of other fixtures.
 * This fixture can be used using Slim's dynamic decision tables or using scripts (and scenarios).
 */
public class MapFixture extends SlimFixture {

    private final Map<String, Object> currentValues;
    private MapHelper mapHelper;

    public MapFixture() {
        this(new LinkedHashMap<String, Object>());
    }

    public MapFixture(Map<String, Object> map) {
        currentValues = map;
        mapHelper = new MapHelper();
    }

    /**
     * Stores value to be passed to template, or GET.
     * @param value value to be passed.
     * @param name name to use this value for.
     */
    public void setValueFor(Object value, String name) {
        setValueForIn(value, name, getCurrentValues());
    }

    /**
     * Stores value in map.
     * @param value value to be passed.
     * @param name name to use this value for.
     * @param map map to store value in.
     */
    public void setValueForIn(Object value, String name, Map<String, Object> map) {
        mapHelper.setValueForIn(value, name, map);
    }

    /**
     * Stores list of values in map.
     * @param values comma separated list of values.
     * @param name name to use this list for.
     */
    public void setValuesFor(String values, String name) {
        mapHelper.setValuesForIn(values, name, getCurrentValues());
    }

    /**
     * Stores list of values in map.
     * @param values comma separated list of values.
     * @param name name to use this list for.
     * @param map map to store values in.
     */
    public void setValuesForIn(String values, String name, Map<String, Object> map) {
        mapHelper.setValuesForIn(values, name, map);
    }

    /**
     * Clears a values previously set.
     * @param name value to remove.
     * @return true if value was present.
     */
    public boolean clearValue(String name) {
        String cleanName = cleanupValue(name);
        boolean result = getCurrentValues().containsKey(cleanName);
        getCurrentValues().remove(cleanName);
        return result;
    }

    /**
     * Clears all values previously set.
     */
    public void clearValues() {
        getCurrentValues().clear();
    }

    /**
     * @return a copy of the current map.
     */
    public Map<String, Object> copyMap() {
        return new LinkedHashMap<String, Object>(getCurrentValues());
    }

    /**
     * Allows subclasses to manipulate current map (not a copy).
     * @return current values.
     */
    protected Map<String, Object> getCurrentValues() {
        return currentValues;
    }

    // methods to support usage in dynamic decision tables
    public void reset() {
        clearValues();
    }

    public void set(String key, Object value) {
        setValueFor(value, key);
    }

    public Map<String, Object> get(String requestedValue) {
        return copyMap();
    }
    // end: methods to support usage in dynamic decision tables

    /**
     * Retrieves value of element at a specified key.
     * @param name key to get value of (nested values may be retrieved by separating the keys with '.').
     * @return value stored.
     */
    public Object value(String name) {
        return valueIn(name, getCurrentValues());
    }

    public Object valueIn(String name, Map<String, Object> map) {
        String cleanName = cleanupValue(name);
        return mapHelper.getValue(map, cleanName);
    }

    /**
     * @return number of elements in map.
     */
    public int size() {
        return sizeOf(getCurrentValues());
    }

    /**
     * @param val map or expression to count elements in.
     * @return number of elements in map or list.
     */
    public int sizeOf(Object val) {
        int result;
        if (val instanceof Map) {
            result = ((Map) val).size();
        } else if (val instanceof String) {
            result = sizeOfIn((String) val, getCurrentValues());
        } else {
            throw new SlimFixtureException(false, "Cannot determine size of: " + val);
        }
        return result;
    }

    public int sizeOfIn(String expr, Map<String, Object> map) {
        return mapHelper.sizeOfIn(expr, map);
    }

    /**
     * @return all values in the map.
     */
    public List<String> allKeys() {
        return new ArrayList<String>(getCurrentValues().keySet());
    }

    /**
     * @return all values in the map.
     */
    public List<Object> allValues() {
        return new ArrayList<Object>(getCurrentValues().values());
    }

    /**
     * Determines whether current map matches other.
     * @param other other map to check.
     * @return true if both maps are equal.
     */
    public boolean contentEquals(Object other) {
        return contentOfEquals(getCurrentValues(), other);
    }

    /**
     * Determines whether map one's content matches two.
     * @param one map the check content of.
     * @param two other map to check.
     * @return true if both maps are equal.
     */
    public boolean contentOfEquals(Map<String, Object> one, Object two) {
        return mapHelper.contentOfEquals(one, two);
    }

    public void setMapHelper(MapHelper mapHelper) {
        this.mapHelper = mapHelper;
    }
}
