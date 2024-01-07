package nl.hsac.fitnesse.fixture.util.selenium.driverfactory;

import nl.hsac.fitnesse.fixture.slim.SlimFixtureException;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
                ChromeOptions chromeOptions = createChromiumOptions(new ChromeOptions(), profile);
                DriverFactory.addDefaultCapabilities(chromeOptions);
                driver = new ChromeDriver(chromeOptions);
            } else if ("internetexplorerdriver".equalsIgnoreCase(driverClass.getSimpleName())) {
                InternetExplorerOptions ieOptions = getInternetExplorerOptions(profile);
                driver = new InternetExplorerDriver(ieOptions);
            } else if ("edgedriver".equalsIgnoreCase(driverClass.getSimpleName())) {
                EdgeOptions edgeOptions = createChromiumOptions(new EdgeOptions(), profile);
                DriverFactory.addDefaultCapabilities(edgeOptions);
                driver = new EdgeDriver(edgeOptions);
            } else {
                driver = driverClass.getDeclaredConstructor().newInstance();
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
     *
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
                    fxProfile.setPreference(key, value);
                } else if (value instanceof Integer) {
                    fxProfile.setPreference(key, value);
                } else if (value == null) {
                    fxProfile.setPreference(key, null);
                } else {
                    fxProfile.setPreference(key, value.toString());
                }
            }
        }
        return fxProfile;
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

    /**
     * Append to a ChromiumOptions object from a map and provide readable exception messages if options can not be mapped
     * @param options The options to append to
     * @param profile The map of chromium configuration options
     * @return the appended options object
     */
    @SuppressWarnings("unchecked")
    private static <T extends ChromiumOptions<?>> T createChromiumOptions(T options, Map<String, Object> profile) {
        if (profile == null) {
            return options;
        }
        for (Map.Entry<String, Object> profileEntry : profile.entrySet()) {
            switch (profileEntry.getKey()) {
                case "args":
                    if (profileEntry.getValue() instanceof List) {
                        options.addArguments((List<String>) profileEntry.getValue());
                    } else {
                        throw new IllegalArgumentException("Chromium args should be a list of Strings");
                    }
                    break;
                case "extensions":
                    if (profileEntry.getValue() instanceof List) {
                        List<File> extensions = new ArrayList<>();
                        for (String extPath : (List<String>) profileEntry.getValue()) {
                            extensions.add(new File(extPath));
                        }
                        options.addExtensions(extensions);
                    } else {
                        throw new IllegalArgumentException("Extensions should be a list of Strings containing paths to extension files");
                    }
                    break;
                case "binary":
                    options.setBinary(String.valueOf(profileEntry.getValue()));
                    break;
                case "proxy":
                    if (profileEntry.getValue() instanceof Map) {
                        options.setProxy(new Proxy((Map<String, Object>) profileEntry.getValue()));
                    } else {
                        throw new IllegalArgumentException("Proxy object is expected to be a map");
                    }
                    break;
                default:
                    options.setExperimentalOption(profileEntry.getKey(), profileEntry.getValue());
            }
        }
        return options;
    }

}
