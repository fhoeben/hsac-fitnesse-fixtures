package nl.hsac.fitnesse.junit.selenium;

import nl.hsac.fitnesse.fixture.util.selenium.driverfactory.DriverFactory;
import nl.hsac.fitnesse.fixture.util.selenium.driverfactory.ProjectDriverFactoryFactory;

import java.util.Map;

/**
 * Creates a Selenium driver factory to override the configuration in the wiki.
 * This factory is configured by setting the system property 'seleniumBrowser' and
 * not setting 'seleniumGridUrl'.
 * An (optional) profile can be specified using 'seleniumJsonProfile'
 */
public class LocalSeleniumDriverFactoryFactory extends SeleniumDriverFactoryFactoryBase {
    @Override
    public boolean willOverride() {
        return !isPropertySet(SELENIUM_GRID_URL)
                && isPropertySet(SELENIUM_BROWSER);
    }

    @Override
    public DriverFactory getDriverFactory() {
        final String browser = getProperty(SELENIUM_BROWSER);
        final Map<String, Object> profile = getProfile();
        return new ProjectDriverFactoryFactory().create(browser, profile);
    }

    protected Map<String, Object> getProfile() {
        return parseJsonProperty(SELENIUM_JSONPROFILE);
    }
}
