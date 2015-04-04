package nl.hsac.fitnesse.junit.selenium;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class SeleniumGridDriverFactoryFactoryTest {
    private SeleniumGridDriverFactoryFactory factoryFactory = new SeleniumGridDriverFactoryFactory();

    @Test
    public void testCapabilitiesParseBasic() {
        String propertyValue = "browser: chrome, platform: Windows 8.1";
        Map<String, String> capa = factoryFactory.parseCapabilities(propertyValue);
        assertEquals("chrome", capa.get("browser"));
        assertEquals("Windows 8.1", capa.get("platform"));
    }

    @Test
    public void testCapabilitiesParseWithQuotes() {
        String propertyValue = "\"browser:chrome,platform:Windows 8.1\"";
        Map<String, String> capa = factoryFactory.parseCapabilities(propertyValue);
        assertEquals("chrome", capa.get("browser"));
        assertEquals("Windows 8.1", capa.get("platform"));
    }

    @Test
    public void testCapabilitiesParseWithColonInValue() {
        String propertyValue = "\"browser:chrome,platform:Windows 8.1, location: D:\\Program Files\\Chromium\\chromium.exe\"";
        Map<String, String> capa = factoryFactory.parseCapabilities(propertyValue);
        assertEquals("chrome", capa.get("browser"));
        assertEquals("Windows 8.1", capa.get("platform"));
        assertEquals("D:\\Program Files\\Chromium\\chromium.exe", capa.get("location"));
    }
}
