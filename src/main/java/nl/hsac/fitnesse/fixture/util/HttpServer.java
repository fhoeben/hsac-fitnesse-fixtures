package nl.hsac.fitnesse.fixture.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Receiver for a callback from application being tested.
 */
public class HttpServer <T extends HttpResponse> {
    private static final ContentType XML_UTF8_TYPE = ContentType.parse(XmlHttpResponse.CONTENT_TYPE_XML_TEXT_UTF8);

    private final T response;
    private final com.sun.net.httpserver.HttpServer server;
    private final AtomicBoolean requestReceived = new AtomicBoolean(false);

    /**
     * Creates new.
     * @param aPath context the server will serve.
     * @param aResponse response to send when request is received, request will
     *                  be added to it when this server receives one.
     */
    public HttpServer(String aPath, T aResponse) {
        response = aResponse;
        try {
            server = com.sun.net.httpserver.HttpServer.create();
            bind(server);
            server.createContext(aPath, getHandler(aResponse));
            server.start();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * @return address the server listens on.
     */
    public InetSocketAddress getAddress() {
        return server.getAddress();
    }

    /**
     * @return response, with request filled if isRequestReceived() is true.
     */
    public T getResponse() {
        return response;
    }

    /**
     * @return true if this server received a request.
     */
    public boolean isRequestReceived() {
        return requestReceived.get();
    }

    /**
     * @param maxWait ms to wait at most.
     * @return response with request filled, if one was received.
     */
    public T waitForRequest(long maxWait) {
        long start = System.currentTimeMillis();
        try {
            while (!isRequestReceived()
                    && (System.currentTimeMillis() - start) < maxWait) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        } finally {
            stopServer();
        }

        return getResponse();
    }

    /**
     * Stops server from listening.
     */
    public void stopServer() {
        server.stop(0);
    }

    private void bind(com.sun.net.httpserver.HttpServer server) {
        try {
            InetAddress address = InetAddress.getLocalHost();
            int port = getFreePort();
            InetSocketAddress isa = new InetSocketAddress(address, port);
            server.bind(isa, 1);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Finds free port number.
     * @return first available port number above 8000.
     */
    protected int getFreePort() {
        int port = 0;
        for (int possiblePort = 8000; port == 0; possiblePort++) {
            ServerSocket s = null;
            try {
                s = new ServerSocket(possiblePort);
                port = s.getLocalPort();
            } catch (IOException e) {
                // try next number
                continue;
            } finally {
                if (s != null) {
                    try {
                        s.close();
                    } catch (IOException e) {
                        // why would this happen?
                        throw new IllegalStateException("Unable to close port: " + possiblePort, e);
                    }
                }
            }
        }
        return port;
    }

    private HttpHandler getHandler(final T aResponse) {
        HttpHandler result = new HttpHandler() {
            @Override
            public void handle(HttpExchange he) throws IOException {
                OutputStream os = null;
                try {
                    InputStream is = he.getRequestBody();
                    String request = FileUtil.streamToString(is, "http request");
                    aResponse.setRequest(request);
                    requestReceived.set(true);

                    ContentType contentType = XML_UTF8_TYPE;
                    byte[] responseBytes = aResponse.getResponse()
                                            .getBytes(contentType.getCharset());
                    he.sendResponseHeaders(aResponse.getStatusCode(),
                                            responseBytes.length);
                    he.getResponseHeaders()
                            .add("Content-Type", contentType.toString());
                    os = he.getResponseBody();
                    os.write(responseBytes);
                    os.flush();
                } finally {
                    if (os != null) {
                        os.close();
                    }
                }

            }
        };
        return result;
    }
}
