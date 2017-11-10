package nl.hsac.fitnesse.fixture.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
                .collect(toMap(
                        e -> e.getKey().toString(),
                        e -> e.getValue()));
    }

    protected static <T, K, U>  Collector<T, ?, Map<K,U>> toMap(Function<? super T, ? extends K> keyMapper,
                                    Function<? super T, ? extends U> valueMapper) {
        return Collectors.toMap(keyMapper, valueMapper, throwingMerger(), LinkedHashMap::new);
    }

    protected static <T> BinaryOperator<T> throwingMerger() {
        return (u,v) -> { throw new IllegalStateException(String.format("Duplicate property %s", u)); };
    }
}
