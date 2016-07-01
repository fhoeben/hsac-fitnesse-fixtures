package nl.hsac.fitnesse.junit.selenium;

import com.google.gson.Gson;
import nl.hsac.fitnesse.fixture.slim.web.SeleniumDriverSetup;
import nl.hsac.fitnesse.fixture.util.selenium.SeleniumHelper;
import nl.hsac.fitnesse.fixture.Environment;

import java.net.MalformedURLException;
import java.util.Map;

/**
 * Creates a Selenium driver factory to override the configuration in the wiki.
 * This factory is configured by setting the system property 'seleniumGridUrl' AND 'seleniumJsonCapabilities'.
 */
public class SeleniumJsonGridDriverFactoryFactory extends SeleniumDriverFactoryFactoryBase {
    @Override
    public boolean willOverride() {
        return isPropertySet(SELENIUM_GRID_URL)
                && !isPropertySet(SELENIUM_CAPABILITIES)
                && isPropertySet(SELENIUM_JSONCAPABILITIES);
    }

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

    protected Map<String, Object> getCapabilities() {
        String capabilitiesString = getProperty(SELENIUM_JSONCAPABILITIES);
        try {
            return Environment.getInstance().getJsonHelper().jsonStringToMap(capabilitiesString);
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse Selenium capabilities: " + capabilitiesString, e);
        }
    }
}
