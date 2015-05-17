package nl.hsac.fitnesse.fixture.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import nl.hsac.fitnesse.fixture.util.HttpResponse;
import nl.hsac.fitnesse.fixture.util.XmlHttpResponse;

import org.apache.http.HttpStatus;

/**
 * HttpResponse subclass intended to store a sequence of XmlHttpResponses which will be served (in sequence) to
 * clients of a mock HttpServer running inside FitNesse. The request received will be stored as the request in
 * each response before it is returned. Requests for which no response is configured will receive a 404 response,
 * the request is stored in a new XmlHttpResponse object.
 */
public class MockXmlHttpResponseSequence extends HttpResponse {
    private final List<XmlHttpResponse> responseList = new ArrayList<XmlHttpResponse>();
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

    public List<XmlHttpResponse> getResponseList() {
        return responseList;
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
        currentResponse().setRequest(aRequest);
    }

    protected XmlHttpResponse currentResponse() {
        if (currentIndex < 0) {
            throw new IllegalStateException("No 'current' response available. No requests received yet.");
        }
        if (currentIndex == responseList.size()) {
            // we allow capturing of requests, so that we can debug what is received
            XmlHttpResponse blankResponse = new XmlHttpResponse();
            // not found
            blankResponse.setStatusCode(HttpStatus.SC_NOT_FOUND);
            blankResponse.setResponse("");
            responseList.add(blankResponse);
        }
        return responseList.get(currentIndex);
    }
}
