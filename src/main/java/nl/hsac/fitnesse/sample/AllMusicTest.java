package nl.hsac.fitnesse.sample;

import nl.hsac.fitnesse.fixture.slim.web.BrowserTest;
import nl.hsac.fitnesse.fixture.slim.web.annotation.TimeoutPolicy;
import nl.hsac.fitnesse.fixture.slim.web.annotation.WaitUntil;
import org.openqa.selenium.WebElement;

/**
 * Fixture class customized to test allmusic.com.
 */
public class AllMusicTest extends BrowserTest {
    // This method is very similar to what could be achieved using a scenario table
    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public String birthData() {
        return valueOf("xpath=//div[h4[contains(text(),'Born')]]/div");
    }

    // Custom fixture can customize heuristic, to first look at our custom location, before using the standard
    @Override
    protected WebElement getElementToRetrieveValue(String place, String container) {
        return findFirstInContainer(container, place,
                () -> findByXPath("//div[h4[contains(text(),'%s')]]/div", place),
                () -> super.getElementToRetrieveValue(place, null));
    }
}
