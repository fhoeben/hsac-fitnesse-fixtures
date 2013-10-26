package nl.glasoperator.fitnesse.vodafone;

import nl.hsac.fitnesse.fixture.slim.web.BrowserTest;
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
        List<WebElement> elements = findAllByXPath("//button[text() = '%s']", place);
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

    public String globalError() {
        String result = null;
        List<WebElement> elements = findAllByXPath("//div[contains(@class, 'alert-formerror')]");
        if (elements != null) {
            for (WebElement element : elements) {
                if (element.isDisplayed()) {
                    result = element.getText();
                    break;
                }
            }
        }
        return result;
    }

    public String errorOn(String label) {
        String result = null;
        WebElement element = findErrorMessageElement(label);
        if (element != null) {
            result = element.getText();
        }
        return result;
    }

    private WebElement findErrorMessageElement(String label) {
        WebElement element = findByXPath("//label[text() = '%s']/following-sibling::div/p[@class='help-block']",
                label);
        if (element == null) {
            element = findByXPath("//label[normalize-space(text()) = '%s']/following-sibling::p[@class='help-block']",
                    label);
            if (element == null) {
                element = findByXPath("//input[@aria-label = '%s']/../../../following-sibling::p[@class='help-block']",
                                label);
                if (element == null) {
                    element = findByXPath("//label[normalize-space(text()) = '%s']/../following-sibling::p[@class='help-block']",
                                    label);
                }
            }
        }
        return element;
    }

    public boolean errorStyleOn(String label) {
        boolean result = false;
        WebElement element = findControlGroup(label);
        if (element != null) {
            result = hasErrorClass(element);
        }
        return result;
    }

    private WebElement findControlGroup(String label) {
        WebElement element = findByXPath("//label[normalize-space(text()) = '%s']/ancestor::div[contains(@class, 'control-group')]", label);
        if (element == null) {
            element = findByXPath("//input[@aria-label = '%s']/ancestor::div[contains(@class, 'control-group')]",
                            label);
        }
        return element;
    }

    private boolean hasErrorClass(WebElement element) {
        boolean result = false;
        String classAttr = element.getAttribute("class");
        if (classAttr != null) {
            String[] classes = classAttr.split(" ");
            result = Arrays.asList(classes).contains("error");
        }
        return result;
    }

    public String errorsOnOthersThan(String label) {
        String result = null;
        List<WebElement> elements = findAllByXPath(
                                        "//label[normalize-space(text()) != '%s']/following-sibling::div/p[@class='help-block' and normalize-space(text()) != '']",
                                        label);
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
        List<WebElement> elements = findAllByXPath(
                                        "//div[contains(@class, 'error') and label[normalize-space(text()) != '%s']]/label",
                                        label);
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

    private WebElement findByXPath(String xpathPattern, String... params) {
        By by = getSeleniumHelper().byXpath(xpathPattern, params);
        return getSeleniumHelper().findElement(by);
    }

    private List<WebElement> findAllByXPath(String xpathPattern, String... params) {
        By by = getSeleniumHelper().byXpath(xpathPattern, params);
        return getSeleniumHelper().driver().findElements(by);
    }
}
