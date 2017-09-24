package nl.hsac.fitnesse.junit.selenium;

import nl.hsac.fitnesse.fixture.util.selenium.driverfactory.DriverFactory;
import nl.hsac.fitnesse.fixture.util.selenium.driverfactory.RemoteDriverFactory;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Creates a Selenium driver factory to override the configuration in the wiki.
 * This factory is configured by setting the system property 'seleniumGridUrl' AND 'seleniumBrowser'.
 */
public class SimpleSeleniumGridDriverFactoryFactory extends SeleniumDriverFactoryFactoryBase {
    @Override
    public boolean willOverride() {
        return isPropertySet(SELENIUM_GRID_URL)
                && isPropertySet(SELENIUM_BROWSER);
    }

    @Override
    public DriverFactory getDriverFactory() {
        String gridUrl = getProperty(SELENIUM_GRID_URL);
        String browser = getProperty(SELENIUM_BROWSER);
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities(browser, "", Platform.ANY);
        return new RemoteDriverFactory<>(RemoteWebDriver::new, gridUrl, desiredCapabilities);
    }
}
