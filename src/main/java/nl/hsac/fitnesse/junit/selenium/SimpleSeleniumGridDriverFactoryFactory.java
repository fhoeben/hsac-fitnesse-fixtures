package nl.hsac.fitnesse.junit.selenium;

import nl.hsac.fitnesse.fixture.slim.web.SeleniumDriverSetup;
import nl.hsac.fitnesse.fixture.util.selenium.SeleniumHelper;

import java.net.MalformedURLException;

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
        final String gridUrl = getProperty(SELENIUM_GRID_URL);
        final String browser = getProperty(SELENIUM_BROWSER);
        return new SeleniumHelper.DriverFactory() {
            @Override
            public void createDriver() {
                SeleniumDriverSetup.unlockConfig();
                try {
                    new SeleniumDriverSetup().connectToDriverForAt(browser, gridUrl);
                } catch (MalformedURLException e) {
                    throw new RuntimeException("Unable to create driver at hub: "
                            + gridUrl + " for: " +browser, e);
                } finally {
                    SeleniumDriverSetup.lockConfig();
                }
            }
        };
    }
}
