package nl.hsac.fitnesse.fixture.util.selenium.driverfactory;

import nl.hsac.fitnesse.fixture.slim.StopTestException;
import nl.hsac.fitnesse.fixture.util.selenium.SeleniumHelper;
import org.openqa.selenium.WebDriver;

/**
 * Helps create and destroy selenium drivers wrapped in helpers.
 */
public class DriverManager {
    /** Default time in seconds the wait web driver waits unit throwing TimeOutException. */
    private static final int DEFAULT_TIMEOUT_SECONDS = 10;
    private DriverFactory factory;
    private int defaultTimeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
    private SeleniumHelper helper;

    public DriverManager() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> closeDriver()));
    }

    public void setFactory(DriverFactory factory) {
        this.factory = factory;
    }

    public void closeDriver() {
        if (helper != null) {
            helper.close();
            helper = null;
        }
    }

    public SeleniumHelper getSeleniumHelper() {
        if (helper == null) {
            if (factory == null) {
                throw new StopTestException("Cannot use Selenium before a driver is started (for instance using SeleniumDriverSetup)");
            } else {
                WebDriver driver = factory.createDriver();
                helper = createHelper(driver);
                helper.setWebDriver(driver, getDefaultTimeoutSeconds());
            }
        }
        return helper;
    }

    protected SeleniumHelper createHelper(WebDriver driver) {
        return new SeleniumHelper();
    }

    public int getDefaultTimeoutSeconds() {
        return defaultTimeoutSeconds;
    }

    public void setDefaultTimeoutSeconds(int defaultTimeoutSeconds) {
        this.defaultTimeoutSeconds = defaultTimeoutSeconds;
    }
}
