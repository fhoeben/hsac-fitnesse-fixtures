package nl.hsac.fitnesse.fixture.util;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;

/**
 * Validates XML.
 */
public class XMLValidator {
    private final static XMLInputFactory XML_INPUT_FACTORY = XMLInputFactory.newInstance();

    /**
     * Checks whether supplied string contains a well formed XML document.
     * @param xml xml document
     * @return true if xml is well formed.
     */
    public boolean checkWelformed(String xml)  {
        boolean result;
        try {
            isWelformed(xml);
            result = true;
        } catch (RuntimeException e) {
            result = false;
        }
        return result;
    }

    /**
     * Checks whether supplied string contains a well formed XML document.
     * @param xml xml document
     * @throws RuntimeException when xml could not be parsed.
     */
    public void isWelformed(String xml) {
        StringReader reader = new StringReader(xml);
        XMLStreamReader r = null;
        try {
            r = XML_INPUT_FACTORY.createXMLStreamReader(reader);
            while (r.hasNext()) {
                r.next();
            }
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        } finally {
            if (r != null) {
                try {
                    r.close();
                } catch (Exception e) {
                    // should not happen
                }
            }
            reader.close();
        }
    }
}
