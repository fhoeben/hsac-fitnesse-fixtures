package nl.hsac.fitnesse.fixture.util.selenium.driverfactory;

import nl.hsac.fitnesse.fixture.slim.SlimFixtureException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.Map;

/**
 * Creates a webdriver at the local machine.
 */
public class LocalDriverFactory implements DriverFactory {
    private String driverClassName;
    private Class<? extends WebDriver> driverClass;
    private Map<String, Object> profile;

    protected LocalDriverFactory() {

    }

    public LocalDriverFactory(String driver, Map<String, Object> profile) {
        this.driverClassName = driver;
        this.profile = profile;
        // ensure we check the provided class name immediately and not on factory usage
        getDriverClass();
    }

    @Override
    public WebDriver createDriver() {
        Class<?> driverClass = getDriverClass();
        try {
            Object driver;

            if ("firefoxdriver".equalsIgnoreCase(driverClass.getSimpleName())) {
                FirefoxProfile fxProfile = getFirefoxProfile(profile);
                FirefoxOptions options = new FirefoxOptions().setProfile(fxProfile);
                driver = new FirefoxDriver(options);
            } else if ("chromedriver".equalsIgnoreCase(driverClass.getSimpleName())) {
                DesiredCapabilities capabilities = getChromeMobileCapabilities(profile);
                DriverFactory.addDefaultCapabilities(capabilities);
                driver = new ChromeDriver(capabilities);
            } else if ("internetexplorerdriver".equalsIgnoreCase(driverClass.getSimpleName())) {
                InternetExplorerOptions ieOptions = getInternetExplorerOptions(profile);
                driver = new InternetExplorerDriver(ieOptions);
            } else if ("edgedriver".equalsIgnoreCase(driverClass.getSimpleName())) {
                DesiredCapabilities edgeOptions = getChromiumEdgeOptions(profile);
                driver = new EdgeDriver(edgeOptions);
            } else {
                driver = driverClass.newInstance();
            }

            return (WebDriver) driver;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected Class<?> getDriverClass() {
        if (driverClass == null) {
            try {
                Class<?> driver = Class.forName(driverClassName);
                if (!WebDriver.class.isAssignableFrom(driver)) {
                    throw new SlimFixtureException(false, driverClassName + " does not implement " + WebDriver.class.getName());
                }
                driverClass = (Class<? extends WebDriver>) driver;
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Unable to create " + driverClassName, e);
            }
        }
        return driverClass;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public void setDriverClass(Class<? extends WebDriver> driverClass) {
        this.driverClass = driverClass;
    }

    public void setProfile(Map<String, Object> profile) {
        this.profile = profile;
    }

    /**
     * Set firefox profile. For example to make sure text/csv file is downloaded without asking (convenient if run on buildserver), do:
     * |script           |selenium driver setup                                                                                               |
     * |start driver for |firefox              |with profile|!{browser.download.folderList:2,browser.helperApps.neverAsk.saveToDisk:text/csv}||
     * @param profile setting from subtable
     * @return firefox profile with specified settings
     */
    public static FirefoxProfile getFirefoxProfile(Map<String, Object> profile) {
        FirefoxProfile fxProfile = new FirefoxProfile();
        if (profile != null) {
            for (Map.Entry<String, Object> profileEntry : profile.entrySet()) {
                String key = profileEntry.getKey();
                Object value = profileEntry.getValue();
                if (value instanceof Boolean) {
                    fxProfile.setPreference(key, (Boolean) value);
                } else if (value instanceof Integer) {
                    fxProfile.setPreference(key, (Integer) value);
                } else if (value == null) {
                    fxProfile.setPreference(key, null);
                } else {
                    fxProfile.setPreference(key, value.toString());
                }
            }
        }
        return fxProfile;
    }

    public static DesiredCapabilities getChromeMobileCapabilities(Map<String, Object> profile) {
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        if (profile != null) {
            capabilities.setCapability(ChromeOptions.CAPABILITY, profile);
        }
        return capabilities;
    }

    public static InternetExplorerOptions getInternetExplorerOptions(Map<String, Object> profile) {
        InternetExplorerOptions ieOptions = new InternetExplorerOptions();
        if (profile != null) {
            for (Map.Entry<String, Object> profileEntry : profile.entrySet()) {
                ieOptions.setCapability(profileEntry.getKey(), profileEntry.getValue());
            }
        }
        return ieOptions;
    }

    public static DesiredCapabilities getChromiumEdgeOptions(Map<String, Object> profile) {
        DesiredCapabilities capabilities = DesiredCapabilities.edge();
        if (profile != null) {
           capabilities.setCapability("ms:edgeOptions", profile);
        }
        return capabilities;
    }
}
