package nl.hsac.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.util.HttpServer;
import nl.hsac.fitnesse.fixture.util.MockXmlHttpResponseSequence;
import nl.hsac.fitnesse.fixture.util.XmlHttpResponse;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Fixture allowing you to host a SOAP server inside FitNesse. This mock server can then be called by a system you
 * are testing so you can verify that (and what) calls are made.
 * The idea is that you first use this fixture to setup the responses the mock server will generated and obtain the
 * URL the service is listening on. This URL is then passed to the system being tested which is expected to make
 * one or more calls, in response to which it will receive the configured responses.
 * Afterwards you can use this fixture to shut down the mock server.
 */
public class MockXmlServerSetup extends SlimFixture {
    private static final Map<String, HttpServer<? extends MockXmlHttpResponseSequence>> SERVERS = new HashMap<>();
    public static final String DEFAULT_PATH = "/FitNesseMock";
    private final String path;
    private final HttpServer<? extends MockXmlHttpResponseSequence> mockServer;

    public static HttpServer<? extends MockXmlHttpResponseSequence> getMockServer(String aPath) {
        HttpServer<? extends MockXmlHttpResponseSequence> server = SERVERS.get(aPath);
        if (server == null) {
            throw new SlimFixtureException(false, "No server created at path: " + aPath);
        }
        return server;
    }

    public static void removeMockServer(String aPath) {
        HttpServer<? extends MockXmlHttpResponseSequence> server = SERVERS.remove(aPath);
        if (server != null) {
            server.stopServer();
        }
    }

    public static List<? extends XmlHttpResponse> getResponses(String aPath) {
        return getMockServer(aPath).getResponse().getResponseList();
    }

    public MockXmlServerSetup() {
        this(DEFAULT_PATH);
    }

    public MockXmlServerSetup(String aPath) {
        this(aPath, () -> createMockServer(aPath));
    }

    public MockXmlServerSetup(String host, String ports, String aPath) {
        this(aPath, () -> createMockServer(host, ports, aPath));
    }

    public MockXmlServerSetup(String aPath, Supplier<HttpServer<? extends MockXmlHttpResponseSequence>> creator) {
        path = cleanupValue(aPath);
        if (SERVERS.containsKey(path)) {
            mockServer = getMockServer(path);
        } else {
            mockServer = creator.get();
            SERVERS.put(path, mockServer);
        }
    }

    public static HttpServer<? extends MockXmlHttpResponseSequence> createMockServer(String host, String ports, String aPath) {
        InetAddress h = getInetAddress(host);
        int[] portRange = getPortRange(ports);
        return new HttpServer<>(h, portRange[0], portRange[1], aPath, createResponse());
    }

    public static int[] getPortRange(String ports) {
        String[] portBoundaries = ports.split("-");
        int[] portRange = new int[2];
        portRange[0] = parseInt(portBoundaries[0]);
        portRange[1] = portBoundaries.length == 1 ? portRange[0] : parseInt(portBoundaries[1]);
        return portRange;
    }

    private static int parseInt(String s) {
        return Integer.parseInt(s.trim());
    }

    public static InetAddress getInetAddress(String host) {
        try {
            return host == null || "null".equalsIgnoreCase(host) ?
                                        InetAddress.getLocalHost() : InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new SlimFixtureException(false, "Unable to resolve address: " + host);
        }
    }

    public static HttpServer<? extends MockXmlHttpResponseSequence> createMockServer(String aPath) {
        HttpServer<MockXmlHttpResponseSequence> mockServer;
        MockXmlHttpResponseSequence response = createResponse();
        try {
            mockServer = new HttpServer<>(InetAddress.getLocalHost(), 8000, HttpServer.MAX_PORT, aPath, response);
        } catch (UnknownHostException | RuntimeException e) {
            mockServer = new HttpServer<>(InetAddress.getLoopbackAddress(),8000, HttpServer.MAX_PORT, aPath, response);
        }
        return mockServer;
    }

    protected static MockXmlHttpResponseSequence createResponse() {
        return new MockXmlHttpResponseSequence();
    }

    public void addResponseFor(String aResponse, String aRequest) {
        addResponseImpl(aResponse, aRequest, null);
    }

    public void addResponse(String aResponse) {
        addResponseImpl(aResponse, null, null);
    }

    public void addResponseWithStatus(String aResponse, int aStatusCode) {
        XmlHttpResponse response = addResponseImpl(aResponse, null, null);
        response.setStatusCode(aStatusCode);
    }

    public void addResponseWithHeaders(String aResponse, Map<String, Object> headers) {
        addResponseImpl(aResponse, null, headers);
    }

    public void addResponseWithStatusAndHeaders(String aResponse, int aStatusCode, Map<String, Object> headers) {
        XmlHttpResponse response = addResponseImpl(aResponse, null, headers);
        response.setStatusCode(aStatusCode);
    }

    public void addResponseFile(String aResponseFile) {
        String fileContent = loadResponseFromFile(aResponseFile);
        addResponse(fileContent);
    }

    public void addResponseFileWithStatus(String aResponseFile, int aStatusCode) {
        String fileContent = loadResponseFromFile(aResponseFile);
        addResponseWithStatus(fileContent, aStatusCode);
    }

    protected String loadResponseFromFile(String aResponseFile) {
        String fileContent;
        String filePath = getFilePathFromWikiUrl(aResponseFile);
        try {
            fileContent = readFile(filePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            fileContent = "Response file not found for: " + aResponseFile;
        }
        return fileContent;
    }

    protected XmlHttpResponse addResponseImpl(String aResponse, String aRequest, Map<String, Object> headers) {
        String responseBody = cleanupBody(aResponse);
        String request = cleanupValue(aRequest);
        XmlHttpResponse response = getResponse().addResponse(responseBody, request);
        if (headers != null) {
            response.getResponseHeaders().putAll(headers);
        }
        return response;
    }

    protected String cleanupBody(String body) {
        return getEnvironment().getHtmlCleaner().cleanupPreFormatted(body);
    }

    /**
     * @return url this server is listening on.
     */
    public String getMockServerUrl() {
        return mockServer.getURL();
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

    protected List<? extends XmlHttpResponse> getResponseList() {
        return getResponse().getResponseList();
    }

    protected MockXmlHttpResponseSequence getResponse() {
        return mockServer.getResponse();
    }

    protected static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
