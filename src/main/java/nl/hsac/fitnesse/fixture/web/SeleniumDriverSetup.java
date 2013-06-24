package nl.hsac.fitnesse.fixture.web;

import nl.hsac.fitnesse.fixture.Environment;
import nl.hsac.fitnesse.fixture.util.SeleniumHelper;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;

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
            setDriver((WebDriver) driver);
            result = true;
        }
        return result;
    }

    /**
     * Connects SeleniumHelper to a remote web driver, without specifying browser version.
     * @param browser name of browser to connect to.
     * @param url url to connect to browser.
     * @return true.
     * @throws MalformedURLException if supplied url can not be transformed to URL.
     */
    public boolean connectToDriverForAt(String browser, String url)
            throws MalformedURLException {
        return connectToDriverForVersionOnAt(browser, "", Platform.ANY.name(), url);
    }

    /**
     * Connects SeleniumHelper to a remote web driver, without specifying browser version.
     * @param browser name of browser to connect to.
     * @param platformName platform browser must run on.
     * @param url url to connect to browser.
     * @return true.
     * @throws MalformedURLException if supplied url can not be transformed to URL.
     */
    public boolean connectToDriverForOnAt(String browser, String platformName, String url)
            throws MalformedURLException {
        return connectToDriverForVersionOnAt(browser, "", platformName, url);
    }

    /**
     * Connects SeleniumHelper to a remote web driver.
     * @param browser name of browser to connect to.
     * @param version version of browser.
     * @param platformName platform browser must run on.
     * @param url url to connect to browser.
     * @return true.
     * @throws MalformedURLException if supplied url can not be transformed to URL.
     */
    public boolean connectToDriverForVersionOnAt(String browser, String version, String platformName, String url)
            throws MalformedURLException {
        Platform platform = Platform.valueOf(platformName);
        URL remoteUrl = new URL(url);
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities(browser, version, platform);
        RemoteWebDriver remoteWebDriver = new RemoteWebDriver(remoteUrl, desiredCapabilities);
        setDriver(remoteWebDriver);
        return true;
    }

    private void setDriver(WebDriver webDriver) {
        SeleniumHelper helper = getHelper();
        helper.setWebDriver(webDriver);
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
