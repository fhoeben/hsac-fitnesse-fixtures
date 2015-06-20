package nl.hsac.fitnesse.fixture.util;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
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
            result = new JSONObject(json).toString(4);
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
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonString);
            return jsonObjectToMap(jsonObject);
        } catch (JSONException e) {
            throw new RuntimeException("Unable to convert string to map: " + jsonString, e);
        }
    }

    private Map<String, Object> jsonObjectToMap(JSONObject jsonObject) throws JSONException {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        for (Object key : jsonObject.keySet()) {
            String stringKey = String.valueOf(key);
            result.put(stringKey, jsonObject.get(stringKey));
        }
        return  result;
    }
}
