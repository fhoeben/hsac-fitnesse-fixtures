package nl.hsac.fitnesse.fixture.slim;

import freemarker.template.Template;
import nl.hsac.fitnesse.fixture.util.BinaryHttpResponse;
import nl.hsac.fitnesse.fixture.util.HttpResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fixture to make HTTP requests using Slim scripts and/or scenarios.
 */
public class HttpTest extends SlimFixtureWithMap {
    /** Default content type for posts and puts. */
    public final static String DEFAULT_POST_CONTENT_TYPE = "application/x-www-form-urlencoded; charset=UTF-8";

    private String downloadBase = new File(filesDir, "downloads").getPath() + "/";
    private final Map<String, Object> headerValues = new LinkedHashMap<>();
    private boolean storeCookies = false;
    private HttpResponse response = createResponse();
    private String template;
    private boolean explicitContentTypeSet = false;
    private String contentType = DEFAULT_POST_CONTENT_TYPE;
    private String lastUrl = null;
    private String lastMethod = null;

    /**
     * Sets template to use.
     * @param aTemplate name of template file (relative to 'templates' directory on classpath)
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
     * Enables content compression support in the current environment (i.e. for the entire test run)
     */
    public void enableCompression() {
        getEnvironment().enableHttpClientCompression();
    }

    /**
     * Disables content compression support in the current environment (i.e. for the entire test run)
     */
    public void disableCompression() {
        getEnvironment().disableHttpClientCompression();
    }

    /**
     * Disables SSL certificate verification in the current environment (i.e. for the entire test run)
     */
    public void disableSSLVerification() {
        getEnvironment().disableHttpClientSSLVerification();
    }

    /**
     * Enables SSL certificate verification in the current environment (i.e. for the entire test run)
     */
    public void enableSSLVerification() {
        getEnvironment().enableHttpClientSSLVerification();
    }

    /**
     * Stores value to be passed as headers.
     * @param value value to be passed.
     * @param name name to use this value for.
     */
    public void setValueForHeader(Object value, String name) {
        getMapHelper().setValueForIn(value, name, headerValues);
    }

    /**
     * Clears a header value previously set.
     * @param name value to remove.
     * @return true if value was present.
     */
    public boolean clearHeaderValue(String name) {
        String cleanName = cleanupValue(name);
        boolean result = headerValues.containsKey(cleanName);
        headerValues.remove(cleanName);
        return result;
    }

    /**
     * Clears all header values previously set.
     */
    public void clearHeaderValues() {
        headerValues.clear();
    }

    /**
     * Adds all values in the supplied map to the current header values.
     * @param map to obtain values from.
     */
    public void copyHeaderValuesFrom(Map<String, Object> map) {
        getMapHelper().copyValuesFromTo(map, headerValues);
    }

    /**
     * Allows subclasses access to the header values.
     * @return header values.
     */
    protected Map<String, Object> getHeaderValues() {
        return headerValues;
    }

    //// methods to support usage in dynamic decision tables

    /**
     * Called before next row is executed. (Clears all current and header values.)
     */
    @Override
    public void reset() {
        clearValues();
        clearHeaderValues();
    }

    private static final Pattern HEADER_KEY_PATTERN = Pattern.compile("header:\\s*(\\.+)");
    /**
     * Sets a value.
     * @param key (possibly nested) key to set value for.
     * @param value value to be stored.
     */
    public void set(String key, Object value) {
        Matcher m = HEADER_KEY_PATTERN.matcher(key);
        if (m.matches()) {
            String headerKey = m.group(1);
            setValueForHeader(value, headerKey);
        } else {
            super.set(key, value);
        }
    }

    //// end: methods to support usage in dynamic decision tables

    protected String createFileFromBase64(String baseName, String base64Content) {
        Base64Fixture base64Fixture = getBase64Fixture();
        return base64Fixture.createFrom(baseName, base64Content);
    }

    /**
     * Sends HTTP POST template with current values to service endpoint.
     * @param serviceUrl service endpoint to send request to.
     * @return true if call could be made and response did not indicate error.
     */
    public boolean postTemplateTo(String serviceUrl) {
        return postTemplateTo(serviceUrl, getContentType());
    }

    /**
     * Sends HTTP POST template with current values to service endpoint.
     * @param serviceUrl service endpoint to send request to.
     * @param aContentType content type to use for post.
     * @return true if call could be made and response did not indicate error.
     */
    public boolean postTemplateTo(String serviceUrl, String aContentType) {
        return sendTemplateTo(serviceUrl, aContentType, "POST");
    }

    /**
     * Sends HTTP POST body to service endpoint.
     * @param body content to post
     * @param serviceUrl service endpoint to send body to.
     * @return true if call could be made and response did not indicate error.
     */
    public boolean postTo(String body, String serviceUrl) {
        String cleanedBody = cleanupBody(body);
        return postToImpl(cleanedBody, serviceUrl);
    }

    /**
     * Sends HTTP DELETE body to service endpoint.
     * @param body content to delete
     * @param serviceUrl service endpoint to send body to.
     * @return true if call could be made and response did not indicate error.
     */
    public boolean deleteWith(String serviceUrl, String body) {
        String cleanedBody = cleanupBody(body);
        return deleteToImpl(cleanedBody, serviceUrl);
    }

    /**
     * Sends a file by HTTP POST body to service endpoint.
     * @param fileName fileName to post
     * @param serviceUrl service endpoint to send body to.
     * @return true if call could be made and response did not indicate error.
     */
    public boolean postFileTo(String fileName, String serviceUrl) {
        return postFileToImpl(fileName, serviceUrl);
    }

    /**
     * Sends all values (url encoded) using post.
     * @param serviceUrl service endpoint to send values to.
     * @return true if call could be made and response did not indicate error.
     */
    public boolean postValuesTo(String serviceUrl) {
        String body = urlEncodeCurrentValues();
        return postToImpl(body, serviceUrl);
    }

    protected boolean postToImpl(String body, String serviceUrl) {
        return sendToImpl(body, serviceUrl, getContentType(), "POST");
    }

    protected boolean deleteToImpl(String body, String serviceUrl) {
        return sendToImpl(body, serviceUrl, getContentType(), "DELETE");
    }

    protected boolean sendToImpl(String body, String serviceUrl, String aContentType, String method) {
        boolean result;
        resetResponse();
        response.setRequest(body);
        String url = getUrl(serviceUrl);
        try {
            storeLastCall(method, serviceUrl);
            switch (method) {
                case "POST":
                    getEnvironment().doHttpPost(url, response, headerValues, aContentType);
                    break;
                case "PUT":
                    getEnvironment().doHttpPut(url, response, headerValues, aContentType);
                    break;
                case "DELETE":
                    getEnvironment().doDelete(url, response, headerValues, aContentType);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported method: " + method);
            }
        } catch (Throwable t) {
            throw new StopTestException("Unable to get response from " + method + " to: " + url, t);
        }
        result = postProcessResponse();
        return result;
    }

    protected boolean postFileToImpl(String fileName, String serviceUrl) {
        boolean result;
        resetResponse();
        String url = getUrl(serviceUrl);

        String filePath = getFilePathFromWikiUrl(fileName);
        File file = new File(filePath);
        if (!file.exists()) {
            throw new StopTestException(false, "File " + filePath + " not found.");
        }

        try {
            response.setRequest(fileName);
            storeLastCall("POST_FILE", serviceUrl);
            getEnvironment().doHttpFilePost(url, response, headerValues, file);
        } catch (Throwable t) {
            throw new StopTestException("Unable to get response from POST to: " + url, t);
        }
        result = postProcessResponse();
        return result;
    }

    /**
     * Sends HTTP PUT template with current values to service endpoint.
     * @param serviceUrl service endpoint to send request to.
     * @return true if call could be made and response did not indicate error.
     */
    public boolean putTemplateTo(String serviceUrl) {
        return sendTemplateTo(serviceUrl, getContentType(), "PUT");
    }

    /**
     * Sends HTTP DELETE template with current values to service endpoint.
     * @param serviceUrl service endpoint to send request to.
     * @return true if call could be made and response did not indicate error.
     */
    public boolean deleteWithTemplate(String serviceUrl) {
        return sendTemplateTo(serviceUrl, getContentType(), "DELETE");
    }

    /**
     * Sends HTTP method call template with current values to service endpoint.
     * @param serviceUrl service endpoint to send request to.
     * @param aContentType content type to use for post.
     * @param method HTTP method to use
     * @return true if call could be made and response did not indicate error.
     */
    public boolean sendTemplateTo(String serviceUrl, String aContentType, String method) {
        boolean result;
        resetResponse();
        if (template == null) {
            throw new StopTestException("No template available to use in " + method);
        } else {
            String url = getUrl(serviceUrl);
            try {
                storeLastCall(method, serviceUrl);
                switch (method) {
                    case "POST":
                        getEnvironment().doHttpPost(url, template, getCurrentValues(), response, headerValues, aContentType);
                        break;
                    case "PUT":
                        getEnvironment().doHttpPut(url, template, getCurrentValues(), response, headerValues, aContentType);
                        break;
                    case "DELETE":
                        getEnvironment().doDelete(url, template, getCurrentValues(), response, headerValues, aContentType);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported method: " + method);
                }
            } catch (Throwable t) {
                throw new StopTestException("Unable to get response from " + method + " to: " + url, t);
            }
            result = postProcessResponse();
        }
        return result;
    }

    /**
     * Sends HTTP PUT body to service endpoint.
     * @param body content to put
     * @param serviceUrl service endpoint to send body to.
     * @return true if call could be made and response did not indicate error.
     */
    public boolean putTo(String body, String serviceUrl) {
        String cleanedBody = cleanupBody(body);
        return putToImpl(cleanedBody, serviceUrl);
    }

    /**
     * Sends all values (url encoded) using put.
     * @param serviceUrl service endpoint to send values to.
     * @return true if call could be made and response did not indicate error.
     */
    public boolean putValuesTo(String serviceUrl) {
        String body = urlEncodeCurrentValues();
        return putToImpl(body, serviceUrl);
    }

    protected boolean putToImpl(String body, String serviceUrl) {
        return sendToImpl(body, serviceUrl, getContentType(), "PUT");
    }

    protected String cleanupBody(String body) {
        return getEnvironment().getHtmlCleaner().cleanupPreFormatted(body);
    }

    /**
     * Sends HTTP GET to service endpoint to retrieve content.
     * @param serviceUrl service endpoint to get content from.
     * @return true if call could be made and response did not indicate error.
     */
    public boolean getFrom(String serviceUrl) {
        return getImpl(serviceUrl, true);
    }

    /**
     * Sends HTTP GET to service endpoint to retrieve content, not following a redirect if sent.
     * @param serviceUrl service endpoint to get content from.
     * @return true if call could be made and response did not indicate error.
     */
    public boolean getFromNoRedirect(String serviceUrl) {
        return getImpl(serviceUrl, false);
    }

    protected boolean getImpl(String serviceUrl, boolean followRedirect) {
        boolean result;
        resetResponse();
        String url = createUrlWithParams(serviceUrl);
        try {
            String method;
            if (followRedirect) {
                method = "GET";
            } else {
                method = "GET_NO_REDIRECT";
            }
            storeLastCall(method, serviceUrl);
            getEnvironment().doGet(url, response, headerValues, followRedirect);
        } catch (Throwable t) {
            throw new StopTestException("Unable to GET response from: " + url, t);
        }
        result = postProcessResponse();
        return result;
    }

    /**
     * Downloads binary content from specified url.
     * @param serviceUrl url to download from
     * @return link to downloaded file
     */
    public String getFileFrom(String serviceUrl) {
        resetResponse();
        String url = createUrlWithParams(serviceUrl);

        BinaryHttpResponse resp = new BinaryHttpResponse();
        resp.setCookieStore(response.getCookieStore());
        getEnvironment().doGet(url, resp, headerValues);
        response.cloneValues(resp);

        byte[] content = resp.getResponseContent();
        if (content == null) {
            try {
                content = resp.getResponse().getBytes("utf-8");
            } catch (UnsupportedEncodingException e) {
                // will not happen
            }
        }
        String fileName = resp.getFileName();
        if (StringUtils.isEmpty(fileName)) {
            fileName = "download";
        }
        return createFile(downloadBase, fileName, content);
    }

    /**
     * Sends HTTP HEAD to service endpoint.
     * @param serviceUrl service endpoint to delete.
     * @return true if call could be made and response did not indicate error.
     */
    public boolean headFrom(String serviceUrl) {
        boolean result;
        resetResponse();
        String url = createUrlWithParams(serviceUrl);
        try {
            storeLastCall("HEAD", serviceUrl);
            getEnvironment().doHead(url, response, headerValues);
        } catch (Throwable t) {
            throw new StopTestException("Unable to HEAD: " + url, t);
        }
        result = postProcessResponse();
        return result;
    }

    /**
     * Sends HTTP DELETE to service endpoint.
     * @param serviceUrl service endpoint to delete.
     * @return true if call could be made and response did not indicate error.
     */
    public boolean delete(String serviceUrl) {
        boolean result;
        resetResponse();
        String url = createUrlWithParams(serviceUrl);
        try {
            storeLastCall("DELETE", serviceUrl);
            getEnvironment().doDelete(url, response, headerValues);
        } catch (Throwable t) {
            throw new StopTestException("Unable to DELETE: " + url, t);
        }
        result = postProcessResponse();
        return result;
    }

    protected void resetResponse() {
        CookieStore cookieStore = null;
        if (storeCookies) {
            cookieStore = getResponse().getCookieStore();
            if (cookieStore == null) {
                cookieStore = new BasicCookieStore();
            }
        }
        response = createResponse();
        if (storeCookies) {
            response.setCookieStore(cookieStore);
        }
    }

    protected String createUrlWithParams(String serviceUrl) {
        String baseUrl = getUrl(serviceUrl);
        if (!getCurrentValues().isEmpty()) {
            if (baseUrl.contains("?") && !baseUrl.endsWith("?")) {
                baseUrl += "&";
            }
            if (!baseUrl.contains("?")) {
                baseUrl += "?";
            }
            baseUrl += urlEncodeCurrentValues();
        }
        return baseUrl;
    }

    protected String urlEncodeCurrentValues() {
        StringBuilder sb = new StringBuilder();
        addUrlEncodedKeyValues(sb, "", getCurrentValues());
        return sb.toString();
    }

    private void addUrlEncodedKeyValues(StringBuilder sb, String prefix, Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof List) {
                List values = (List) value;
                for (Object v : values) {
                    addEncodedKeyValue(sb, key, v);
                }
            } else if (value instanceof Map) {
                addUrlEncodedKeyValues(sb, key + ".", (Map) value);
            } else {
                addEncodedKeyValue(sb, key, value);
            }
        }
    }

    private void addEncodedKeyValue(StringBuilder sb, String key, Object value) {
        if (sb.length() != 0) {
            sb.append("&");
        }
        sb.append(urlEncode(key));
        if (value != null) {
            sb.append("=");
            sb.append(urlEncode(value.toString()));
        }
    }

    protected String urlEncode(String str) {
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
     * @return request sent last time.
     */
    public String request() {
        return safeFormatValue(response.getRequest());
    }

    /**
     * @return response received last time postTo(), delete() or getFrom() was called.
     */
    public String response() {
        return safeFormatValue(response.getResponse());
    }

    /**
     * Internal method to format a value, which will just return the 'raw' value if there is a problem formatting.
     * @param value value to format
     * @return formatted value
     */
    protected final String safeFormatValue(String value) {
        String result;
        try {
            result = formatValue(value);
        } catch (Exception e) {
            result = value;
        }
        return result;
    }

    /**
     * Method that will take care of formatting a value, which may be overridden in subclasses.
     * This implementation just returns value.
     * @param value value to format
     * @return formatted value
     */
    protected String formatValue(String value) {
        return value;
    }

    /**
     * @return HTML response received last time postTo() or get() was called.
     */
    public String htmlResponse() {
        String content = response.getResponse();
        content = "<div>" + content + "</div>";
        return content;
    }

    /**
     * @return response time in ms for call to service.
     */
    public long responseTime() {
        return getResponse().getResponseTime();
    }

    /**
     * @return http status received in response to last request.
     */
    public int responseStatus() {
        return response.getStatusCode();
    }

    /**
     * @return headers received with response to last request.
     */
    public Map<String, Object> responseHeaders() {
        return response.getResponseHeaders();
    }

    /**
     * @param headerName name of response header.
     * @return value of header in last response (may be a list if the saame header name was sent multiple times
     * (e.g. Set-Cookie).
     */
    public Object responseHeader(String headerName) {
        return responseHeaders().get(headerName);
    }

    public void setStoreCookies(boolean storeCookies) {
        this.storeCookies = storeCookies;
    }

    /**
     * Adds all current Selenium cookies to this fixture's cookie store.
     * This will also ensure this class will store cookies (otherwise copying the cookies has no purpose).
     */
    public void copyBrowserCookies() {
        setStoreCookies(true);
        getEnvironment().addSeleniumCookies(getResponse());
    }

    /**
     * @return name->value of cookies in the cookie store.
     */
    public Map<String, String> cookieValues() {
        Map<String, String> result = null;
        CookieStore cookies = getResponse().getCookieStore();
        if (cookies != null) {
            result = new LinkedHashMap<>();
            for (Cookie cookie : cookies.getCookies()) {
                result.put(cookie.getName(), cookie.getValue());
            }
        }
        return result;
    }

    /**
     * @param cookieName name of cookie.
     * @return value of cookie in the cookie store.
     */
    public String cookieValue(String cookieName) {
        String result = null;
        Cookie cookie = getCookie(cookieName);
        if (cookie != null) {
            result = cookie.getValue();
        }
        return result;
    }

    /**
     * @param cookieName name of cookie.
     * @return domain of cookie in the cookie store.
     */
    public String cookieDomain(String cookieName) {
        String result = null;
        Cookie cookie = getCookie(cookieName);
        if (cookie != null) {
            result = cookie.getDomain();
        }
        return result;
    }

    /**
     * @param cookieName name of cookie.
     * @return path of cookie in the cookie store.
     */
    public String cookiePath(String cookieName) {
        String result = null;
        Cookie cookie = getCookie(cookieName);
        if (cookie != null) {
            result = cookie.getPath();
        }
        return result;
    }

    /**
     * @param cookieName name of cookie.
     * @return whether cookie in the cookie store is persistent.
     */
    public Boolean cookieIsPersistent(String cookieName) {
        Boolean result = null;
        Cookie cookie = getCookie(cookieName);
        if (cookie != null) {
            result = cookie.isPersistent();
        }
        return result;
    }

    /**
     * @param cookieName name of cookie.
     * @return whether cookie in the cookie store requires a secure connection.
     */
    public Boolean cookieIsSecure(String cookieName) {
        Boolean result = null;
        Cookie cookie = getCookie(cookieName);
        if (cookie != null) {
            result = cookie.isSecure();
        }
        return result;
    }

    /**
     * @param cookieName name of cookie.
     * @return whether cookie in the cookie store is http-only (not accessible to Javascript).
     */
    public Boolean cookieIsHttpOnly(String cookieName) {
        return cookieAttribute(cookieName, "httponly") != null;
    }

    /**
     * @param cookieName name of cookie.
     * @param attributeName name of attribute.
     * @return value of attribute for cookie.
     */
    public String cookieAttribute(String cookieName, String attributeName) {
        String result = null;
        Cookie cookie = getCookie(cookieName);
        if (cookie instanceof BasicClientCookie) {
            result = ((BasicClientCookie) cookie).getAttribute(attributeName.toLowerCase(Locale.ENGLISH));
        }
        return result;
    }

    private Cookie getCookie(String cookieName) {
        return getResponse().getCookieNamed(cookieName);
    }

    /**
     * Removes all cookies from the cookie store.
     */
    public void clearCookies() {
        getResponse().getCookieStore().clear();
    }

    protected HttpResponse getResponse() {
        return response;
    }

    protected HttpResponse createResponse() {
        return new HttpResponse();
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String aContentType) {
        explicitContentTypeSet = true;
        contentType = aContentType;
    }

    public void setBasicAuthorizationHeaderForUserAndPassword(String user, String password) {
        String credential = user + ":" + password;
        try {
            String base64credential = Base64.getEncoder().encodeToString(credential.getBytes("ISO-8859-1"));
            setValueForHeader("Basic " + base64credential, "Authorization");
        } catch (UnsupportedEncodingException e) {
            throw new SlimFixtureException("ISO-8859-1 encoding unavailable!", e);
        }
    }

    public boolean isExplicitContentTypeSet() {
        return explicitContentTypeSet;
    }

    // Polling
    public boolean repeatUntilResponseStatusIs(final int expectedStatus) {
        return repeatUntil(
                new RepeatLastCall() {
                    @Override
                    public boolean isFinished() {
                        return responseStatus() == expectedStatus;
                    }
                });
    }

    public boolean repeatUntilResponseIs(final String expectedResponse) {
        RepeatCompletion completion;
        if (expectedResponse == null) {
            completion = new RepeatLastCall() {
                @Override
                public boolean isFinished() {
                    return response() == null;
                }
            };
        } else {
            completion = new RepeatLastCall() {
                @Override
                public boolean isFinished() {
                    Object actual = response();
                    return compareActualToExpected(expectedResponse, actual);
                }
            };
        }
        return repeatUntil(completion);
    }

    public boolean repeatUntilHeaderIs(final String header, final Object expectedValue) {
        RepeatCompletion completion;
        if (expectedValue == null) {
            completion = new RepeatLastCall() {
                @Override
                public boolean isFinished() {
                    return responseHeader(header) == null;
                }
            };
        } else {
            completion = new RepeatLastCall() {
                @Override
                public boolean isFinished() {
                    Object actual = responseHeader(header);
                    return compareActualToExpected(expectedValue, actual);
                }
            };
        }
        return repeatUntil(completion);
    }

    protected void repeatLastCall() {
        if (lastMethod == null) {
            throw new SlimFixtureException(false, "First make a call before trying to repeat one.");
        }
        switch (lastMethod) {
            case "GET":
                getImpl(lastUrl, true);
                break;
            case "HEAD":
                headFrom(lastUrl);
                break;
            case "POST":
                postToImpl(response.getRequest(), lastUrl);
                break;
            case "PUT":
                putToImpl(response.getRequest(), lastUrl);
                break;
            case "DELETE":
                if (lastUrl.equals(response.getRequest())) {
                    delete(lastUrl);
                } else {
                    deleteToImpl(response.getRequest(), lastUrl);
                }
                break;
            case "GET_NO_REDIRECT":
                getImpl(lastUrl, false);
                break;
            case "POST_FILE":
                postFileToImpl(response.getRequest(), lastUrl);
                break;
            default:
                throw new SlimFixtureException(false, "Repeat of method: " + lastMethod + " not configured.");
        }
    }

    protected void storeLastCall(String method, String url) {
        lastMethod = method;
        lastUrl = url;
    }

    protected abstract class RepeatLastCall implements RepeatCompletion {

        protected boolean compareActualToExpected(Object expected, Object actual) {
            boolean result;
            if (actual == null) {
                result = expected.equals("null");
            } else {
                result = expected.equals(actual)
                        || expected.toString().equals(actual.toString());
            }
            return result;
        }

        @Override
        public void repeat() {
            repeatLastCall();
        }
    }
    // Polling
}
