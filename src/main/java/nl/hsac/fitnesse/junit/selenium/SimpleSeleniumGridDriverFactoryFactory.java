package nl.hsac.fitnesse.junit.selenium;

import nl.hsac.fitnesse.fixture.slim.web.SeleniumDriverSetup;
import nl.hsac.fitnesse.fixture.util.selenium.SeleniumHelper;
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
    public SeleniumHelper.DriverFactory getDriverFactory() {
        String gridUrl = getProperty(SELENIUM_GRID_URL);
        String browser = getProperty(SELENIUM_BROWSER);
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities(browser, "", Platform.ANY);
        return SeleniumDriverSetup.getDriverFactory(RemoteWebDriver::new, gridUrl, desiredCapabilities);
    }
}
