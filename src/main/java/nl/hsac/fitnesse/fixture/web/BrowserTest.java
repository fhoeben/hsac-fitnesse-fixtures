package nl.hsac.fitnesse.fixture.web;

import nl.hsac.fitnesse.fixture.util.SeleniumHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BrowserTest extends SlimFixture {
    private static final Pattern PATTERN = Pattern.compile("<a href=\"(.*?)\">(.*?)</a>", Pattern.CASE_INSENSITIVE);

    private SeleniumHelper seleniumHelper = getEnvironment().getSeleniumHelper();
    private int secondsBeforeTimeout = 10;

    public boolean open(String address) {
        String url = urlFromLink(address);
        if (url == null) {
            // not a html link, use raw value
            url = address;
        }
        getSeleniumHelper().navigate().to(url);
        return true;
    }

    public String pageTitle() {
        return getSeleniumHelper().getPageTitle();
    }

    /**
     * Replaces content at place by value.
     * @param value value to set.
     * @param place element to set value on.
     * @return true, if element was found.
     */
    public boolean enterAs(String value, String place) {
        boolean result = false;
        WebElement element = getElement(place);
        if (element != null) {
            element.clear();
            sendValue(element, value);
            result = true;
        }
        return result;
    }

    /**
     * Adds content to place.
     * @param value value to add.
     * @param place element to add value to.
     * @return true, if element was found.
     */
    public boolean enterFor(String value, String place) {
        boolean result = false;
        WebElement element = getElement(place);
        if (element != null) {
            sendValue(element, value);
            result = true;
        }
        return result;
    }

    /**
     * Sends Fitnesse cell content to element.
     * @param element element to call sendKeys() on.
     * @param value cell content.
     */
    protected void sendValue(WebElement element, String value) {
        String keys = cleanupValue(value);
        element.sendKeys(keys);
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
                String attrToUse = "id";
                String attrValue = element.getAttribute(attrToUse);
                if (attrValue == null || attrValue.isEmpty()) {
                    attrToUse = "name";
                    attrValue = element.getAttribute(attrToUse);
                }

                if (attrValue != null && !attrValue.isEmpty()) {
                    String xpathToOptions = "//select[@" + attrToUse + "='%s']//option";
                    result = clickOption(attrValue, xpathToOptions + "[text()='%s']", optionValue);
                    if (!result) {
                        result = clickOption(attrValue, xpathToOptions + "[contains(text(), '%s')]", optionValue);
                    }
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
        // if other element hides the element (in Chrome) an exception is thrown
        // we retry clicking the element a few times before giving up.
        boolean result = false;
        boolean retry = true;
        for (int i = 0;
             !result && retry && i < secondsBeforeTimeout();
             i++) {
            try {
                result = clickImpl(place);
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

    protected boolean clickImpl(String place) {
        WebElement element = getElement(place);
        return clickElement(element);
    }

    protected boolean clickElement(WebElement element) {
        boolean result = false;
        if (element != null) {
            if (element.isDisplayed() && element.isEnabled()) {
                element.click();
                result = true;
            }
        }
        return result;
    }

    public boolean clickAndWaitForPage(String place, final String pageName) {
        boolean result = click(place);
        if (result) {
            result = waitUntil(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver webDriver) {
                    boolean ok = false;
                    try {
                        ok = pageTitle().equals(pageName);
                    } catch (StaleElementReferenceException e) {
                        // element detached from DOM
                        ok = false;
                    }
                    return ok;
                }
            });
        }
        return result;
    }

    public boolean clickAndWaitForTagWithText(String place, final String tagName, final String expectedText) {
        boolean result = click(place);
        if (result) {
            result = waitForTagWithText(tagName, expectedText);
        }
        return result;
    }

    public boolean waitForTagWithText(final String tagName, final String expectedText) {
        boolean result;
        result = waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver webDriver) {
                boolean ok = false;
                List<WebElement> elements = webDriver.findElements(By.tagName(tagName));
                if (elements != null) {
                    for (WebElement element : elements) {
                        try {
                            String actual = element.getText();
                            if (expectedText == null) {
                                ok = actual == null;
                            } else {
                                if (actual == null) {
                                    actual = element.getAttribute("value");
                                }
                                ok = expectedText.equals(actual);
                            }
                        } catch (StaleElementReferenceException e) {
                            // element detached from DOM
                            ok = false;
                        }
                        if (ok) {
                            // no need to continue to check other elements
                            break;
                        }
                    }
                }
                return ok;
            }
        });
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
                if (result == null) {
                    result = element.getText();
                }
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

    /**
     * @return helper to use.
     */
    protected final SeleniumHelper getSeleniumHelper() {
        return seleniumHelper;
    }

    /**
     * Sets SeleniumHelper to use, for testing purposes.
     * @param helper helper to use.
     */
    void setSeleniumHelper(SeleniumHelper helper) {
        seleniumHelper = helper;
    }
}
