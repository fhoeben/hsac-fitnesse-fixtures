package nl.hsac.fitnesse.fixture.slim;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fixture to manipulate and generate map values. Generated values can be stored in variables so the can
 * be passed as arguments to methods of other fixtures.
 * This fixture can be used using Slim's dynamic decision tables or using scripts (and scenarios).
 */
public class MapFixture extends SlimFixtureWithMap {
    /**
     * Creates new, having an empty current values collection.
     */
    public MapFixture() {
        super();
    }

    /**
     * Creates new, using the elements of the supplied map as current values.
     * @param map map to obtain current elements from.
     */
    public MapFixture(Map<String, Object> map) {
        super(map);
    }

    //// methods to support usage in dynamic decision tables

    /**
     * Retrieves value for output column.
     * @param headerName header of output column (without trailing '?').
     * @return new map containing current values.
     */
    public Map<String, Object> get(String headerName) {
        return copyMap();
    }

    //// methods to support usage in dynamic decision tables

    /**
     * @return a copy of the current map.
     */
    public Map<String, Object> copyMap() {
        return new LinkedHashMap<String, Object>(getCurrentValues());
    }

    /**
     * Stores integer value in map.
     * @param value value to be passed.
     * @param name name to use this value for.
     * @param map map to store value in.
     */
    public void setIntValueForIn(int value, String name, Map<String, Object> map) {
        setValueForIn(Integer.valueOf(value), name, map);
    }

    /**
     * Stores double value in map.
     * @param value value to be passed.
     * @param name name to use this value for.
     * @param map map to store value in.
     */
    public void setDoubleValueForIn(double value, String name, Map<String, Object> map) {
        setValueForIn(Double.valueOf(value), name, map);
    }

    /**
     * Stores value in map.
     * @param value value to be passed.
     * @param name name to use this value for.
     * @param map map to store value in.
     */
    public void setValueForIn(Object value, String name, Map<String, Object> map) {
        getMapHelper().setValueForIn(value, name, map);
    }

    /**
     * Stores list of values in map.
     * @param values comma separated list of values.
     * @param name name to use this list for.
     * @param map map to store values in.
     */
    public void setValuesForIn(String values, String name, Map<String, Object> map) {
        getMapHelper().setValuesForIn(values, name, map);
    }

    /**
     * Gets value from map.
     * @param name name of (possibly nested) property to get value from.
     * @param map map to get value from.
     * @return value found, if it could be found, null otherwise.
     */
    public Object valueIn(String name, Map<String, Object> map) {
        return getMapHelper().getValue(map, name);
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

    /**
     * Determines size of either (Map or Collection) value in the map.
     * @param expr expression indicating which (possibly nested) value in the map to determine size of.
     * @param map map to find value in.
     * @return size of value.
     * @throws SlimFixtureException if the value found is not a Map or Collection.
     */
    public int sizeOfIn(String expr, Map<String, Object> map) {
        return getMapHelper().sizeOfIn(expr, map);
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
        return getMapHelper().contentOfEquals(one, two);
    }
}
