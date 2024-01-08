package nl.hsac.fitnesse.sample;

import nl.hsac.fitnesse.fixture.slim.web.Ng2BrowserTest;
import nl.hsac.fitnesse.fixture.slim.web.annotation.TimeoutPolicy;
import nl.hsac.fitnesse.fixture.slim.web.annotation.WaitUntil;
import org.openqa.selenium.WebElement;

/**
 * Fixture class customized to test practicesoftwaretesting.com.
 */
public class PracticeSoftwareTestingTest extends Ng2BrowserTest {
    // This method is very similar to what could be achieved using a scenario table
    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public String city() {
        return valueOf("xpath=//input[@data-test= 'city']");
    }

    // Custom fixture can customize heuristic, to first look at our custom location, before using the standard
    @Override
    protected WebElement getElementToRetrieveValue(String place, String container) {
        return findFirstInContainer(container, place,
                () -> findByXPath("//*[@data-test= '%s']", place),
                () -> super.getElementToRetrieveValue(place, null));
    }

    @Override
    protected WebElement getElementToSendValue(String place, String container) {
        return findFirstInContainer(container, place,
                () -> findByXPath("//*[@data-test= '%s']", place),
                () -> super.getElementToRetrieveValue(place, null));
    }
}
