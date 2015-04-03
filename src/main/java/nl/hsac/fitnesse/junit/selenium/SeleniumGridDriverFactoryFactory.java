package nl.hsac.fitnesse.junit.selenium;

import nl.hsac.fitnesse.fixture.slim.web.SeleniumDriverSetup;
import nl.hsac.fitnesse.fixture.util.SeleniumHelper;

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
        return isPropertySet(seleniumOverrideUrlVariableName)
                && isPropertySet(seleniumOverrideCapabilitiesVariableName);
    }

    @Override
    public SeleniumHelper.DriverFactory getDriverFactory() {
        final String gridUrl = System.getProperty(seleniumOverrideUrlVariableName);
        final String capabilitiesString = System.getProperty(seleniumOverrideCapabilitiesVariableName);
        final Map<String, String> capabilities = parseCapabilities(capabilitiesString);
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

    protected Map<String, String> parseCapabilities(String capabilitiesString) {
        try {
            Map<String, String> result = new LinkedHashMap<String, String>();
            if (capabilitiesString.startsWith("\"") && capabilitiesString.endsWith("\"")) {
                capabilitiesString = capabilitiesString.substring(1, capabilitiesString.length() - 2);
            }
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
