package nl.hsac.fitnesse.fixture.util;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wrapper around HTTP response (and request).
 */
public class HttpResponse {
    private final static Map<String, HttpResponse> INSTANCES = new ConcurrentHashMap<>();

    private Map<String, Object> responseHeaders = new LinkedHashMap<>();
    private String method;
    private String request;
    protected String response;
    private int statusCode;
    private CookieStore cookieStore;
    private long responseTime = -1;

    /**
     * @throws RuntimeException if no valid response is available
     */
    public void validResponse() {
        if (statusCode >= 500 && statusCode <= 599) {
            throw new RuntimeException("Server error returned: " + statusCode);
        }
        if (statusCode >= 400 && statusCode <= 499) {
            throw new RuntimeException("Server reported client error: " + statusCode);
        }
    }

    /**
     * @return the request
     */
    public String getRequest() {
        return request;
    }

    /**
     * @param aRequest the request to set
     */
    public void setRequest(String aRequest) {
        request = aRequest;
    }

    /**
     * @return the response
     */
    public String getResponse() {
        return response;
    }

    /**
     * @param aResponse the response to set
     */
    public void setResponse(String aResponse) {
        response = aResponse;
    }

    /**
     * @return the statusCode
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * @param aStatusCode the statusCode to set
     */
    public void setStatusCode(int aStatusCode) {
        statusCode = aStatusCode;
    }

    /**
     * @return headers in response.
     */
    public Map<String, Object> getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * @return cookie store for this request/response
     */
    public CookieStore getCookieStore() {
        return cookieStore;
    }

    /**
     * @param cookieName name of cookie to be found in cookie store
     * @return cookie found, if any, null otherwise.
     */
    public Cookie getCookieNamed(String cookieName) {
        Cookie result = null;
        if (cookieStore != null) {
            for (Cookie cookie : getCookieStore().getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    result = cookie;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Sets cookie store to use for this request response
     * @param cookieStore cookie store for this request/response
     */
    public void setCookieStore(CookieStore cookieStore) {
        this.cookieStore = cookieStore;
    }

    /**
     * @return response time in ms for call.
     */
    public long getResponseTime() {
        return responseTime;
    }

    /**
     * Sets response time for obtaining this response.
     * @param responseTime response time in ms for call.
     */
    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void cloneValues(HttpResponse resp) {
        setCookieStore(resp.getCookieStore());
        setRequest(resp.getRequest());
        setMethod(resp.getMethod());
        getResponseHeaders().putAll(resp.getResponseHeaders());
        setStatusCode(resp.getStatusCode());
        setResponse(resp.getResponse());
        setResponseTime(resp.getResponseTime());
    }

    @Override
    public String toString() {
        // toString() is not normally called on responses,
        // but Fitnesse will if it is to be stored in a parameter
        // we make sure these could later be retrieved
        // see also: parse()
        String result = super.toString();
        INSTANCES.put(result, this);
        return result;
    }

    /**
     * Returns response toString() was called on previously.
     * @param value toString() of response being searched.
     * @return response if one is known, null otherwise.
     */
    public static HttpResponse parse(String value) {
        return INSTANCES.get(value);
    }

    /**
     * Clears set of known responses (that can be returned by parse()).
     */
    public static void clearInstances() {
        INSTANCES.clear();
    }
}