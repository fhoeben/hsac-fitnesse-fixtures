package nl.hsac.fitnesse.junit.selenium;

import nl.hsac.fitnesse.fixture.util.SeleniumHelper;
import org.apache.commons.lang3.StringUtils;

/**
 * Base class for Selenium driver factory factories.
 */
public abstract class SeleniumDriverFactoryFactoryBase implements SeleniumDriverFactoryFactory {
    public final static String seleniumOverrideUrlVariableName = "seleniumGridUrl";
    public final static String seleniumOverrideBrowserVariableName = "seleniumBrowser";
    public final static String seleniumOverrideCapabilitiesVariableName = "seleniumCapabilities";

    @Override
    public abstract boolean willOverride();

    @Override
    public abstract SeleniumHelper.DriverFactory getDriverFactory();

    protected boolean isPropertySet(String propertyName) {
        String capabilitiesString = System.getProperty(propertyName);
        return !StringUtils.isEmpty(capabilitiesString);
    }
}
