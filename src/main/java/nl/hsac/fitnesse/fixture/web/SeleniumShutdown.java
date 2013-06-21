package nl.hsac.fitnesse.fixture.web;

/**
 * Fixture to ensure proper shutdown of Selenium.
 */
public class SeleniumShutdown extends SlimFixture {
    public String close() {
        getEnvironment().getSeleniumHelper().close();
        return "OK";
    }
}
