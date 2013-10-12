package nl.glasoperator.fitnesse.vodafone;

import nl.hsac.fitnesse.fixture.web.BrowserTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Test fixture to run Selenium tests against Vodafone sites.
 */
public class VodafoneBrowserTest extends BrowserTest {
    @Override
    public boolean click(String place) {
        boolean result = false;
        boolean retry = true;
        for (int i = 0;
             !result && retry && i < secondsBeforeTimeout();
             i++) {
            try {
                result = clickFirstButton(place);
                if (!result) {
                    result = super.click(place);
                }
            } catch (WebDriverException e) {
                String msg = e.getMessage();
                if (!msg.contains("Other element would receive the click")) {
                    retry = false;
                } else {
                    waitSeconds(1);
                }
            }
        }
        return result;
    }

    private boolean clickFirstButton(String place) {
        boolean result = false;
        By byButtonText = getSeleniumHelper().byXpath("//button[text() = '%s']", place);
        List<WebElement> elements = getSeleniumHelper().driver().findElements(byButtonText);
        if (elements != null) {
            for (WebElement element : elements) {
                if (element.isDisplayed() && element.isEnabled()) {
                    element.click();
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public boolean waitSeconds(int i) {
        boolean result;
        try {
            Thread.sleep(1 * 1000);
            result = true;
        } catch (InterruptedException e) {
            result = false;
        }
        return result;
    }
}
