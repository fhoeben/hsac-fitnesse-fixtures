package nl.hsac.fitnesse.junit.selenium;

import java.util.Map;

/**
 * Creates a Selenium driver factory to override the configuration in the wiki.
 * This factory is configured by setting the system property 'seleniumGridUrl' AND 'seleniumJsonCapabilities'.
 */
public class SeleniumJsonGridDriverFactoryFactory extends SeleniumGridWithCapabilitiesDriverFactoryFactoryBase {
    @Override
    public boolean willOverride() {
        return isPropertySet(SELENIUM_GRID_URL)
                && !isPropertySet(SELENIUM_CAPABILITIES)
                && isPropertySet(SELENIUM_JSONCAPABILITIES);
    }

    @Override
    protected Map<String, Object> getCapabilities() {
        return parseJsonProperty(SELENIUM_JSONCAPABILITIES);
    }
}
