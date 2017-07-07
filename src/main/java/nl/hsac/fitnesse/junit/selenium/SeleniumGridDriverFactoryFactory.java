package nl.hsac.fitnesse.junit.selenium;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Creates a Selenium driver factory to override the configuration in the wiki.
 * This factory is configured by setting the system property 'seleniumGridUrl' AND 'seleniumCapabilities'.
 */
public class SeleniumGridDriverFactoryFactory extends SeleniumGridWithCapabilitiesDriverFactoryFactoryBase {
    @Override
    public boolean willOverride() {
        return isPropertySet(SELENIUM_GRID_URL)
                && !isPropertySet(SELENIUM_JSONCAPABILITIES)
                && isPropertySet(SELENIUM_CAPABILITIES);
    }

    @Override
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
