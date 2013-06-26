package nl.hsac.fitnesse.fixture.web;

import nl.hsac.fitnesse.fixture.util.SeleniumHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BrowserTest extends SlimFixture {
    private static final Pattern PATTERN = Pattern.compile("<a href=\"(.*?)\">(.*?)</a>", Pattern.CASE_INSENSITIVE);

    private final SeleniumHelper seleniumHelper = getEnvironment().getSeleniumHelper();
    private int secondsBeforeTimeout = 10;

    protected SeleniumHelper getSeleniumHelper() {
        return seleniumHelper;
    }

    public boolean open(String htmlLink) {
        String url = urlFromLink(htmlLink);
        getSeleniumHelper().navigate().to(url);
        return true;
    }

    public String pageTitle() {
        return getSeleniumHelper().getPageTitle();
    }

    public boolean enterAs(String value, String place) {
        return enterFor(value, place);
    }

    public boolean enterFor(String value, String place) {
        boolean result = false;
        WebElement element = getElement(place);
        if (element != null) {
            String keys = cleanupValue(value);
            element.sendKeys(keys);
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

    public boolean enterForHidden(String value, String idOrName) {
        return getSeleniumHelper().setHiddenInputValue(idOrName, value);
    }

    private boolean clickSelectOption(String selectPlace, String optionValue) {
        boolean result = false;
        WebElement element = getElement(selectPlace);
        if (element != null) {
            if (isSelect(element)) {
                String id = element.getAttribute("id");
                result = clickOption(id, "//select[@id='%s']//option[text()='%s']", optionValue);
                if (!result) {
                    result = clickOption(id, "//select[@id='%s']//option[contains(text(), '%s')]", optionValue);
                }
            }
        }
        return result;
    }

    private boolean clickOption(String selectId, String optionXPath, String optionValue) {
        boolean result = false;
        By optionWithText = getSeleniumHelper().byXpath(optionXPath, selectId, optionValue);
        WebElement option = getSeleniumHelper().findElement(true, optionWithText);
        if (option != null) {
            option.click();
            result = true;
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

    public boolean clickAndWaitForPage(String place, final String pageName) {
        boolean result = click(place);
        if (result) {
            result = waitUntil(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver webDriver) {
                    return pageTitle().equals(pageName);
                }
            });
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
                By selectedOption = getSeleniumHelper().byXpath("//select[@id='%s']//option[@selected]", id);
                WebElement option = getSeleniumHelper().findElement(true, selectedOption);
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

    protected WebElement getElement(String place) {
        return getSeleniumHelper().getElement(place);
    }

    /**
     * @param timeout number of seconds before waitUntil() throws TimeOutException.
     */
    public void secondsBeforeTimeout(int timeout) {
        secondsBeforeTimeout = timeout;
    }

    /**
     * @return number of seconds waitUntil() will wait at most.
     */
    public int secondsBeforeTimeout() {
        return secondsBeforeTimeout;
    }

    /**
     * Implementations should wait until the condition evaluates to a value that is neither null nor
     * false. Because of this contract, the return type must not be Void.
     * @param <T> the return type of the method, which must not be Void
     * @param condition condition to evaluate to determine whether waiting can be stopped.
     * @throws org.openqa.selenium.TimeoutException if condition was not met before secondsBeforeTimeout.
     * @return result of condition.
     */
    protected <T> T waitUntil(ExpectedCondition<T> condition) {
        FluentWait<WebDriver> wait = waitDriver().withTimeout(secondsBeforeTimeout(), TimeUnit.SECONDS);
        return wait.until(condition);
    }

    private WebDriverWait waitDriver() {
        return getSeleniumHelper().waitDriver();
    }

    /**
     * Removes result of wiki formatting (for e.g. email addresses) if needed.
     * @param rawValue value as received from Fitnesse.
     * @return rawValue if it was just text, cleaned version if it was not.
     */
    protected String cleanupValue(String rawValue) {
        String result = null;
        Matcher matcher = PATTERN.matcher(rawValue);
        if (matcher.matches()) {
            result = matcher.group(2);
        } else {
            result = rawValue;
        }
        return result;
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
