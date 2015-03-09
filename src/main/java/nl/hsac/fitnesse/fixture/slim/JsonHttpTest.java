package nl.hsac.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.util.JsonFormatter;
import nl.hsac.fitnesse.fixture.util.JsonPathHelper;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.Consts;
import org.apache.http.entity.ContentType;

public class JsonHttpTest extends HttpTest {
    private final JsonFormatter formatter = new JsonFormatter();
    private final JsonPathHelper pathHelper = new JsonPathHelper();

    public JsonHttpTest() {
        setContentType(ContentType.create(ContentType.APPLICATION_FORM_URLENCODED.getMimeType(), Consts.UTF_8));
    }
    /**
     * @return request sent last time postTo() or getFrom() was called.
     */
    @Override
    public String request() {
        return formatValue(super.request());
    }

    /**
     * @return response received last time postTo() or getFrom() was called.
     */
    @Override
    public String response() {
        return formatValue(super.response());
    }

    private String formatValue(String value) {
        String result = null;
        try {
            if (value != null) {
                if ("".equals(value)) {
                    result = "";
                } else {
                    String formattedResponse = formatter.format(value);
                    result = "<pre>" + StringEscapeUtils.escapeHtml4(formattedResponse) + "</pre>";
                }
            }
        } catch (Exception e) {
            result = value;
        }
        return result;
    }

    public Object jsonPath(String path) {
        String jsonPath = path;
        if (!path.startsWith("$")) {
            if (path.startsWith("[") || path.startsWith(".")) {
                jsonPath = "$" + path;
            } else {
                jsonPath = "$." + path;
            }
        }
        return pathHelper.getJsonPath(getResponse().getResponse(), jsonPath);
    }
}
