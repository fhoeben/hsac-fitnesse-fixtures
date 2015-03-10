package nl.hsac.fitnesse.fixture.util;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Formats XML.
 */
public class XMLFormatter implements Formatter {
    /**
     * Creates formatted version of the supplied XML.
     * @param xml XML to format.
     * @return formatted version.
     */
    public String format(String xml) {
        try {
            boolean keepDeclaration = xml.startsWith("<?xml");

            Source xmlInput = new StreamSource(new StringReader(xml));
            StreamResult xmlOutput = new StreamResult(new StringWriter());
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            if (keepDeclaration) {
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            }
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
