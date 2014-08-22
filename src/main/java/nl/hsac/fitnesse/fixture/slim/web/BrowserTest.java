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
    private static final String ELEMENT_ON_SCREEN_JS =
            "var rect = arguments[0].getBoundingClientRect();\n" +
                    "return (\n" +
                    "  rect.top >= 0 &&\n" +
                    "  rect.left >= 0 &&\n" +
                    "  rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&\n" +
                    "  rect.right <= (window.innerWidth || document.documentElement.clientWidth));";

    private SeleniumHelper seleniumHelper = getEnvironment().getSeleniumHelper();
    private int secondsBeforeTimeout;
    private int waitAfterScroll = 0;
    private final String filesDir = getEnvironment().getFitNesseFilesSectionDir();
    private String screenshotBase = new File(filesDir, "screenshots").getPath() + "/";
    private String screenshotHeight = "200";

    public BrowserTest() {
        secondsBeforeTimeout(SeleniumHelper.DEFAULT_TIMEOUT_SECONDS);
    }

    public boolean open(String address) {
        String url = getUrl(address);
        try {
            getNavigation().to(url);
        } catch (TimeoutException e) {
            throw new TimeoutStopTestException("Unable to go to: " + url, e);
        }
        return true;
    }

    public boolean back() {
        getNavigation().back();

        // firefox sometimes prevents immediate back, if previous page was reached via POST
        waitMilliSeconds(500);
        WebElement element = getSeleniumHelper().findElement(By.id("errorTryAgain"));
        if (element != null) {
            element.click();
            Alert alert = getSeleniumHelper().driver().switchTo().alert();
            alert.accept();
        }
        return true;
    }

    public boolean forward() {
        getNavigation().forward();
        return true;
    }

    public boolean refresh() {
        getNavigation().refresh();
        return true;
    }

    private WebDriver.Navigation getNavigation() {
        return getSeleniumHelper().navigate();
    }

    public boolean switchToNextTab() {
        boolean result = false;
        List<String> tabs = getTabHandles();
        if (tabs.size() > 1) {
            int currentTab = getCurrentTabIndex(tabs);
            int nextTab = currentTab + 1;
            if (nextTab == tabs.size()) {
                nextTab = 0;
            }
            goToTab(tabs, nextTab);
            result = true;
        }
        return result;
    }

    public boolean switchToPreviousTab() {
        boolean result = false;
        List<String> tabs = getTabHandles();
        if (tabs.size() > 1) {
            int currentTab = getCurrentTabIndex(tabs);
            int nextTab = currentTab - 1;
            if (nextTab < 0) {
                nextTab = tabs.size() - 1;
            }
            goToTab(tabs, nextTab);
            result = true;
        }
        return result;
    }

    public boolean closeTab() {
        boolean result = false;
        List<String> tabs = getTabHandles();
        int currentTab = getCurrentTabIndex(tabs);
        int tabToGoTo = -1;
        if (currentTab > 0) {
            tabToGoTo = currentTab - 1;
        } else {
            if (tabs.size() > 1) {
                tabToGoTo = 1;
            }
        }
        if (tabToGoTo > -1) {
            WebDriver driver = getSeleniumHelper().driver();
            driver.close();
            goToTab(tabs, tabToGoTo);
            result = true;
        }
        return result;
    }

    public int tabCount() {
        return getTabHandles().size();
    }

    public int currentTabIndex() {
        return getCurrentTabIndex(getTabHandles()) + 1;
    }

    protected int getCurrentTabIndex(List<String> tabHandles) {
        return getSeleniumHelper().getCurrentTabIndex(tabHandles);
    }

    protected void goToTab(List<String> tabHandles, int indexToGoTo) {
        getSeleniumHelper().goToTab(tabHandles, indexToGoTo);
    }

    protected List<String> getTabHandles() {
        return getSeleniumHelper().getTabHandles();
    }

    public String pageTitle() {
        return getSeleniumHelper().getPageTitle();
    }

    /**
     * @return current page's content type.
     */
    public String pageContentType() {
        String result = null;
        Object ct = getSeleniumHelper().executeJavascript("return document.contentType;");
        if (ct != null) {
            result = ct.toString();
        }
        return result;
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
     * Simulates pressing the 'Tab' key.
     * @return true, if an element was active the key could be sent to.
     */
    public boolean pressTab() {
        return sendKeysToActiveElement(Keys.TAB);
    }

    /**
     * Simulates pressing the 'Enter' key.
     * @return true, if an element was active the key could be sent to.
     */
    public boolean pressEnter() {
        return sendKeysToActiveElement(Keys.ENTER);
    }

    /**
     * Simulates pressing the 'Esc' key.
     * @return true, if an element was active the key could be sent to.
     */
    public boolean pressEsc() {
        return sendKeysToActiveElement(Keys.ESCAPE);
    }

    /**
     * Simulates pressing a key.
     * @param key key to press, can be a normal letter (e.g. 'M') or a special key (e.g. 'down').
     * @return true, if an element was active the key could be sent to.
     */
    public boolean press(String key) {
        CharSequence s;
        try {
            s = Keys.valueOf(key.toUpperCase());
        } catch (IllegalArgumentException e) {
            s = key;
        }
        return sendKeysToActiveElement(s);
    }

    /**
     * Simulates pressing keys.
     * @param keys keys to press.
     * @return true, if an element was active the keys could be sent to.
     */
    protected boolean sendKeysToActiveElement(CharSequence keys) {
        boolean result = false;
        WebElement element = getSeleniumHelper().getActiveElement();
        if (element != null) {
            element.sendKeys(keys);
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
            // don't wait forever trying to click
            // only try secondsBeforeTimeout + 1 times
            retry &= i < secondsBeforeTimeout();
        }
        return result;
    }

    protected boolean clickImpl(String place) {
        WebElement element = getElement(place);
        if (element == null) {
            element = findByXPath("//*[@onclick and normalize-space(text())='%s']", place);
            if (element == null) {
                element = findByXPath("//*[@onclick and contains(normalize-space(text()),'%s')]", place);
                if (element == null) {
                    element = findByXPath("//*[@onclick and normalize-space(descendant::text())='%s']", place);
                    if (element == null) {
                        element = findByXPath("//*[@onclick and contains(normalize-space(descendant::text()),'%s')]", place);
                    }
                }
            }
        }
        return clickElement(element);
    }

    protected boolean clickElement(WebElement element) {
        boolean result = false;
        if (element != null) {
            scrollIfNotOnScreen(element);
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
            result = waitForPage(pageName);
        }
        return result;
    }

    public boolean waitForPage(final String pageName) {
        return waitUntil(new ExpectedCondition<Boolean>() {
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

    public boolean clickAndWaitForTagWithText(String place, final String tagName, final String expectedText) {
        boolean result = click(place);
        if (result) {
            result = waitForTagWithText(tagName, expectedText);
        }
        return result;
    }

    public boolean waitForTagWithText(final String tagName, final String expectedText) {
        return waitForElementWithText(By.tagName(tagName), expectedText);
    }

    public boolean clickAndWaitForClassWithText(String place, final String cssClassName, final String expectedText) {
        boolean result = click(place);
        if (result) {
            result = waitForClassWithText(cssClassName, expectedText);
        }
        return result;
    }

    public boolean waitForClassWithText(final String cssClassName, final String expectedText) {
        return waitForElementWithText(By.className(cssClassName), expectedText);
    }

    protected boolean waitForElementWithText(final By by, String expectedText) {
        final String textToLookFor = cleanExpectedValue(expectedText);
        return waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver webDriver) {
                boolean ok = false;

                List<WebElement> elements = webDriver.findElements(by);
                if (elements != null) {
                    for (WebElement element : elements) {
                        ok = hasText(element, textToLookFor);
                        if (ok) {
                            // no need to continue to check other elements
                            break;
                        }
                    }
                }
                return ok;
            }
        });
    }

    protected String cleanExpectedValue(String expectedText) {
        String textToLookFor;
        if (expectedText != null) {
            // wiki sends newlines as <br/>, Selenium reports <br/> as newlines ;-)
            textToLookFor = expectedText.replace("<br/>", "\n");
        } else {
            textToLookFor = expectedText;
        }
        return textToLookFor;
    }

    protected boolean hasText(WebElement element, String textToLookFor) {
        boolean ok;
        try {
            String actual = getElementText(element);
            if (textToLookFor == null) {
                ok = actual == null;
            } else {
                if (actual == null) {
                    actual = element.getAttribute("value");
                }
                ok = textToLookFor.equals(actual);
            }
        } catch (StaleElementReferenceException e) {
            // element detached from DOM
            ok = false;
        }
        return ok;
    }

    public boolean clickAndWaitForClass(String place, final String cssClassName) {
        boolean result = click(place);
        if (result) {
            result = waitForClass(cssClassName);
        }
        return result;
    }

    public boolean waitForClass(final String cssClassName) {
        boolean result;
        result = waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver webDriver) {
                boolean ok = false;

                WebElement element = webDriver.findElement(By.className(cssClassName));
                if (element != null) {
                    ok = true;
                }
                return ok;
            }
        });
        return result;
    }

    public String spaceNormalized(String input) {
        return input.trim().replaceAll("\\s+", " ");
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
                result = getElementText(option);
            } else {
                result = element.getAttribute("value");
                if (result == null) {
                    scrollIfNotOnScreen(element);
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

    public String valueOfColumnNumberInRowNumber(int columnIndex, int rowIndex) {
        String xPath = String.format("//tr[%s]/td[%s]", rowIndex, columnIndex);
        WebElement element = findByXPath(xPath);
        return getElementText(element);
    }

    public String valueOfInRowNumber(String requestedColumnName, int rowIndex) {
        String columnXPath = String.format("//tr[%s]/td", rowIndex);
        return valueInRow(columnXPath, requestedColumnName);
    }

    public String valueOfInRowWhereIs(String requestedColumnName, String selectOnColumn, String selectOnValue) {
        String columnXPath = getXPathForColumnInRowByValueInOtherColumn(selectOnColumn, selectOnValue);
        return valueInRow(columnXPath, requestedColumnName);
    }

    protected String valueInRow(String columnXPath, String requestedColumnName) {
        String requestedIndex = getXPathForColumnIndex(requestedColumnName);
        WebElement element = findByXPath("%s[%s]", columnXPath, requestedIndex);
        return getElementText(element);
    }

    public boolean clickInRowNumber(String place, int rowIndex) {
        String columnXPath = String.format("//tr[%s]/td", rowIndex);
        return clickInRow(columnXPath, place);
    }

    public boolean clickInRowWhereIs(String place, String selectOnColumn, String selectOnValue) {
        String columnXPath = getXPathForColumnInRowByValueInOtherColumn(selectOnColumn, selectOnValue);
        return clickInRow(columnXPath, place);
    }

    protected boolean clickInRow(String columnXPath, String place) {
        boolean result = false;
        // find an input to click in the row
        WebElement element = findByXPath("%s//input[contains(@value, '%s')]", columnXPath, place);
        if (element == null) {
            // see whether there is an element with the specified place as text() in the row
            element = findByXPath("%s//*[contains(normalize-space(text()),'%s')]", columnXPath, place);
            if (element == null) {
                // find an input to click in the row by its title (aka tooltip)
                element = findByXPath("%s//input[contains(@title, '%s')]", columnXPath, place);
            }
        }
        if (element != null) {
            result = clickElement(element);
        }
        return result;
    }

    /**
     * Creates an XPath expression that will find a cell in a row, selecting the row based on the
     * text in a specific column (identified by its header text).
     * @param columnName header text of the column to find value in.
     * @param value text to find in column with the supplied header.
     * @return XPath expression selecting a td in the row
     */
    protected String getXPathForColumnInRowByValueInOtherColumn(String columnName, String value) {
        String selectIndex = getXPathForColumnIndex(columnName);
        return String.format("//tr[normalize-space(td[%s]/descendant-or-self::text())='%s']/td", selectIndex, value);
    }

    /**
     * Creates an XPath expression that will determine, for a row, which index to use to select the cell in the column
     * with the supplied header text value.
     * @param columnName name of column in header (th)
     * @return XPath expression which can be used to select a td in a row
     */
    protected String getXPathForColumnIndex(String columnName) {
        // determine how many columns are before the column with the requested name
        // the column with the requested name will have an index of the value +1 (since XPath indexes are 1 based)
        return String.format("count(//tr/th[normalize-space(descendant-or-self::text())='%s']/preceding-sibling::th)+1", columnName);
    }

    protected WebElement getElement(String place) {
        return getSeleniumHelper().getElement(place);
    }

    public String textByXPath(String xPath) {
        return getTextByXPath(xPath);
    }

    protected String getTextByXPath(String xpathPattern, String... params) {
        WebElement element = findByXPath(xpathPattern, params);
        return getElementText(element);
    }

    public String textByClassName(String className) {
        return getTextByClassName(className);
    }

    protected String getTextByClassName(String className) {
        WebElement element = findByClassName(className);
        return getElementText(element);
    }

    protected WebElement findByClassName(String className) {
        By by = By.className(className);
        return getSeleniumHelper().findElement(by);
    }

    protected WebElement findByXPath(String xpathPattern, String... params) {
        By by = getSeleniumHelper().byXpath(xpathPattern, params);
        return getSeleniumHelper().findElement(by);
    }

    protected List<WebElement> findAllByXPath(String xpathPattern, String... params) {
        By by = getSeleniumHelper().byXpath(xpathPattern, params);
        return getSeleniumHelper().driver().findElements(by);
    }

    protected List<WebElement> findAllByCss(String cssPattern, String... params) {
        By by = getSeleniumHelper().byCss(cssPattern, params);
        return getSeleniumHelper().driver().findElements(by);
    }

    public void waitMilliSecondAfterScroll(int msToWait) {
        waitAfterScroll = msToWait;
    }

    protected String getElementText(WebElement element) {
        String result = null;
        if (element != null) {
            scrollIfNotOnScreen(element);
            result = element.getText();
        }
        return result;
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
    protected void scrollIfNotOnScreen(WebElement element) {
        if (element.isDisplayed() && !isElementOnScreen(element)) {
            scrollTo(element);
        }
    }

    /**
     * Determines whether element can be see in browser's window.
     * @param place element to check.
     * @return true if element is displayed and in viewport.
     */
    public boolean isVisible(String place) {
        boolean result = false;
        WebElement element = getElement(place);
        if (element != null) {
            result = element.isDisplayed() && isElementOnScreen(element);
        }
        return result;
    }

    /**
     * Checks whether element is in browser's viewport.
     * @param element element to check
     * @return true if element is in browser's viewport.
     */
    protected boolean isElementOnScreen(WebElement element) {
        return (Boolean)getSeleniumHelper().executeJavascript(ELEMENT_ON_SCREEN_JS, element);
    }

    /**
     * @param timeout number of seconds before waitUntil() throws TimeOutException.
     */
    public void secondsBeforeTimeout(int timeout) {
        secondsBeforeTimeout = timeout;
        int timeoutInMs = timeout * 1000;
        getSeleniumHelper().setPageLoadWait(timeoutInMs);
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
        String screenshotFile = createScreenshot(basename);
        if (screenshotFile == null) {
            throw new RuntimeException("Unable to take screenshot: does the webdriver support it?");
        } else {
            if (screenshotFile.startsWith(filesDir)) {
                // make href to screenshot
                String relativeFile = screenshotFile.substring(filesDir.length());
                relativeFile = relativeFile.replace('\\', '/');
                String wikiUrl = "files" + relativeFile;
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

    private String createScreenshot(String basename) {
        String name = screenshotBase + basename;
        return getSeleniumHelper().takeScreenshot(name);
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
            String screenShotFile = null;
            try {
                screenShotFile = createScreenshot("timeouts/" + getClass().getSimpleName() + "/timeout");
            } catch (Exception sse) {
                // unable to take screenshot
                sse.printStackTrace();
            }
            if (screenShotFile == null) {
                throw new TimeoutStopTestException(e);
            } else {
                throw new TimeoutStopTestException("Screenshot available at: " + screenShotFile, e);
            }
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

    public int currentBrowserWidth() {
        WebDriver.Window window = getSeleniumHelper().driver().manage().window();
        window.setPosition(new Point(0, 0));
        return window.getSize().getWidth();
    }

    public void setBrowserWidth(int newWidth) {
        WebDriver.Window window = getSeleniumHelper().driver().manage().window();
        window.setPosition(new Point(0, 0));
        int currentBrowserHeight = window.getSize().getHeight();
        window.setSize(new Dimension(newWidth, currentBrowserHeight));
    }
}
