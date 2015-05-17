package nl.hsac.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.util.HttpServer;
import nl.hsac.fitnesse.fixture.util.MockXmlHttpResponseSequence;
import nl.hsac.fitnesse.fixture.util.XmlHttpResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fixture allowing you to host a SOAP server inside FitNesse. This mock server can then be called by a system you
 * are testing so you can verify that (and what) calls are made.
 * The idea is that you first use this fixture to setup the responses the mock server will generated and obtain the
 * URL the service is listening on. This URL is then passed to the system being tested which is expected to make
 * one or more calls, in response to which it will receive the configured responses.
 * Afterwards you can use this fixture to shut down the mock server.
 */
public class MockXmlServerSetup extends SlimFixture {
    private static final Map<String, HttpServer<MockXmlHttpResponseSequence>> SERVERS = new HashMap<String, HttpServer<MockXmlHttpResponseSequence>>();
    public static final String DEFAULT_PATH = "/FitNesseMock";
    private final String path;
    private final HttpServer<MockXmlHttpResponseSequence> mockServer;

    public static HttpServer<MockXmlHttpResponseSequence> getMockServer(String aPath) {
        HttpServer<MockXmlHttpResponseSequence> server = SERVERS.get(aPath);
        if (server == null) {
            throw new SlimFixtureException(false, "No server created at path: " + aPath);
        }
        return server;
    }

    public static void removeMockServer(String aPath) {
        HttpServer<MockXmlHttpResponseSequence> server = SERVERS.remove(aPath);
        if (server != null) {
            server.stopServer();
        }
    }

    public static List<XmlHttpResponse> getResponses(String aPath) {
        return getMockServer(aPath).getResponse().getResponseList();
    }

    public MockXmlServerSetup() {
        this(DEFAULT_PATH);
    }

    public MockXmlServerSetup(String aPath) {
        path = cleanupValue(aPath);
        if (SERVERS.containsKey(path)) {
            mockServer = getMockServer(path);
        } else {
            mockServer = new HttpServer<MockXmlHttpResponseSequence>(path, new MockXmlHttpResponseSequence());
            SERVERS.put(path, mockServer);
        }
    }

    public void addResponse(String aResponse) {
        XmlHttpResponse newResponse = new XmlHttpResponse();
        newResponse.setStatusCode(HttpStatus.SC_OK);
        newResponse.setResponse(cleanupBody(aResponse));
        addResponse(newResponse);
    }

    protected String cleanupBody(String body) {
        return getEnvironment().getHtmlCleaner().cleanupPreFormatted(body);
    }

    /**
     * @return url this server is listening on.
     */
    public String getMockServerUrl() {
        return "http:/" + mockServer.getAddress() + path;
    }

    public boolean verifyAllResponsesServed() {
        List<XmlHttpResponse> responses = getResponseList();
        String not = getMissingRequestsMessage(responses);
        if (not != null) {
            throw new SlimFixtureException(false, not);
        }
        return true;
    }

    public boolean verifyNoExtraRequests() {
        List<XmlHttpResponse> responses = getResponseList();
        String extra = getExtraRequestsMessage(responses);
        if (extra != null) {
            throw new SlimFixtureException(false, extra);
        }
        return true;
    }

    public void stop() {
        removeMockServer(path);
    }

    protected String getExtraRequestsMessage(List<XmlHttpResponse> responses) {
        List<String> extraResponses = getNotExpected(responses);
        return createUnexpectedMessage("%s extra request(s) received: %s", extraResponses);
    }

    protected String getMissingRequestsMessage(List<XmlHttpResponse> responses) {
        List<String> extraResponses = getNotCalled(responses);
        return createUnexpectedMessage("%s response(s) not requested: %s", extraResponses);
    }

    protected List<String> getNotCalled(List<XmlHttpResponse> responses) {
        List<String> result = new ArrayList<String>(1);
        for (XmlHttpResponse response : responses) {
            if (StringUtils.isEmpty(response.getRequest())) {
                result.add(response.getResponse());
            }
        }
        return result;
    }

    protected String createUnexpectedMessage(String messagePattern, List<String> extraResponses) {
        String msg = null;
        if (!extraResponses.isEmpty()) {
            StringBuilder extraRequests = new StringBuilder("<ol>");
            for (int i = 0; i < extraResponses.size(); i++) {
                extraRequests.append("<li>");
                String request = extraResponses.get(i);
                extraRequests.append(formatValue(request));
                extraRequests.append("</li>");
            }
            extraRequests.append("</ol>");
            msg = String.format("<div>" + messagePattern + "</div>",
                    extraResponses.size(), extraRequests);
        }
        return msg;
    }

    protected List<String> getNotExpected(List<XmlHttpResponse> responses) {
        List<String> result = new ArrayList<String>(1);
        for (XmlHttpResponse response : responses) {
            if (StringUtils.isEmpty(response.getResponse())) {
                result.add(response.getRequest());
            }
        }
        return result;
    }

    protected String formatValue(String value) {
        String result;
        try {
            result = getEnvironment().getHtmlForXml(value);
        } catch (Exception e) {
            result = value;
        }
        return result;
    }

    protected void addResponse(XmlHttpResponse newResponse) {
        getResponseList().add(newResponse);
    }

    protected List<XmlHttpResponse> getResponseList() {
        return mockServer.getResponse().getResponseList();
    }
}
