package nl.hsac.fitnesse.fixture.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

import static nl.hsac.fitnesse.fixture.util.StreamUtil.toLinkedMap;

/**
 * Helper dealing with property files.
 */
public class PropertiesHelper {
    /**
     * Converts String to Properties.
     * @param propertiesAsString contents of .properties file.
     * @return Properties as parsed.
     */
    public Properties parsePropertiesString(String propertiesAsString) {
        final Properties p = new Properties();
        try (StringReader reader = new StringReader(propertiesAsString)) {
            p.load(reader);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to parse .properties: " + propertiesAsString, e);
        }
        return p;
    }

    /**
     * Converts Properties to Map
     * @param properties properties to convert.
     * @return map version of properties.
     */
    public Map<String, Object> convertPropertiesToMap(Properties properties) {
        return properties.entrySet().stream()
                .collect(toLinkedMap(
                        e -> e.getKey().toString(),
                        e -> e.getValue()));
    }

    public String writePropertiesToString(Properties properties) {
        try (StringWriter sw = new StringWriter()) {
            properties.store(sw, null);
            String content = sw.toString();
            // strip first line containing creation date & time
            int firstNewline = content.indexOf('\n');
            return content.substring(firstNewline + 1);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to write: " + properties, e);
        }
    }
}
