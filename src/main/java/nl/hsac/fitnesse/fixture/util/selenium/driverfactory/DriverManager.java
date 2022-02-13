package nl.hsac.fitnesse.fixture.util.selenium.driverfactory;

import nl.hsac.fitnesse.fixture.Environment;
import nl.hsac.fitnesse.fixture.slim.StopTestException;
import nl.hsac.fitnesse.fixture.util.selenium.SeleniumHelper;
import nl.hsac.fitnesse.fixture.util.selenium.by.BestMatchBy;
import nl.hsac.fitnesse.fixture.util.selenium.caching.WebElementConverter;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.JsonToWebElementConverter;
import org.openqa.selenium.remote.RemoteWebDriver;

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

    public DriverFactory getFactory() {
        return factory;
    }

    public void closeDriver() {
        setSeleniumHelper(null);
    }

    public SeleniumHelper getSeleniumHelper() {
        if (helper == null) {
            DriverFactory currentFactory = getFactory();
            if (currentFactory == null) {
                throw new StopTestException("Cannot use Selenium before configuring how to start a driver (for instance using SeleniumDriverSetup)");
            } else {
                try {
                    WebDriver driver = currentFactory.createDriver();
                    postProcessDriver(driver);
                    SeleniumHelper newHelper = createHelper(driver);
                    newHelper.setWebDriver(driver, getDefaultTimeoutSeconds());
                    setSeleniumHelper(newHelper);
                } catch (SessionNotCreatedException e) {
                    throw new StopTestException(e.getMessage(), e);
                }
            }
        }
        return helper;
    }

    private void postProcessDriver(WebDriver driver) {
        if (driver instanceof RemoteWebDriver) {
            setElementConverter((RemoteWebDriver) driver);
        }
    }

    public void setSeleniumHelper(SeleniumHelper helper) {
        if (this.helper != null) {
            this.helper.close();
        }
        this.helper = helper;
    }

    protected SeleniumHelper createHelper(WebDriver driver) {
        // set default 'Best Function'
        BestMatchBy.setBestFunction(BestMatchBy::selectBestElement);
        return new SeleniumHelper();
    }

    public int getDefaultTimeoutSeconds() {
        return defaultTimeoutSeconds;
    }

    public void setDefaultTimeoutSeconds(int defaultTimeoutSeconds) {
        this.defaultTimeoutSeconds = defaultTimeoutSeconds;
    }

    protected void setElementConverter(RemoteWebDriver d) {
        JsonToWebElementConverter converter = createElementConverter(d);
        Environment.getInstance().getReflectionHelper().setField(d, "converter", converter);
    }

    protected JsonToWebElementConverter createElementConverter(RemoteWebDriver d) {
        return new WebElementConverter(d);
    }
}
