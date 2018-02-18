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
    public abstract String createContainingValues(String filename);

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
}
