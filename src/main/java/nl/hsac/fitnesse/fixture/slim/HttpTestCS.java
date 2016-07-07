package nl.hsac.fitnesse.fixture.slim;


import java.util.Map;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import nl.hsac.fitnesse.fixture.util.HttpResponse;





/**
 * Fixture to make HTTP requests (With multiple cookies support) using Slim scripts and/or scenarios.
 */
public class HttpTestCS extends HttpTest {
   
	
	/**
     * @return headers received with response to last request.
     */
	
	
    public Map<String, String> responseHeaders() {
    	
    	HttpResponse response = super.getResponse();
    	Map<String, String> responseHeaders = super.responseHeaders();
    	StringBuilder sb = new StringBuilder();
    	CookieStore store = response.getCookieStore();
    	
    	if(store!=null)
    	{
	    	for(Cookie cookie : store.getCookies())
	    	{
	    		sb.append(cookie.getName() + "=" + cookie.getValue());
	    		sb.append(";");
	    	}
    	}
    	
    	responseHeaders.put("Set-Cookie", sb.toString());
    	return responseHeaders;
        
    }
    
    
    protected HttpResponse createResponse() {
    	HttpResponse response = new HttpResponse();
		response.setCookieStore(new BasicCookieStore());
    	return response;
    }
    

	
}
