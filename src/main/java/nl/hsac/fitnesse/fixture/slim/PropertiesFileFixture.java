package nl.hsac.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.util.PropertiesHelper;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Deals with .properties files.
 */
public class PropertiesFileFixture extends FileFixture {
    /**
     * Adds the properties loaded from the specified file to current values.
     * @param filename .properties file to load
     * @return true when file is loaded
     * @throws IOException when unable to load file's content.
     */
    public boolean loadValuesFrom(String filename) {
        String propContent = textIn(filename);
        PropertiesHelper propHelper = getEnvironment().getPropertiesHelper();
        Properties properties = propHelper.parsePropertiesString(propContent);
        Map<String, Object> propAsMap = propHelper.convertPropertiesToMap(properties);
        getCurrentValues().putAll(propAsMap);
        return true;
    }

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
     * Creates new .properties file, containing current values.
     * @param filename name of file to create.
     * @return file created.
     */
    public String createContainingValues(String filename) {
        Properties p = new Properties();
        p.putAll(getCurrentValues());
        PropertiesHelper propHelper = getEnvironment().getPropertiesHelper();
        String fileContent = propHelper.writePropertiesToString(p);
        return createContaining(filename, fileContent);
    }
}
