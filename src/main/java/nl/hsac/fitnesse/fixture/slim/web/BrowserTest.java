package nl.hsac.fitnesse.fixture.slim.web;

import fitnesse.slim.fixtureInteraction.FixtureInteraction;
import nl.hsac.fitnesse.fixture.slim.SlimFixture;
import nl.hsac.fitnesse.fixture.slim.SlimFixtureException;
import nl.hsac.fitnesse.fixture.slim.StopTestException;
import nl.hsac.fitnesse.fixture.slim.web.annotation.TimeoutPolicy;
import nl.hsac.fitnesse.fixture.slim.web.annotation.WaitUntil;
import nl.hsac.fitnesse.fixture.util.BinaryHttpResponse;
import nl.hsac.fitnesse.fixture.util.FileUtil;
import nl.hsac.fitnesse.fixture.util.HttpResponse;
import nl.hsac.fitnesse.fixture.util.ReflectionHelper;
import nl.hsac.fitnesse.fixture.util.selenium.PageSourceSaver;
import nl.hsac.fitnesse.fixture.util.selenium.SeleniumHelper;
import nl.hsac.fitnesse.slim.interaction.ExceptionHelper;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class BrowserTest extends SlimFixture {
    private SeleniumHelper seleniumHelper = getEnvironment().getSeleniumHelper();
    private ReflectionHelper reflectionHelper = getEnvironment().getReflectionHelper();
    private NgBrowserTest ngBrowserTest;
    private boolean implicitWaitForAngular = false;
    private boolean implicitFindInFrames = true;
    private int secondsBeforeTimeout;
    private int secondsBeforePageLoadTimeout;
    private int waitAfterScroll = 150;
    private String screenshotBase = new File(filesDir, "screenshots").getPath() + "/";
    private String screenshotHeight = "200";
    private String downloadBase = new File(filesDir, "downloads").getPath() + "/";
    private String pageSourceBase = new File(filesDir, "pagesources").getPath() + "/";

    @Override
    protected void beforeInvoke(Method method, Object[] arguments) {
        super.beforeInvoke(method, arguments);
        waitForAngularIfNeeded(method);
    }

    @Override
    protected Object invoke(final FixtureInteraction interaction, final Method method, final Object[] arguments)
            throws Throwable {
        Object result;
        WaitUntil waitUntil = reflectionHelper.getAnnotation(WaitUntil.class, method);
        if (waitUntil == null) {
            result = superInvoke(interaction, method, arguments);
        } else {
            result = invokedWrappedInWaitUntil(waitUntil, interaction, method, arguments);
        }
        return result;
    }

    protected Object invokedWrappedInWaitUntil(WaitUntil waitUntil, final FixtureInteraction interaction, final Method method, final Object[] arguments) {
        ExpectedCondition<Object> condition = new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                try {
                    return superInvoke(interaction, method, arguments);
                } catch (Throwable e) {
                    Throwable realEx = ExceptionHelper.stripReflectionException(e);
                    if (realEx instanceof RuntimeException) {
                        throw (RuntimeException) realEx;
                    } else if (realEx instanceof Error) {
                        throw (Error) realEx;
                    } else {
                        throw new RuntimeException(realEx);
                    }
                }
            }
        };
        if (implicitFindInFrames) {
            condition = getSeleniumHelper().conditionForAllFrames(condition);
        }
        Object result;
        switch (waitUntil.value()) {
            case STOP_TEST:
                result = waitUntilOrStop(condition);
                break;
            case RETURN_NULL:
                result = waitUntilOrNull(condition);
                break;
            case RETURN_FALSE:
                result = waitUntilOrNull(condition) != null;
                break;
            case THROW:
            default:
                result = waitUntil(condition);
                break;
        }
        return result;
    }

    protected Object superInvoke(FixtureInteraction interaction, Method method, Object[] arguments) throws Throwable {
        return super.invoke(interaction, method, arguments);
    }

    /**
     * Determines whether the current method might require waiting for angular given the currently open site,
     * and ensure it does if needed.
     * @param method
     */
    protected void waitForAngularIfNeeded(Method method) {
        if (isImplicitWaitForAngularEnabled()) {
            try {
                if (ngBrowserTest == null) {
                    ngBrowserTest = new NgBrowserTest();
                }
                if (ngBrowserTest.requiresWaitForAngular(method) && currentSiteUsesAngular()) {
                    try {
                        ngBrowserTest.waitForAngularRequestsToFinish();
                    } catch (Exception e) {
                        // if something goes wrong, just use normal behavior: continue to invoke()
                        System.err.print("Found Angular, but encountered an error while waiting for it to be ready. ");
                        e.printStackTrace();
                    }
                }
            } catch (UnhandledAlertException e) {
                System.err.println("Cannot determine whether Angular is present while alert is active.");
            } catch (Exception e) {
                // if something goes wrong, just use normal behavior: continue to invoke()
                System.err.print("Error while determining whether Angular is present. ");
                e.printStackTrace();
            }
        }
    }

    protected boolean currentSiteUsesAngular() {
        Object windowHasAngular = getSeleniumHelper().executeJavascript("return window.angular?1:0;");
        return Long.valueOf(1).equals(windowHasAngular);
    }

    @Override
    protected Throwable handleException(Method method, Object[] arguments, Throwable t) {
        Throwable result;
        if (t instanceof UnhandledAlertException) {
            UnhandledAlertException e = (UnhandledAlertException) t;
            String alertText = e.getAlertText();
            if (alertText == null) {
                alertText = alertText();
            }
            String msgBase = "Unhandled alert: alert must be confirmed or dismissed before test can continue. Alert text: " + alertText;
            String msg = getSlimFixtureExceptionMessage("alertException", msgBase, e);
            result = new StopTestException(false, msg, t);
        } else if (t instanceof SlimFixtureException) {
            result = super.handleException(method, arguments, t);
        } else {
            String msg = getSlimFixtureExceptionMessage("exception", null, t);
            result = new SlimFixtureException(false, msg, t);
        }
        return result;
    }

    public BrowserTest() {
        secondsBeforeTimeout(seleniumHelper.getDefaultTimeoutSeconds());
        ensureActiveTabIsNotClosed();
    }

    public BrowserTest(int secondsBeforeTimeout) {
        secondsBeforeTimeout(secondsBeforeTimeout);
        ensureActiveTabIsNotClosed();
    }

    public boolean open(String address) {
        final String url = getUrl(address);
        try {
            getNavigation().to(url);
        } catch (TimeoutException e) {
            handleTimeoutException(e);
        } finally {
            switchToDefaultContent();
        }
        waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver webDriver) {
                String readyState = getSeleniumHelper().executeJavascript("return document.readyState").toString();
                // IE 7 is reported to return "loaded"
                boolean done = "complete".equalsIgnoreCase(readyState) || "loaded".equalsIgnoreCase(readyState);
                if (!done) {
                    System.err.printf("Open of %s returned while document.readyState was %s", url, readyState);
                    System.err.println();
                }
                return done;
            }
        });
        return true;
    }

    public String location() {
        return driver().getCurrentUrl();
    }

    public boolean back() {
        getNavigation().back();
        switchToDefaultContent();

        // firefox sometimes prevents immediate back, if previous page was reached via POST
        waitMilliseconds(500);
        WebElement element = findElement(By.id("errorTryAgain"));
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
        switchToDefaultContent();
        return true;
    }

    public boolean refresh() {
        getNavigation().refresh();
        switchToDefaultContent();
        return true;
    }

    private WebDriver.Navigation getNavigation() {
        return getSeleniumHelper().navigate();
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public String alertText() {
        Alert alert = getAlert();
        String text = null;
        if (alert != null) {
            text = alert.getText();
        }
        return text;
    }

    @WaitUntil
    public boolean confirmAlert() {
        Alert alert = getAlert();
        boolean result = false;
        if (alert != null) {
            alert.accept();
            onAlertHandled(true);
            result = true;
        }
        return result;
    }

    @WaitUntil
    public boolean dismissAlert() {
        Alert alert = getAlert();
        boolean result = false;
        if (alert != null) {
            alert.dismiss();
            onAlertHandled(false);
            result = true;
        }
        return result;
    }

    /**
     * Called when an alert is either dismissed or accepted.
     * @param accepted true if the alert was accepted, false if dismissed.
     */
    protected void onAlertHandled(boolean accepted) {
        // if we were looking in nested frames, we could not go back to original frame
        // because of the alert. Ensure we do so now the alert is handled.
        getSeleniumHelper().resetFrameDepthOnAlertError();
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

    @WaitUntil
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

    @WaitUntil
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
            WebDriver driver = driver();
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

    /**
     * Activates main/top-level iframe (i.e. makes it the current frame).
     */
    public void switchToDefaultContent() {
        getSeleniumHelper().switchToDefaultContent();
        clearSearchContext();
    }

    /**
     * Activates specified child frame of current iframe.
     * @param technicalSelector selector to find iframe.
     * @return true if iframe was found.
     */
    public boolean switchToFrame(String technicalSelector) {
        boolean result = false;
        WebElement iframe = getElement(technicalSelector);
        if (iframe != null) {
            getSeleniumHelper().switchToFrame(iframe);
            result = true;
        }
        return result;
    }

    /**
     * Activates parent frame of current iframe.
     * Does nothing if when current frame is the main/top-level one.
     */
    public void switchToParentFrame() {
        getSeleniumHelper().switchToParentFrame();
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
    @WaitUntil
    public boolean enterAs(String value, String place) {
        return enterAsIn(value, place, null);
    }

    /**
     * Replaces content at place by value.
     * @param value value to set.
     * @param place element to set value on.
     * @param container element containing place.
     * @return true, if element was found.
     */
    @WaitUntil
    public boolean enterAsIn(String value, String place, String container) {
        return enter(value, place, container, true);
    }

    /**
     * Adds content to place.
     * @param value value to add.
     * @param place element to add value to.
     * @return true, if element was found.
     */
    @WaitUntil
    public boolean enterFor(String value, String place) {
        return enterForIn(value, place, null);
    }

    /**
     * Adds content to place.
     * @param value value to add.
     * @param place element to add value to.
     * @param container element containing place.
     * @return true, if element was found.
     */
    @WaitUntil
    public boolean enterForIn(String value, String place, String container) {
        return enter(value, place, container, false);
    }

    protected boolean enter(String value, String place, boolean shouldClear) {
        return enter(value, place, null, shouldClear);
    }

    protected boolean enter(String value, String place, String container, boolean shouldClear) {
        WebElement element = getElementToSendValue(place, container);
        boolean result = element != null && isInteractable(element);
        if (result) {
            if (shouldClear) {
                element.clear();
            }
            sendValue(element, value);
        }
        return result;
    }

    protected WebElement getElementToSendValue(String place) {
        return getElementToSendValue(place, null);
    }

    protected WebElement getElementToSendValue(String place, String container) {
        return getElement(place, container);
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
     * Simulates typing a text to the current active element.
     * @param text text to type.
     * @return true, if an element was active the text could be sent to.
     */
    public boolean type(String text) {
        String value = cleanupValue(text);
        return sendKeysToActiveElement(value);
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

    @WaitUntil
    public boolean selectAs(String value, String place) {
        return selectFor(value, place);
    }

    @WaitUntil
    public boolean selectFor(String value, String place) {
        return selectForIn(value, place, null);
    }

    @WaitUntil
    public boolean selectForIn(String value, String place, String container) {
        SearchContext searchContext = setSearchContextToContainer(container);
        try {
            // choose option for select, if possible
            boolean result = clickSelectOption(place, value);
            if (!result) {
                // try to click the first element with right value
                result = click(value);
            }
            return result;
        } finally {
            resetSearchContext(searchContext);
        }
    }

    @WaitUntil
    public boolean enterForHidden(final String value, final String idOrName) {
        return getSeleniumHelper().setHiddenInputValue(idOrName, value);
    }

    private boolean clickSelectOption(String selectPlace, String optionValue) {
        WebElement element = getElementToSelectFor(selectPlace);
        return clickSelectOption(element, optionValue);
    }

    protected WebElement getElementToSelectFor(String selectPlace) {
        return getElement(selectPlace);
    }

    protected boolean clickSelectOption(WebElement element, String optionValue) {
        boolean result = false;
        if (element != null) {
            if (isSelect(element)) {
                optionValue = cleanupValue(optionValue);
                By xpath = getSeleniumHelper().byXpath(".//option[normalized(text()) = '%s']", optionValue);
                WebElement option = getSeleniumHelper().findElement(element, false, xpath);
                if (option == null) {
                    xpath = getSeleniumHelper().byXpath(".//option[contains(normalized(text()), '%s')]", optionValue);
                    option = getSeleniumHelper().findElement(element, false, xpath);
                }
                if (option != null) {
                    result = clickElement(option);
                }
            }
        }
        return result;
    }

    @WaitUntil
    public boolean click(final String place) {
        return clickImp(place, null);
    }

    @WaitUntil(TimeoutPolicy.RETURN_FALSE)
    public boolean clickIfAvailable(String place) {
        return clickIfAvailableIn(place, null);
    }

    @WaitUntil(TimeoutPolicy.RETURN_FALSE)
    public boolean clickIfAvailableIn(String place, String container) {
        return clickImp(place, container);
    }

    @WaitUntil
    public boolean clickIn(String place, String container) {
        return clickImp(place, container);
    }

    protected boolean clickImp(String place, String container) {
        boolean result = false;
        place = cleanupValue(place);
        try {
            WebElement element = getElementToClick(place, container);
            result = clickElement(element);
        } catch (WebDriverException e) {
            // if other element hides the element (in Chrome) an exception is thrown
            String msg = e.getMessage();
            if (msg == null || !msg.contains("Other element would receive the click")) {
                throw e;
            }
        }
        return result;
    }

    @WaitUntil
    public boolean doubleClick(final String place) {
        WebElement element = getElementToClick(place);
        return doubleClick(element);
    }

    protected boolean doubleClick(WebElement element) {
        boolean result = false;
        if (element != null) {
            scrollIfNotOnScreen(element);
            if (isInteractable(element)) {
                Actions actions = getActions();
                actions.doubleClick(element).perform();
                result = true;
            }
        }
        return result;
    }

    protected Actions getActions() {
        WebDriver driver = driver();
        return new Actions(driver);
    }

    protected WebElement getElementToClick(String place) {
        return getElementToClick(place, null);
    }

    protected WebElement getElementToClick(String place, String container) {
        SearchContext currentSearchContext = setSearchContextToContainer(container);
        try {
            return getSeleniumHelper().getElementToClick(place);
        } finally {
            resetSearchContext(currentSearchContext);
        }
    }

    @WaitUntil
    public boolean setSearchContextTo(String container) {
        boolean result = false;
        WebElement containerElement = getContainerElement(container);
        if (containerElement != null) {
            getSeleniumHelper().setCurrentContext(containerElement);
            result = true;
        }
        return result;
    }

    protected SearchContext setSearchContextToContainer(String container) {
        SearchContext result = null;
        if (container != null) {
            SearchContext currentSearchContext = getSeleniumHelper().getCurrentContext();
            if (setSearchContextTo(container)) {
                result = currentSearchContext;
            }
        }
        return result;
    }

    public void clearSearchContext() {
        getSeleniumHelper().setCurrentContext(null);
    }

    protected void resetSearchContext(SearchContext currentSearchContext) {
        if (currentSearchContext != null) {
            getSeleniumHelper().setCurrentContext(currentSearchContext);
        }
    }

    protected WebElement getContainerElement(String container) {
        WebElement containerElement = null;
        By by = getSeleniumHelper().placeToBy(container);
        if (by != null) {
            containerElement = findElement(by);
        } else {
            containerElement = findByXPath(".//fieldset[.//legend/text()[normalized(.) = '%s']]", container);
            if (containerElement == null) {
                containerElement = getSeleniumHelper().getElementByAriaLabel(container, -1);
                if (containerElement == null) {
                    containerElement = findByXPath(".//fieldset[.//legend/text()[contains(normalized(.), '%s')]]", container);
                    if (containerElement == null) {
                        containerElement = getSeleniumHelper().getElementByPartialAriaLabel(container, -1);
                    }
                }
            }
        }
        return containerElement;
    }

    protected boolean clickElement(WebElement element) {
        boolean result = false;
        if (element != null) {
            scrollIfNotOnScreen(element);
            if (isInteractable(element)) {
                element.click();
                result = true;
            }
        }
        return result;
    }

    protected boolean isInteractable(WebElement element) {
        return getSeleniumHelper().isInteractable(element);
    }

    @WaitUntil(TimeoutPolicy.STOP_TEST)
    public boolean waitForPage(String pageName) {
        return pageTitle().equals(pageName);
    }

    public boolean waitForTagWithText(String tagName, String expectedText) {
        return waitForElementWithText(By.tagName(tagName), expectedText);
    }

    public boolean waitForClassWithText(String cssClassName, String expectedText) {
        return waitForElementWithText(By.className(cssClassName), expectedText);
    }

    protected boolean waitForElementWithText(final By by, String expectedText) {
        final String textToLookFor = cleanExpectedValue(expectedText);
        return waitUntilOrStop(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver webDriver) {
                boolean ok = false;

                List<WebElement> elements = webDriver.findElements(by);
                if (elements != null) {
                    for (WebElement element : elements) {
                        // we don't want stale elements to make single
                        // element false, but instead we stop processing
                        // current list and do a new findElements
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
        return cleanupValue(expectedText);
    }

    protected boolean hasText(WebElement element, String textToLookFor) {
        boolean ok;
        String actual = getElementText(element);
        if (textToLookFor == null) {
            ok = actual == null;
        } else {
            if (StringUtils.isEmpty(actual)) {
                String value = element.getAttribute("value");
                if (!StringUtils.isEmpty(value)) {
                    actual = value;
                }
            }
            if (actual != null) {
                actual = actual.trim();
            }
            ok = textToLookFor.equals(actual);
        }
        return ok;
    }

    @WaitUntil(TimeoutPolicy.STOP_TEST)
    public boolean waitForClass(String cssClassName) {
        boolean ok = false;

        WebElement element = findElement(By.className(cssClassName));
        if (element != null) {
            ok = true;
        }
        return ok;
    }

    @WaitUntil(TimeoutPolicy.STOP_TEST)
    public boolean waitForVisible(String place) {
        return waitForVisibleIn(place, null);
    }


    @WaitUntil(TimeoutPolicy.STOP_TEST)
    public boolean waitForVisibleIn(String place, String container) {
        Boolean result = Boolean.FALSE;
        WebElement element = getElementToCheckVisibility(place, container);
        if (element != null) {
            scrollIfNotOnScreen(element);
            result = element.isDisplayed();
        }
        return result;
    }

    /**
     * @deprecated use #waitForVisible(xpath=) instead
     */
    @Deprecated
    public boolean waitForXPathVisible(String xPath) {
        By by = By.xpath(xPath);
        return waitForVisible(by);
    }

    @Deprecated
    protected boolean waitForVisible(final By by) {
        return waitUntilOrStop(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver webDriver) {
                Boolean result = Boolean.FALSE;
                WebElement element = findElement(by);
                if (element != null) {
                    scrollIfNotOnScreen(element);
                    result = element.isDisplayed();
                }
                return result;
            }
        });
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public String valueOf(String place) {
        return valueFor(place);
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public String valueFor(String place) {
        return valueForIn(place, null);
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public String valueOfIn(String place, String container) {
        return valueForIn(place, container);
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public String valueForIn(String place, String container) {
        WebElement element = getElementToRetrieveValue(place, container);
        return valueFor(element);
    }

    protected WebElement getElementToRetrieveValue(String place, String container) {
        return getElement(place, container);
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
                String elementType = element.getAttribute("type");
                if ("checkbox".equals(elementType)
                        || "radio".equals(elementType)) {
                    result = String.valueOf(element.isSelected());
                } else if ("li".equalsIgnoreCase(element.getTagName())) {
                    result = getElementText(element);
                } else {
                    result = element.getAttribute("value");
                    if (result == null) {
                        result = getElementText(element);
                    }
                }
            }
        }
        return result;
    }

    private boolean isSelect(WebElement element) {
        return "select".equalsIgnoreCase(element.getTagName());
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public ArrayList<String> valuesOf(String place) {
        return valuesFor(place);
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public ArrayList<String> valuesOfIn(String place, String container) {
        return valuesForIn(place, container);
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public ArrayList<String> valuesFor(String place) {
        return valuesForIn(place, null);
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public ArrayList<String> valuesForIn(String place, String container) {
        ArrayList<String> values = null;
        WebElement element = getElementToRetrieveValue(place, container);
        if (element != null) {
            values = new ArrayList<String>();
            String tagName = element.getTagName();
            if ("ul".equalsIgnoreCase(tagName)
                    || "ol".equalsIgnoreCase(tagName)) {
                List<WebElement> items = element.findElements(By.tagName("li"));
                for (WebElement item : items) {
                    if (item.isDisplayed()) {
                        values.add(getElementText(item));
                    }
                }
            } else if (isSelect(element)) {
                Select s = new Select(element);
                List<WebElement> options = s.getAllSelectedOptions();
                for (WebElement item : options) {
                    values.add(getElementText(item));
                }
            } else {
                values.add(valueFor(element));
            }
        }
        return values;
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public Integer numberFor(String place) {
        return numberForIn(place, null);
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public Integer numberForIn(String place, String container) {
        SearchContext searchContext = setSearchContextToContainer(container);
        try {
            Integer number = null;
            WebElement element = findByXPath(".//ol/li/descendant-or-self::text()[normalized(.)='%s']/ancestor-or-self::li", place);
            if (element == null) {
                element = findByXPath(".//ol/li/descendant-or-self::text()[contains(normalized(.),'%s')]/ancestor-or-self::li", place);
            }
            if (element != null) {
                scrollIfNotOnScreen(element);
                number = getSeleniumHelper().getNumberFor(element);
            }
            return number;
        } finally {
            resetSearchContext(searchContext);
        }
    }

    public ArrayList<String> availableOptionsFor(String place) {
        ArrayList<String> result = null;
        WebElement element = getElementToSelectFor(place);
        if (element != null) {
            scrollIfNotOnScreen(element);
            result = getSeleniumHelper().getAvailableOptions(element);
        }
        return result;
    }

    @WaitUntil
    public boolean clear(String place) {
        return clearIn(place, null);
    }

    @WaitUntil
    public boolean clearIn(String place, String container) {
        boolean result = false;
        WebElement element = getElementToClear(place, container);
        if (element != null) {
            element.clear();
            result = true;
        }
        return result;
    }

    protected WebElement getElementToClear(String place, String container) {
        return getElementToSendValue(place, container);
    }

    @WaitUntil
    public boolean enterAsInRowWhereIs(String value, String requestedColumnName, String selectOnColumn, String selectOnValue) {
        boolean result = false;
        String columnXPath = getXPathForColumnInRowByValueInOtherColumn(selectOnColumn, selectOnValue);
        String requestedIndex = getXPathForColumnIndex(requestedColumnName);
        WebElement cell = findByXPath("%s[%s]", columnXPath, requestedIndex);
        if (cell != null) {
            WebElement element = getSeleniumHelper().getNestedElementForValue(cell);
            if (isSelect(element)) {
                result = clickSelectOption(element, value);
            } else {
                if (isInteractable(element)) {
                    result = true;
                    element.clear();
                    sendValue(element, value);
                }
            }
        }
        return result;
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public String valueOfColumnNumberInRowNumber(int columnIndex, int rowIndex) {
        return getValueByXPath("(.//tr[boolean(td)])[%s]/td[%s]", Integer.toString(rowIndex), Integer.toString(columnIndex));
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public String valueOfInRowNumber(String requestedColumnName, int rowIndex) {
        String columnXPath = String.format("(.//tr[boolean(td)])[%s]/td", rowIndex);
        return valueInRow(columnXPath, requestedColumnName);
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public String valueOfInRowWhereIs(String requestedColumnName, String selectOnColumn, String selectOnValue) {
        String columnXPath = getXPathForColumnInRowByValueInOtherColumn(selectOnColumn, selectOnValue);
        return valueInRow(columnXPath, requestedColumnName);
    }

    protected String valueInRow(String columnXPath, String requestedColumnName) {
        String requestedIndex = getXPathForColumnIndex(requestedColumnName);
        return getValueByXPath("%s[%s]", columnXPath, requestedIndex);
    }

    protected String getValueByXPath(String xpathPattern, String... params) {
        WebElement element = findByXPath(xpathPattern, params);
        if (element != null) {
            WebElement nested = getSeleniumHelper().getNestedElementForValue(element);
            if (isInteractable(nested)) {
                element = nested;
            }
        }
        return valueFor(element);
    }

    @WaitUntil(TimeoutPolicy.RETURN_FALSE)
    public boolean rowExistsWhereIs(String selectOnColumn, String selectOnValue) {
        String columnXPath = getXPathForColumnInRowByValueInOtherColumn(selectOnColumn, selectOnValue);
        return findByXPath(columnXPath) != null;
    }

    @WaitUntil
    public boolean clickInRowNumber(String place, int rowIndex) {
        String columnXPath = String.format("(.//tr[boolean(td)])[%s]/td", rowIndex);
        return clickInRow(columnXPath, place);
    }

    @WaitUntil
    public boolean clickInRowWhereIs(String place, String selectOnColumn, String selectOnValue) {
        String columnXPath = getXPathForColumnInRowByValueInOtherColumn(selectOnColumn, selectOnValue);
        return clickInRow(columnXPath, place);
    }

    protected boolean clickInRow(String columnXPath, String place) {
        // find an input to click in the row
        WebElement element = findByXPath("%s//*[(local-name()='input' and contains(@value, '%s'))" +
                                            " or contains(normalized(text()),'%s') or contains(@title, '%s')]",
                                            columnXPath, place,
                                            place, place);
        return clickElement(element);
    }

    /**
     * Downloads the target of a link in a grid's row.
     * @param place which link to download.
     * @param rowNumber (1-based) row number to retrieve link from.
     * @return downloaded file if any, null otherwise.
     */
    @WaitUntil
    public String downloadFromRowNumber(String place, int rowNumber) {
        String columnXPath = String.format("(.//tr[boolean(td)])[%s]/td", rowNumber);
        return downloadFromRow(columnXPath, place);
    }

    /**
     * Downloads the target of a link in a grid, finding the row based on one of the other columns' value.
     * @param place which link to download.
     * @param selectOnColumn column header of cell whose value must be selectOnValue.
     * @param selectOnValue value to be present in selectOnColumn to find correct row.
     * @return downloaded file if any, null otherwise.
     */
    @WaitUntil
    public String downloadFromRowWhereIs(String place, String selectOnColumn, String selectOnValue) {
        String columnXPath = getXPathForColumnInRowByValueInOtherColumn(selectOnColumn, selectOnValue);
        return downloadFromRow(columnXPath, place);
    }

    protected String downloadFromRow(String columnXPath, String place) {
        String result = null;
        // find an a to download from based on its text()
        WebElement element = findByXPath("%s//a[contains(normalized(text()),'%s')]", columnXPath, place);
        if (element == null) {
            // find an a to download based on its column header
            String requestedIndex = getXPathForColumnIndex(place);
            element = findByXPath("%s[%s]//a", columnXPath, requestedIndex);
            if (element == null) {
                // find an a to download in the row by its title (aka tooltip)
                element = findByXPath("%s//a[contains(@title, '%s')]", columnXPath, place);
            }
        }
        if (element != null) {
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
        return String.format(".//tr[td[%s]/descendant-or-self::text()[normalized(.)='%s']]/td", selectIndex, value);
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
        return String.format("count(ancestor::table[1]//tr/th/descendant-or-self::text()[normalized(.)='%s']/ancestor-or-self::th[1]/preceding-sibling::th)+1", columnName);
    }

    protected WebElement getElement(String place) {
        return getElement(place, null);
    }

    protected WebElement getElement(String place, String container) {
        SearchContext currentSearchContext = setSearchContextToContainer(container);
        try {
            return getSeleniumHelper().getElement(place);
        } finally {
            resetSearchContext(currentSearchContext);
        }
    }

    /**
     * @deprecated use #click(xpath=) instead.
     */
    @WaitUntil
    @Deprecated
    public boolean clickByXPath(String xPath) {
        WebElement element = findByXPath(xPath);
        return clickElement(element);
    }

    /**
     * @deprecated use #valueOf(xpath=) instead.
     */
    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    @Deprecated
    public String textByXPath(String xPath) {
        return getTextByXPath(xPath);
    }

    protected String getTextByXPath(String xpathPattern, String... params) {
        WebElement element = findByXPath(xpathPattern, params);
        return getElementText(element);
    }

    /**
     * @deprecated use #valueOf(css=.) instead.
     */
    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    @Deprecated
    public String textByClassName(String className) {
        return getTextByClassName(className);
    }

    protected String getTextByClassName(String className) {
        WebElement element = findByClassName(className);
        return getElementText(element);
    }

    protected WebElement findByClassName(String className) {
        By by = By.className(className);
        return findElement(by);
    }

    protected WebElement findByXPath(String xpathPattern, String... params) {
        return getSeleniumHelper().findByXPath(xpathPattern, params);
    }

    protected WebElement findByJavascript(String script, Object... parameters) {
        By by = getSeleniumHelper().byJavascript(script, parameters);
        return findElement(by);
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
        return driver().findElements(by);
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
    @WaitUntil
    public boolean scrollTo(String place) {
        return scrollToIn(place, null);
    }

    /**
     * Scrolls browser window so top of place becomes visible.
     * @param place element to scroll to.
     * @param container parent of place.
     */
    @WaitUntil
    public boolean scrollToIn(String place, String container) {
        boolean result = false;
        WebElement element = getElementToScrollTo(place, container);
        if (element != null) {
            scrollTo(element);
            result = true;
        }
        return result;
    }

    protected WebElement getElementToScrollTo(String place, String container) {
        return getElementToCheckVisibility(place, container);
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
     * Determines whether element is enabled (i.e. can be clicked).
     * @param place element to check.
     * @return true if element is enabled.
     */
    @WaitUntil(TimeoutPolicy.RETURN_FALSE)
    public boolean isEnabled(String place) {
        return isEnabledIn(place, null);
    }

    /**
     * Determines whether element is enabled (i.e. can be clicked).
     * @param place element to check.
     * @param container parent of place.
     * @return true if element is enabled.
     */
    @WaitUntil(TimeoutPolicy.RETURN_FALSE)
    public boolean isEnabledIn(String place, String container) {
        boolean result = false;
        WebElement element = getElementToCheckVisibility(place, container);
        if (element != null) {
            result = element.isEnabled();
        }
        return result;
    }

    /**
     * Determines whether element can be see in browser's window.
     * @param place element to check.
     * @return true if element is displayed and in viewport.
     */
    @WaitUntil(TimeoutPolicy.RETURN_FALSE)
    public boolean isVisible(String place) {
        return isVisibleIn(place, null);
    }

    /**
     * Determines whether element can be see in browser's window.
     * @param place element to check.
     * @param container parent of place.
     * @return true if element is displayed and in viewport.
     */
    @WaitUntil(TimeoutPolicy.RETURN_FALSE)
    public boolean isVisibleIn(String place, String container) {
        return isVisibleImpl(place, container, true);
    }

    /**
     * Determines whether element is somewhere in browser's window.
     * @param place element to check.
     * @return true if element is displayed.
     */
    @WaitUntil(TimeoutPolicy.RETURN_FALSE)
    public boolean isVisibleOnPage(String place) {
        return isVisibleOnPageIn(place, null);
    }

    /**
     * Determines whether element is somewhere in browser's window.
     * @param place element to check.
     * @param container parent of place.
     * @return true if element is displayed.
     */
    @WaitUntil(TimeoutPolicy.RETURN_FALSE)
    public boolean isVisibleOnPageIn(String place, String container) {
        return isVisibleImpl(place, container, false);
    }

    protected boolean isVisibleImpl(String place, String container, boolean checkOnScreen) {
        boolean result = false;
        WebElement element = getElementToCheckVisibility(place, container);
        if (element != null && element.isDisplayed()) {
            if (checkOnScreen) {
                result = isElementOnScreen(element);
            } else {
                result = true;
            }
        }
        return result;
    }

    protected WebElement getElementToCheckVisibility(String place) {
        return getElementToCheckVisibility(place, null);
    }

    protected WebElement getElementToCheckVisibility(String place, String container) {
        SearchContext currentSearchContext = setSearchContextToContainer(container);
        try {
            return getElementToClick(place);
        } finally {
            resetSearchContext(currentSearchContext);
        }
    }

    /**
     * Checks whether element is in browser's viewport.
     * @param element element to check
     * @return true if element is in browser's viewport.
     */
    protected boolean isElementOnScreen(WebElement element) {
        Boolean onScreen = getSeleniumHelper().isElementOnScreen(element);
        return onScreen == null || onScreen.booleanValue();
    }

    @WaitUntil
    public boolean hoverOver(String place) {
        return hoverOverIn(place, null);
    }

    @WaitUntil
    public boolean hoverOverIn(String place, String container) {
        WebElement element = getElementToClick(place, container);
        return hoverOver(element);
    }

    protected boolean hoverOver(WebElement element) {
        boolean result = false;
        if (element != null) {
            scrollIfNotOnScreen(element);
            if (element.isDisplayed()) {
                getSeleniumHelper().hoverOver(element);
                result = true;
            }
        }
        return result;
    }

    /**
     * @param timeout number of seconds before waitUntil() and waitForJavascriptCallback() throw TimeOutException.
     */
    public void secondsBeforeTimeout(int timeout) {
        secondsBeforeTimeout = timeout;
        secondsBeforePageLoadTimeout(timeout);
        int timeoutInMs = timeout * 1000;
        getSeleniumHelper().setScriptWait(timeoutInMs);
    }

    /**
     * @return number of seconds waitUntil() will wait at most.
     */
    public int secondsBeforeTimeout() {
        return secondsBeforeTimeout;
    }

    /**
     * @param timeout number of seconds before waiting for a new page to load will throw a TimeOutException.
     */
    public void secondsBeforePageLoadTimeout(int timeout) {
        secondsBeforePageLoadTimeout = timeout;
        int timeoutInMs = timeout * 1000;
        getSeleniumHelper().setPageLoadWait(timeoutInMs);
    }

    /**
     * @return number of seconds Selenium will wait at most for a request to load a page.
     */
    public int secondsBeforePageLoadTimeout() {
        return secondsBeforePageLoadTimeout;
    }

    /**
     * Clears HTML5's localStorage (for the domain of the current open page in the browser).
     */
    public void clearLocalStorage() {
        getSeleniumHelper().executeJavascript("localStorage.clear();");
    }

    /**
     * Deletes all cookies(for the domain of the current open page in the browser).
     */
    public void deleteAllCookies() {
        getSeleniumHelper().deleteAllCookies();
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
     * @return (escaped) HTML content of current page.
     */
    public String pageSource() {
        String result = null;
        String html = getSeleniumHelper().getHtml();
        if (html != null) {
            result = "<pre>" + StringEscapeUtils.escapeHtml4(html) + "</pre>";
        }
        return result;
    }

    /**
     * Saves current page's source to the wiki'f files section and returns a link to the
     * created file.
     * @return hyperlink to the file containing the page source.
     */
    public String savePageSource() {
        String fileName = getSeleniumHelper().getResourceNameFromLocation();
        return savePageSource(fileName, fileName + ".html");
    }

    protected String savePageSource(String fileName, String linkText) {
        PageSourceSaver saver = getSeleniumHelper().getPageSourceSaver(pageSourceBase);
        // make href to file
        String url = saver.savePageSource(fileName);
        return String.format("<a href=\"%s\">%s</a>", url, linkText);
    }

    /**
     * Takes screenshot from current page
     * @param basename filename (below screenshot base directory).
     * @return location of screenshot.
     */
    public String takeScreenshot(String basename) {
        try {
            String screenshotFile = createScreenshot(basename);
            if (screenshotFile == null) {
                throw new SlimFixtureException(false, "Unable to take screenshot: does the webdriver support it?");
            } else {
                screenshotFile = getScreenshotLink(screenshotFile);
            }
            return screenshotFile;
        } catch (UnhandledAlertException e) {
            // standard behavior will stop test, this breaks storyboard that will attempt to take screenshot
            // after triggering alert, but before the alert can be handled.
            // so we output a message but no exception. We rely on a next line to actually handle alert
            // (which may mean either really handle or stop test).
            return String.format(
                    "<div><strong>Unable to take screenshot</strong>, alert is active. Alert text:<br/>" +
                            "'<span>%s</span>'</div>",
                    StringEscapeUtils.escapeHtml4(alertText()));
        }
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
     * Waits until the condition evaluates to a value that is neither null nor
     * false. Because of this contract, the return type must not be Void.
     * @param <T> the return type of the method, which must not be Void
     * @param condition condition to evaluate to determine whether waiting can be stopped.
     * @throws SlimFixtureException if condition was not met before secondsBeforeTimeout.
     * @return result of condition.
     */
    protected <T> T waitUntil(ExpectedCondition<T> condition) {
        try {
            return waitUntilImpl(condition);
        } catch (TimeoutException e) {
            String message = getTimeoutMessage(e);
            return lastAttemptBeforeThrow(condition, new SlimFixtureException(false, message, e));
        }
    }

    /**
     * Waits until the condition evaluates to a value that is neither null nor
     * false. If that does not occur the whole test is stopped.
     * Because of this contract, the return type must not be Void.
     * @param <T> the return type of the method, which must not be Void
     * @param condition condition to evaluate to determine whether waiting can be stopped.
     * @throws TimeoutStopTestException if condition was not met before secondsBeforeTimeout.
     * @return result of condition.
     */
    protected <T> T waitUntilOrStop(ExpectedCondition<T> condition) {
        try {
            return waitUntilImpl(condition);
        } catch (TimeoutException e) {
            try {
                return handleTimeoutException(e);
            } catch (TimeoutStopTestException tste) {
                return lastAttemptBeforeThrow(condition, tste);
            }
        }
    }

    /**
     * Tries the condition one last time before throwing an exception.
     * This to prevent exception messages in the wiki that show no problem, which could happen if the browser's
     * window content has changed between last (failing) try at condition and generation of the exception.
     * @param <T> the return type of the method, which must not be Void
     * @param condition condition that caused exception.
     * @param e exception that will be thrown if condition does not return a result.
     * @return last attempt results, if not null.
     * @throws SlimFixtureException throws e if last attempt returns null.
     */
    protected <T> T lastAttemptBeforeThrow(ExpectedCondition<T> condition, SlimFixtureException e) {
        T lastAttemptResult = null;
        try {
            // last attempt to ensure condition has not been met
            // this to prevent messages that show no problem
            lastAttemptResult = condition.apply(getSeleniumHelper().driver());
        } catch (Throwable t) {
            // ignore
        }
        if (lastAttemptResult != null) {
            return lastAttemptResult;
        }
        throw e;
    }

    /**
     * Waits until the condition evaluates to a value that is neither null nor
     * false. If that does not occur null is returned.
     * Because of this contract, the return type must not be Void.
     * @param <T> the return type of the method, which must not be Void
     * @param condition condition to evaluate to determine whether waiting can be stopped.
     * @return result of condition.
     */
    protected <T> T waitUntilOrNull(ExpectedCondition<T> condition) {
        try {
            return waitUntilImpl(condition);
        } catch (TimeoutException e) {
            return null;
        }
    }

    protected <T> T waitUntilImpl(ExpectedCondition<T> condition) {
        return getSeleniumHelper().waitUntil(secondsBeforeTimeout(), condition);
    }

    protected <T> T handleTimeoutException(TimeoutException e) {
        String message = getTimeoutMessage(e);
        throw new TimeoutStopTestException(false, message, e);
    }

    private String getTimeoutMessage(TimeoutException e) {
        String messageBase = String.format("Timed-out waiting (after %ss)", secondsBeforeTimeout());
        return getSlimFixtureExceptionMessage("timeouts", "timeout", messageBase, e);
    }

    protected void handleRequiredElementNotFound(String toFind) {
        handleRequiredElementNotFound(toFind, null);
    }

    protected void handleRequiredElementNotFound(String toFind, Throwable t) {
        String messageBase = String.format("Unable to find: %s", toFind);
        String message = getSlimFixtureExceptionMessage("notFound", toFind, messageBase, t);
        throw new SlimFixtureException(false, message, t);
    }

    protected String getSlimFixtureExceptionMessage(String screenshotFolder, String screenshotFile, String messageBase, Throwable t) {
        String screenshotBaseName = String.format("%s/%s/%s", screenshotFolder, getClass().getSimpleName(), screenshotFile);
        return getSlimFixtureExceptionMessage(screenshotBaseName, messageBase, t);
    }

    protected String getSlimFixtureExceptionMessage(String screenshotBaseName, String messageBase, Throwable t) {
        // take a screenshot of what was on screen
        String screenShotFile = null;
        try {
            screenShotFile = createScreenshot(screenshotBaseName, t);
        } catch (UnhandledAlertException e) {
            // https://code.google.com/p/selenium/issues/detail?id=4412
            System.err.println("Unable to take screenshot while alert is present for exception: " + messageBase);
        } catch (Exception sse) {
            System.err.println("Unable to take screenshot for exception: " + messageBase);
            sse.printStackTrace();
        }
        String message = messageBase;
        if (message == null) {
            if (t == null) {
                message = "";
            } else {
                message = ExceptionUtils.getStackTrace(t);
            }
        }
        if (screenShotFile != null) {
            String label = "Page content";
            try {
                String fileName;
                if (t != null) {
                    fileName = t.getClass().getName();
                } else if (screenshotBaseName != null) {
                    fileName = screenshotBaseName;
                } else {
                    fileName = "exception";
                }
                label = savePageSource(fileName, label);
            } catch (UnhandledAlertException e) {
                // https://code.google.com/p/selenium/issues/detail?id=4412
                System.err.println("Unable to capture page source while alert is present for exception: " + messageBase);
            } catch (Exception e) {
                System.err.println("Unable to capture page source for exception: " + messageBase);
                e.printStackTrace();
            }

            String exceptionMsg = formatExceptionMsg(message);
            message = String.format("<div><div>%s.</div><div>%s:%s</div></div>",
                    exceptionMsg, label, getScreenshotLink(screenShotFile));
        }
        return message;
    }

    protected String formatExceptionMsg(String value) {
        return StringEscapeUtils.escapeHtml4(value);
    }

    private WebDriver driver() {
        return getSeleniumHelper().driver();
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
        return getWindowSize().getWidth();
    }

    public int currentBrowserHeight() {
        return getWindowSize().getHeight();
    }

    public void setBrowserWidth(int newWidth) {
        int currentHeight = currentBrowserHeight();
        setBrowserSizeToBy(newWidth, currentHeight);
    }

    public void setBrowserHeight(int newHeight) {
        int currentWidth = currentBrowserWidth();
        setBrowserSizeToBy(currentWidth, newHeight);
    }

    public void setBrowserSizeToBy(int newWidth, int newHeight) {
        getSeleniumHelper().setWindowSize(newWidth, newHeight);
        Dimension actualSize = getWindowSize();
        if (actualSize.getHeight() != newHeight || actualSize.getWidth() != newWidth) {
            String message = String.format("Unable to change size to: %s x %s; size is: %s x %s",
                                newWidth, newHeight, actualSize.getWidth(), actualSize.getHeight());
            throw new SlimFixtureException(false, message);
        }
    }

    protected Dimension getWindowSize() {
        return getSeleniumHelper().getWindowSize();
    }

    public void setBrowserSizeToMaximum() {
        getSeleniumHelper().setWindowSizeToMaximum();
    }

    /**
     * Downloads the target of the supplied link.
     * @param place link to follow.
     * @return downloaded file if any, null otherwise.
     */
    @WaitUntil
    public String download(String place) {
        return downloadIn(place, null);
    }

    /**
     * Downloads the target of the supplied link.
     * @param place link to follow.
     * @param container part of screen containing link.
     * @return downloaded file if any, null otherwise.
     */
    @WaitUntil
    public String downloadIn(String place, String container) {
        SearchContext currentSearchContext = setSearchContextToContainer(container);
        try {
            By selector = By.linkText(place);
            WebElement element = findElement(selector);
            if (element == null) {
                selector = By.partialLinkText(place);
                element = findElement(selector);
                if (element == null) {
                    selector = By.id(place);
                    element = findElement(selector);
                    if (element == null) {
                        selector = By.name(place);
                        element = findElement(selector);
                    }
                }
            }
            return downloadLinkTarget(element);
        } finally {
            resetSearchContext(currentSearchContext);
        }
    }

    protected WebElement findElement(By selector) {
        return getSeleniumHelper().findElement(selector);
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
     * Selects a file using the first file upload control.
     * @param fileName file to upload
     * @return true, if a file input was found and file existed.
     */
    @WaitUntil
    public boolean selectFile(String fileName) {
        return selectFileFor(fileName, "css=input[type='file']");
    }

    /**
     * Selects a file using a file upload control.
     * @param fileName file to upload
     * @param place file input to select the file for
     * @return true, if place was a file input and file existed.
     */
    @WaitUntil
    public boolean selectFileFor(String fileName, String place) {
        return selectFileForIn(fileName, place, null);
    }

    /**
     * Selects a file using a file upload control.
     * @param fileName file to upload
     * @param place file input to select the file for
     * @param container part of screen containing place
     * @return true, if place was a file input and file existed.
     */
    @WaitUntil
    public boolean selectFileForIn(String fileName, String place, String container) {
        boolean result = false;
        if (fileName != null) {
            String fullPath = getFilePathFromWikiUrl(fileName);
            if (new File(fullPath).exists()) {
                WebElement element = getElementToSelectFile(place, container);
                if (element != null) {
                    element.sendKeys(fullPath);
                    result = true;
                }
            } else {
                throw new SlimFixtureException(false, "Unable to find file: " + fullPath);
            }
        }
        return result;
    }

    protected WebElement getElementToSelectFile(String place, String container) {
        WebElement result = null;
        WebElement element = getElement(place, container);
        if (element != null
                && "input".equalsIgnoreCase(element.getTagName())
                && "file".equalsIgnoreCase(element.getAttribute("type"))) {
            result = element;
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
        getEnvironment().addSeleniumCookies(resp);
        getEnvironment().doGet(url, resp);
    }

    /**
     * Gets the value of the cookie with the supplied name.
     * @param cookieName name of cookie to get value from.
     * @return cookie's value if any.
     */
    public String cookieValue(String cookieName) {
        String result = null;
        Cookie cookie = getSeleniumHelper().getCookie(cookieName);
        if (cookie != null) {
            result = cookie.getValue();
        }
        return result;
    }

    protected Object waitForJavascriptCallback(String statement, Object... parameters) {
        try {
            return getSeleniumHelper().waitForJavascriptCallback(statement, parameters);
        } catch (TimeoutException e) {
            return handleTimeoutException(e);
        }
    }

    public NgBrowserTest getNgBrowserTest() {
        return ngBrowserTest;
    }

    public void setNgBrowserTest(NgBrowserTest ngBrowserTest) {
        this.ngBrowserTest = ngBrowserTest;
    }

    public boolean isImplicitWaitForAngularEnabled() {
        return implicitWaitForAngular;
    }

    public void setImplicitWaitForAngularTo(boolean implicitWaitForAngular) {
        this.implicitWaitForAngular = implicitWaitForAngular;
    }

    public void setImplicitFindInFramesTo(boolean implicitFindInFrames) {
        this.implicitFindInFrames = implicitFindInFrames;
    }

}
