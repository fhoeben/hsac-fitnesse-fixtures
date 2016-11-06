package nl.hsac.fitnesse.fixture.util;

import nl.hsac.fitnesse.fixture.Environment;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HttpResponse subclass intended to store a sequence of XmlHttpResponses which will be served (in sequence) to
 * clients of a mock HttpServer running inside FitNesse. The request received will be stored as the request in
 * each response before it is returned. Requests for which no response is configured will receive a 404 response,
 * the request is stored in a new XmlHttpResponse object.
 */
public class MockXmlHttpResponseSequence extends HttpResponse {
    private final List<XmlHttpResponse> responseList = new ArrayList<XmlHttpResponse>();
    private final Map<String, XmlHttpResponse> responsesForRequests = new HashMap<String, XmlHttpResponse>();
    private int currentIndex;

    public <T extends XmlHttpResponse> MockXmlHttpResponseSequence(T... responses) {
        this(responses == null ? null : Arrays.asList(responses));
    }

    public <T extends XmlHttpResponse> MockXmlHttpResponseSequence(Collection<T> responses) {
        this();
        if (responses != null) {
            responseList.addAll(responses);
        }
    }

    public MockXmlHttpResponseSequence() {
        resetCurrentIndex();
    }

    public XmlHttpResponse addResponse(String responseBody, String request) {
        XmlHttpResponse newResponse = new XmlHttpResponse();
        newResponse.setStatusCode(HttpStatus.SC_OK);
        newResponse.setResponse(responseBody);
        if (request == null) {
            responseList.add(newResponse);
        } else {
            responsesForRequests.put(request, newResponse);
        }
        return newResponse;
    }

    public List<? extends XmlHttpResponse> getResponseList() {
        return responseList;
    }

    public String getExtraRequestsMessage() {
        List<String> extraRequests = getNotExpected();
        return createUnexpectedMessage("%s extra request(s) received: %s", extraRequests);
    }

    public String getMissingRequestsMessage() {
        List<String> extraResponses = getNotCalled();
        return createUnexpectedMessage("%s response(s) not requested: %s", extraResponses);
    }

    protected String createUnexpectedMessage(String messagePattern, List<String> extraMessages) {
        String msg = null;
        if (!extraMessages.isEmpty()) {
            StringBuilder messageList = new StringBuilder("<ol>");
            for (String message : extraMessages) {
                messageList.append("<li>");
                String formattedMsg = formatValue(message);
                messageList.append(formattedMsg);
                messageList.append("</li>");
            }
            messageList.append("</ol>");
            msg = String.format("<div>" + messagePattern + "</div>",
                    extraMessages.size(), messageList);
        }
        return msg;
    }

    protected String formatValue(String value) {
        String result;
        try {
            result = Environment.getInstance().getHtmlForXml(value);
        } catch (Exception e) {
            result = value;
        }
        return result;
    }

    public List<String> getNotCalled() {
        List<String> result = new ArrayList<String>(1);
        for (XmlHttpResponse response : getResponseList()) {
            if (StringUtils.isEmpty(response.getRequest())) {
                result.add(response.getResponse());
            }
        }
        for (XmlHttpResponse responseForRequest : responsesForRequests.values()) {
            if (!getResponseList().contains(responseForRequest)) {
                result.add(responseForRequest.getResponse());
            }
        }
        return result;
    }

    public List<String> getNotExpected() {
        List<String> result = new ArrayList<String>(1);
        for (XmlHttpResponse response : getResponseList()) {
            if (StringUtils.isEmpty(response.getResponse())) {
                result.add(response.getRequest());
            }
        }
        return result;
    }

    public void resetCurrentIndex() {
        currentIndex = -1;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    @Override
    public String getResponse() {
        return currentResponse().getResponse();
    }

    @Override
    public int getStatusCode() {
        return currentResponse().getStatusCode();
    }

    @Override
    public void setRequest(String aRequest) {
        // when a new request is received we move to next element in list
        currentIndex++;
        if (responsesForRequests.containsKey(aRequest)) {
            responseList.add(currentIndex, responsesForRequests.get(aRequest));
        }
        currentResponse().setRequest(aRequest);
    }

    @Override
    public Map<String, Object> getResponseHeaders() {
        return currentResponse().getResponseHeaders();
    }

    protected XmlHttpResponse currentResponse() {
        if (currentIndex < 0) {
            throw new IllegalStateException("No 'current' response available. No requests received yet.");
        }
        if (currentIndex == responseList.size()) {
            // we allow capturing of requests, so that we can debug what is received
            XmlHttpResponse blankResponse = addResponse("", null);
            // not found
            blankResponse.setStatusCode(HttpStatus.SC_NOT_FOUND);
        }
        return getResponseList().get(currentIndex);
    }
}
