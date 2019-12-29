package nl.hsac.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.util.HttpClientFactory;
import org.apache.http.client.HttpClient;

import java.io.File;

/**
 * Fixture to configure http client used for HTTP tests.
 */
public class HttpClientSetup extends SlimFixture {
    private final HttpClient originalClient;
    private final HttpClientFactory clientFactory;

    public HttpClientSetup() {
        this(new HttpClientFactory());
    }

    public HttpClientSetup(HttpClientFactory clientFactory) {
        this.originalClient = getHttpClient().getHttpClient();
        this.clientFactory = clientFactory;
    }

    /**
     * Configure future http test usage to use the current settings.
     * @return true
     */
    public boolean configureHttpTest() {
        HttpClient client = createClient();
        return configureHttpTestWith(client);
    }

    /**
     * Creates a client using the current settings.
     * @return apache http client.
     */
    public HttpClient createClient() {
        return clientFactory.createClient();
    }

    /**
     * Configure future http test usage to use the client supplied.
     * @param client client to use.
     * @return true
     */
    public boolean configureHttpTestWith(HttpClient client) {
        getHttpClient().setHttpClient(client);
        return true;
    }

    /**
     * Enables content compression support (i.e. gzip) for all subsequent requests until the end of the test run.
     */
    public void enableContentCompression() {
        clientFactory.setContentCompression(true);
    }

    /**
     * Disables SSL certificate verification for all subsequent requests until the end of the test run.
     * Use at your own risk! This makes https potentially as unsafe as http!
     */
    public void disableSSLVerification() {
        clientFactory.setDisableSslVerification(true);
    }

    /**
     * Enables SSL certificate verification for all subsequent requests until the end of the test run.
     */
    public void enableSSLVerification() {
        clientFactory.setDisableSslVerification(false);
    }

    /**
     * Sets proxy server to use. Proxy URL should contain a host name and may contain a port
     * (separated by ':'). A scheme (http or https) is only required if the proxy should only be used for
     * that scheme.
     * @param proxyUrl proxy server to use (e.g. proxy.mydomain.com:8080)
     */
    public void setProxy(String proxyUrl) {
        String proxy = cleanupValue(proxyUrl);
        clientFactory.setProxy(proxy);
    }

    /**
     * Sets credentials to supply to proxy.
     * @param username username to send to proxy server
     * @param password password to send to proxy server
     */
    public void setProxyUsernameAndPassword(String username, String password) {
        String user = cleanupValue(username);
        String passwd = cleanupValue(password);
        clientFactory.setProxyUsernameAndPassword(user, passwd);
    }

    /**
     * Use SSL trust store (i.e. .jks).
     * @param trustStore .jks file to use
     * @param password password to open .jks
     */
    public void setTrustStoreWithPassword(String trustStore, String password) {
        clientFactory.setTrustStoreFile(cleanupFile(trustStore));
        clientFactory.setTrustStorePassword(cleanupValue(password).toCharArray());
    }

    /**
     * Send client certificate.
     * @param keyStore .jks file to use
     * @param keyStorePassword password to open .jks
     * @param keyPassword password for client certificate in .jks
     */
    public void setClientCertificateFromWithPasswordKeyPassword(String keyStore, String keyStorePassword, String keyPassword) {
        setClientCertificateFromWithPasswordKeyPasswordAndAlias(keyStore, keyStorePassword, keyPassword, null);
    }

    /**
     * Send client certificate.
     * @param keyStore .jks file to use
     * @param keyStorePassword password to open .jks
     * @param keyPassword password for client certificate in .jks
     * @param alias alias to choose client certificate
     */
    public void setClientCertificateFromWithPasswordKeyPasswordAndAlias(String keyStore, String keyStorePassword, String keyPassword, String alias) {
        clientFactory.setKeyStoreFile(cleanupFile(keyStore));
        clientFactory.setKeyStorePassword(cleanupValue(keyStorePassword).toCharArray());
        clientFactory.setKeyPassword(cleanupValue(keyPassword).toCharArray());
        if (alias != null) {
            String keyAlias = cleanupValue(alias);
            clientFactory.setPrivateKeyStrategy((aliases, socket) -> keyAlias);
        }
    }

    /**
     * @return the http client at the moment this http client setup instance was created,
     *          so it may be stored in a Slim symbol to be restored later.
     */
    public HttpClient getOriginalClient() {
        return originalClient;
    }

    /**
     * @return factory to actually create Apache HttpClient instances
     */
    public HttpClientFactory getClientFactory() {
        return clientFactory;
    }

    private File cleanupFile(String fileReg) {
        File file = new File(getFilePathFromWikiUrl(fileReg));
        if (!file.exists()) {
            throw new SlimFixtureException(false, "Unable to find: " + file.getAbsolutePath());
        }
        return file;
    }

    private nl.hsac.fitnesse.fixture.util.HttpClient getHttpClient() {
        return getEnvironment().getHttpClient();
    }
}
