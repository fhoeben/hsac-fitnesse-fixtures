package nl.hsac.fitnesse.fixture.web;

import nl.hsac.fitnesse.fixture.Environment;

/**
 * Base class for Slim fixtures.
 */
public class SlimFixture {
    private Environment environment = Environment.getInstance();

    /**
     * @return environment to be used.
     */
    protected Environment getEnvironment() {
        return environment;
    }
}
