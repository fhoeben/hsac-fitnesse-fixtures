package nl.hsac.fitnesse.fixture.util;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
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

    private boolean enableContentCompression = false;
    private boolean disableSslVerification = false;

    private File trustStoreFile;
    private char[] trustStorePassword;
    private TrustStrategy trustStrategy;

    private File keyStoreFile;
    private char[] keyStorePassword;
    private char[] keyPassword;
    private PrivateKeyStrategy keyStrategy;

    public HttpClientFactory() {
        clientBuilder = createClientBuilder()
                .setDefaultRequestConfig(getDefaultRequestConfig())
                .setUserAgent(getUserAgent())
                .setConnectionReuseStrategy(getConnectionReuseStrategy());
    }

    /**
     * Creates a client using the current settings.
     * @return apache http client.
     */
    public HttpClient createClient() {
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
        return buildClient();
    }

    /**
     * Disables SSL certificate verification.
     */
    public void disableSSLVerification() {
        try {
            clientBuilder.setSSLSocketFactory(generateAllTrustingSSLConnectionSocketFactory());
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

    protected SSLConnectionSocketFactory generateAllTrustingSSLConnectionSocketFactory() throws Exception {
        SSLContext allTrustingSSLContext = SSLContexts.custom()
                .loadTrustMaterial(null, (a, b) -> true)
                .build();
        return new SSLConnectionSocketFactory(allTrustingSSLContext, (a, b) -> true);
    }

    protected HttpClient buildClient() {
        return clientBuilder.build();
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

    protected org.apache.http.impl.client.HttpClientBuilder getClientBuilder() {
        return clientBuilder;
    }

    protected void setClientBuilder(org.apache.http.impl.client.HttpClientBuilder builder) {
        clientBuilder = builder;
    }

    protected org.apache.http.impl.client.HttpClientBuilder createClientBuilder() {
        return HttpClients.custom()
                .useSystemProperties();
    }

    protected ConnectionReuseStrategy getConnectionReuseStrategy() {
        return NoConnectionReuseStrategy.INSTANCE;
    }

    protected String getUserAgent() {
        return nl.hsac.fitnesse.fixture.util.HttpClient.class.getName();
    }

    protected RequestConfig getDefaultRequestConfig() {
        return RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD)
                .build();
    }
}
