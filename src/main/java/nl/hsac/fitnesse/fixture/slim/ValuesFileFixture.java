package nl.hsac.fitnesse.fixture.slim;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Base class for fixtures reading/writing its values from/to files.
 */
public abstract class ValuesFileFixture extends FileFixture {
    /**
     * Adds content from the specified file to current values.
     * @param filename file to load
     * @return true when file is loaded
     * @throws IOException when unable to load file's content.
     */
    public abstract boolean loadValuesFrom(String filename);

    /**
     * Creates new file, containing current values.
     * @param filename name of file to create.
     * @return file created.
     */
    public String createContainingValues(String filename) {
        return createContaining(filename, getCurrentValues());
    }

    @Override
    public String createContaining(String filename, Object data) {
        if (data instanceof Map) {
            return createContaining(filename, (Map) data);
        } else {
            return filesCreateContaining(filename, data);
        }
    }

    protected String filesCreateContaining(String filename, Object data) {
        return super.createContaining(filename, data);
    }

    protected abstract String createContaining(String filename, Map<String, Object> map);

    /**
     * @return copy of current values.
     */
    public Map<String, Object> values() {
        return new LinkedHashMap<>(getCurrentValues());
    }

    /**
     * @return number of values.
     */
    public int numberOfValues() {
        return getCurrentValues().size();
    }

    /**
     * Saves content of a key's value as file in the files section.
     * @param basename filename to use.
     * @param key key to get value from.
     * @return file created.
     */
    public String createContainingBase64Value(String basename, String key) {
        String file;
        Object value = value(key);
        if (value == null) {
            throw new SlimFixtureException(false, "No value for key: " + key);
        } else if (value instanceof String) {
            file = createFileFromBase64(basename, (String) value);
        } else {
            throw new SlimFixtureException(false, "Value for key: " + key + " is not a String, but: " + value);
        }
        return file;
    }
}
