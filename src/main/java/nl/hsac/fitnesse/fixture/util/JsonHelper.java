package nl.hsac.fitnesse.fixture.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minidev.json.JSONArray;
import nl.hsac.fitnesse.fixture.Environment;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper dealing with JSON objects.
 */
public class JsonHelper {
    private final static Gson GSON = new Gson();
    private final static Type MAP_TYPE = new TypeToken<LinkedHashMap<String, Object>>(){}.getType();

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
            return GSON.fromJson(jsonString, MAP_TYPE);
        } catch (JSONException e) {
            throw new RuntimeException("Unable to convert string to map: " + jsonString, e);
        }
    }

    private Map<String, Object> jsonObjectToMap(JSONObject jsonObject) throws JSONException {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Object key : jsonObject.keySet()) {
            String stringKey = String.valueOf(key);
            Object value = convertJsonObject(jsonObject.get(stringKey));
            result.put(stringKey, value);
        }
        return  result;
    }

    private Object convertJsonObject(Object value) {
        Object result = value;
        if (value instanceof JSONObject) {
            result = jsonObjectToMap((JSONObject) value);
        } else if (value instanceof org.json.JSONArray) {
            List<Object> newVal = new ArrayList<>();
            for (Object o : (org.json.JSONArray) value) {
                newVal.add(convertJsonObject(o));
            }
            result = newVal;
        }
        return result;
    }

    /**
     * Sorts an array in a json object.
     * @param json json document.
     * @param arrayExpr JsonPath expression to select array to sort.
     * @param nestedPathExpr JsonPath expression to select value of array's elements to sort them on.
     * @return json document with specified array sorted.
     */
    public String sort(String json, String arrayExpr, String nestedPathExpr) {
        JsonPathHelper pathHelper = getPathHelper();
        Object topLevel = pathHelper.getJsonPath(json, arrayExpr);
        if (topLevel instanceof JSONArray) {
            JSONArray a = (JSONArray) topLevel;
            JSONArray aSorted = sort(pathHelper, a, nestedPathExpr);
            return pathHelper.updateJsonPathWithValue(json, arrayExpr, aSorted);
        } else {
            throw new IllegalArgumentException("Unable to find array using: " + arrayExpr);
        }
    }

    private JSONArray sort(final JsonPathHelper pathHelper, JSONArray a, final String nestedPathExpr) {
        List<String> elements = new ArrayList<>(a.size());
        for (Object element : a) {
            net.minidev.json.JSONObject jsonObject = new net.minidev.json.JSONObject((Map<String, ?>) element);
            elements.add(jsonObject.toJSONString());
        }
        Collections.sort(elements, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                Comparable val1 = (Comparable) pathHelper.getJsonPath(o1, nestedPathExpr);
                Comparable val2 = (Comparable) pathHelper.getJsonPath(o2, nestedPathExpr);
                return val1.compareTo(val2);
            }
        });
        return convertToArray(elements);
    }

    private JSONArray convertToArray(List<String> elements) {
        List<Map<String, Object>> result = new ArrayList<>(elements.size());
        for (String str : elements) {
            Map<String, Object> map = jsonStringToMap(str);
            result.add(map);
        }
        JSONArray array = new JSONArray();
        array.addAll(result);
        return array;
    }

    public JsonPathHelper getPathHelper() {
        return Environment.getInstance().getJsonPathHelper();
    }
}
