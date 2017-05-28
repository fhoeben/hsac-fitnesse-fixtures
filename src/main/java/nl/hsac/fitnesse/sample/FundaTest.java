package nl.hsac.fitnesse.sample;

import nl.hsac.fitnesse.fixture.slim.web.BrowserTest;
import nl.hsac.fitnesse.fixture.slim.web.annotation.TimeoutPolicy;
import nl.hsac.fitnesse.fixture.slim.web.annotation.WaitUntil;

/**
 * Fixture class customized to test funda.nl.
 */
public class FundaTest extends BrowserTest {
    // This method is very similar to what could be achieved using a scenario table
    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public String numberOfPhotos() {
        return valueOf("xpath=//span[@class='object-media-teaser-count']");
    }

    // Custom fixture can use 'if' which scenarios cannot
    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public String classification() {
        String countStr = numberOfPhotos();
        int count = Integer.parseInt(countStr);
        if (count > 10) {
            return "Good";
        } else {
            return "Bad";
        }
    }
}
