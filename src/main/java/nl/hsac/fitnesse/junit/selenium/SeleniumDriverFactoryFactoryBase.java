package nl.hsac.fitnesse.junit.selenium;

import nl.hsac.fitnesse.fixture.Environment;
import nl.hsac.fitnesse.fixture.util.selenium.driverfactory.DriverFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Base class for Selenium driver factory factories.
 */
public abstract class SeleniumDriverFactoryFactoryBase implements SeleniumDriverFactoryFactory {
    public final static String SELENIUM_GRID_URL = "seleniumGridUrl";
    public final static String SELENIUM_BROWSER = "seleniumBrowser";
    public final static String SELENIUM_DRIVER_CLASS = "seleniumDriverClass";
    public final static String SELENIUM_CAPABILITIES = "seleniumCapabilities";
    public final static String SELENIUM_JSONCAPABILITIES = "seleniumJsonCapabilities";
    public final static String SELENIUM_JSONPROFILE = "seleniumJsonProfile";

    @Override
    public abstract boolean willOverride();

    @Override
    public abstract DriverFactory getDriverFactory();

    protected boolean isPropertySet(String propertyName) {
        String value = getProperty(propertyName);
        return !StringUtils.isEmpty(value);
    }

    protected String getProperty(String propertyName) {
        String value = System.getProperty(propertyName);
        if (value != null && value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }

    protected Map<String, Object> parseJsonProperty(String propertyName) {
        String capabilitiesString = getProperty(propertyName);
        try {
            return Environment.getInstance().getJsonHelper().jsonStringToMap(capabilitiesString);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse value of JSON property: " + propertyName +
                    ". Value was: " + capabilitiesString, e);
        }
    }
}
