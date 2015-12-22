package nl.hsac.fitnesse.junit.selenium;

import org.junit.AfterClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LocalSeleniumDriverFactoryFactoryTest {
    private static final String ORIGINAL_PROP_VALUE = System.getProperty(SeleniumDriverFactoryFactoryBase.SELENIUM_BROWSER);

    private LocalSeleniumDriverFactoryFactory factoryFactory = new LocalSeleniumDriverFactoryFactory();

    @AfterClass
    public static void cleanUp() {
        if (ORIGINAL_PROP_VALUE == null) {
            System.clearProperty(SeleniumDriverFactoryFactoryBase.SELENIUM_BROWSER);
        } else {
            setBrowser(ORIGINAL_PROP_VALUE);
        }
    }

    @Test
    public void testSupportNull() {
        System.clearProperty(SeleniumDriverFactoryFactoryBase.SELENIUM_BROWSER);
        assertNull(factoryFactory.getProperty(SeleniumDriverFactoryFactoryBase.SELENIUM_BROWSER));
    }

    @Test
    public void testBasic() {
        setBrowser("chrome");
        assertEquals("chrome", factoryFactory.getProperty(SeleniumDriverFactoryFactoryBase.SELENIUM_BROWSER));
    }

    @Test
    public void testWithSpace() {
        setBrowser("internet explorer");
        assertEquals("internet explorer", factoryFactory.getProperty(SeleniumDriverFactoryFactoryBase.SELENIUM_BROWSER));
    }

    @Test
    public void testWithSpaceAndQuotes() {
        setBrowser("\"internet explorer\"");
        assertEquals("internet explorer", factoryFactory.getProperty(SeleniumDriverFactoryFactoryBase.SELENIUM_BROWSER));
    }

    private static void setBrowser(String value) {
        System.setProperty(SeleniumDriverFactoryFactoryBase.SELENIUM_BROWSER, value);
    }
}
