package nl.glasoperator.fitnesse.vodafone;

import nl.hsac.fitnesse.fixture.web.BrowserTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
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
                            .byXpath("//label[text() = '%s']/following-sibling::div/p[@class='help-block']",
                                    label);
        WebElement element = getSeleniumHelper().findElement(errorXPath);
        if (element != null) {
            result = element.getText();
        }
        return result;
    }

    public boolean errorStyleOn(String label) {
        boolean result = false;
        By controlGroupXPath = getSeleniumHelper().byXpath("//div[label[text() = '%s']]", label);
        WebElement element = getSeleniumHelper().findElement(controlGroupXPath);
        if (element != null) {
            String classAttr = element.getAttribute("class");
            if (classAttr != null) {
                String[] classes = classAttr.split(" ");
                result = Arrays.asList(classes).contains("error");
            }
        }
        return result;
    }

    public String errorsOnOthersThan(String label) {
        String result = null;
        By otherErrorXPath = getSeleniumHelper()
                .byXpath("//label[text() != '%s']/following-sibling::div/p[@class='help-block' and normalize-space(text()) != '']",
                        label);
        List<WebElement> elements = getSeleniumHelper().driver().findElements(otherErrorXPath);
        if (elements != null) {
            List<String> errors = new ArrayList<String>(elements.size());
            for (WebElement element : elements) {
                String errorText = element.getText();
                errors.add(errorText);
            }
            if (!errors.isEmpty()) {
                result = errors.toString();
            }
        }
        return result;
    }

    public String errorStyleOnOthersThan(String label) {
        String result = null;
        By otherErrorXPath = getSeleniumHelper()
                .byXpath("//div[contains(@class, 'error') and label[text() != '%s']]/label",
                        label);
        List<WebElement> elements = getSeleniumHelper().driver().findElements(otherErrorXPath);
        if (elements != null) {
            List<String> labels = new ArrayList<String>(elements.size());
            for (WebElement element : elements) {
                String labelText = element.getText();
                labels.add(labelText);
            }
            if (!labels.isEmpty()) {
                result = labels.toString();
            }
        }
        return result;
    }
}
