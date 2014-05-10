package nl.hsac.fitnesse.fixture.fit;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Parameter for Maps to be passed from one fixture to another.
 */
public class MapParameter extends HashMap<String, Object> {
    private final static Map<String, MapParameter> INSTANCES = new ConcurrentHashMap<String, MapParameter>();
    private final String key;
    /**
     * Creates new.
     * @param aHeaderName display type name
     * @param aNr instance number
     */
    public MapParameter(String aHeaderName, String aNr) {
        key = aHeaderName + "@" + aNr;
        INSTANCES.put(toString(), this);
    }

    public static MapParameter parse(String value) {
        return INSTANCES.get(value);
    }
    
    public String toString() {
        // ensure unique toString that does not change
        return key;
    }

    /**
     * Clears set of known map parameters (that can be returned by parse()).
     */
    public static void clearInstances() {
        INSTANCES.clear();
    }
}
