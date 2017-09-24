package nl.hsac.fitnesse.junit.selenium;

import nl.hsac.fitnesse.fixture.util.selenium.driverfactory.DriverFactory;
import nl.hsac.fitnesse.fixture.util.selenium.driverfactory.LocalDriverFactory;

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
    public DriverFactory getDriverFactory() {
        final String driverClass = getProperty(SELENIUM_DRIVER_CLASS);
        return new LocalDriverFactory(driverClass, null);
    }
}
