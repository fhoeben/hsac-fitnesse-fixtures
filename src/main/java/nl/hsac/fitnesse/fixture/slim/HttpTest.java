package nl.hsac.fitnesse.fixture.slim;

import freemarker.template.Template;
import nl.hsac.fitnesse.fixture.util.HttpResponse;
import nl.hsac.fitnesse.fixture.web.SlimFixture;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fixture to make HTTP requests using Slim scripts and/or scenarios.
 */
public class HttpTest extends SlimFixture {
    private static final Pattern PATTERN = Pattern.compile("<a href=\"(.*?)\">(.*?)</a>(.*)", Pattern.CASE_INSENSITIVE);
    private final Map<String, String> currentValues = new LinkedHashMap<String, String>();
    private HttpResponse response = createResponse();
    private String template;

    /**
     * Sets template to use.
     * @param aTemplate name of template file
     * @return true if template could be found.
     */
    public boolean template(String aTemplate) {
        boolean result = false;
        Template t = getEnvironment().getTemplate(aTemplate);
        if (t != null) {
            template = aTemplate;
            result = true;
        }
        return result;
    }

    /**
     * Stores value to be passed to template.
     * @param value value to be passed.
     * @param name name to use this value for.
     */
    public void setValueFor(String value, String name) {
        currentValues.put(name, value);
    }

    /**
     * Clears a values previously set.
     * @param name value to remove.
     * @return true if value was present.
     */
    public boolean clearValue(String name) {
        boolean result = currentValues.containsKey(name);
        currentValues.remove(name);
        return result;
    }

    /**
     * Clears all values previously set.
     */
    public void clearValues() {
        currentValues.clear();
    }

    /**
     * Sends HTTP POST template with current values to service endpoint.
     * @param serviceUrl service endpoint to send XML to.
     * @return true if call could be made and response did not indicate error.
     */
    public boolean postTo(String serviceUrl) {
        boolean result = false;
        response = createResponse();
        if (template != null) {
            getEnvironment().doHttpPost(getUrl(serviceUrl), template, currentValues, response);
            result = postProcessResponse();
        }
        return result;
    }

    /**
     * Sends HTTP GET to service endpoint to retrieve XML.
     * @param serviceUrl service endpoint to send XML to.
     * @return true if call could be made and response did not indicate error.
     */
    public boolean getFrom(String serviceUrl) {
        String url = createUrlWithParams(serviceUrl);
        response = getEnvironment().doHttpGet(url);
        return postProcessResponse();
    }

    String createUrlWithParams(String serviceUrl) {
        String baseUrl = getUrl(serviceUrl);
        boolean isFirst = true;
        if (baseUrl.contains("?") && !baseUrl.endsWith("?")) {
            isFirst = false;
        }

        for (Map.Entry<String, String> entry : currentValues.entrySet()) {
            if (isFirst) {
                isFirst = false;
                if (!baseUrl.endsWith("?")) {
                    baseUrl += "?";
                }
            } else {
                baseUrl += "&";
            }
            baseUrl += urlEncode(entry.getKey()) + "=" + urlEncode(entry.getValue());
        }
        return baseUrl;
    }

    private String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    /**
     * Performs any post processing directly after retrieving response.
     * @return true if all is well, false otherwise.
     */
    protected boolean postProcessResponse() {
        return responseIsValid();
    }

    /**
     * @return true if response does not indicate an error.
     */
    public boolean responseIsValid() {
        boolean result;
        try {
            response.validResponse();
            result = true;
        } catch (RuntimeException e) {
            result = false;
        }
        return result;
    }

    /**
     * @return request sent last time postTo() was called.
     */
    public String request() {
        return response.getRequest();
    }

    /**
     * @return response received last time postTo() was called.
     */
    public String response() {
        return response.getResponse();
    }

    /**
     * @return http status received last time postTo() was called.
     */
    public int responseStatus() {
        return response.getStatusCode();
    }

    String getUrl(String htmlLink) {
        String result = htmlLink;
        Matcher matcher = PATTERN.matcher(htmlLink);
        if (matcher.matches()) {
            result = matcher.group(1) + matcher.group(3);
        }
        return result;
    }

    protected HttpResponse getResponse() {
        return response;
    }

    protected HttpResponse createResponse() {
        return new HttpResponse();
    }
}
