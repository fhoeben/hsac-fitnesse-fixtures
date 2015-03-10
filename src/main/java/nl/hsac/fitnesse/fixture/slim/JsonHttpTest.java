package nl.hsac.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.util.JsonFormatter;
import nl.hsac.fitnesse.fixture.util.JsonPathHelper;
import org.apache.http.Consts;
import org.apache.http.entity.ContentType;

import java.util.List;
import java.util.Map;

/**
 * Fixture to make Http calls and interpret the result as JSON.
 */
public class JsonHttpTest extends HttpTest {
    private final JsonFormatter formatter = new JsonFormatter();
    private final JsonPathHelper pathHelper = new JsonPathHelper();

    public JsonHttpTest() {
        setContentType(ContentType.create(ContentType.APPLICATION_FORM_URLENCODED.getMimeType(), Consts.UTF_8));
    }

    /**
     * @return response received last time postTo() or getFrom() was called.
     */
    @Override
    public String response() {
        return formatValue(super.response());
    }

    private String formatValue(String value) {
        return getEnvironment().getHtml(formatter, value);
    }

    public void setValuesFor(String values, String name) {
        String[] valueArrays = values.split("\\s*,\\s*");
        getCurrentValues().put(name, valueArrays);
    }

    public boolean postValuesTo(String serviceUrl) {
        String body = urlEncodeCurrentValues();
        return postToImpl(body, serviceUrl);
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

    protected String urlEncodeCurrentValues() {
        boolean isFirst = true;
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : getCurrentValues().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String[]) {
                String[] values = (String[]) value;
                for (String v : values) {
                    addEncodedKeyValue(sb, isFirst, key, v);
                    isFirst = false;
                }
            } else {
                addEncodedKeyValue(sb, isFirst, key, value);
                isFirst = false;
            }
        }
        return sb.toString();
    }

    private boolean addEncodedKeyValue(StringBuilder sb, boolean isFirst, String key, Object value) {
        if (!isFirst) {
            sb.append("&");
        }
        sb.append(urlEncode(key));
        sb.append("=");
        if (value != null) {
            sb.append(urlEncode(value.toString()));
        }
        return isFirst;
    }

    @Override
    protected String urlEncode(String str) {
        String strNoSpaces = str.replace(" ", "+");
        return super.urlEncode(strNoSpaces);
    }
}
