package nl.hsac.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.util.MapHelper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Slim fixture base class allowing values to be set to a Map via a script or dynamic decision table.
 */
public class SlimFixtureWithMap extends SlimFixture {
    private final Map<String, Object> currentValues;
    private MapHelper mapHelper;

    /**
     * Creates new, having an empty current values collection.
     */
    public SlimFixtureWithMap() {
        this(new LinkedHashMap<String, Object>());
    }

    /**
     * Creates new, using the elements of the supplied map as current values.
     * @param map map to obtain current elements from.
     */
    public SlimFixtureWithMap(Map<String, Object> map) {
        currentValues = map;
        // default map helper
        // (shared by all fixtures so default configuration can be changed by one call, if needed)
        mapHelper = getEnvironment().getMapHelper();
    }

    /**
     * Stores integer value.
     * @param value value to be passed.
     * @param name name to use this value for.
     */
    public void setIntValueFor(int value, String name) {
        setValueFor(Integer.valueOf(value), name);
    }

    /**
     * Stores double value.
     * @param value value to be passed.
     * @param name name to use this value for.
     */
    public void setDoubleValueFor(double value, String name) {
        setValueFor(Double.valueOf(value), name);
    }

    /**
     * Stores value to be passed to template, or GET.
     * @param value value to be passed.
     * @param name name to use this value for.
     */
    public void setValueFor(Object value, String name) {
        getMapHelper().setValueForIn(value, name, getCurrentValues());
    }

    /**
     * Stores list of values in map.
     * @param values comma separated list of values.
     * @param name name to use this list for.
     */
    public void setValuesFor(String values, String name) {
        getMapHelper().setValuesForIn(values, name, getCurrentValues());
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
     * Allows subclasses to manipulate current map (not a copy).
     * @return current values.
     */
    protected Map<String, Object> getCurrentValues() {
        return currentValues;
    }

    /**
     * Retrieves value of element at a specified key.
     * @param name key to get value of (nested values may be retrieved by separating the keys with '.').
     * @return value stored.
     */
    public Object value(String name) {
        return getMapHelper().getValue(getCurrentValues(), name);
    }

    //// methods to support usage in dynamic decision tables

    /**
     * Called before next row is executed. (Clears all current values.)
     */
    public void reset() {
        clearValues();
    }

    /**
     * Sets a value.
     * @param key (possibly nested) key to set value for.
     * @param value value to be stored.
     */
    public void set(String key, Object value) {
        setValueFor(value, key);
    }

    //// end: methods to support usage in dynamic decision tables

    /**
     * @return helper to assist getting/setting (nested) values in a map.
     */
    public MapHelper getMapHelper() {
        return mapHelper;
    }

    /**
     * @param mapHelper helper to assist getting/setting (nested) values in a map.
     */
    public void setMapHelper(MapHelper mapHelper) {
        this.mapHelper = mapHelper;
    }
}
