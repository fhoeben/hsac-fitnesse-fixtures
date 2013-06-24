package nl.hsac.fitnesse.fixture.web;

import nl.hsac.fitnesse.fixture.Environment;
import nl.hsac.fitnesse.fixture.util.SeleniumHelper;
import org.openqa.selenium.WebDriver;

/**
 * Script fixture to set up webdriver to be used by Selenium tests.
 */
public class SeleniumDriverSetup {
    /**
     * Sets system property (needed by the WebDriver to be set up).
     * @param propName name of property to set.
     * @param value value to set.
     * @return true.
     */
    public boolean setPropertyValue(String propName, String value) {
        System.setProperty(propName, value);
        return true;
    }

    /**
     * Creates an instance of the specified class an injects it into SeleniumHelper, so other fixtures can use it.
     * @param driverClassName name of Java class of WebDriver to use.
     * @return true if instance was created and injected into SeleniumHelper.
     * @throws Exception if no instance could be created.
     */
    public boolean startDriver(String driverClassName) throws Exception {
        boolean result = false;
        Class<?> driverClass = Class.forName(driverClassName);
        Object driver = driverClass.newInstance();
        if (driver instanceof WebDriver) {
            SeleniumHelper helper = getHelper();
            helper.setWebDriver((WebDriver) driver);
            result = true;
        }
        return result;
    }

    /**
     * Stops the current driver registered in the SeleniumHelper.
     * @return true.
     */
    public boolean stopDriver() {
        getHelper().close();
        return true;
    }

    private SeleniumHelper getHelper() {
        return Environment.getInstance().getSeleniumHelper();
    }
}
