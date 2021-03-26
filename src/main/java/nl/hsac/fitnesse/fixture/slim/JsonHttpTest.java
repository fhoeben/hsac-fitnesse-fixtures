package nl.hsac.fitnesse.fixture.slim;

import net.minidev.json.JSONObject;
import nl.hsac.fitnesse.fixture.util.DataUrlHelper;
import nl.hsac.fitnesse.fixture.util.JsonPathHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;

import java.util.ArrayList;
import java.util.List;

/**
 * Fixture to make Http calls and interpret the result as JSON.
 */
public class JsonHttpTest extends HttpTest {
    public static final String JSON_CONTENT_TYPE = ContentType.APPLICATION_JSON.toString();

    public boolean postValuesAsJsonTo(String serviceUrl) {
        return sendToImpl(jsonEncodeCurrentValues(), serviceUrl, getContentTypeForJson(), "POST");
    }

    public boolean putValuesAsJsonTo(String serviceUrl) {
        return sendToImpl(jsonEncodeCurrentValues(), serviceUrl, getContentTypeForJson(), "PUT");
    }

    public boolean deleteWithValuesAsJson(String serviceUrl) {
        return sendToImpl(jsonEncodeCurrentValues(), serviceUrl, getContentTypeForJson(), "DELETE");
    }

    protected String jsonEncodeCurrentValues() {
         return JSONObject.toJSONString(getCurrentValues());
    }

    protected String getContentTypeForJson() {
        // for methods that post JSON we change the default content type to be application/json
        String contentType;
        if (isExplicitContentTypeSet()) {
            contentType = getContentType();
        } else {
            contentType = JSON_CONTENT_TYPE;
        }
        return contentType;
    }

    @Override
    protected String formatValue(String value) {
        String formatted = super.formatValue(value);
        if (value != null) {
            String trimmed = value.trim();
            if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                formatted = getEnvironment().getHtmlForJson(trimmed);
            }
        }
        return formatted;
    }

    public Object jsonPath(String path) {
        String responseString = getResponseBody();
        String jsonPath = getPathExpr(path);
        return getPathHelper().getJsonPath(responseString, jsonPath);
    }

    /**
     * @param baseName base of filename to generate (a number might be added to the name to make it unique).
     * @param jsonPath expression to evaluate.
     * @return link to created file.
     */
    public String createFileFromBase64ContentOf(String baseName, String jsonPath) {
        Object base64Content = jsonPath(jsonPath);
        if (base64Content instanceof String) {
            if ("".equals(base64Content)) {
                throw new SlimFixtureException(false, "No content from json path: '" + getPathExpr(jsonPath) + "'");
            } else {
                return createFileFromBase64(baseName, (String) base64Content);
            }
        } else {
            throw new SlimFixtureException(false, "Non string result from json path. '" + getPathExpr(jsonPath) + "' returned: " + base64Content);
        }
    }

    @Override
    protected String createFileFromBase64(String baseName, String base64Content) {
        if (DataUrlHelper.isDataUrl(base64Content)) {
            base64Content = DataUrlHelper.getData(base64Content);
        }
        return super.createFileFromBase64(baseName, base64Content);
    }

    public Object elementOfJsonPath(int index, String path) {
        List<Object> all = listJsonPathMatches(path);
        return all.size() > index ? all.get(index) : null;
    }

    public int jsonPathCount(String path) {
        List<Object> all = listJsonPathMatches(path);
        return all.size();
    }

    public ArrayList<Object> listJsonPathMatches(String path) {
        String responseString = getResponseBody();
        String jsonPath = getPathExpr(path);
        List<Object> results = getPathHelper().getAllJsonPath(responseString, jsonPath);
        return results instanceof ArrayList ? (ArrayList<Object>) results : new ArrayList<>(results);
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
     *
     * @param expr expression to evaluate.
     * @return list containing all results of expression evaluation against last response received, null if there were no matches.
     * @throws RuntimeException if no valid response was available or Json Path could not be evaluated.
     */
    public String allJsonPathMatches(String expr) {
        String result = null;
        List<Object> allJsonPath = listJsonPathMatches(expr);
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
     *
     * @param path  the jsonPath to locate the key whose value needs changing
     * @param value the new value to set
     */
    public void setJsonPathTo(String path, String value) {
        String jsonStr = getResponseBody();
        String jsonPath = getPathExpr(path);
        String newResponse = getPathHelper().updateJsonPathWithValue(jsonStr, jsonPath, value);
        getResponse().setResponse(newResponse);
    }

    public boolean repeatUntilJsonPathIs(final String jsonPath, final Object expectedValue) {
        RepeatCompletion completion;
        if (expectedValue == null) {
            completion = new RepeatLastCall() {
                @Override
                public boolean isFinished() {
                    return jsonPath(jsonPath) == null;
                }
            };
        } else {
            Object cleanedExpected = cleanupValue(expectedValue);
            completion = new RepeatLastCall() {
                @Override
                public boolean isFinished() {
                    Object actual = jsonPath(jsonPath);
                    return compareActualToExpected(cleanedExpected, actual);
                }
            };
        }
        return repeatUntil(completion);
    }

    public boolean jsonPathExists(String path) {
        return getPathHelper().jsonPathExists(getResponseBody(), getPathExpr(path));
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

    protected JsonPathHelper getPathHelper() {
        return getEnvironment().getJsonPathHelper();
    }
}
