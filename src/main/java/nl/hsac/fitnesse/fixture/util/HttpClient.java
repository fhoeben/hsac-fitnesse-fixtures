package nl.hsac.fitnesse.fixture.util;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DecompressingHttpClient;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Helper to make Http calls and get response.
 */
public class HttpClient {
    final static ContentType TYPE = ContentType.create(ContentType.TEXT_XML.getMimeType(), Consts.UTF_8);
    private final static org.apache.http.client.HttpClient HTTP_CLIENT;

    static {
        SystemDefaultHttpClient backend = new SystemDefaultHttpClient();
        HTTP_CLIENT = new DecompressingHttpClient(backend);
        HTTP_CLIENT.getParams().setParameter("http.useragent", HttpClient.class.getName());

    }

    /**
     * @param url URL of service
     * @param response response pre-populated with request to send. Response content and
     *          statusCode will be filled.
     */
    public void post(String url, HttpResponse response) {
        HttpPost methodPost = new HttpPost(url);
        HttpEntity ent = new StringEntity(response.getRequest(), TYPE);
        methodPost.setEntity(ent);
        getResponse(url, response, methodPost);
    }

    /**
     * @param url URL of service
     * @param response response to be filled.
     */
    public void get(String url, XmlHttpResponse response) {
        HttpGet method = new HttpGet(url);
        getResponse(url, response, method);
    }

    /**
     * @param url URL of service
     * @param response response to be filled.
     */
    public void get(String url, HttpResponse response) {
        HttpGet method = new HttpGet(url);
        getResponse(url, response, method);
    }

    private void getResponse(String url, HttpResponse response, HttpRequestBase method) {
        try {
            org.apache.http.HttpResponse resp = getHttpResponse(url, method);
            int returnCode = resp.getStatusLine().getStatusCode();
            response.setStatusCode(returnCode);
            String result = EntityUtils.toString(resp.getEntity());
            response.setResponse(result);
        } catch (Exception e) {
            throw new RuntimeException("Unable to get response from: " + url, e);
        } finally {
            method.reset();
        }
    }

    protected org.apache.http.HttpResponse getHttpResponse(String url, HttpRequestBase method) throws IOException {
        return HTTP_CLIENT.execute(method);
    }
}
