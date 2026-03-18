package nl.hsac.fitnesse.fixture.util;

public class MultipartPart {
    private final Object data;
    private final String contentType;
    private final String fileName;

    public MultipartPart(Object data, String contentType, String fileName) {
        this.data = data;
        this.contentType = contentType;
        this.fileName = fileName;
    }

    public MultipartPart(Object data, String contentType) {
        this(data, contentType, null);
    }

    public Object getData() {
        return data;
    }

    public String getContentType() {
        return contentType;
    }

    public String getFileName() {
        return fileName;
    }
}
