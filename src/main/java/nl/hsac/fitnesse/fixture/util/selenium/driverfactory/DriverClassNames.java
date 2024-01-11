package nl.hsac.fitnesse.fixture.util.selenium.driverfactory;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.safari.SafariDriver;

import java.util.Arrays;

public enum DriverClassNames {
    CHROME(ChromeDriver.class.getName()),
    EDGE(EdgeDriver.class.getName()),
    FIREFOX(FirefoxDriver.class.getName()),
    SAFARI(SafariDriver.class.getName()),
    INTERNET_EXPLORER(InternetExplorerDriver.class.getName());

    private final String driverClassName;
    DriverClassNames(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public static String getClassNameFor(String browser) {
        return Arrays.stream(DriverClassNames.values()).filter(dcn -> dcn.name().equalsIgnoreCase(browser))
                .findFirst().map(dcn -> dcn.driverClassName).orElse(null);
    }
}
