package nl.hsac.fitnesse.fixture.util;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class BinaryHttpResponse extends HttpResponse {
    private byte[] responseContent;
    private String fileName;

    public byte[] getResponseContent() {
        return responseContent;
    }

    public void setResponseContent(byte[] aResponseContent) {
        responseContent = aResponseContent;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String aFileName) {
        this.fileName = aFileName;
    }

    @Override
    public String getResponse() {
        String result = null;
        if (responseContent != null) {
            result = new BASE64Encoder().encode(responseContent);
        } else {
            result = super.getResponse();
        }
        return result;
    }

    @Override
    public void setResponse(String aResponse) {
        if (aResponse != null) {
            try {
                byte[] content = new BASE64Decoder().decodeBuffer(aResponse);
                responseContent = content;
            } catch (Exception e) {
                responseContent = null;
            }
        } else {
            responseContent = null;
        }
        super.setResponse(aResponse);
    }

    @Override
    public String toString() {
        return getClass().getName() + ": " + getRequest();
    }
}
