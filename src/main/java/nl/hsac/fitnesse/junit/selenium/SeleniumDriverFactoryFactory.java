package nl.hsac.fitnesse.junit.selenium;

import nl.hsac.fitnesse.fixture.util.selenium.SeleniumHelper;

/**
 * Creates a Selenium driver factory to override the configuration in the wiki.
 */
public interface SeleniumDriverFactoryFactory {
    /**
     * @return true when getDriverFactory() will not return null.
     */
    boolean willOverride();

    /**
     * Creates driver factory to be used.
     * @return driver factory to use, null indicates wiki's configuration will be used (i.e. not overridden).
     */
    SeleniumHelper.DriverFactory getDriverFactory();
}
