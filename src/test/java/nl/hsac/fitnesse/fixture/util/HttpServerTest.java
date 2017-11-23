package nl.hsac.fitnesse.fixture.util;

import org.junit.After;
import org.junit.Test;

import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HttpServerTest {
    private static final String PATH = "/unitTestPath";

    private HttpResponse response = new HttpResponse();
    private HttpServer<HttpResponse> server;

    @Test
    public void secondServerGetsNextAddress() throws Exception {
        HttpServer<HttpResponse> server = storeNew(new HttpServer<>(PATH, response));

        HttpResponse resp = server.getResponse();
        assertSame(response, resp);

        int port1 = checkDefaultAddress(server);

        HttpServer<HttpResponse> server2 = storeNew(new HttpServer<>(PATH, response));
        int port2 = checkDefaultAddress(server2);

        assertEquals(port1 + 1, port2);
    }

    @Test
    public void canPassExplicitAddress() throws Exception {
        checkMultipleRandomAtSameAddress("127.0.0.1");
    }

    @Test
    public void canPassExplicitHostname() throws Exception {
        checkMultipleRandomAtSameAddress("localhost");
    }

    @Test
    public void canPassWildcardAddress() throws Exception {
        checkMultipleRandomAtSameAddress("0.0.0.0");
    }

    @Test
    public void canPassExplicitPort() throws Exception {
        int expectedPort = 65535 - 3;
        InetSocketAddress address = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), expectedPort);

        HttpServer<HttpResponse> server = storeNew(new HttpServer<>(address, PATH, response));

        InetSocketAddress addressOfServer = server.getAddress();
        assertSame(address.getAddress(), addressOfServer.getAddress());

        int port = addressOfServer.getPort();
        assertEquals(expectedPort, port);
    }

    @Test
    public void explicitPortFailsWhenInUse() throws Exception {
        canPassExplicitPort();

        try {
            canPassExplicitPort();
            fail("Expected exception");
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            assertEquals(BindException.class, cause.getClass());
        }
    }

    private void checkMultipleRandomAtSameAddress(String hostName) throws UnknownHostException {
        InetAddress name = InetAddress.getByName(hostName);
        int port1 = createAndCheckAtRandomFreePort(name);
        int port2 = createAndCheckAtRandomFreePort(name);

        assertNotEquals(port1, port2);
    }

    private int createAndCheckAtRandomFreePort(InetAddress inetAddress) {
        InetSocketAddress address = new InetSocketAddress(inetAddress, 0);

        HttpServer<HttpResponse> server = storeNew(new HttpServer<>(address, PATH, response));

        HttpResponse resp = server.getResponse();

        assertSame(response, resp);

        InetSocketAddress addressOfServer = server.getAddress();
        assertSame(address.getAddress(), addressOfServer.getAddress());

        int port = addressOfServer.getPort();
        assertNotEquals(0, port);

        return port;
    }

    private int checkDefaultAddress(HttpServer<HttpResponse> aServer) throws UnknownHostException {
        InetSocketAddress address = aServer.getAddress();
        assertNotNull(address);

        InetAddress inetAddress = address.getAddress();
        assertEquals(InetAddress.getLocalHost(), inetAddress);

        int port = address.getPort();
        assertTrue("Bad port: " + port, port >= 8000);

        return port;
    }

    public HttpServer<HttpResponse> storeNew(HttpServer<HttpResponse> newServer) {
        if (server != null) {
            server.stopServer();
        }
        server = newServer;
        return server;
    }

    @After
    public void tearDown() {
        if (server != null) {
            server.stopServer();
        }
    }

}