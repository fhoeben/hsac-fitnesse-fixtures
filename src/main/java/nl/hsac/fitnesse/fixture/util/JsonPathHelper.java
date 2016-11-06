package nl.hsac.fitnesse.fixture.util;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.PathNotFoundException;

import java.util.Collections;
import java.util.List;

/**
 * Helper to evaluate JsonPath expressions against a JSON object.
 * @link https://github.com/jayway/JsonPath
 * @link http://jsonpath.herokuapp.com/
 */
public class JsonPathHelper {
    private final static Configuration CONF = Configuration
                                                .defaultConfiguration()
                                                .addOptions(Option.SUPPRESS_EXCEPTIONS);
    private final static ParseContext CONTEXT = JsonPath.using(CONF);

    private String lastJson;
    private DocumentContext lastContext;

    /**
     * Evaluates a JsonPath expression returning a single element.
     * @param json JSON value.
     * @param jsonPath expression to evaluate.
     * @return result result of expression.
     * @throws java.lang.RuntimeException if jsonPath would return multiple elements.
     */
    public Object getJsonPath(String json, String jsonPath) {
        if (!JsonPath.isPathDefinite(jsonPath)) {
            throw new RuntimeException(jsonPath + " returns multiple results, not a single.");
        }
        return parseJson(json).read(jsonPath);
    }

    /**
     * Evaluates a JsonPath expression returning a multiple elements.
     * @param json JSON value.
     * @param jsonPath expression to evaluate.
     * @return result results of expression.
     * @throws java.lang.RuntimeException if jsonPath would return a single element.
     */
    public List<Object> getAllJsonPath(String json, String jsonPath) {
        List<Object> result;
        if (JsonPath.isPathDefinite(jsonPath)) {
            Object val = getJsonPath(json, jsonPath);
            if (val == null) {
                result = Collections.emptyList();
            } else {
                result = Collections.singletonList(val);
            }
        } else {
            result = parseJson(json).read(jsonPath);
        }
        return result;
    }

    public String updateJsonPathWithValue(String json, String jsonPath, Object value) {
        if(null != getJsonPath(json, jsonPath)) {
            return parseJson(json).set(jsonPath, value).jsonString();
        } else {
            throw new PathNotFoundException("No result for: " + jsonPath + " IN: " + json);
        }
    }

    protected DocumentContext parseJson(String json) {
        DocumentContext result;
        if (lastContext != null && lastJson != null
                && lastJson.equals(json)) {
            result = lastContext;
        } else {
            result = getContext().parse(json);
            lastContext = result;
            lastJson = json;
        }
        return result;
    }

    protected ParseContext getContext() {
        return CONTEXT;
    }
}
