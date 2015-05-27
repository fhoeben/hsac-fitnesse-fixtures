package nl.hsac.fitnesse.fixture.util;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Formats XML.
 */
public class XMLFormatter implements Formatter {
    public static final Pattern DECL_PATTERN = Pattern.compile("^<\\?xml\\s.*?\\?>", Pattern.DOTALL);
    public static final Pattern ELEMENT_CONTENT_PATTERN = Pattern.compile(">\\s*(.*?)\\s*<", Pattern.DOTALL);

    private boolean trimElements = true;

    /**
     * Creates formatted version of the supplied XML.
     * @param xml XML to format.
     * @return formatted version.
     */
    public String format(String xml) {
        try {
            boolean keepDeclaration = DECL_PATTERN.matcher(xml).find();
            if (trimElements) {
                xml = trimElements(xml);
            }
            Source xmlInput = new StreamSource(new StringReader(xml));
            StreamResult xmlOutput = new StreamResult(new StringWriter());
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, keepDeclaration? "no" : "yes");
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return whether elements are trimmed on format.
     */
    public boolean areElementsTrimmed() {
        return trimElements;
    }

    /**
     * @param trimElements whether elements should be trimmed on format.
     */
    public void setTrimElements(boolean trimElements) {
        this.trimElements = trimElements;
    }

    /**
     * Removes both XML declaration and trims all elements.
     * @param xml XML to trim.
     * @return trimmed version.
     */
    public static String trim(String xml) {
        String content = removeDeclaration(xml);
        return trimElements(content);
    }

    /**
     * Removes XML declaration (if present).
     * @param xml XML to remove declaration from.
     * @return XML without declaration.
     */
    public static String removeDeclaration(String xml) {
        Matcher matcher = DECL_PATTERN.matcher(xml);
        return matcher.replaceFirst("");
    }

    /**
     * Removes whitespace before and after each elements (and the entire document).
     * @param xml XML to trim.
     * @return trimmed version.
     */
    public static String trimElements(String xml) {
        String result = xml.trim();
        Matcher matcher = ELEMENT_CONTENT_PATTERN.matcher(result);
        return matcher.replaceAll(">$1<");
    }
}
