package nl.hsac.fitnesse.fixture.util;

import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;

/**
 * Formats XML.
 */
public class XMLFormatter {
    /**
     * Creates formatted version of the supplied XML.
     * @param xml XML to format.
     * @return formatted version.
     */
    public String format(String xml) {
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            Node document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is).getDocumentElement();
            boolean keepDeclaration = xml.startsWith("<?xml");
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
            LSSerializer writer = impl.createLSSerializer();

            writer.getDomConfig().setParameter("format-pretty-print", true); 
            writer.getDomConfig().setParameter("xml-declaration", keepDeclaration);
            return writer.writeToString(document).replace(" encoding=\"UTF-16\"?", " encoding=\"UTF-8\"?");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
