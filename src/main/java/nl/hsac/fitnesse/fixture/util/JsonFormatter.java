package nl.hsac.fitnesse.fixture.util;


import org.json.JSONObject;

/**
 * Formats JSON strings.
 */
public class JsonFormatter implements Formatter {
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
}
