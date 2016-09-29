package nl.hsac.fitnesse.junit.selenium;

import nl.hsac.fitnesse.fixture.slim.web.SeleniumDriverSetup;
import nl.hsac.fitnesse.fixture.util.selenium.SeleniumHelper;

import java.net.MalformedURLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Creates a Selenium driver factory to override the configuration in the wiki.
 * This factory is configured by setting the system property 'seleniumGridUrl' AND 'seleniumCapabilities'.
 */
public class SeleniumGridDriverFactoryFactory extends SeleniumDriverFactoryFactoryBase {
    @Override
    public boolean willOverride() {
        return isPropertySet(SELENIUM_GRID_URL)
                && !isPropertySet(SELENIUM_JSONCAPABILITIES)
                && isPropertySet(SELENIUM_CAPABILITIES);
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
        String capabilitiesString = getProperty(SELENIUM_CAPABILITIES);
        try {
            Map<String, Object> result = new LinkedHashMap<String, Object>();
            String[] capas = capabilitiesString.split(",");
            for (String capa : capas) {
                String[] kv = capa.split(":");
                String key = kv[0].trim();
                String value = "";
                if (kv.length > 1) {
                    value = capa.substring(capa.indexOf(":") + 1).trim();
                }
                result.put(key, value);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse Selenium capabilities: " + capabilitiesString
                    + "\nExpected format: key:value(, key:value)*", e);
        }
    }
}
