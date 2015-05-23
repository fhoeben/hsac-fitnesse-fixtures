package nl.hsac.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.util.HttpServer;
import nl.hsac.fitnesse.fixture.util.MockXmlHttpResponseSequence;
import nl.hsac.fitnesse.fixture.util.XmlHttpResponse;
import org.apache.http.HttpStatus;

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
        String not = getResponse().getMissingRequestsMessage();
        if (not != null) {
            throw new SlimFixtureException(false, not);
        }
        return true;
    }

    public boolean verifyNoExtraRequests() {
        String extra = getResponse().getExtraRequestsMessage();
        if (extra != null) {
            throw new SlimFixtureException(false, extra);
        }
        return true;
    }

    public void stop() {
        removeMockServer(path);
    }

    protected void addResponse(XmlHttpResponse newResponse) {
        getResponseList().add(newResponse);
    }

    protected List<XmlHttpResponse> getResponseList() {
        return getResponse().getResponseList();
    }

    protected MockXmlHttpResponseSequence getResponse() {
        return mockServer.getResponse();
    }
}
