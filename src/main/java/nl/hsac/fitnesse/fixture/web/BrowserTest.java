package nl.hsac.fitnesse.fixture.web;

import nl.hsac.fitnesse.fixture.util.SeleniumHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BrowserTest extends SlimFixture {
    private static final Pattern PATTERN = Pattern.compile("<a href=\"(.*?)\">(.*?)</a>", Pattern.CASE_INSENSITIVE);

    private SeleniumHelper seleniumHelper = getEnvironment().getSeleniumHelper();

    public boolean open(String htmlLink) {
        String url = urlFromLink(htmlLink);
        seleniumHelper.navigate().to(url);
        return true;
    }

    public String pageTitle() {
        return seleniumHelper.getPageTitle();
    }

    public boolean enterAs(String value, String place) {
        return enterFor(value, place);
    }

    public boolean enterFor(String value, String place) {
        boolean result = false;
        WebElement element = getElement(place);
        if (element != null) {
            element.sendKeys(value);
            result = true;
        }
        return result;
    }

    public boolean selectAs(String value, String place) {
        return selectFor(value, place);
    }

    public boolean selectFor(String value, String place) {
        // choose option for select, if possible
        boolean result = clickSelectOption(place, value);
        if (!result) {
            // try to click the first element with right value
            result = click(value);
        }
        return result;
    }

    private boolean clickSelectOption(String selectPlace, String optionValue) {
        boolean result = false;
        WebElement element = getElement(selectPlace);
        if (element != null) {
            if (isSelect(element)) {
                String id = element.getAttribute("id");
                By optionWithText = seleniumHelper.byXpath("//select[@id='%s']//option[text()='%s']", id, optionValue);
                WebElement option = seleniumHelper.findElement(element, true, optionWithText);
                if (option != null) {
                    option.click();
                    result = true;
                }
            }
        }
        return result;
    }

    public boolean click(String place) {
        boolean result = false;
        WebElement element = getElement(place);
        if (element != null) {
            element.click();
            result = true;
        }
        return result;
    }

    public String valueOf(String place) {
        return valueFor(place);
    }

    public String valueFor(String place) {
        String result = null;
        WebElement element = getElement(place);
        if (element != null) {
            if (isSelect(element)) {
                String id = element.getAttribute("id");
                By selectedOption = seleniumHelper.byXpath("//select[@id='%s']//option[@selected]", id);
                WebElement option = seleniumHelper.findElement(element, true, selectedOption);
                if (option != null) {
                    result = option.getText();
                }
            } else {
                result = element.getAttribute("value");
            }
        }
        return result;
    }

    private boolean isSelect(WebElement element) {
        return "select".equalsIgnoreCase(element.getTagName());
    }

    public boolean clear(String place) {
        boolean result = false;
        WebElement element = getElement(place);
        if (element != null) {
            element.clear();
            result = true;
        }
        return result;
    }

    private WebElement getElement(String place) {
        return seleniumHelper.getElement(place);
    }

    private String urlFromLink(String htmlLink) {
        String result = null;
        Matcher matcher = PATTERN.matcher(htmlLink);
        if (matcher.matches()) {
            result = matcher.group(1);
        }
        return result;
    }
}
