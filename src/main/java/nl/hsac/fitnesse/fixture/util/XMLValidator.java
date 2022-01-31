package nl.hsac.fitnesse.fixture.util;

import nl.hsac.fitnesse.fixture.slim.SlimFixtureException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
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

    /**
     * Validates xml against an xsd
     * @param xmlContent XML to validate
     * @param xsdContent the XSD schema file to use for validation
     * @return true if the xml validates against the schema.
     * Throws a descriptive SlimFixtureException when validation fails
     */
    public boolean validateAgainst(String xmlContent, String xsdContent) {
        try {
            Source xsd = new SAXSource(new InputSource(new StringReader(xsdContent)));
            Source xml = new SAXSource(new InputSource(new StringReader(xmlContent)));

            SchemaFactory schemaFactory = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(xsd);

            Validator validator = schema.newValidator();
            validator.validate(xml);
            return true;
        } catch (SAXException e) {
            throw new SlimFixtureException(false, "XML Validation failed: " + e.getMessage());
        } catch (IOException e) {
            throw new SlimFixtureException(e);
        }
    }
}
