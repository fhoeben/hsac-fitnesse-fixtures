package nl.hsac.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.util.JsonPathHelper;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.List;

/**
 * Fixture to make Http calls and interpret the result as JSON.
 */
public class JsonHttpTest extends HttpTest {
    public boolean postValuesAsJsonTo(String serviceUrl) {
        return postToImpl(jsonEncodeCurrentValues(), serviceUrl);
    }
    
    public boolean putValuesAsJsonTo(String serviceUrl) {
        return putToImpl(jsonEncodeCurrentValues(), serviceUrl);
    }

    protected String jsonEncodeCurrentValues() {
        return new JSONObject(getCurrentValues()).toString();
    }

    @Override
    protected String formatValue(String value) {
        return getEnvironment().getHtmlForJson(value);
    }

    public Object jsonPath(String path) {
        String responseString = getResponseBody();
        String jsonPath = getPathExpr(path);
        return getPathHelper().getJsonPath(responseString, jsonPath);
    }

    public int jsonPathCount(String path) {
        List<Object> all = getAllMatches(path);
        return all.size();
    }

    protected List<Object> getAllMatches(String path) {
        String responseString = getResponseBody();
        String jsonPath = getPathExpr(path);
        return getPathHelper().getAllJsonPath(responseString, jsonPath);
    }

    protected String getResponseBody() {
        String responseString = getResponse().getResponse();
        if (StringUtils.isEmpty(responseString)) {
            throw new SlimFixtureException(false, "No response body available");
        }
        return responseString;
    }

    /**
     * Gets a HTML list with all matches to the supplied JsonPath.
     * @param expr expression to evaluate.
     * @return list containing all results of expression evaluation against last response received, null if there were no matches.
     * @throws RuntimeException if no valid response was available or Json Path could not be evaluated.
     */
    public String allJsonPathMatches(String expr) {
        String result = null;
        List<Object> allJsonPath = getAllMatches(expr);
        if (allJsonPath != null && !allJsonPath.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("<div><ul>");
            for (Object match : allJsonPath) {
                sb.append("<li>");
                sb.append(match);
                sb.append("</li>");
            }
            sb.append("</ul></div>");
            result = sb.toString();
        }
        return result;
    }

    /**
     * Update a value in a the response by supplied jsonPath
     * @param path the jsonPath to locate the key whose value needs changing
     * @param value the new value to set
     */
    public void setJsonPathTo(String path, String value) {
        String jsonStr = getResponseBody();
        String jsonPath = getPathExpr(path);
        String newResponse = getPathHelper().updateJsonPathWithValue(jsonStr, jsonPath, value);
        getResponse().setResponse(newResponse);
    }

    protected String getPathExpr(String path) {
        String jsonPath = path;
        if (!path.startsWith("$")) {
            if (path.startsWith("[") || path.startsWith(".")) {
                jsonPath = "$" + path;
            } else {
                jsonPath = "$." + path;
            }
        }
        return jsonPath;
    }

    @Override
    protected String urlEncode(String str) {
        String strNoSpaces = str.replace(" ", "+");
        return super.urlEncode(strNoSpaces);
    }

    protected JsonPathHelper getPathHelper() {
        return getEnvironment().getJsonPathHelper();
    }
}
