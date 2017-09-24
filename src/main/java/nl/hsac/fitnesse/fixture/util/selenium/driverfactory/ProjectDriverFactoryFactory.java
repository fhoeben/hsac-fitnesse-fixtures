package nl.hsac.fitnesse.fixture.util.selenium.driverfactory;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.safari.SafariDriver;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Gets a selenium driver installed by project (in wiki/webdrivers).
 */
public class ProjectDriverFactoryFactory {
    public LocalDriverFactory create(String browser, Map<String, Object> profile) {
        String driverClass;
        String browserName = browser.toLowerCase();
        switch (browserName) {
            case "firefox": {
                String driverPath = getExecutable("geckodriver");
                setPropertyValue("webdriver.gecko.driver", driverPath);
                driverClass = FirefoxDriver.class.getName();
                break;
            }
            case "safari": {
                driverClass = SafariDriver.class.getName();
                break;
            }
            case "chrome mobile emulation":
                Map<String, Object> chromeOptions = new HashMap<>();
                chromeOptions.put("mobileEmulation", profile);
                profile = chromeOptions;
            case "chrome": {
                String driverPath = getExecutable("chromedriver");
                setPropertyValue("webdriver.chrome.driver", driverPath);
                driverClass = ChromeDriver.class.getName();
                break;
            }
            case "microsoftedge":
            case "edge": {
                String driverPath = getExecutable("edgedriver");
                setPropertyValue("webdriver.edge.driver", driverPath);
                driverClass = EdgeDriver.class.getName();
                break;
            }
            case "internet explorer": {
                String driverPath = getExecutable("internetexplorerdriver");
                setPropertyValue("webdriver.ie.driver", driverPath);
                driverClass = InternetExplorerDriver.class.getName();
                break;
            }
            case "phantomjs": {
                String driverPath = getExecutable("phantomjs");
                setPropertyValue("phantomjs.binary.path", driverPath);
                driverClass = PhantomJSDriver.class.getName();
                break;
            }
            default:
                throw new IllegalArgumentException("No defaults known for: " + browser);
        }
        return new LocalDriverFactory(driverClass, profile);
    }

    protected String getExecutable(String basename) {
        String name = getExecutableForOs(basename);
        for (int bit : new int[] {32, 64}) {
            String exec = String.format(name, bit);
            String execPath = getAbsoluteWebDriverPath(exec);
            if (execPath != null) {
                name = execPath;
                break;
            }
        }
        return name;
    }

    protected String getAbsoluteWebDriverPath(String executable) {
        String path = null;
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
            name = basename + "-windows-%dbit.exe";
        } else if (os.contains("mac")) {
            name = basename + "-mac-%dbit";
        } else if (os.contains("linux")) {
            name = basename + "-linux-%dbit";
        }
        return name;
    }

    public static void setPropertyValue(String propName, String value) {
        System.setProperty(propName, value);
    }
}
