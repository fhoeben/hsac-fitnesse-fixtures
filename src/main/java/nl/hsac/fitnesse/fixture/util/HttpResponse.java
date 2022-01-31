package nl.hsac.fitnesse.fixture.util;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wrapper around HTTP response (and request).
 */
public class HttpResponse {
    private final static Map<String, HttpResponse> INSTANCES = new ConcurrentHashMap<>();

    private Map<String, Object> requestHeaders = new LinkedHashMap<>();
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
        if (statusCode == 0) {
            throw new NonValidResponseReceivedException("Status code is 0. Probably no response was received.");
        }
        if (statusCode < 100) {
            throw new NonValidResponseReceivedException("Status code is less than 100: " + statusCode);
        }
        if (statusCode >= 400 && statusCode <= 499) {
            throw new NonValidResponseReceivedException("Server reported client error: " + statusCode);
        }
        if (statusCode >= 500 && statusCode <= 599) {
            throw new NonValidResponseReceivedException("Server error returned: " + statusCode);
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
     * @return actual headers sent (these will contain the requested headers and some implicit).
     */
    public Map<String, Object> getRequestHeaders() {
        return requestHeaders;
    }

    /**
     * Adds value to actual headers sent (there may be multiple values per name).
     * @param headerName name of header to add.
     * @param headerValue value to add.
     * @return all current values for name.
     */
    public Object addRequestHeader(String headerName, String headerValue) {
        return addValue(getRequestHeaders(), headerName, headerValue);
    }

    /**
     * @return headers in response.
     */
    public Map<String, Object> getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * Adds value to headers received (there may be multiple values per name).
     * @param headerName name of header to add.
     * @param headerValue value to add.
     * @return all current values for name.
     */
    public Object addResponseHeader(String headerName, String headerValue) {
        return addValue(getResponseHeaders(), headerName, headerValue);
    }

    protected Object addValue(Map<String, Object> headers, String name, String value) {
        Object result;
        Object existingValue = headers.get(name);
        if (existingValue == null) {
            headers.put(name, value);
            result = value;
        } else if (existingValue instanceof Collection) {
            ((Collection) existingValue).add(value);
            result = existingValue;
        } else {
            List<Object> valueList = new ArrayList<Object>();
            valueList.add(existingValue);
            valueList.add(value);
            headers.put(name, valueList);
            result = valueList;
        }
        return result;
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
