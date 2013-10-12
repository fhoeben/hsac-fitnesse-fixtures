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

    public String errorOn(String label) {
        String result = null;
        By errorXPath = getSeleniumHelper()
                            .byXpath("//div[starts-with(@class, 'control-group')]/label[text() = '%s']/following-sibling::div/p[@class='help-block']",
                                    label);
        WebElement element = getSeleniumHelper().findElement(errorXPath);
        if (element != null) {
            result = element.getText();
        }
        return result;
    }
}
