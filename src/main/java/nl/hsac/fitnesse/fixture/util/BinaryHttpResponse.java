package nl.hsac.fitnesse.fixture.util;

import org.apache.commons.io.FilenameUtils;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.net.MalformedURLException;
import java.net.URL;

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
        if (fileName == null) {
            try {
                String reqUrl = getRequest();
                URL url = new URL(reqUrl);
                String path = url.getPath();
                fileName = FilenameUtils.getName(path);
            } catch (MalformedURLException e) {
                // ignore
            }
        }
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
