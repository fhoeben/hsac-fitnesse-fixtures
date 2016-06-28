package nl.hsac.fitnesse.fixture.util;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.apache.commons.lang3.StringUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper dealing with JSON objects.
 */
public class JsonHelper implements Formatter {
    /**
     * Creates formatted version of the supplied JSON.
     * @param json JSON to format.
     * @return formatted version.
     */
    public String format(String json) {
        String result = null;
        if (json != null){
            result = new Gson().toJson(json);
        }
        return result;
    }

    /**
     * Interprets supplied String as Json and converts it into a Map.
     * @param jsonString string to interpret as Json object.
     * @return property -> value.
     */
    public Map<String, Object> jsonStringToMap(String jsonString) {
        if (StringUtils.isEmpty(jsonString)) {
            return null;
        }
        try {
            Map<String, Object> result = new HashMap<String, Object>();
            result = (Map<String, Object>)new Gson().fromJson(jsonString, result.getClass());
            return result;
        } catch (JsonParseException e) {
            throw new RuntimeException("Unable to convert string to map: " + jsonString, e);
        }
    }
}
