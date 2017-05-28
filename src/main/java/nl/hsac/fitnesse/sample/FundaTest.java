package nl.hsac.fitnesse.sample;

import nl.hsac.fitnesse.fixture.slim.web.BrowserTest;
import nl.hsac.fitnesse.fixture.slim.web.annotation.TimeoutPolicy;
import nl.hsac.fitnesse.fixture.slim.web.annotation.WaitUntil;

/**
 * Fixture class customized to test funda.nl.
 */
public class FundaTest extends BrowserTest {
    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public String numberOfPhotos() {
        return valueOf("xpath=//span[@class='object-media-teaser-count']");
    }
}
