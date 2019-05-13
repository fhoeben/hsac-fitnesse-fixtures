package nl.hsac.fitnesse.fixture.util;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
/**
 * Helper to make Http calls and get response.
 */
public class HttpClient {
    /** Default HttpClient instance used. */
    public final static org.apache.http.client.HttpClient DEFAULT_HTTP_CLIENT;
    private org.apache.http.client.HttpClient httpClient;
    private boolean contentCompression = false,
            sslVerification = true;

    static {
        DEFAULT_HTTP_CLIENT = buildHttpClient(false, true);
    }

    /**
     * Creates new.
     */
    public HttpClient() {
        httpClient = DEFAULT_HTTP_CLIENT;
    }

    /**
     * @param url URL of service
     * @param response response pre-populated with request to send. Response content and
     *          statusCode will be filled.
     * @param headers http headers to add
     * @param type contentType for request.
     */
    public void post(String url, HttpResponse response, Map<String, Object> headers, String type) {
        HttpPost methodPost = new HttpPost(url);
        ContentType contentType = ContentType.parse(type);
        HttpEntity ent = new StringEntity(response.getRequest(), contentType);
        methodPost.setEntity(ent);
        getResponse(url, response, methodPost, headers);
    }

    /**
     * Posts file as 'application/octet-stream'.
     * @param url URL of service
     * @param response response pre-populated with request to send. Response content and
     *          statusCode will be filled.
     * @param headers http headers to add
     * @param file file containing binary data to post.
     */
    public void post(String url, HttpResponse response, Map<String, Object> headers, File file) {
        HttpPost methodPost = new HttpPost(url);
        HttpEntity multipart = buildBodyWithFile(file);
        methodPost.setEntity(multipart);
        getResponse(url, response, methodPost, headers);
    }

    /**
     * @param url URL of service
     * @param response response pre-populated with request to send. Response content and
     *          statusCode will be filled.
     * @param headers http headers to add
     * @param type contentType for request.
     */
    public void put(String url, HttpResponse response, Map<String, Object> headers, String type) {
        HttpPut methodPut = new HttpPut(url);
        ContentType contentType = ContentType.parse(type);
        HttpEntity ent = new StringEntity(response.getRequest(), contentType);
        methodPut.setEntity(ent);
        getResponse(url, response, methodPut, headers);
    }

    /**
     * Put file as 'application/octet-stream'.
     * @param url URL of service
     * @param response response pre-populated with request to send. Response content and
     *          statusCode will be filled.
     * @param headers http headers to add
     * @param file file containing binary data to put.
     */
    public void put(String url, HttpResponse response, Map<String, Object> headers, File file) {
        HttpPut methodPut = new HttpPut(url);
        HttpEntity multipart = buildBodyWithFile(file);
        methodPut.setEntity(multipart);
        getResponse(url, response, methodPut, headers);
    }

    /**
     * @param url URL of service
     * @param response response pre-populated with request to send. Response content and
     *          statusCode will be filled.
     * @param headers http headers to add
     * @param type contentType for request.
     */
    public void patch(String url, HttpResponse response, Map<String, Object> headers, String type){
        HttpPatch methodPatch = new HttpPatch(url);
        ContentType contentType = ContentType.parse(type);
        HttpEntity ent = new StringEntity(response.getRequest(), contentType);
        methodPatch.setEntity(ent);
        getResponse(url,response,methodPatch, headers);
    }

    /**
     * Builds request body with a given file
     * @param file file containing binary data.
     */
    private HttpEntity buildBodyWithFile(File file) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("file", file,
                ContentType.APPLICATION_OCTET_STREAM, file.getName());
        HttpEntity multipart = builder.build();
        return multipart;
    }

    /**
     * @param url URL of service
     * @param response response to be filled.
     * @param headers http headers to add
     */
    public void get(String url, HttpResponse response, Map<String, Object> headers, boolean followRedirect) {
        HttpGet method = new HttpGet(url);
        if (!followRedirect) {
            RequestConfig r = RequestConfig.copy(RequestConfig.DEFAULT)
                                .setRedirectsEnabled(false)
                                .build();
            method.setConfig(r);
        }
        getResponse(url, response, method, headers);
    }

    /**
     * @param url URL of service
     * @param response response to be filled.
     * @param headers http headers to add
     */
    public void head(String url, HttpResponse response, Map<String, Object> headers) {
        HttpHead method = new HttpHead(url);
        getResponse(url, response, method, headers);
    }

    /**
     * @param url URL to send to DELETE to
     * @param response response to be filled.
     * @param headers headers to add.
     */
    public void delete(String url, HttpResponse response, Map<String, Object> headers) {
        HttpDelete method = new HttpDelete(url);
        getResponse(url, response, method, headers);
    }

    /**
     * @param url URL of service
     * @param response response pre-populated with request to send. Response content and
     *          statusCode will be filled.
     * @param headers http headers to add
     * @param type contentType for request.
     */
    public void delete(String url, HttpResponse response, Map<String, Object> headers, String type) {
        HttpDeleteWithBody methodPost = new HttpDeleteWithBody(url);
        ContentType contentType = ContentType.parse(type);
        HttpEntity ent = new StringEntity(response.getRequest(), contentType);
        methodPost.setEntity(ent);
        getResponse(url, response, methodPost, headers);
    }

    /**
     * Ensures the apache HttpClient used supports content compression
     */
    public void enableCompression() {
        if (!this.contentCompression) {
            this.contentCompression = true;
            updateHttpClient();
        }
    }

    /**
     * Ensures the apache HttpClient used does not support content compression
     */
    public void disableCompression() {
        if (this.contentCompression) {
            this.contentCompression = false;
            updateHttpClient();
        }
    }

    /**
     * Ensures the apache HttpClient used does not verify SSL certificates
     */
    public void disableSSLVerification() {
        if (this.sslVerification) {
            this.sslVerification = false;
            updateHttpClient();
        }
    }

    /**
     * Ensures the apache HttpClient used verifies SSL certificates
     */
    public void enableSSLVerification() {
        if (!this.sslVerification) {
            this.sslVerification = true;
            updateHttpClient();
        }
    }

    /**
     * Sets the apache HttpClient to one matching the current contentCompression and sslVerification values
     */
    protected void updateHttpClient() {
        if (!contentCompression && sslVerification) {
            this.httpClient = DEFAULT_HTTP_CLIENT;
        } else {
            this.httpClient = buildHttpClient(this.contentCompression, this.sslVerification);
        }
    }

    /**
     * Builds an apache HttpClient instance
     * @param contentCompression if true, the returned instance will support content compression
     * @return an apache HttpClient instance
     */
    protected static org.apache.http.client.HttpClient buildHttpClient(boolean contentCompression, boolean sslVerification) {
        RequestConfig rc = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD)
                .build();

        HttpClientBuilder builder = HttpClients.custom()
                .useSystemProperties();

        if (!contentCompression) {
            builder.disableContentCompression();
        }

        if (!sslVerification) {
            try {
                builder.setSSLSocketFactory(generateAllTrustingSSLConnectionSocketFactory());
            } catch (Exception e) {
                throw new RuntimeException("Unable to create all-trusting SSLConnectionSocketFactory", e);
            }

        }

        return builder.setConnectionReuseStrategy(NoConnectionReuseStrategy.INSTANCE)
                .setUserAgent(HttpClient.class.getName())
                .setDefaultRequestConfig(rc)
                .build();
    }

    protected static SSLConnectionSocketFactory generateAllTrustingSSLConnectionSocketFactory() throws Exception {
        SSLContext allTrustingSSLContext = SSLContexts.custom()
                .loadTrustMaterial(null, (a, b) -> true)
                .build();
        return new SSLConnectionSocketFactory(allTrustingSSLContext, (a, b) -> true);
    }

    protected void getResponse(String url, HttpResponse response, HttpRequestBase method, Map<String, Object> headers) {
        long startTime = 0;
        long endTime = -1;
        org.apache.http.HttpResponse resp = null;
        try {
            if (headers != null) {
                addHeadersToMethod(headers, method);
            }

            HttpContext context = createContext(response);
            response.setMethod(method.getMethod());

            startTime = currentTimeMillis();
            resp = executeMethod(context, method);
            endTime = currentTimeMillis();

            storeResponse(response, resp);
        } catch (Exception e) {
            throw new RuntimeException("Unable to get response from: " + url, e);
        } finally {
            if (startTime > 0) {
                if (endTime < 0) {
                    endTime = currentTimeMillis();
                }
            }
            response.setResponseTime(endTime - startTime);
            cleanupAfterRequest(resp, method);
        }
    }

    protected void addHeadersToMethod(Map<String, Object> requestHeaders, HttpRequestBase method) {
        for (String key : requestHeaders.keySet()) {
            Object value = requestHeaders.get(key);
            if (value != null) {
                method.setHeader(key, value.toString());
            }
        }
    }

    protected HttpContext createContext(HttpResponse response) {
        HttpContext localContext = new BasicHttpContext();
        CookieStore store = response.getCookieStore();
        if (store != null) {
            localContext.setAttribute(HttpClientContext.COOKIE_STORE, store);
        }
        return localContext;
    }

    protected org.apache.http.HttpResponse executeMethod(HttpContext context, HttpRequestBase method)
            throws IOException {
        return httpClient.execute(method, context);
    }

    protected void storeResponse(HttpResponse response, org.apache.http.HttpResponse resp) throws IOException {
        int returnCode = resp.getStatusLine().getStatusCode();
        response.setStatusCode(returnCode);

        addHeadersFromResponse(response.getResponseHeaders(), resp.getAllHeaders());

        copyResponseContent(response, resp);
    }

    protected void addHeadersFromResponse(Map<String, Object> responseHeaders, Header[] respHeaders) {
        for (Header h : respHeaders) {
            String headerName = h.getName();
            String headerValue = h.getValue();
            if (responseHeaders.containsKey(headerName)) {
                handleRepeatedHeaderValue(responseHeaders, headerName, headerValue);
            } else {
                responseHeaders.put(headerName, headerValue);
            }
        }
    }

    protected void handleRepeatedHeaderValue(Map<String, Object> responseHeaders,
                                             String headerName, String headerValue) {
        Object previousHeaderValue = responseHeaders.get(headerName);
        if (previousHeaderValue instanceof Collection) {
            ((Collection) previousHeaderValue).add(headerValue);
        } else {
            List<Object> valueList = new ArrayList();
            valueList.add(previousHeaderValue);
            valueList.add(headerValue);
            responseHeaders.put(headerName, valueList);
        }
    }

    protected void copyResponseContent(HttpResponse response, org.apache.http.HttpResponse resp) throws IOException {
        HttpEntity entity = resp.getEntity();
        if (entity == null) {
            response.setResponse(null);
        } else {
            if (response instanceof BinaryHttpResponse) {
                BinaryHttpResponse binaryHttpResponse = (BinaryHttpResponse) response;

                byte[] content = EntityUtils.toByteArray(entity);
                binaryHttpResponse.setResponseContent(content);

                String fileName = getAttachmentFileName(resp);
                binaryHttpResponse.setFileName(fileName);
            } else {
                String result = EntityUtils.toString(entity);
                response.setResponse(result);
            }
        }
    }

    protected String getAttachmentFileName(org.apache.http.HttpResponse resp) {
        String fileName = null;
        Header[] contentDisp = resp.getHeaders("content-disposition");
        if (contentDisp != null && contentDisp.length > 0) {
            HeaderElement[] headerElements = contentDisp[0].getElements();
            if (headerElements != null) {
                for (HeaderElement headerElement : headerElements) {
                    if ("attachment".equals(headerElement.getName())) {
                        NameValuePair param = headerElement.getParameterByName("filename");
                        if (param != null) {
                            fileName = param.getValue();
                            break;
                        }
                    }
                }
            }
        }
        return fileName;
    }

    protected void cleanupAfterRequest(org.apache.http.HttpResponse response, HttpRequestBase method) {
        method.reset();
        if (response instanceof CloseableHttpResponse) {
            try {
                ((CloseableHttpResponse)response).close();
            } catch (IOException e) {
                throw new RuntimeException("Unable to close connection", e);
            }
        }
    }

    protected long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * @return http client used to make calls.
     */
    public org.apache.http.client.HttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * @param httpClient http client used to make calls.
     */
    public void setHttpClient(org.apache.http.client.HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Custom DELETE entity, which allows a body to be sent.
     */
    public static class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
        public String getMethod() {
            return HttpDelete.METHOD_NAME;
        }

        public HttpDeleteWithBody(final String uri) {
            setURI(URI.create(uri));
        }
    }
}
