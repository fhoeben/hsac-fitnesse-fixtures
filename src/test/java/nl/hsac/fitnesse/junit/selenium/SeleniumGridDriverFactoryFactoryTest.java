package nl.hsac.fitnesse.junit.selenium;

import org.junit.AfterClass;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class SeleniumGridDriverFactoryFactoryTest {
    private static final String ORIGINAL_PROP_VALUE = System.getProperty(SeleniumDriverFactoryFactoryBase.SELENIUM_CAPABILITIES);

    private SeleniumGridDriverFactoryFactory factoryFactory = new SeleniumGridDriverFactoryFactory();

    @AfterClass
    public static void cleanUp() {
        if (ORIGINAL_PROP_VALUE == null) {
            System.clearProperty(SeleniumDriverFactoryFactoryBase.SELENIUM_CAPABILITIES);
        } else {
            setCapabilities(ORIGINAL_PROP_VALUE);
        }
    }

    @Test
    public void testCapabilitiesParseBasic() {
        setCapabilities("browser: chrome, platform: Windows 8.1");
        Map<String, Object> capa = factoryFactory.getCapabilities();
        assertEquals("chrome", capa.get("browser"));
        assertEquals("Windows 8.1", capa.get("platform"));
    }

    @Test
    public void testCapabilitiesParseWithQuotes() {
        setCapabilities("\"browser:chrome,platform:Windows 8.1\"");
        Map<String, Object> capa = factoryFactory.getCapabilities();
        assertEquals("chrome", capa.get("browser"));
        assertEquals("Windows 8.1", capa.get("platform"));
    }

    @Test
    public void testCapabilitiesParseWithColonInValue() {
        setCapabilities("\"browser:chrome,platform:Windows 8.1, location: D:\\Program Files\\Chromium\\chromium.exe\"");
        Map<String, Object> capa = factoryFactory.getCapabilities();
        assertEquals("chrome", capa.get("browser"));
        assertEquals("Windows 8.1", capa.get("platform"));
        assertEquals("D:\\Program Files\\Chromium\\chromium.exe", capa.get("location"));
    }

    private static void setCapabilities(String value) {
        System.setProperty(SeleniumDriverFactoryFactoryBase.SELENIUM_CAPABILITIES, value);
    }
}
