package nl.hsac.fitnesse.fixture.slim.web;

import nl.hsac.fitnesse.fixture.slim.SlimFixture;
import nl.hsac.fitnesse.fixture.util.SeleniumHelper;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BrowserTest extends SlimFixture {
    private static final String FILES_DIR = new File("FitNesseRoot/files/").getAbsolutePath();

    private SeleniumHelper seleniumHelper = getEnvironment().getSeleniumHelper();
    private int secondsBeforeTimeout = 10;
    private int waitAfterScroll = 0;
    private String screenshotBase = FILES_DIR + "/screenshots/";
    private String screenshotHeight = "200";

    public boolean open(String address) {
        String url = getUrl(address);
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
        final WebElement element = getElement(place);
        boolean result = waitUntilInteractable(element);
        if (result) {
            element.clear();
            sendValue(element, value);
        }
        return result;
    }

    protected boolean waitUntilInteractable(final WebElement element) {
        return waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver webDriver) {
                return element != null && element.isDisplayed() && element.isEnabled();
            }
        });
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
            result = clickElement(option);
        }
        return result;
    }

    public boolean click(String place) {
        // if other element hides the element (in Chrome) an exception is thrown
        // we retry clicking the element a few times before giving up.
        boolean result = false;
        boolean retry = true;
        for (int i = 0;
             !result && retry;
             i++) {
            try {
                if (i > 0) {
                    waitSeconds(1);
                }
                result = clickImpl(place);
            } catch (WebDriverException e) {
                String msg = e.getMessage();
                if (!msg.contains("Other element would receive the click")
                        ||  i == secondsBeforeTimeout()) {
                    retry = false;
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
            scrollIfNotDisplayed(element);
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
                    scrollIfNotDisplayed(option);
                    result = option.getText();
                }
            } else {
                result = element.getAttribute("value");
                if (result == null) {
                    scrollIfNotDisplayed(element);
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

    protected WebElement getElement(String place) {
        return getSeleniumHelper().getElement(place);
    }

    public String textByXPath(String xPath) {
        return getTextByXPath(xPath);
    }

    protected String getTextByXPath(String xpathPattern, String... params) {
        String result = null;
        WebElement element = findByXPath(xpathPattern, params);
        if (element != null) {
            scrollIfNotDisplayed(element);
            result = element.getText();
        }
        return result;
    }

    protected WebElement findByXPath(String xpathPattern, String... params) {
        By by = getSeleniumHelper().byXpath(xpathPattern, params);
        return getSeleniumHelper().findElement(by);
    }

    protected List<WebElement> findAllByXPath(String xpathPattern, String... params) {
        By by = getSeleniumHelper().byXpath(xpathPattern, params);
        return getSeleniumHelper().driver().findElements(by);
    }

    public void waitMilliSecondAfterScroll(int msToWait) {
        waitAfterScroll = msToWait;
    }

    /**
     * Scrolls browser window so top of place becomes visible.
     * @param place element to scroll to.
     */
    public void scrollTo(String place) {
        WebElement element = getElement(place);
        if (place != null) {
            scrollTo(element);
        }
    }

    /**
     * Scrolls browser window so top of element becomes visible.
     * @param element element to scroll to.
     */
    protected void scrollTo(WebElement element) {
        getSeleniumHelper().executeJavascript("arguments[0].scrollIntoView(true);", element);
        if (waitAfterScroll > 0) {
            waitMilliSeconds(waitAfterScroll);
        }
    }

    /**
     * Scrolls browser window if element is not currently visible so top of element becomes visible.
     * @param element element to scroll to.
     */
    protected void scrollIfNotDisplayed(WebElement element) {
        if (!element.isDisplayed()) {
            scrollTo(element);
        }
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
     * Clears HTML5's localStorage.
     */
    public void clearLocalStorage() {
        getSeleniumHelper().executeJavascript("localStorage.clear();");
    }

    /**
     * @param directory sets base directory where screenshots will be stored.
     */
    public void screenshotBaseDirectory(String directory) {
        if (directory.equals("")
                || directory.endsWith("/")
                || directory.endsWith("\\")) {
            screenshotBase = directory;
        } else {
            screenshotBase = directory + "/";
        }
    }

    /**
     * @param height height to use to display screenshot images
     */
    public void screenshotShowHeight(String height) {
        screenshotHeight = height;
    }

    /**
     * Takes screenshot from current page
     * @param basename filename (below screenshot base directory).
     * @return location of screenshot.
     */
    public String takeScreenshot(String basename) {
        String name = screenshotBase + basename;
        String screenshotFile = getSeleniumHelper().takeScreenshot(name);
        if (screenshotFile == null) {
            throw new RuntimeException("Unable to take screenshot: does the webdriver support it?");
        } else {
            if (screenshotFile.startsWith(FILES_DIR)) {
                // make href to screenshot
                String relativeFile = screenshotFile.substring(FILES_DIR.length());
                relativeFile = relativeFile.replace('\\', '/');
                String wikiUrl = "/files" + relativeFile;
                if ("".equals(screenshotHeight)) {
                    wikiUrl = String.format("<a href=\"%s\">%s</a>",
                            wikiUrl, screenshotFile);
                } else {
                    wikiUrl = String.format("<a href=\"%1$s\"><img src=\"%1$s\" title=\"%2$s\" height=\"%3$s\"></a>",
                            wikiUrl, screenshotFile, screenshotHeight);
                }
                screenshotFile = wikiUrl;
            }
        }
        return screenshotFile;
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
        try {
            FluentWait<WebDriver> wait = waitDriver().withTimeout(secondsBeforeTimeout(), TimeUnit.SECONDS);
            return wait.until(condition);
        } catch (TimeoutException e) {
            // take a screenshot of what was on screen
            takeScreenshot("timeouts/" + getClass().getSimpleName() + "/timeout");
            throw e;
        }
    }

    private WebDriverWait waitDriver() {
        return getSeleniumHelper().waitDriver();
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
