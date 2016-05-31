package nl.hsac.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.util.JsonPathHelper;
import org.json.JSONObject;

import java.util.List;

/**
 * Fixture to make Http calls and interpret the result as JSON.
 */
public class JsonHttpTest extends HttpTest {
    private final JsonPathHelper pathHelper = new JsonPathHelper();

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
        String jsonPath = getPathExpr(path);
        return pathHelper.getJsonPath(getResponse().getResponse(), jsonPath);
    }

    public int jsonPathCount(String path) {
        List<Object> all = getAllMatches(path);
        return all.size();
    }

    protected List<Object> getAllMatches(String path) {
        String jsonPath = getPathExpr(path);
        return pathHelper.getAllJsonPath(getResponse().getResponse(), jsonPath);
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
    public void setJsonPathTo(String path, String value){
        String jsonStr = getResponse().getResponse();
        String jsonPath = getPathExpr(path);
        String newResponse = pathHelper.updateJsonPathWithValue(jsonStr, jsonPath, value);
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
}
