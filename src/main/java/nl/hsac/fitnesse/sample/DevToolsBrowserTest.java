package nl.hsac.fitnesse.sample;

import fitnesse.util.Base64;
import nl.hsac.fitnesse.fixture.slim.SlimFixtureException;
import nl.hsac.fitnesse.fixture.slim.StopTestException;
import nl.hsac.fitnesse.fixture.slim.web.BrowserTest;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v97.emulation.Emulation;
import org.openqa.selenium.devtools.v97.fetch.Fetch;
import org.openqa.selenium.devtools.v97.fetch.model.RequestPattern;
import org.openqa.selenium.devtools.v97.fetch.model.RequestStage;
import org.openqa.selenium.devtools.v97.log.Log;
import org.openqa.selenium.devtools.v97.network.Network;
import org.openqa.selenium.devtools.v97.network.model.Cookie;
import org.openqa.selenium.devtools.v97.network.model.CookiePriority;
import org.openqa.selenium.devtools.v97.network.model.CookieSameSite;
import org.openqa.selenium.devtools.v97.network.model.Headers;
import org.openqa.selenium.devtools.v97.network.model.RequestId;
import org.openqa.selenium.devtools.v97.network.model.RequestWillBeSent;
import org.openqa.selenium.devtools.v97.network.model.ResourceType;
import org.openqa.selenium.devtools.v97.network.model.ResponseReceived;
import org.openqa.selenium.devtools.v97.network.model.TimeSinceEpoch;
import org.openqa.selenium.devtools.v97.page.Page;
import org.openqa.selenium.devtools.v97.performance.Performance;
import org.openqa.selenium.devtools.v97.performance.model.Metric;
import org.openqa.selenium.devtools.v97.runtime.Runtime;
import org.openqa.selenium.devtools.v97.security.Security;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static java.util.Optional.empty;

/**
 * Examples
 * @param <T>
 */
public class DevToolsBrowserTest<T extends WebElement> extends BrowserTest<T> {
    private DevTools devTools;
    private final List<String> logEntries = new ArrayList<>();
    private final List<RequestWillBeSent> requests = new ArrayList<>();
    private final List<ResponseReceived> responses = new ArrayList<>();
    private final List<RequestPattern> requestPatternsToOverride = new ArrayList<>();

    /**
     * Experimental class leveraging selenium 4's devTools api's. Use with devTools enabled browser
     * Current api: v97
     */

    public DevToolsBrowserTest() {
        super();
        ensureDevToolsEnabledDriver();
        enableConsoleLogCapturing();
    }

    public DevToolsBrowserTest(int secondsBeforeTimeout) {
        super(secondsBeforeTimeout);
        ensureDevToolsEnabledDriver();
        enableConsoleLogCapturing();
    }

    public DevToolsBrowserTest(int secondsBeforeTimeout, boolean confirmAlertIfAvailable) {
        super(secondsBeforeTimeout, confirmAlertIfAvailable);
        ensureDevToolsEnabledDriver();
        enableConsoleLogCapturing();
    }

    private void ensureDevToolsEnabledDriver() {
        if (!(getSeleniumHelper().driver() instanceof HasDevTools)) {
            throw new StopTestException(false, "DevTools enabled Browser Test can only be used with a chromium based browser (Chrome/Edge)");
        }
        devTools = ((HasDevTools) getSeleniumHelper().driver()).getDevTools();
        devTools.createSessionIfThereIsNotOne();
        devTools.send(Network.enable(Optional.of(100000), Optional.of(100000), Optional.of(100000)));
    }

    /**
     * Inject custom request headers in each request the browser sends
     *
     * @param headers A map conaining headers as key:value
     */
    public void injectHttpHeaders(Map<String, Object> headers) {
        devTools.send(Network.setExtraHTTPHeaders(new Headers(headers)));
    }

    /**
     * Clear the browser's cache
     */
    public void clearBrowserCache() {
        devTools.send(Network.clearBrowserCache());
    }

    /**
     * Delete ALL cookies (not limited to current domain)
     */
    public void clearBrowserCookies() {
        devTools.send(Network.clearBrowserCookies());
    }

    /**
     * Get a map containing all cookies for the given URL
     *
     * @param url The url to list cookies for
     * @return a map containing the cookies as name:value pairs
     */
    public Map<String, String> getCookiesForUrl(String url) {
        Map<String, String> result = new HashMap<>();
        List<Cookie> cookies = devTools.send(Network.getCookies(Optional.of(singletonList(url))));
        for (Cookie c : cookies) {
            result.put(c.getName(), c.getValue());
        }
        return result.size() > 0 ? result : null;
    }

    /**
     * Set a cookie for a specific domain
     *
     * @param name   The name of the cookie
     * @param value  The cookie value
     * @param domain The domain the cookie is valid for
     */
    public void setCookieWithValueForDomain(String name, String value, String domain) {
        devTools.send(Network.setCookie(name, value, empty(), Optional.of(domain),
                empty(), empty(), empty(), empty(), empty(), empty(), empty(), empty(), empty()));
    }

    /**
     * Set a cookie with the possibility to set all properties
     *
     * @param cookieData a map containing key/value for name, value, url, domain, path, secure, httpOnly, sameSite, expires, priority
     *                   Only name and value are mandatory, the others are optional.
     */
    public void setCookieFromMap(Map<String, Object> cookieData) {
        devTools.send(Network.setCookie(
                String.valueOf(cookieData.get("name")),
                String.valueOf(cookieData.get("value")),
                Optional.of(String.valueOf(cookieData.get("url"))),
                Optional.of(String.valueOf(cookieData.get("domain"))),
                Optional.of(String.valueOf(cookieData.get("path"))),
                Optional.of(Boolean.valueOf(cookieData.get("secure").toString())),
                Optional.of(Boolean.valueOf(cookieData.get("httpOnly").toString())),
                Optional.of(CookieSameSite.fromString(String.valueOf(cookieData.get("sameSite")))),
                Optional.of(new TimeSinceEpoch(new BigInteger(String.valueOf(cookieData.get("expires"))))),
                Optional.of(CookiePriority.fromString(String.valueOf(cookieData.get("priority")))),
                empty(),
                empty(),
                empty()
        ));
    }

    /**
     * Toggle browser caching
     *
     * @param disable true to disable caching, false to re-enable it
     */
    public void disableBrowserCache(boolean disable) {
        devTools.send(Network.setCacheDisabled(disable));
    }

    /**
     * Mock geo location and accuracy
     *
     * @param lat      Latitude
     * @param lon      Longitude
     * @param accuracy Accuracy
     */
    public void setGeoLocationLatLonAccuracy(String lat, String lon, String accuracy) {
        devTools.send(Emulation.setGeolocationOverride(Optional.of(Double.valueOf(lat)), Optional.of(Double.valueOf(lon)), Optional.of(Integer.valueOf(accuracy))));
    }

    /**
     * Mock geo location with a default accuracy of 1.
     *
     * @param lat Latitude
     * @param lon Longitude
     */
    public void setGeoLocationLatLon(String lat, String lon) {
        setGeoLocationLatLonAccuracy(lat, lon, "1");
    }

    /**
     * Override the user agent string
     *
     * @param userAgent The useragent String to identify with
     */
    public void setUserAgent(String userAgent) {
        devTools.send(Network.setUserAgentOverride(userAgent, empty(), empty(), empty()));
    }

    /**
     * Take a screenshot of the full web page, by mocking the viewport size to the current content's size
     *
     * @param baseName The base name of the screensho file
     * @return A link to the screenshot file
     */
    public String takeFullPageScreenshot(String baseName) {
        Page.GetLayoutMetricsResponse layoutMetrics = devTools.send(Page.getLayoutMetrics());

        devTools.send(Emulation.setDeviceMetricsOverride(
                layoutMetrics.getContentSize().getWidth().intValue(),
                layoutMetrics.getContentSize().getHeight().intValue(), 1, false,
                empty(), empty(), empty(), empty(), empty(), empty(), empty(), empty(), empty()));

        String result = takeScreenshot(baseName);

        devTools.send(Emulation.clearDeviceMetricsOverride());

        return result;
    }

    /**
     * Ignore SSL errors
     *
     * @param ignore true to ignore, false for default behaviour
     */
    public void ignoreCertificateErrors(boolean ignore) {
        devTools.send(Security.setIgnoreCertificateErrors(ignore));
    }

    /**
     * Retrieve the console output
     *
     * @return Log entries, one on each line
     */
    public String consoleLog() {
        return formattedConsoleLog(logEntries);
    }

    private String formattedConsoleLog(List<String> logEntryList) {
        devTools.send(Log.enable());
        String logView = logEntryList.stream().map(entry -> entry + "\r\n").collect(Collectors.joining());
        return logView;
    }

    /**
     * Clear the list of logEntries
     */
    public void clearConsoleLog() {
        logEntries.clear();
    }

    /**
     * Start collecting performance metrics
     */
    public void startPerformanceMetrics() {
        devTools.send(Performance.enable(Optional.of(Performance.EnableTimeDomain.TIMETICKS)));
    }

    /**
     * Stop collecting performance metrics
     */
    public void stopPerformanceMetrics() {
        devTools.send(Performance.disable());
    }

    /**
     * Get the collected performance metrics as a key:value map
     *
     * @return All collected metrics
     */
    public Map<String, Number> performanceMetrics() {
        return devTools.send(Performance.getMetrics()).stream()
                .collect(Collectors.toMap(Metric::getName, Metric::getValue, (a, b) -> b));
    }

    /**
     * Get a specific metric by name
     *
     * @param metric the metric to retrieve
     * @return a number representing the metric's measurement
     */
    public Number performanceMetric(String metric) {
        return devTools.send(Performance.getMetrics()).stream().filter(m ->
                m.getName().equalsIgnoreCase(metric)).findFirst().map(Metric::getValue).orElse(null);
    }

    /**
     * Keep track of requests and responses during the session
     */
    public void startRequestLogging() {
        devTools.addListener(Network.requestWillBeSent(), requests::add);
        devTools.addListener(Network.responseReceived(), responses::add);
    }

    /**
     * Keep track of requets and responses for a specific ResourceType during the session.
     *
     * @param resourceType The resource type to log. Valid options: Document, Stylesheet, Image, Media, Font, Script,
     *                     TextTrack, XHR, Fetch, EventSource, WebSocket, Manifest, SignedExchange, Ping,
     *                     CSPViolationReport, Other
     */
    public void startRequestLoggingFor(String resourceType) {
        devTools.addListener(Network.requestWillBeSent(), requestWillBeSent -> {
            if (requestWillBeSent.getType().orElse(ResourceType.OTHER).equals(ResourceType.fromString(resourceType))) {
                requests.add(requestWillBeSent);
            }
        });
        devTools.addListener(Network.responseReceived(), responseReceived -> {
            if (responseReceived.getType().equals(ResourceType.fromString(resourceType))) {
                responses.add(responseReceived);
            }
        });
    }

    /**
     * Currently only shows browser's own messages (i.e. XSS warnings)
     * Cannot use Runtime.ConsoleApiCalled , as the fromJson of RemoteObject is broken in alpha 6.
     * Await new version that uses the idealized version of the CDP impl.
     */
    private void enableConsoleLogCapturing() {
        //TODO: Add runtime Console API call logging when new version is released
        devTools.send(Log.enable());
        devTools.addListener(Runtime.consoleAPICalled(), call -> {
            String entry = call.getType().toString() + "; " +
                    new Timestamp(System.currentTimeMillis()).getTime() + "; " +
                    call.getArgs().stream().map(Object::toString).collect(Collectors.joining());

            logEntries.add(entry);
        });
        devTools.addListener(Log.entryAdded(), logEntry -> {
            String entry = logEntry.getLevel().name() + "; " +
                    logEntry.getTimestamp().toString() + "; " +
                    logEntry.getText();
            logEntries.add(entry);
        });

    }

    /**
     * Override the response for a given URL pattern.
     * This method can not be used in combination with override authentication
     *
     * @param urlPattern   The url pattern to match. Wildcards ('*' -> zero or more, '?' -> exactly one) are allowed.
     *                     Escape character is backslash.
     * @param type         The resource type. Allowed Values: Document, Stylesheet, Image, Media, Font, Script, TextTrack, XHR,
     *                     Fetch, EventSource, WebSocket, Manifest, SignedExchange, Ping, CSPViolationReport, Other
     * @param responseBody The response to inject
     */
    public void overrideResponseForUrlOfTypeWith(String urlPattern, String type, String responseBody) {
        requestPatternsToOverride.add(new RequestPattern(
                Optional.of(urlPattern),
                Optional.of(ResourceType.fromString(type)),
                Optional.of(RequestStage.RESPONSE)));

        devTools.addListener(Fetch.requestPaused(), requestPaused -> {
            if (requestPaused.getRequest().getUrl().matches(wildcardAsRegex(urlPattern))) {
                devTools.send(Fetch.fulfillRequest(
                        requestPaused.getRequestId(),
                        requestPaused.getResponseStatusCode().orElse(200),
                        requestPaused.getResponseHeaders(),
                        empty(),
                        Optional.of(Base64.encode(responseBody)),
                        empty()));
            } else {
                devTools.send(Fetch.continueRequest(requestPaused.getRequestId(), empty(), empty(), empty(), empty(), empty()));
            }
        });
    }

    /**
     * Enable response- or authentication overrides
     */
    public void enableNetworkOverrides() {
        Optional<List<RequestPattern>> patterns = requestPatternsToOverride.isEmpty() ? empty() : Optional.of(requestPatternsToOverride);
        boolean overrideAuth = false;
        devTools.send(Fetch.enable(patterns, Optional.of(overrideAuth)));
    }

    /**
     * Clear any response- or authentication overrides
     */
    public void clearNetworkOverrides() {
        devTools.clearListeners();
        requestPatternsToOverride.clear();
        devTools.send(Fetch.disable());
    }

    /**
     * Get the response body of a request to a url that matches the pattern
     *
     * @param requestUrlPattern The url pattern to match. Wildcards ('*' -> zero or more, '?' -> exactly one) are allowed.
     *                          Escape character is backslash.
     * @return the response body as a String
     */
    public String responseBodyOfRequestTo(String requestUrlPattern) {
        RequestId id = getRequestIdForUrl(requestUrlPattern);
        Network.GetResponseBodyResponse body = devTools.send(Network.getResponseBody(id));
        return body.getBody();
    }

    /**
     * Get the response status of a request to a url that matches the pattern
     *
     * @param requestUrlPattern The url pattern to match. Wildcards ('*' -> zero or more, '?' -> exactly one) are allowed.
     *                          Escape character is backslash.
     * @return the response status code (i.e. 200 for OK)
     */
    public int responseStatusOfRequestTo(String requestUrlPattern) {
        RequestId id = getRequestIdForUrl(requestUrlPattern);
        return responses.stream().filter(response -> response.getRequestId().toString().equals(id.toString())).findFirst()
                .orElseThrow(() -> new SlimFixtureException("No response found for: " + requestUrlPattern)).getResponse().getStatus();
    }

    /**
     * Get a list of logged urls that were requested
     *
     * @return list of request URL's
     */
    public List<String> requestLog() {
        return requests.stream().map(request -> request.getRequest().getUrl()).collect(Collectors.toList());
    }

    private RequestId getRequestIdForUrl(String requestUrlPattern) {
        for (RequestWillBeSent r : requests) {
            if (r.getRequest().getUrl().matches(wildcardAsRegex(requestUrlPattern))) {
                return r.getRequestId();
            }
        }
        throw new SlimFixtureException(false, "No request found for: " + requestUrlPattern);
    }

    /**
     * Convert a wildcard pattern to a regex pattern so we can match consistently
     *
     * @param wildcardPattern The pattern to convert
     * @return a regex String
     */
    private String wildcardAsRegex(String wildcardPattern) {
        return "\\Q" + wildcardPattern
                .replace("?", "\\E.\\Q")
                .replace("*", "\\E.*\\Q") +
                "\\E";
    }

}
