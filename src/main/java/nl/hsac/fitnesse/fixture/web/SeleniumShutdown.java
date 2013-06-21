package nl.hsac.fitnesse.fixture.web;

import nl.hsac.fitnesse.fixture.util.SeleniumHelper;

/**
 * Fixture to ensure proper shutdown of Selenium.
 */
public class SeleniumShutdown {
    public String close() {
        SeleniumHelper.close();
        return "OK";
    }
}
