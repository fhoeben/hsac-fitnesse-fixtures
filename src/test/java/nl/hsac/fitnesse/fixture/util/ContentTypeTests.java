package nl.hsac.fitnesse.fixture.util;

import nl.hsac.fitnesse.fixture.slim.HttpTest;
import org.apache.http.entity.ContentType;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;

public class ContentTypeTests {
    @Test
    public void testXMLParse() {
        ContentType contentType = ContentType.parse(XmlHttpResponse.CONTENT_TYPE_XML_TEXT_UTF8);
        assertEquals(Charset.forName("UTF-8"), contentType.getCharset());
        assertEquals(ContentType.TEXT_XML.getMimeType(), contentType.getMimeType());
    }

    @Test
    public void testFormEncodedParse() {
        ContentType contentType = ContentType.parse(HttpTest.DEFAULT_POST_CONTENT_TYPE);
        assertEquals(Charset.forName("UTF-8"), contentType.getCharset());
        assertEquals(ContentType.APPLICATION_FORM_URLENCODED.getMimeType(), contentType.getMimeType());
    }

}
