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
public class MapFixture extends SlimFixture {

    private final Map<String, Object> currentValues;

    public MapFixture() {
        this(new LinkedHashMap<String, Object>());
    }

    public MapFixture(Map<String, Object> map) {
        currentValues = map;
    }

    /**
     * Stores value to be passed to template, or GET.
     * @param value value to be passed.
     * @param name name to use this value for.
     */
    public void setValueFor(Object value, String name) {
        String cleanName = cleanupValue(name);
        Object cleanValue = value;
        if (value instanceof String) {
            cleanupValue((String) value);
        }
        getCurrentValues().put(cleanName, cleanValue);
    }

    /**
     * Stores list of values to be passed to template.
     * @param values comma separated list of values.
     * @param name name to use this list for.
     */
    public void setValuesFor(String values, String name) {
        String cleanName = cleanupValue(name);
        String[] valueArrays = values.split("\\s*,\\s*");
        for (int i = 0; i < valueArrays.length; i++) {
            valueArrays[i] = cleanupValue(valueArrays[i]);
        }
        getCurrentValues().put(cleanName, valueArrays);
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
        return getValue(map, cleanName);
    }

    protected Object getValue(Map<String, Object> map, String name) {
        Object value = null;
        if (map.containsKey(name)) {
            value = map.get(name);
        } else {
            String[] parts = name.split("\\.", 2);
            if (parts.length > 1) {
                Object nested = map.get(parts[0]);
                if (nested instanceof Map) {
                    Map<String, Object> nestedMap = (Map<String, Object>) nested;
                    value = getValue(nestedMap, parts[1]);
                }
            }
        }
        return value;
    }

    /**
     * @return number of elements in map.
     */
    public int size() {
        return getCurrentValues().size();
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
        if (one == null) {
            return two == null;
        } else {
            return one.equals(two);
        }
    }
}
