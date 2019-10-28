package nl.hsac.fitnesse.fixture.util.selenium.driverfactory;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;

/**
 * Interface to encapsulate creation of Selenium driver.
 */
public interface DriverFactory {
    WebDriver createDriver();

    static void addDefaultCapabilities(MutableCapabilities capabilities) {
        if (!capabilities.getCapabilityNames().contains(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR)) {
            capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
        }
    }
}
