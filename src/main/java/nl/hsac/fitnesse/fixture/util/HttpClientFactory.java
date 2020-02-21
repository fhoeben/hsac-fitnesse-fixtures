package nl.hsac.fitnesse.fixture.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.SystemDefaultCredentialsProvider;
import org.apache.http.impl.client.WinHttpClients;
import org.apache.http.ssl.PrivateKeyStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/**
 * Helper to create Apache http client.
 */
public class HttpClientFactory {
    private HttpClientBuilder clientBuilder;
    private String userAgent;
    private ConnectionReuseStrategy connectionReuseStrategy;
    private RequestConfig.Builder requestConfigBuilder;
    private CredentialsProvider credentialsProvider;

    private HttpHost proxy;

    private boolean enableContentCompression = false;
    private boolean disableSslVerification = false;

    private File trustStoreFile;
    private char[] trustStorePassword;
    private TrustStrategy trustStrategy;

    private File keyStoreFile;
    private char[] keyStorePassword;
    private char[] keyPassword;
    private PrivateKeyStrategy keyStrategy;

    private boolean useWindowsAuthenticationSettings = false;

    public HttpClientFactory() {
        userAgent = nl.hsac.fitnesse.fixture.util.HttpClient.class.getName();
        requestConfigBuilder = createRequestConfigBuilder();
        credentialsProvider = createCredentialsProvider();
        connectionReuseStrategy = createConnectionReuseStrategy();
        clientBuilder = createClientBuilder();
    }

    /**
     * Creates a client using the current settings.
     *
     * @return apache http client.
     */
    public HttpClient createClient() {

        if (useWindowsAuthenticationSettings) {
            clientBuilder = WinHttpClients.custom();
        } else {
            clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        }
        if (isSslVerificationDisabled()) {
            disableSSLVerification();
        }
        if (isSSLContextRequired()) {
            SSLContext sslContext = generateSSLContext();
            clientBuilder.setSSLContext(sslContext);
        }
        if (!isContentCompressionEnabled()) {
            clientBuilder.disableContentCompression();
        }
        clientBuilder.setUserAgent(userAgent);
        clientBuilder.setConnectionReuseStrategy(connectionReuseStrategy);
        clientBuilder.setDefaultRequestConfig(requestConfigBuilder.build());

        return buildClient();
    }

    /**
     * Disables SSL certificate verification.
     */
    public void disableSSLVerification() {
        try {
            clientBuilder.setSSLSocketFactory(createAllTrustingSSLConnectionSocketFactory());
        } catch (Exception e) {
            throw new RuntimeException("Unable to create all-trusting SSLConnectionSocketFactory", e);
        }
    }

    /**
     * Resets SSL trust store to default.
     */
    public void clearTrustStore() {
        this.trustStoreFile = null;
        this.trustStrategy = null;
        // erase current password value from memory
        Arrays.fill(this.trustStorePassword, '0');
        this.trustStorePassword = null;
    }

    /**
     * No longer send client certificate.
     */
    public void clearClientCertificate() {
        keyStoreFile = null;
        keyStrategy = null;

        // erase current password values from memory
        Arrays.fill(this.keyStorePassword, '0');
        this.keyStorePassword = null;
        Arrays.fill(this.keyPassword, '0');
        this.keyPassword = null;
    }

    protected SSLContext generateSSLContext() {
        SSLContextBuilder contextBuilder = SSLContexts.custom();
        try {
            if (getTrustStoreFile() != null) {
                contextBuilder.loadTrustMaterial(getTrustStoreFile(), getTrustStorePassword(), getTrustStrategy());
            }

            if (getKeyStoreFile() != null) {
                contextBuilder.loadKeyMaterial(getKeyStoreFile(), getKeyStorePassword(), getKeyPassword(), getPrivateKeyStrategy());
            }

            return contextBuilder.build();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Unable to configure SSL", e);
        }
    }

    protected HttpClient buildClient() {
        return clientBuilder.build();
    }

    public void setProxy(String proxyString) {
        proxy = StringUtils.isBlank(proxyString) ? null : HttpHost.create(proxyString);
        getRequestConfigBuilder().setProxy(proxy);
    }

    public void setProxyUsernameAndPassword(String username, String password) {
        if (proxy == null) {
            throw new IllegalStateException("No proxy set, please configure that before setting credentials");
        }
        AuthScope proxyAuthScope = new AuthScope(proxy);
        Credentials proxyCredentials = new UsernamePasswordCredentials(username, password);
        setCredentials(proxyAuthScope, proxyCredentials);
    }

    public void useWindowsAuthentication(boolean useWindowsAuth) {
        this.useWindowsAuthenticationSettings = useWindowsAuth;
    }

    public void configureBasicAuthentication(String username, String password) {
        setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
    }

    public void setCredentials(AuthScope scope, Credentials credentials) {
        getCredentialsProvider().setCredentials(scope, credentials);
    }

    public void setConnectionTimeout(int timeoutInMs) {
        getRequestConfigBuilder().setConnectTimeout(timeoutInMs);
    }

    public int getConnectionTimeout() {
        return getRequestConfigBuilder().build().getConnectTimeout();
    }

    public void setSocketTimeout(int timeoutInMs) {
        getRequestConfigBuilder().setSocketTimeout(timeoutInMs);
    }

    public int getSocketTimeout() {
        return getRequestConfigBuilder().build().getSocketTimeout();
    }

    public boolean isContentCompressionEnabled() {
        return enableContentCompression;
    }

    public void setContentCompression(boolean contentCompression) {
        this.enableContentCompression = contentCompression;
    }

    public boolean isSslVerificationDisabled() {
        return disableSslVerification;
    }

    public void setDisableSslVerification(boolean disableSslVerification) {
        this.disableSslVerification = disableSslVerification;
        clientBuilder.setSSLSocketFactory(null);
    }

    protected boolean isSSLContextRequired() {
        return keyStoreFile != null || trustStoreFile != null;
    }

    public File getTrustStoreFile() {
        return trustStoreFile;
    }

    public void setTrustStoreFile(File trustStoreFile) {
        this.trustStoreFile = trustStoreFile;
    }

    public char[] getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(char[] trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    public TrustStrategy getTrustStrategy() {
        return trustStrategy;
    }

    public void setTrustStrategy(TrustStrategy trustStrategy) {
        this.trustStrategy = trustStrategy;
    }

    public File getKeyStoreFile() {
        return keyStoreFile;
    }

    public void setKeyStoreFile(File keyStoreFile) {
        this.keyStoreFile = keyStoreFile;
    }

    public char[] getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(char[] keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public char[] getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(char[] keyPassword) {
        this.keyPassword = keyPassword;
    }

    public PrivateKeyStrategy getPrivateKeyStrategy() {
        return keyStrategy;
    }

    public void setPrivateKeyStrategy(PrivateKeyStrategy keyStrategy) {
        this.keyStrategy = keyStrategy;
    }

    public HttpClientBuilder getClientBuilder() {
        return clientBuilder;
    }

    public void setClientBuilder(HttpClientBuilder builder) {
        clientBuilder = builder;
    }

    public ConnectionReuseStrategy getConnectionReuseStrategy() {
        return connectionReuseStrategy;
    }

    public void setConnectionReuseStrategy(ConnectionReuseStrategy connectionReuseStrategy) {
        this.connectionReuseStrategy = connectionReuseStrategy;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public CredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }

    public void setCredentialsProvider(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
    }

    public RequestConfig.Builder getRequestConfigBuilder() {
        return requestConfigBuilder;
    }

    public void setRequestConfigBuilder(RequestConfig.Builder requestConfigBuilder) {
        this.requestConfigBuilder = requestConfigBuilder;
    }

    protected HttpClientBuilder createClientBuilder() {
        return HttpClients.custom()
                .useSystemProperties();
    }

    protected ConnectionReuseStrategy createConnectionReuseStrategy() {
        return NoConnectionReuseStrategy.INSTANCE;
    }

    protected SystemDefaultCredentialsProvider createCredentialsProvider() {
        return new SystemDefaultCredentialsProvider();
    }

    protected RequestConfig.Builder createRequestConfigBuilder() {
        return RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD);
    }

    protected SSLConnectionSocketFactory createAllTrustingSSLConnectionSocketFactory() throws Exception {
        SSLContext allTrustingSSLContext = SSLContexts.custom()
                .loadTrustMaterial(null, (a, b) -> true)
                .build();
        return new SSLConnectionSocketFactory(allTrustingSSLContext, (a, b) -> true);
    }
}
