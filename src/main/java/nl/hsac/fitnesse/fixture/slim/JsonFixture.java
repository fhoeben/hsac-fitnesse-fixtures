package nl.hsac.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.util.DataUrlHelper;
import nl.hsac.fitnesse.fixture.util.JsonPathHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Fixture to work with JSON (strings).
 */
public class JsonFixture extends SlimFixture {
    private String content;

    /**
     * @param filename JSON file to be loaded.
     * @return true
     */
    public boolean loadFile(String filename) {
        content = getFileFixture().textIn(filename);
        return true;
    }

    /**
     * @param json JSON content to be loaded.
     * @return true
     */
    public boolean load(String json) {
        content = cleanupValue(json);
        return true;
    }

    /**
     * @return formatted loaded JSON content.
     */
    public String object() {
        String formatted = content;
        if (content != null) {
            String trimmed = content.trim();
            if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                formatted = getEnvironment().getHtmlForJson(trimmed);
            }
        }
        return formatted;
    }

    public Object jsonPath(String path) {
        String jsonPath = getPathExpr(path);
        return getPathHelper().getJsonPath(content, jsonPath);
    }

    /**
     * @param baseName base of filename to generate (a number might be added to the name to make it unique).
     * @param jsonPath expression to evaluate.
     * @return link to created file.
     */
    public String createFileFromBase64ContentOf(String baseName, String jsonPath) {
        Object base64Content = jsonPath(jsonPath);
        if (base64Content instanceof String) {
            String base64String = (String) base64Content;
            if ("".equals(base64Content)) {
                throw new SlimFixtureException(false, "No content from json path: '" + getPathExpr(jsonPath) + "'");
            } else {
                if (DataUrlHelper.isDataUrl(base64String)) {
                    base64String = DataUrlHelper.getData(base64String);
                }
                return createFileFromBase64(baseName, base64String);
            }
        } else {
            throw new SlimFixtureException(false, "Non string result from json path. '" + getPathExpr(jsonPath) + "' returned: " + base64Content);
        }
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
        String jsonPath = getPathExpr(path);
        List<Object> results = getPathHelper().getAllJsonPath(content, jsonPath);
        return results instanceof ArrayList? (ArrayList<Object>) results : new ArrayList<>(results);
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

    /**
     * Update a value in a the content by supplied jsonPath
     * @param path the jsonPath to locate the key whose value needs changing
     * @param value the new value to set
     */
    public void setJsonPathTo(String path, Object value) {
        Object cleanValue = cleanupValue(value);
        String jsonPath = getPathExpr(path);
        String newContent = getPathHelper().updateJsonPathWithValue(content, jsonPath, cleanValue);
        content = newContent;
    }

    public boolean jsonPathExists(String path) {
        return getPathHelper().jsonPathExists(content, getPathExpr(path));
    }

    protected String getContent() {
        return content;
    }

    protected String createFileFromBase64(String baseName, String base64Content) {
        Base64Fixture base64Fixture = getBase64Fixture();
        return base64Fixture.createFrom(baseName, base64Content);
    }

    protected Base64Fixture getBase64Fixture() {
        return new Base64Fixture();
    }

    protected FileFixture getFileFixture() {
        return new FileFixture();
    }

    protected JsonPathHelper getPathHelper() {
        return getEnvironment().getJsonPathHelper();
    }
}
