package nl.hsac.fitnesse.fixture.util.selenium.driverfactory;

import org.openqa.selenium.WebDriver;

/**
 * Interface to encapsulate creation of Selenium driver.
 */
public interface DriverFactory {
    WebDriver createDriver();
}
