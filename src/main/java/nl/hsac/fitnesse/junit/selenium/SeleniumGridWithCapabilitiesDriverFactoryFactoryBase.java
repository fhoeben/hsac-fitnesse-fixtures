package nl.hsac.fitnesse.junit.selenium;

import nl.hsac.fitnesse.fixture.slim.web.SeleniumDriverSetup;
import nl.hsac.fitnesse.fixture.util.selenium.SeleniumHelper;

import java.net.MalformedURLException;
import java.util.Map;

/**
 * Base class for factories connecting to Selenium Grid, specifying capabilities to use.
 * Factories using it are configured by setting the system property 'seleniumGridUrl'.
 */
public abstract class SeleniumGridWithCapabilitiesDriverFactoryFactoryBase extends SeleniumDriverFactoryFactoryBase {
    @Override
    public SeleniumHelper.DriverFactory getDriverFactory() {
        final String gridUrl = getProperty(SELENIUM_GRID_URL);
        final Map<String, Object> capabilities = getCapabilities();
        return new SeleniumHelper.DriverFactory() {
            @Override
            public void createDriver() {
                SeleniumDriverSetup.unlockConfig();
                try {
                    new SeleniumDriverSetup().connectToDriverAtWithCapabilities(gridUrl, capabilities);
                } catch (MalformedURLException e) {
                    throw new RuntimeException("Unable to create driver at: "
                            + gridUrl + " with: " + capabilities, e);
                } finally {
                    SeleniumDriverSetup.lockConfig();
                }
            }
        };
    }

    protected abstract Map<String, Object> getCapabilities();
}
