package nl.glasoperator.fitnesse.vodafone;

import nl.hsac.fitnesse.fixture.web.BrowserTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Test fixture to run Selenium tests against Vodafone sites.
 */
public class VodafoneBrowserTest extends BrowserTest {
    @Override
    public boolean clickImpl(String place) {
        boolean result = clickFirstButton(place);
        if (!result) {
            result = super.clickImpl(place);
        }
        return result;
    }

    private boolean clickFirstButton(String place) {
        boolean result = false;
        By byButtonText = getSeleniumHelper().byXpath("//button[text() = '%s']", place);
        List<WebElement> elements = getSeleniumHelper().driver().findElements(byButtonText);
        if (elements != null) {
            for (WebElement element : elements) {
                if (clickElement(element)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
}
