package nl.hsac.fitnesse.junit.selenium;

import nl.hsac.fitnesse.fixture.slim.web.SeleniumDriverSetup;
import nl.hsac.fitnesse.fixture.util.selenium.SeleniumHelper;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Base class for factories connecting to Selenium Grid, specifying capabilities to use.
 * Factories using it are configured by setting the system property 'seleniumGridUrl'.
 */
public abstract class SeleniumGridWithCapabilitiesDriverFactoryFactoryBase extends SeleniumDriverFactoryFactoryBase {
    private BiFunction<URL, Capabilities, RemoteWebDriver> webDriverConstructor = RemoteWebDriver::new;

    @Override
    public SeleniumHelper.DriverFactory getDriverFactory() {
        String gridUrl = getProperty(SELENIUM_GRID_URL);
        Map<String, Object> capabilities = getCapabilities();
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities(capabilities);
        BiFunction<URL, Capabilities, RemoteWebDriver> constr = getRemoteWebDriverConstructor();
        return () -> {
            SeleniumDriverSetup.unlockConfig();
            try {
                new SeleniumDriverSetup().createAndSetRemoteWebDriver(constr, gridUrl, desiredCapabilities);
            } catch (MalformedURLException e) {
                throw new RuntimeException("Unable to create driver using "+ constr + " at: "
                        + gridUrl + " with: " + capabilities, e);
            } finally {
                SeleniumDriverSetup.lockConfig();
            }
        };
    }

    protected BiFunction<URL, Capabilities, RemoteWebDriver> getRemoteWebDriverConstructor() {
        return webDriverConstructor;
    }

    protected void setWebDriverConstructor(BiFunction<URL, Capabilities, RemoteWebDriver> constructor) {
        webDriverConstructor = constructor;
    }

    protected abstract Map<String, Object> getCapabilities();
}
