package nl.hsac.fitnesse.junit.selenium;

import org.junit.AfterClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LocalSeleniumDriverFactoryFactoryTest {
    private static final String ORIGINAL_PROP_VALUE = System.getProperty(SeleniumDriverFactoryFactoryBase.seleniumOverrideBrowserVariableName);

    private LocalSeleniumDriverFactoryFactory factoryFactory = new LocalSeleniumDriverFactoryFactory();

    @AfterClass
    public static void cleanUp() {
        if (ORIGINAL_PROP_VALUE == null) {
            System.clearProperty(SeleniumDriverFactoryFactoryBase.seleniumOverrideBrowserVariableName);
        } else {
            setBrowser(ORIGINAL_PROP_VALUE);
        }
    }

    @Test
    public void testBasic() {
        setBrowser("chrome");
        assertEquals("chrome", factoryFactory.getProperty(SeleniumDriverFactoryFactoryBase.seleniumOverrideBrowserVariableName));
    }

    @Test
    public void testWithSpace() {
        setBrowser("internet explorer");
        assertEquals("internet explorer", factoryFactory.getProperty(SeleniumDriverFactoryFactoryBase.seleniumOverrideBrowserVariableName));
    }

    @Test
    public void testWithSpaceAndQuotes() {
        setBrowser("\"internet explorer\"");
        assertEquals("internet explorer", factoryFactory.getProperty(SeleniumDriverFactoryFactoryBase.seleniumOverrideBrowserVariableName));
    }

    private static void setBrowser(String value) {
        System.setProperty(SeleniumDriverFactoryFactoryBase.seleniumOverrideBrowserVariableName, value);
    }
}
