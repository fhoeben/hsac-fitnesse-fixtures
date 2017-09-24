package nl.hsac.fitnesse.junit.selenium;

import nl.hsac.fitnesse.fixture.util.LambdaMetaHelper;
import nl.hsac.fitnesse.fixture.util.selenium.driverfactory.DriverFactory;
import nl.hsac.fitnesse.fixture.util.selenium.driverfactory.RemoteDriverFactory;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Base class for factories connecting to Selenium Grid, specifying capabilities to use.
 * Factories using it are configured by setting the system property 'seleniumGridUrl'.
 */
public abstract class SeleniumGridWithCapabilitiesDriverFactoryFactoryBase extends SeleniumDriverFactoryFactoryBase {
    private BiFunction<URL, Capabilities, RemoteWebDriver> webDriverConstructor;

    @Override
    public DriverFactory getDriverFactory() {
        String gridUrl = getProperty(SELENIUM_GRID_URL);
        Map<String, Object> capabilities = getCapabilities();
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities(capabilities);
        BiFunction<URL, Capabilities, RemoteWebDriver> constr = getRemoteWebDriverConstructor();
        return new RemoteDriverFactory(constr, gridUrl, desiredCapabilities);
    }

    protected BiFunction<URL, Capabilities, RemoteWebDriver> getRemoteWebDriverConstructor() {
        if (webDriverConstructor == null) {
            if (isPropertySet(SELENIUM_DRIVER_CLASS)) {
                String driverClass = getProperty(SELENIUM_DRIVER_CLASS);
                try {
                    Class<?> clazz = Class.forName(driverClass);
                    if (RemoteWebDriver.class.isAssignableFrom(clazz)) {
                        Class<? extends RemoteWebDriver> rmd = (Class<? extends RemoteWebDriver>) clazz;
                        webDriverConstructor = new LambdaMetaHelper().getConstructor(rmd, URL.class, Capabilities.class);
                    } else {
                        throw new IllegalArgumentException(driverClass + " does not implement RemoteWebDiver");
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException("Unable to create RemoteWebDriver using: " + driverClass, e);
                }
            } else {
                webDriverConstructor = RemoteWebDriver::new;
            }
        }

        return webDriverConstructor;
    }

    protected void setWebDriverConstructor(BiFunction<URL, Capabilities, RemoteWebDriver> constructor) {
        webDriverConstructor = constructor;
    }

    protected abstract Map<String, Object> getCapabilities();
}
