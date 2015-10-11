package nl.hsac.fitnesse.junit.selenium;

import nl.hsac.fitnesse.fixture.slim.web.SeleniumDriverSetup;
import nl.hsac.fitnesse.fixture.util.selenium.SeleniumHelper;

/**
 * Creates a Selenium driver factory to override the configuration in the wiki.
 * This factory is configured by setting the system property 'seleniumDriverClass' and
 * not setting 'seleniumGridUrl' or 'seleniumBrowser'.
 */
public class LocalSeleniumDriverClassFactoryFactory extends SeleniumDriverFactoryFactoryBase {
    @Override
    public boolean willOverride() {
        return !isPropertySet(SELENIUM_GRID_URL)
                && !isPropertySet(SELENIUM_BROWSER)
                && isPropertySet(SELENIUM_DRIVER_CLASS);
    }

    @Override
    public SeleniumHelper.DriverFactory getDriverFactory() {
        final String driverClass = getProperty(SELENIUM_DRIVER_CLASS);
        return new SeleniumHelper.DriverFactory() {
            @Override
            public void createDriver() {
                SeleniumDriverSetup.unlockConfig();
                try {
                    new SeleniumDriverSetup().startDriver(driverClass);
                } catch (Exception e) {
                    throw new RuntimeException("Unable to create local driver for: " + driverClass, e);
                } finally {
                    SeleniumDriverSetup.lockConfig();
                }
            }
        };
    }
}
