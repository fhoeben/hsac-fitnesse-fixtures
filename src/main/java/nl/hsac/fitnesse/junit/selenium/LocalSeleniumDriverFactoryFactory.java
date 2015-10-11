package nl.hsac.fitnesse.junit.selenium;

import nl.hsac.fitnesse.fixture.slim.web.SeleniumDriverSetup;
import nl.hsac.fitnesse.fixture.util.selenium.SeleniumHelper;

/**
 * Creates a Selenium driver factory to override the configuration in the wiki.
 * This factory is configured by setting the system property 'seleniumBrowser' and
 * not setting 'seleniumGridUrl'.
 */
public class LocalSeleniumDriverFactoryFactory extends SeleniumDriverFactoryFactoryBase {
    @Override
    public boolean willOverride() {
        return !isPropertySet(SELENIUM_GRID_URL)
                && isPropertySet(SELENIUM_BROWSER);
    }

    @Override
    public SeleniumHelper.DriverFactory getDriverFactory() {
        final String browser = getProperty(SELENIUM_BROWSER);
        return new SeleniumHelper.DriverFactory() {
            @Override
            public void createDriver() {
                SeleniumDriverSetup.unlockConfig();
                try {
                    new SeleniumDriverSetup().startDriverFor(browser);
                } catch (Exception e) {
                    throw new RuntimeException("Unable to create local driver for: " +browser, e);
                } finally {
                    SeleniumDriverSetup.lockConfig();
                }
            }
        };
    }
}
