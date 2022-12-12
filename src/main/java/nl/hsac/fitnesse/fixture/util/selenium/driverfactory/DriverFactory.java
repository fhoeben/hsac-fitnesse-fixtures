package nl.hsac.fitnesse.fixture.util.selenium.driverfactory;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;

import java.util.Set;

/**
 * Interface to encapsulate creation of Selenium driver.
 */
public interface DriverFactory {
    WebDriver createDriver();

    static void addDefaultCapabilities(MutableCapabilities capabilities) {
        Set<String> capabilityNames = capabilities.getCapabilityNames();
        if (capabilityNames.contains(CapabilityType.BROWSER_NAME)
                && !capabilityNames.contains(CapabilityType.UNHANDLED_PROMPT_BEHAVIOUR)) {
            capabilities.setCapability(CapabilityType.UNHANDLED_PROMPT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
        }
    }
}
