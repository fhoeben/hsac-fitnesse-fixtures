package nl.hsac.fitnesse.fixture.slim.web;

import nl.hsac.fitnesse.fixture.slim.SlimFixture;
import nl.hsac.fitnesse.fixture.slim.SlimFixtureException;
import nl.hsac.fitnesse.fixture.util.BinaryHttpResponse;
import nl.hsac.fitnesse.fixture.util.FileUtil;
import nl.hsac.fitnesse.fixture.util.HttpResponse;
import nl.hsac.fitnesse.fixture.util.SeleniumHelper;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class BrowserTest extends SlimFixture {
    private SeleniumHelper seleniumHelper = getEnvironment().getSeleniumHelper();
    private int secondsBeforeTimeout;
    private int waitAfterScroll = 0;
    private String screenshotBase = new File(filesDir, "screenshots").getPath() + "/";
    private String screenshotHeight = "200";
    private String downloadBase = new File(filesDir, "downloads").getPath() + "/";

    public BrowserTest() {
        secondsBeforeTimeout(SeleniumHelper.DEFAULT_TIMEOUT_SECONDS);
        ensureActiveTabIsNotClosed();
    }

    public boolean open(String address) {
        final String url = getUrl(address);
        try {
            getNavigation().to(url);
        } catch (TimeoutException e) {
            handleTimeoutException(e);
        }
        waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver webDriver) {
                String readyState = getSeleniumHelper().executeJavascript("return document.readyState").toString();
                // IE 7 is reported to return "loaded"
                boolean done = "complete".equalsIgnoreCase(readyState) || "loaded".equalsIgnoreCase(readyState);
                if (!done) {
                    System.err.printf("Open of %s returned while document.readyState was %s", url, readyState);
                }
                return done;
            }
        });
        return true;
    }

    public boolean back() {
        getNavigation().back();

        // firefox sometimes prevents immediate back, if previous page was reached via POST
        waitMilliseconds(500);
        WebElement element = getSeleniumHelper().findElement(By.id("errorTryAgain"));
        if (element != null) {
            element.click();
            // don't use confirmAlert as this may be overridden in subclass and to get rid of the
            // firefox pop-up we need the basic behavior
            getSeleniumHelper().getAlert().accept();
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

    public boolean confirmAlert() {
        boolean result = false;
        Alert alert = getAlert();
        if (alert != null) {
            alert.accept();
            result = true;
        }
        return result;
    }

    public boolean dismissAlert() {
        boolean result = false;
        Alert alert = getAlert();
        if (alert != null) {
            alert.dismiss();
            result = true;
        }
        return result;
    }

    protected Alert getAlert() {
        return getSeleniumHelper().getAlert();
    }

    public boolean openInNewTab(String url) {
        String cleanUrl = getUrl(url);
        final int tabCount = tabCount();
        getSeleniumHelper().executeJavascript("window.open('%s', '_blank')", cleanUrl);
        // ensure new window is open
        waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver webDriver) {
                return tabCount() > tabCount;
            }
        });
        return switchToNextTab();
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

    public void ensureOnlyOneTab() {
        ensureActiveTabIsNotClosed();
        int tabCount = tabCount();
        for (int i = 1; i < tabCount; i++) {
            closeTab();
        }
    }

    public boolean ensureActiveTabIsNotClosed() {
        boolean result = false;
        List<String> tabHandles = getTabHandles();
        int currentTab = getCurrentTabIndex(tabHandles);
        if (currentTab < 0) {
            result = true;
            goToTab(tabHandles, 0);
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
        return element != null
                && waitUntil(new ExpectedCondition<Boolean>() {
                    @Override
                    public Boolean apply(WebDriver webDriver) {
                        return element.isDisplayed() && element.isEnabled();
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
     * Simulates pressing a key (or a combination of keys).
     * (Unfortunately not all combinations seem to be accepted by all drivers, e.g.
     * Chrome on OSX seems to ignore Command+A or Command+T; https://code.google.com/p/selenium/issues/detail?id=5919).
     * @param key key to press, can be a normal letter (e.g. 'M') or a special key (e.g. 'down').
     *            Combinations can be passed by separating the keys to send with '+' (e.g. Command + T).
     * @return true, if an element was active the key could be sent to.
     */
    public boolean press(String key) {
        CharSequence s;
        String[] parts = key.split("\\s*\\+\\s*");
        if (parts.length > 1
                && !"".equals(parts[0]) && !"".equals(parts[1])) {
            CharSequence[] sequence = new CharSequence[parts.length];
            for (int i = 0; i < parts.length; i++) {
                sequence[i] = parseKey(parts[i]);
            }
            s = Keys.chord(sequence);
        } else {
            s = parseKey(key);
        }

        return sendKeysToActiveElement(s);
    }

    protected CharSequence parseKey(String key) {
        CharSequence s;
        try {
            s = Keys.valueOf(key.toUpperCase());
        } catch (IllegalArgumentException e) {
            s = key;
        }
        return s;
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
        if (StringUtils.isNotEmpty(value)) {
            String keys = cleanupValue(value);
            element.sendKeys(keys);
        }
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
        WebElement element = getElement(selectPlace);
        return clickSelectOption(element, optionValue);
    }

    protected boolean clickSelectOption(WebElement element, String optionValue) {
        boolean result = false;
        if (element != null) {
            if (isSelect(element)) {
                By xpath = getSeleniumHelper().byXpath(".//option[normalize-space(text()) = '%s']", optionValue);
                WebElement option = getSeleniumHelper().findElement(element, true, xpath);
                if (option == null) {
                    xpath = getSeleniumHelper().byXpath(".//option[contains(normalize-space(text()), '%s')]", optionValue);
                    option = getSeleniumHelper().findElement(element, true, xpath);
                }
                if (option != null) {
                    result = clickElement(option);
                }
            }
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
            } catch (TimeoutException e) {
                String message = getTimeoutMessage(e);
                throw new SlimFixtureException(false, message, e);
            } catch (WebDriverException e) {
                String msg = e.getMessage();
                if (!msg.contains("Other element would receive the click")) {
                    // unexpected exception: throw to wiki
                    throw e;
                }
                if (i == secondsBeforeTimeout()) {
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

    public boolean waitForTagWithText(final String tagName, final String expectedText) {
        return waitForElementWithText(By.tagName(tagName), expectedText);
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
                    try {
                        for (WebElement element : elements) {
                            // we don't want stale elements to make single
                            // element false, but instead we stop processing
                            // current list and do a new findElements
                            ok = hasTextUnsafe(element, textToLookFor);
                            if (ok) {
                                // no need to continue to check other elements
                                break;
                            }
                        }
                    } catch (StaleElementReferenceException e) {
                        // find elements again if still allowed
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
            ok = hasTextUnsafe(element, textToLookFor);
        } catch (StaleElementReferenceException e) {
            // element detached from DOM
            ok = false;
        }
        return ok;
    }

    protected boolean hasTextUnsafe(WebElement element, String textToLookFor) {
        boolean ok;
        String actual = getElementText(element);
        if (textToLookFor == null) {
            ok = actual == null;
        } else {
            if (actual == null) {
                actual = element.getAttribute("value");
            }
            ok = textToLookFor.equals(actual);
        }
        return ok;
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

    public String valueOf(String place) {
        return valueFor(place);
    }

    public String valueFor(String place) {
        String result;
        try {
            result = valueForImpl(place);
        } catch (StaleElementReferenceException e) {
            // sometimes ajax updates get in the way. In that case we try again
            result = valueForImpl(place);
        }
        return result;
    }

    private String valueForImpl(String place) {
        WebElement element = getElement(place);
        return valueFor(element);
    }

    protected String valueFor(WebElement element) {
        String result = null;
        if (element != null) {
            if (isSelect(element)) {
                Select s = new Select(element);
                List<WebElement> options = s.getAllSelectedOptions();
                if (options.size() > 0) {
                    result = getElementText(options.get(0));
                }
            } else {
                if ("checkbox".equals(element.getAttribute("type"))) {
                    result = String.valueOf("true".equals(element.getAttribute("checked")));
                } else {
                    result = element.getAttribute("value");
                    if (result == null) {
                        scrollIfNotOnScreen(element);
                        result = element.getText();
                    }
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

    public boolean enterAsInRowWhereIs(String value, String requestedColumnName, String selectOnColumn, String selectOnValue) {
        String columnXPath = getXPathForColumnInRowByValueInOtherColumn(selectOnColumn, selectOnValue);
        String requestedIndex = getXPathForColumnIndex(requestedColumnName);
        WebElement element = findByXPath("%s[%s]//input", columnXPath, requestedIndex);
        if (element == null) {
            element = findByXPath("%s[%s]//textarea", columnXPath, requestedIndex);
        }
        boolean result = waitUntilInteractable(element);
        if (result) {
            element.clear();
            sendValue(element, value);
        }
        return result;
    }

    public String valueOfColumnNumberInRowNumber(int columnIndex, int rowIndex) {
        return getTextByXPath("(//tr[boolean(td)])[%s]/td[%s]", Integer.toString(rowIndex), Integer.toString(columnIndex));
    }

    public String valueOfInRowNumber(String requestedColumnName, int rowIndex) {
        String columnXPath = String.format("(//tr[boolean(td)])[%s]/td", rowIndex);
        return valueInRow(columnXPath, requestedColumnName);
    }

    public String valueOfInRowWhereIs(String requestedColumnName, String selectOnColumn, String selectOnValue) {
        String columnXPath = getXPathForColumnInRowByValueInOtherColumn(selectOnColumn, selectOnValue);
        return valueInRow(columnXPath, requestedColumnName);
    }

    protected String valueInRow(String columnXPath, String requestedColumnName) {
        String requestedIndex = getXPathForColumnIndex(requestedColumnName);
        return getTextByXPath("%s[%s]", columnXPath, requestedIndex);
    }

    public boolean rowExistsWhereIs(String selectOnColumn, String selectOnValue) {
        String columnXPath = getXPathForColumnInRowByValueInOtherColumn(selectOnColumn, selectOnValue);
        WebElement element = findByXPath(columnXPath);
        return element != null;
    }

    public boolean clickInRowNumber(String place, int rowIndex) {
        String columnXPath = String.format("(//tr[boolean(td)])[%s]/td", rowIndex);
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
                // find an element to click in the row by its title (aka tooltip)
                element = findByXPath("%s//*[contains(@title, '%s')]", columnXPath, place);
            }
        }
        if (element != null) {
            result = clickElement(element);
        }
        return result;
    }

    /**
     * Downloads the target of a link in a grid's row.
     * @param place which link to download.
     * @param rowNumber (1-based) row number to retrieve link from.
     * @return downloaded file if any, null otherwise.
     */
    public String downloadFromRowNumber(String place, int rowNumber) {
        String columnXPath = String.format("(//tr[boolean(td)])[%s]/td", rowNumber);
        return downloadFromRow(columnXPath, place);
    }

    /**
     * Downloads the target of a link in a grid, finding the row based on one of the other columns' value.
     * @param place which link to download.
     * @param selectOnColumn column header of cell whose value must be selectOnValue.
     * @param selectOnValue value to be present in selectOnColumn to find correct row.
     * @return downloaded file if any, null otherwise.
     */
    public String downloadFromRowWhereIs(String place, String selectOnColumn, String selectOnValue) {
        String columnXPath = getXPathForColumnInRowByValueInOtherColumn(selectOnColumn, selectOnValue);
        return downloadFromRow(columnXPath, place);
    }

    protected String downloadFromRow(String columnXPath, String place) {
        String result = null;
        // find an a to download from based on its text()
        WebElement element = findByXPath("%s//a[contains(normalize-space(text()),'%s')]", columnXPath, place);
        if (element == null) {
            // find an a to download based on its column header
            String requestedIndex = getXPathForColumnIndex(place);
            element = findByXPath("%s[%s]//a", columnXPath, requestedIndex);
            if (element == null) {
                // find an a to download in the row by its title (aka tooltip)
                element = findByXPath("%s//a[contains(@title, '%s')]", columnXPath, place);
            }
        }
        if (element == null) {
            throw new SlimFixtureException(false, "Unable to find link: " + place);
        } else {
            result = downloadLinkTarget(element);
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
        return String.format("//tr[td[%s]/descendant-or-self::text()[normalize-space(.)='%s']]/td", selectIndex, value);
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
        return String.format("count(ancestor::table[1]//tr/th/descendant-or-self::text()[normalize-space(.)='%s']/ancestor-or-self::th[1]/preceding-sibling::th)+1", columnName);
    }

    protected WebElement getElement(String place) {
        return getSeleniumHelper().getElement(place);
    }

    public String textByXPath(String xPath) {
        return getTextByXPath(xPath);
    }

    protected String getTextByXPath(String xpathPattern, String... params) {
        String result;
        try {
            WebElement element = findByXPath(xpathPattern, params);
            result = getElementText(element);
        } catch (StaleElementReferenceException e) {
            // sometime we are troubled by ajax updates that cause 'stale state' let's try once more if that is the case
            WebElement element = findByXPath(xpathPattern, params);
            result = getElementText(element);
        }
        return result;
    }

    public String textByClassName(String className) {
        return getTextByClassName(className);
    }

    protected String getTextByClassName(String className) {
        String result;
        try {
            WebElement element = findByClassName(className);
            result = getElementText(element);
        } catch (StaleElementReferenceException e) {
            // sometime we are troubled by ajax updates that cause 'stale state' let's try once more if that is the case
            WebElement element = findByClassName(className);
            result = getElementText(element);
        }
        return result;
    }

    protected WebElement findByClassName(String className) {
        By by = By.className(className);
        return getSeleniumHelper().findElement(by);
    }

    protected WebElement findByXPath(String xpathPattern, String... params) {
        By by = getSeleniumHelper().byXpath(xpathPattern, params);
        return getSeleniumHelper().findElement(by);
    }

    protected WebElement findByJavascript(String script, Object... parameters) {
        By by = getSeleniumHelper().byJavascript(script, parameters);
        return getSeleniumHelper().findElement(by);
    }

    protected List<WebElement> findAllByXPath(String xpathPattern, String... params) {
        By by = getSeleniumHelper().byXpath(xpathPattern, params);
        return findElements(by);
    }

    protected List<WebElement> findAllByCss(String cssPattern, String... params) {
        By by = getSeleniumHelper().byCss(cssPattern, params);
        return findElements(by);
    }

    protected List<WebElement> findAllByJavascript(String script, Object... parameters) {
        By by = getSeleniumHelper().byJavascript(script, parameters);
        return findElements(by);
    }

    protected List<WebElement> findElements(By by) {
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
        getSeleniumHelper().scrollTo(element);
        if (waitAfterScroll > 0) {
            waitMilliseconds(waitAfterScroll);
        }
    }

    /**
     * Scrolls browser window if element is not currently visible so top of element becomes visible.
     * @param element element to scroll to.
     */
    protected void scrollIfNotOnScreen(WebElement element) {
        if (!element.isDisplayed() || !isElementOnScreen(element)) {
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
        return getSeleniumHelper().isElementOnScreen(element);
    }

    /**
     * @param timeout number of seconds before waitUntil() and waitForJavascriptCallback() throw TimeOutException.
     */
    public void secondsBeforeTimeout(int timeout) {
        secondsBeforeTimeout = timeout;
        int timeoutInMs = timeout * 1000;
        getSeleniumHelper().setPageLoadWait(timeoutInMs);
        getSeleniumHelper().setScriptWait(timeoutInMs);
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
            throw new SlimFixtureException(false, "Unable to take screenshot: does the webdriver support it?");
        } else {
            screenshotFile = getScreenshotLink(screenshotFile);
        }
        return screenshotFile;
    }

    private String getScreenshotLink(String screenshotFile) {
        String wikiUrl = getWikiUrl(screenshotFile);
        if (wikiUrl != null) {
            // make href to screenshot

            if ("".equals(screenshotHeight)) {
                wikiUrl = String.format("<a href=\"%s\">%s</a>",
                        wikiUrl, screenshotFile);
            } else {
                wikiUrl = String.format("<a href=\"%1$s\"><img src=\"%1$s\" title=\"%2$s\" height=\"%3$s\"/></a>",
                        wikiUrl, screenshotFile, screenshotHeight);
            }
            screenshotFile = wikiUrl;
        }
        return screenshotFile;
    }

    private String createScreenshot(String basename) {
        String name = getScreenshotBasename(basename);
        return getSeleniumHelper().takeScreenshot(name);
    }

    private String createScreenshot(String basename, Throwable t) {
        String screenshotFile;
        byte[] screenshotInException = getSeleniumHelper().findScreenshot(t);
        if (screenshotInException == null || screenshotInException.length == 0) {
            screenshotFile = createScreenshot(basename);
        } else {
            String name = getScreenshotBasename(basename);
            screenshotFile = getSeleniumHelper().writeScreenshot(name, screenshotInException);
        }
        return screenshotFile;
    }

    private String getScreenshotBasename(String basename) {
        return screenshotBase + basename;
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
            return handleTimeoutException(e);
        }
    }

    private <T> T handleTimeoutException(TimeoutException e) {
        String message = getTimeoutMessage(e);
        throw new TimeoutStopTestException(false, message, e);
    }

    private String getTimeoutMessage(TimeoutException e) {
        // take a screenshot of what was on screen
        String screenShotFile = null;
        try {
            screenShotFile = createScreenshot("timeouts/" + getClass().getSimpleName() + "/timeout", e);
        } catch (Exception sse) {
            // unable to take screenshot
            sse.printStackTrace();
        }
        String message;
        if (screenShotFile == null) {
            message = String.format("Timed-out waiting (after %ss).",
                                    secondsBeforeTimeout());
        } else {
            message = String.format("<div>Timed-out waiting (after %ss). Page content:%s</div>",
                                    secondsBeforeTimeout(), getScreenshotLink(screenShotFile));
        }
        return message;
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
        return getSeleniumHelper().getWindowSize().getWidth();
    }

    public int currentBrowserHeight() {
        return getSeleniumHelper().getWindowSize().getHeight();
    }

    public void setBrowserWidth(int newWidth) {
        int currentHeight = getSeleniumHelper().getWindowSize().getHeight();
        setBrowserSizeToBy(newWidth, currentHeight);
    }

    public void setBrowserHeight(int newHeight) {
        int currentWidth = getSeleniumHelper().getWindowSize().getWidth();
        setBrowserSizeToBy(currentWidth, newHeight);
    }

    public void setBrowserSizeToBy(int newWidth, int newHeight) {
        getSeleniumHelper().setWindowSize(newWidth, newHeight);
        Dimension actualSize = getSeleniumHelper().getWindowSize();
        if (actualSize.getHeight() != newHeight || actualSize.getWidth() != newWidth) {
            String message = String.format("Unable to change size to: %s x %s; size is: %s x %s",
                                newWidth, newHeight, actualSize.getWidth(), actualSize.getHeight());
            throw new SlimFixtureException(false, message);
        }
    }

    /**
     * Downloads the target of the supplied link.
     * @param place link to follow.
     * @return downloaded file if any, null otherwise.
     */
    public String download(String place) {
        By selector = By.linkText(place);
        WebElement element = getSeleniumHelper().findElement(selector);
        if (element == null) {
            selector = By.partialLinkText(place);
            element = getSeleniumHelper().findElement(selector);
            if (element == null) {
                selector = By.id(place);
                element = getSeleniumHelper().findElement(selector);
                if (element == null) {
                    selector = By.name(place);
                    element = getSeleniumHelper().findElement(selector);
                }
            }
        }
        if (element == null) {
            throw new SlimFixtureException(false, "Unable to determine what to download for: " + place);
        }
        return downloadLinkTarget(element);
    }

    /**
     * Downloads the target of the supplied link.
     * @param element link to follow.
     * @return downloaded file if any, null otherwise.
     */
    protected String downloadLinkTarget(WebElement element) {
        String result = null;
        if (element != null) {
            String href = element.getAttribute("href");
            if (href != null) {
                result = downloadContentFrom(href);
            } else {
                throw new SlimFixtureException(false, "Could not determine url to download from");
            }
        }
        return result;
    }

    /**
     * Downloads binary content from specified url (using the browser's cookies).
     * @param urlOrLink url to download from
     * @return link to downloaded file
     */
    public String downloadContentFrom(String urlOrLink) {
        String result = null;
        if (urlOrLink != null) {
            String url = getUrl(urlOrLink);
            BinaryHttpResponse resp = new BinaryHttpResponse();
            getUrlContent(url, resp);
            byte[] content = resp.getResponseContent();
            if (content == null) {
                result = resp.getResponse();
            } else {
                String fileName = resp.getFileName();
                String baseName = FilenameUtils.getBaseName(fileName);
                String ext = FilenameUtils.getExtension(fileName);
                String downloadedFile = FileUtil.saveToFile(getDownloadName(baseName), ext, content);
                String wikiUrl = getWikiUrl(downloadedFile);
                if (wikiUrl != null) {
                    // make href to file
                    result = String.format("<a href=\"%s\">%s</a>", wikiUrl, fileName);
                } else {
                    result = downloadedFile;
                }
            }
        }
        return result;
    }

    /**
     * Selects a file using a file upload control.
     * @param fileName file to upload
     * @param place file input to select the file for
     * @return true, if place was a file input and file existed.
     */
    public boolean selectFileFor(String fileName, String place) {
        boolean result = false;
        if (fileName != null) {
            WebElement element = getElement(place);
            if (element != null) {
                if ("input".equalsIgnoreCase(element.getTagName())
                        && "file".equalsIgnoreCase(element.getAttribute("type"))) {
                    String fullPath = getFilePathFromWikiUrl(fileName);
                    if (new File(fullPath).exists()) {
                        element.sendKeys(fullPath);
                        result = true;
                    } else {
                        throw new SlimFixtureException(false, "Unable to find file: " + fullPath);
                    }
                }
            }
        }
        return result;
    }

    private String getDownloadName(String baseName) {
        return downloadBase + baseName;
    }

    /**
     * GETs content of specified URL, using the browsers cookies.
     * @param url url to retrieve content from
     * @param resp response to store content in
     */
    protected void getUrlContent(String url, HttpResponse resp) {
        Set<Cookie> browserCookies = getSeleniumHelper().getCookies();
        BasicCookieStore cookieStore = new BasicCookieStore();
        for (Cookie browserCookie : browserCookies) {
            BasicClientCookie cookie = convertCookie(browserCookie);
            cookieStore.addCookie(cookie);
        }
        resp.setCookieStore(cookieStore);
        getEnvironment().doGet(url, resp);
    }

    private BasicClientCookie convertCookie(Cookie browserCookie) {
        BasicClientCookie cookie = new BasicClientCookie(browserCookie.getName(), browserCookie.getValue());
        cookie.setDomain(browserCookie.getDomain());
        cookie.setPath(browserCookie.getPath());
        cookie.setExpiryDate(browserCookie.getExpiry());
        return cookie;
    }

    protected Object waitForJavascriptCallback(String statement, Object... parameters) {
        try {
            return getSeleniumHelper().waitForJavascriptCallback(statement, parameters);
        } catch (TimeoutException e) {
            return handleTimeoutException(e);
        }
    }
}
