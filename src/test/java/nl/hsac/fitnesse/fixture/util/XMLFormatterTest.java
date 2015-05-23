package nl.hsac.fitnesse.fixture.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class XMLFormatterTest {
    private XMLFormatter formatter = new XMLFormatter();

    @Test
    public void testDefaultNamespacePreserved() {
        String response = FileUtil.loadFile("GetWeatherSoapResponse.xml");
        String expected = FileUtil.loadFile("GetWeatherSoapResponseFormatted.xml");

        String formatted = formatter.format(response);

        assertEquals(expected.replace("\r", ""), formatted.replace("\r", ""));

    }

    @Test
    public void testTrimElements() {
        String expected = FileUtil.loadFile("GetWeatherSoapResponse.xml");
        String formatted = FileUtil.loadFile("GetWeatherSoapResponseFormatted.xml");

        String trimmed = XMLFormatter.trimElements(formatted);

        assertEquals(expected, trimmed);
        assertEquals(expected, XMLFormatter.trimElements(trimmed));
    }

    @Test
    public void testTrim() {
        String expected = FileUtil.loadFile("GetWeatherSoapResponse.xml")
                            .replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
        String formatted = FileUtil.loadFile("GetWeatherSoapResponseFormatted.xml");

        String trimmed = XMLFormatter.trim(formatted);

        assertEquals(expected, trimmed);
    }
}
