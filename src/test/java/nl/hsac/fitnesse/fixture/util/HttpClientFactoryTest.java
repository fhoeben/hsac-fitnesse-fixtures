package nl.hsac.fitnesse.fixture.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HttpClientFactoryTest {
    private final HttpClientFactory factory = new HttpClientFactory();

    @Test
    public void connectionTimeout() {
        int timeout = 100;

        assertEquals(-1, factory.getConnectionTimeout());

        factory.setConnectionTimeout(timeout);
        assertEquals(timeout, factory.getConnectionTimeout());
    }

    @Test
    public void socketTimeout() {
        int timeout = 800;

        assertEquals(-1, factory.getSocketTimeout());

        factory.setSocketTimeout(timeout);
        assertEquals(timeout, factory.getSocketTimeout());
    }
}
