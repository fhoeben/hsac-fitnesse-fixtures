package nl.hsac.fitnesse.fixture.slim.web;

import nl.hsac.fitnesse.fixture.Environment;
import nl.hsac.fitnesse.fixture.slim.SlimFixture;
import nl.hsac.fitnesse.fixture.util.selenium.SauceLabsHelper;
import nl.hsac.fitnesse.fixture.util.selenium.SeleniumHelper;
import nl.hsac.fitnesse.fixture.util.selenium.driverfactory.DriverFactory;
import nl.hsac.fitnesse.fixture.util.selenium.driverfactory.DriverManager;
import nl.hsac.fitnesse.fixture.util.selenium.driverfactory.LocalDriverFactory;
import nl.hsac.fitnesse.fixture.util.selenium.driverfactory.ProjectDriverFactoryFactory;
import nl.hsac.fitnesse.fixture.util.selenium.driverfactory.RemoteDriverFactory;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static nl.hsac.fitnesse.fixture.util.selenium.driverfactory.LocalDriverFactory.getFirefoxProfile;
import static nl.hsac.fitnesse.fixture.util.selenium.driverfactory.RemoteDriverFactory.REMOTE_URL_KEY;

/**
 * Script fixture to set up webdriver to be used by Selenium tests.
 */
public class SeleniumDriverSetup extends SlimFixture {
    private static final String LAST_RUN_SUMMARY = "SeleniumLastRunSummary";
    protected static boolean OVERRIDE_ACTIVE = false;
    private final List<String> secretCapabilities = new ArrayList<>();

    /**
     * Sets system property (needed by the WebDriver to be set up).
     * @param propName name of property to set.
     * @param value value to set.
     * @return true.
     */
    public boolean setPropertyValue(String propName, String value) {
        if (OVERRIDE_ACTIVE) {
            return true;
        }

        System.setProperty(propName, value);
        return true;
    }

    /**
     * Creates an instance of the specified class an injects it into SeleniumHelper, so other fixtures can use it.
     * @param driverClassName name of Java class of WebDriver to use.
     * @param profile profile to use (for firefox, chrome mobile and IE only for now)
     * @return true if instance was created and injected into SeleniumHelper.
     * @throws Exception if no instance could be created.
     */
    public boolean startDriver(String driverClassName, final Map<String, Object> profile) throws Exception {
        if (OVERRIDE_ACTIVE) {
            return true;
        }

        DriverFactory driverFactory = new LocalDriverFactory(driverClassName, profile);
        WebDriver driver = setAndUseDriverFactory(driverFactory);
        return driver != null;
    }

    /**
     * Creates an instance of the specified class an injects it into SeleniumHelper, so other fixtures can use it.
     * @param driverClassName name of Java class of WebDriver to use.
     * @return true if instance was created and injected into SeleniumHelper.
     * @throws Exception if no instance could be created.
     */
    public boolean startDriver(String driverClassName) throws Exception {
        return startDriver(driverClassName, null);
    }

    /**
     * Starts a local instance of the selenium driver for the specified browser
     * (using defaults to determine the correct class and configuration properties).
     * and injects it into SeleniumHelper, so other fixtures can use it.
     * @param browser name of browser to connect to.
     * @param profile setting of the browser (for firefox, chrome mobile and IE only for now)
     * @return true if instance was created and injected into SeleniumHelper.
     * @throws Exception if no instance could be created.
     */
    public boolean startDriverForWithProfile(String browser, Map<String, Object> profile) throws Exception {
        if (OVERRIDE_ACTIVE) {
            return true;
        }

        DriverFactory driverFactory = new ProjectDriverFactoryFactory().create(browser, profile);
        WebDriver driver = setAndUseDriverFactory(driverFactory);
        return driver != null;
    }

    public boolean startDriverFor(String browser) throws Exception {
        return startDriverForWithProfile(browser, null);
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
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities(browser, version, platform);
        desiredCapabilities.setVersion(version);
        return createAndSetRemoteDriver(url, desiredCapabilities);
    }

    public boolean connectToDriverAtWithCapabilities(String url, Map<String, Object> capabilities)
            throws MalformedURLException {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities(capabilities);
        return createAndSetRemoteDriver(url, desiredCapabilities);
    }

    public boolean connectToFirefoxDriverAtWithProfile(String url, Map<String, Object> profile)
            throws MalformedURLException {
        FirefoxProfile fxProfile = getFirefoxProfile(profile);
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setCapability("browserName", "firefox");
        desiredCapabilities.setCapability(FirefoxDriver.PROFILE, fxProfile);
        return createAndSetRemoteDriver(url, desiredCapabilities);
    }

    public boolean connectToDriverAtWithJsonCapabilities(String url, String capabilitiesInJson)
            throws MalformedURLException {
        Map<String, Object> desiredCapabilities = getEnvironment().getJsonHelper().jsonStringToMap(capabilitiesInJson);
        return connectToDriverAtWithCapabilities (url, desiredCapabilities);
    }

    public String driverDescription() {
        String result = null;
        WebDriver driver = getHelper().driver();
        if (driver != null) {
            StringBuilder builder = new StringBuilder("<div>");
            builder.append("<pre>");
            builder.append(driver.getClass().getName());
            builder.append("</pre>");

            if (driver instanceof RemoteWebDriver) {
                RemoteWebDriver remoteDriver = (RemoteWebDriver) driver;

                URL lastRemoteUrl = getLastRemoteUrl();
                if (lastRemoteUrl != null) {
                    builder.append(" at <strong>");
                    builder.append(lastRemoteUrl.getHost());
                    builder.append("</strong>");
                }
                builder.append(describeCapabilities(remoteDriver));
                builder.append(extendedDriverDescription(lastRemoteUrl));
            }
            builder.append("</div>");
            result = builder.toString();
        }
        return result;
    }

    public String runSummary() {
        String result = null;
        String sessionId = getHelper().getSessionId();
        if (sessionId != null && !"".equals(sessionId)) {
            URL lastRemoteUrl = getLastRemoteUrl();
            if (lastRemoteUrl != null) {
                if (SauceLabsHelper.isRelevant(lastRemoteUrl)) {
                    result = SauceLabsHelper.getTagToEmbedVideoOfRun(lastRemoteUrl, sessionId);
                }
            }
        }
        getEnvironment().setSymbol(LAST_RUN_SUMMARY, result);
        return result;
    }

    public static String getLastRunSummary() {
        return Environment.getInstance().getSymbol(LAST_RUN_SUMMARY);
    }

    protected String describeCapabilities(RemoteWebDriver remoteDriver) {
        Capabilities capabilities = remoteDriver.getCapabilities();
        Map<String, ?> capaToShow = getCapabilitiesToDescribe(capabilities);
        StringBuilder result = new StringBuilder("<table><tbody>");
        for (Map.Entry<String, ?> entry : capaToShow.entrySet()) {
            result.append("<tr><th>");
            result.append(entry.getKey());
            result.append("</th><td>");
            result.append(entry.getValue());
            result.append("</td></tr>");
        }
        result.append("</tbody></table>");
        return result.toString();
    }

    protected Map<String, ?> getCapabilitiesToDescribe(Capabilities capabilities) {
        Map<String, ?> result = capabilities.asMap();
        Collection<String> keysToMask = getSecretCapabilities();
        if (!keysToMask.isEmpty()) {
            result = getEnvironment().getSecretMasker().replaceSecrets(keysToMask, result);
        }
        return result;
    }

    protected String extendedDriverDescription(URL lastRemoteUrl) {
        String result = "";
        if (SauceLabsHelper.isRelevant(lastRemoteUrl)) {
            String jobLink = SauceLabsHelper.getJobLink(lastRemoteUrl, getHelper().getSessionId());
            result = String.format("<a href=\"%s\" target=\"_blank\">Job details</a>", jobLink);
        }
        return result;
    }

    protected boolean createAndSetRemoteDriver(String url, final DesiredCapabilities desiredCapabilities)
            throws MalformedURLException {
        return createAndSetRemoteWebDriver(RemoteWebDriver::new, url, desiredCapabilities);
    }

    protected boolean createAndSetRemoteWebDriver(BiFunction<URL, Capabilities, ? extends RemoteWebDriver> constr,
                                                  String url,
                                                  DesiredCapabilities desiredCapabilities)
            throws MalformedURLException {
        if (OVERRIDE_ACTIVE) {
            return true;
        }

        String cleanUrl = cleanupValue(url);
        DriverFactory driverFactory = new RemoteDriverFactory(constr, cleanUrl, desiredCapabilities);
        WebDriver driver = setAndUseDriverFactory(driverFactory);
        return driver != null;
    }

    public static URL getLastRemoteUrl() {
        URL result = null;
        String urlValue = Environment.getInstance().getSymbol(REMOTE_URL_KEY);
        if (urlValue != null) {
            try {
                result = new URL(urlValue);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    protected WebDriver setAndUseDriverFactory(DriverFactory driverFactory) {
        DriverManager driverManager = getEnvironment().getSeleniumDriverManager();
        driverManager.setFactory(driverFactory);
        return driverManager.getSeleniumHelper().driver();
    }

    /**
     * Stops the current driver registered in the SeleniumHelper.
     * @return true.
     */
    public boolean stopDriver() {
        if (OVERRIDE_ACTIVE) {
            return true;
        }

        // ensure we store summary
        runSummary();
        getEnvironment().getSeleniumDriverManager().closeDriver();
        return true;
    }

    public List<String> getSecretCapabilities() {
        return secretCapabilities;
    }

    /**
     * Prevents instances from creating new Selenium drivers.
     * This can be used to control Selenium configuration independent from Wiki content.
     */
    public static void lockConfig() {
        OVERRIDE_ACTIVE = true;
    }

    /**
     * Enables normal behavior (i.e. instances can create new Selenium drivers).
     */
    public static void unlockConfig() {
        OVERRIDE_ACTIVE = false;
    }

    protected SeleniumHelper getHelper() {
        return Environment.getInstance().getSeleniumHelper();
    }
}
