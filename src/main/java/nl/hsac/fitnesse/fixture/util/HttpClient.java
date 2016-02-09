package nl.hsac.fitnesse.fixture.util;

import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.http.util.TextUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Map;
/**
 * Helper to make Http calls and get response.
 */
public class HttpClient {
    private final static org.apache.http.client.HttpClient HTTP_CLIENT;

    public static boolean followRedirect;

    static {

        HTTP_CLIENT = HttpClients.custom()
                .useSystemProperties()
                .disableContentCompression()
                .setUserAgent(HttpClient.class.getName())
                .setRedirectStrategy(new DefaultRedirectStrategy() {
                    @Override public boolean isRedirected(HttpRequest request, org.apache.http.HttpResponse response, HttpContext context) throws ProtocolException {
                        boolean isRedirect = super.isRedirected(request, response, context);
                        if(isRedirect && !followRedirect) {
                            return false;
                        }
                        if (!isRedirect && followRedirect) {
                            int responseCode = response.getStatusLine().getStatusCode();
                            if (responseCode == HttpStatus.SC_MOVED_PERMANENTLY || responseCode == HttpStatus.SC_MOVED_TEMPORARILY) {
                                return true;
                            }
                        }
                        return isRedirect;
                    }


                })
                .build();
    }

    public URI createLocationURI(final String location) throws ProtocolException {
        try {
            final URIBuilder b = new URIBuilder(new URI(location).normalize());
            final String host = b.getHost();
            if (host != null) {
                b.setHost(host.toLowerCase(Locale.ENGLISH));
            }
            final String path = b.getPath();
            if (TextUtils.isEmpty(path)) {
                b.setPath("/");
            }
            return b.build();
        } catch (final URISyntaxException ex) {
            throw new ProtocolException("Invalid redirect URI: " + location, ex);
        }
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
     * @param url URL of service
     * @param response response to be filled.
     * @param headers http headers to add
     */
    public void get(String url, HttpResponse response, Map<String, Object> headers, boolean followRedirect) {
        HttpGet method = new HttpGet(url);
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

    protected void getResponse(String url, HttpResponse response, HttpRequestBase method, Map<String, Object> headers) {
        try {
            if (headers != null) {
                for (String key : headers.keySet()) {
                    Object value = headers.get(key);
                    if (value != null) {
                        method.setHeader(key, value.toString());
                    }
                }
            }
            org.apache.http.HttpResponse resp;
            CookieStore store = response.getCookieStore();
            if (store == null) {
                resp = getHttpResponse(url, method);
            } else {
                resp = getHttpResponse(store, url, method);
            }
            int returnCode = resp.getStatusLine().getStatusCode();
            response.setStatusCode(returnCode);
            HttpEntity entity = resp.getEntity();

            Map<String, String> responseHeaders = response.getResponseHeaders();
            for (Header h : resp.getAllHeaders()) {
                responseHeaders.put(h.getName(), h.getValue());
            }

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
        } catch (Exception e) {
            throw new RuntimeException("Unable to get response from: " + url, e);
        } finally {
            method.reset();
        }
    }

    private String getAttachmentFileName(org.apache.http.HttpResponse resp) {
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

    protected org.apache.http.HttpResponse getHttpResponse(CookieStore store, String url, HttpRequestBase method) throws IOException {
        HttpContext localContext = new BasicHttpContext();
        localContext.setAttribute(HttpClientContext.COOKIE_STORE, store);
        return HTTP_CLIENT.execute(method, localContext);
    }

    protected org.apache.http.HttpResponse getHttpResponse(String url, HttpRequestBase method) throws IOException {
        return HTTP_CLIENT.execute(method);
    }
}
