package nl.hsac.fitnesse.fixture.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BinaryHttpResponseTest {
    private final String reqUrl = "http://www.debilt.nl/~/media/Wonen%20en%20Leven/Afval%20en%20Milieu/afvalkalender2015pdf.pdf";

    @Test
    public void testFileNameOverridesUrl() {
        BinaryHttpResponse response = new BinaryHttpResponse();
        response.setRequest(reqUrl);
        response.setFileName("hallo.pdf");
        assertEquals("hallo.pdf", response.getFileName());
    }

    @Test
    public void testFileNameFromUrl() {
        BinaryHttpResponse response = new BinaryHttpResponse();
        response.setRequest(reqUrl);
        assertEquals("afvalkalender2015pdf.pdf", response.getFileName());
    }

    @Test
    public void testFileNameFromUrlWithQuery() {
        BinaryHttpResponse response = new BinaryHttpResponse();
        response.setRequest(reqUrl + "?hallo=false");
        assertEquals("afvalkalender2015pdf.pdf", response.getFileName());
    }
}
