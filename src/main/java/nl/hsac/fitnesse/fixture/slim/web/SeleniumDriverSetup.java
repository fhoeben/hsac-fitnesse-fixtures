package nl.hsac.fitnesse.fixture.slim.web;

import nl.hsac.fitnesse.fixture.Environment;
import nl.hsac.fitnesse.fixture.slim.SlimFixture;
import nl.hsac.fitnesse.fixture.util.SauceLabsHelper;
import nl.hsac.fitnesse.fixture.util.SeleniumHelper;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.FileDetector;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.UselessFileDetector;
import org.openqa.selenium.safari.SafariDriver;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Script fixture to set up webdriver to be used by Selenium tests.
 */
public class SeleniumDriverSetup extends SlimFixture {
    public static final String REMOTE_URL_KEY = "SeleniumRemoteUrl";
    private static final String LAST_RUN_SUMMARY = "SeleniumLastRunSummary";
    protected static boolean OVERRIDE_ACTIVE = false;

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
     * @return true if instance was created and injected into SeleniumHelper.
     * @throws Exception if no instance could be created.
     */
    public boolean startDriver(String driverClassName) throws Exception {
        if (OVERRIDE_ACTIVE) {
            return true;
        }

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
     * Starts a local instance of the selenium driver for the specified browser
     * (using defaults to determine the correct class and configuration properties).
     * and injects it into SeleniumHelper, so other fixtures can use it.
     * @param browser name of browser to connect to.
     * @return true if instance was created and injected into SeleniumHelper.
     * @throws Exception if no instance could be created.
     */
    public boolean startDriverFor(String browser) throws Exception {
        if (OVERRIDE_ACTIVE) {
            return true;
        }

        boolean result = false;
        String browserName = browser.toLowerCase();
        if ("firefox".equals(browserName)) {
            result = startDriver(FirefoxDriver.class.getName());
        } else if ("safari".equals(browserName)) {
            result = startDriver(SafariDriver.class.getName());
        } else if ("chrome".equals(browserName)) {
            String driverPath = getExecutable("chromedriver");
            setPropertyValue("webdriver.chrome.driver", driverPath);
            result = startDriver(ChromeDriver.class.getName());
        } else if ("internet explorer".equals(browserName)) {
            String driverPath = getExecutable("IEDriverServer");
            setPropertyValue("webdriver.ie.driver", driverPath);
            result = startDriver(InternetExplorerDriver.class.getName());
        } else if ("phantomjs".equals(browserName)) {
            String driverPath = getExecutable("phantomjs");
            setPropertyValue("phantomjs.binary.path", driverPath);
            result = startDriver(PhantomJSDriver.class.getName());
        } else {
            throw new IllegalArgumentException("No defaults known for: " + browser);
        }
        return result;
    }

    private String getExecutable(String basename) {
        String name = getExecutableForOs(basename);
        return getAbsoluteWebDriverPath(name);
    }

    protected String getAbsoluteWebDriverPath(String executable) {
        String path = executable;
        File f = new File("webdrivers", executable);
        if (f.exists()) {
            path = f.getAbsolutePath();
        } else {
            f = new File("wiki/webdrivers", executable);
            if (f.exists()) {
                path = f.getAbsolutePath();
            }
        }
        return path;
    }

    protected String getExecutableForOs(String basename) {
        String name = basename;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            name += ".exe";
        } else if (os.contains("mac")) {
            name = "osx" + File.separator + basename;
        } else if (os.contains("linux")) {
            name = "linux" + File.separator + basename;
        }
        return name;
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

    public boolean connectToDriverAtWithCapabilities(String url, Map<String, String> capabilities)
            throws MalformedURLException {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        for (Map.Entry<String, String> capability : capabilities.entrySet()) {
            desiredCapabilities.setCapability(capability.getKey(), capability.getValue());
        }
        return createAndSetRemoteDriver(url, desiredCapabilities);
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
        StringBuilder result = new StringBuilder("<table><tbody>");
        Capabilities capabilities = remoteDriver.getCapabilities();
        for (Map.Entry<String, ?> entry : capabilities.asMap().entrySet()) {
            result.append("<tr><th>");
            result.append(entry.getKey());
            result.append("</th><td>");
            result.append(entry.getValue());
            result.append("</td></tr>");
        }
        result.append("</tbody></table>");
        return result.toString();
    }

    protected String extendedDriverDescription(URL lastRemoteUrl) {
        String result = "";
        if (SauceLabsHelper.isRelevant(lastRemoteUrl)) {
            String jobLink = SauceLabsHelper.getJobLink(lastRemoteUrl, getHelper().getSessionId());
            result = String.format("<a href=\"%s\" target=\"_blank\">Job details</a>", jobLink);
        }
        return result;
    }

    protected boolean createAndSetRemoteDriver(String url, DesiredCapabilities desiredCapabilities)
            throws MalformedURLException {
        if (OVERRIDE_ACTIVE) {
            return true;
        }

        String cleanUrl = cleanupValue(url);
        URL remoteUrl = new URL(cleanUrl);
        RemoteWebDriver remoteWebDriver = new RemoteWebDriver(remoteUrl, desiredCapabilities);
        FileDetector fd = remoteWebDriver.getFileDetector();
        if (fd == null || fd instanceof UselessFileDetector) {
            remoteWebDriver.setFileDetector(new LocalFileDetector());
        }
        setDriver(remoteWebDriver);
        getEnvironment().setSymbol(REMOTE_URL_KEY, cleanUrl);
        return true;
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

    private void setDriver(WebDriver webDriver) {
        SeleniumHelper helper = getHelper();
        helper.setWebDriver(webDriver);
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
        getHelper().close();
        return true;
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
