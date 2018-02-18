package nl.hsac.fitnesse.fixture.slim;

import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Slim fixture base class allowing values to be set to a Map via a script or dynamic decision table.
 */
public class SlimFixtureWithMap extends SlimFixtureWithMapHelper {
    private final Map<String, Object> currentValues;

    /**
     * Creates new, having an empty current values collection.
     */
    public SlimFixtureWithMap() {
        this(new LinkedHashMap<>());
    }

    /**
     * Creates new, using the elements of the supplied map as current values.
     * @param map map to obtain current elements from.
     */
    public SlimFixtureWithMap(Map<String, Object> map) {
        currentValues = map;
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
     * Stores boolean value.
     * @param value value to be passed.
     * @param name name to use this value for.
     */
    public void setBooleanValueFor(boolean value, String name) {
        setValueFor(Boolean.valueOf(value), name);
    }

    /**
     * Stores value.
     * @param value value to be stored.
     * @param name name to use this value for.
     */
    public void setValueFor(Object value, String name) {
        getMapHelper().setValueForIn(value, name, getCurrentValues());
    }

    /**
     * Adds value to (end of) a list.
     * @param value value to be stored.
     * @param name name of list to extend.
     */
    public void addValueTo(Object value, String name) {
        getMapHelper().addValueToIn(value, name, getCurrentValues());
    }

    /**
     * Adds all values in the supplied map to the current values.
     * @param map to obtain values from.
     */
    public void copyValuesFrom(Map<String, Object> map) {
        getMapHelper().copyValuesFromTo(map, getCurrentValues());
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
     * @param file file's whose content should be set as byte[]
     * @param key key whose value should be set.
     */
    public void setContentOfAsValueFor(String file, String key) {
        String filePath = getFilePathFromWikiUrl(file);
        try {
            byte[] content = IOUtils.toByteArray(new FileInputStream(filePath));
            setValueFor(content, key);
        } catch (IOException e) {
            throw new SlimFixtureException("Unable to read: " + filePath, e);
        }
    }

    /**
     * @param file file's whose content should be base64 encoded
     * @param key key whose value should be set.
     */
    public void setBase64EncodedContentOfAsValueFor(String file, String key) {
        Base64Fixture base64Fixture = getBase64Fixture();
        String base64 = base64Fixture.encode(file);
        setValueFor(base64, key);
    }

    protected Base64Fixture getBase64Fixture() {
        return new Base64Fixture();
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
}
