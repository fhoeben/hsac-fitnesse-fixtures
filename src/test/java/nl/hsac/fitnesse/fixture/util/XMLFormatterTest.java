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

        assertEquals(expected, formatted);

    }
}
