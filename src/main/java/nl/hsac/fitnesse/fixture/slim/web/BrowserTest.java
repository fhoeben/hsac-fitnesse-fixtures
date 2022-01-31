package nl.hsac.fitnesse.fixture.slim.web;

import fitnesse.slim.fixtureInteraction.FixtureInteraction;
import nl.hsac.fitnesse.fixture.slim.HttpTest;
import nl.hsac.fitnesse.fixture.slim.SlimFixture;
import nl.hsac.fitnesse.fixture.slim.SlimFixtureException;
import nl.hsac.fitnesse.fixture.slim.StopTestException;
import nl.hsac.fitnesse.fixture.slim.web.annotation.TimeoutPolicy;
import nl.hsac.fitnesse.fixture.slim.web.annotation.WaitUntil;
import nl.hsac.fitnesse.fixture.util.ReflectionHelper;
import nl.hsac.fitnesse.fixture.util.selenium.AllFramesDecorator;
import nl.hsac.fitnesse.fixture.util.selenium.PageSourceSaver;
import nl.hsac.fitnesse.fixture.util.selenium.SelectHelper;
import nl.hsac.fitnesse.fixture.util.selenium.SeleniumHelper;
import nl.hsac.fitnesse.fixture.util.selenium.StaleContextException;
import nl.hsac.fitnesse.fixture.util.selenium.by.AltBy;
import nl.hsac.fitnesse.fixture.util.selenium.by.AriaGridBy;
import nl.hsac.fitnesse.fixture.util.selenium.by.ContainerBy;
import nl.hsac.fitnesse.fixture.util.selenium.by.GridBy;
import nl.hsac.fitnesse.fixture.util.selenium.by.ListItemBy;
import nl.hsac.fitnesse.fixture.util.selenium.by.OptionBy;
import nl.hsac.fitnesse.fixture.util.selenium.by.XPathBy;
import nl.hsac.fitnesse.slim.interaction.ExceptionHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class BrowserTest<T extends WebElement> extends SlimFixture {
    private final List<String> currentSearchContextPath = new ArrayList<>();

    private SeleniumHelper<T> seleniumHelper = getEnvironment().getSeleniumHelper();
    private ReflectionHelper reflectionHelper = getEnvironment().getReflectionHelper();
    private NgBrowserTest ngBrowserTest;
    private boolean implicitWaitForAngular = false;
    private boolean implicitFindInFrames = true;
    private boolean continueIfReadyStateInteractive = false;
    private boolean scrollElementToCenter = false;
    private boolean waiAriaTables = false;
    private int secondsBeforeTimeout;
    private int secondsBeforePageLoadTimeout;
    private int waitAfterScroll = 150;
    private String screenshotBase = new File(filesDir, "screenshots").getPath() + "/";
    private String screenshotHeight = "200";
    private String pageSourceBase = new File(filesDir, "pagesources").getPath() + "/";
    private boolean sendCommandForControlOnMac = false;
    private boolean trimOnNormalize = true;
    private Long dragPressDelay;
    private Integer dragDistance;
    private static final String CHROME_HIDDEN_BY_OTHER_ELEMENT_ERROR = "Other element would receive the click",
            EDGE_HIDDEN_BY_OTHER_ELEMENT_ERROR = "Element is obscured";
    private static final Pattern FIREFOX_HIDDEN_BY_OTHER_ELEMENT_ERROR_PATTERN =
            Pattern.compile("Element.+is not clickable.+because another element.+obscures it");

    protected List<String> getCurrentSearchContextPath() {
        return currentSearchContextPath;
    }
    protected int minStaleContextRefreshCount = 5;

    @Override
    protected void beforeInvoke(Method method, Object[] arguments) {
        super.beforeInvoke(method, arguments);
        waitForAngularIfNeeded(method);
    }

    @Override
    protected Object invoke(FixtureInteraction interaction, Method method, Object[] arguments)
            throws Throwable {
        try {
            Object result;
            WaitUntil waitUntil = reflectionHelper.getAnnotation(WaitUntil.class, method);
            if (waitUntil == null) {
                result = superInvoke(interaction, method, arguments);
            } else {
                result = invokedWrappedInWaitUntil(waitUntil, interaction, method, arguments);
            }
            return result;
        } catch (StaleContextException e) {
            // current context was no good to search in
            if (getCurrentSearchContextPath().isEmpty()) {
                throw e;
            } else {
                refreshSearchContext();
                return invoke(interaction, method, arguments);
            }
        }
    }

    protected Object invokedWrappedInWaitUntil(WaitUntil waitUntil, FixtureInteraction interaction, Method method, Object[] arguments) {
        ExpectedCondition<Object> condition = webDriver -> {
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
        };
        condition = wrapConditionForFramesIfNeeded(condition);

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
                    ngBrowserTest = new NgBrowserTest(secondsBeforeTimeout());
                    ngBrowserTest.secondsBeforePageLoadTimeout(secondsBeforePageLoadTimeout());
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
        } else if (t instanceof WebDriverException
                && getSeleniumHelper().exceptionIndicatesConnectionLost((WebDriverException) t)) {
            Throwable msgT = t.getCause() != null ? t.getCause() : t;
            String msg = "Problem communicating with webdriver: " + msgT;
            result = new StopTestException(false, msg, t);
        } else {
            String msg = getSlimFixtureExceptionMessage("exception", null, t);
            result = new SlimFixtureException(false, msg, t);
        }
        return result;
    }

    public BrowserTest() {
        secondsBeforeTimeout(getEnvironment().getSeleniumDriverManager().getDefaultTimeoutSeconds());
        if (!ensureActiveTabIsNotClosed()) {
            confirmAlertIfAvailable();
        }
    }

    public BrowserTest(int secondsBeforeTimeout) {
        this(secondsBeforeTimeout, true);
    }

    public BrowserTest(int secondsBeforeTimeout, boolean confirmAlertIfAvailable) {
        secondsBeforeTimeout(secondsBeforeTimeout);
        if (!ensureActiveTabIsNotClosed() && confirmAlertIfAvailable) {
            confirmAlertIfAvailable();
        }
    }

    public boolean open(String address) {
        String url = getUrl(address);
        try {
            getNavigation().to(url);
        } catch (TimeoutException e) {
            handleTimeoutException(e);
        } finally {
            switchToDefaultContent();
        }
        waitUntil(webDriver -> {
            String readyState = getSeleniumHelper().executeJavascript("return document.readyState").toString();
            // IE 7 is reported to return "loaded"
            boolean done = "complete".equalsIgnoreCase(readyState) || "loaded".equalsIgnoreCase(readyState);
            if (!done) {
                System.err.printf("Open of %s returned while document.readyState was %s", url, readyState);
                System.err.println();
                if (isContinueIfReadyStateInteractive() && "interactive".equals(readyState)) {
                    done = true;
                }
            }
            return done;
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

    @WaitUntil(TimeoutPolicy.RETURN_FALSE)
    public boolean confirmAlertIfAvailable() {
        return confirmAlert();
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

    @WaitUntil(TimeoutPolicy.RETURN_FALSE)
    public boolean dismissAlertIfAvailable() {
        return dismissAlert();
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
        int tabCount = tabCount();
        getSeleniumHelper().executeJavascript("window.open('%s', '_blank')", cleanUrl);
        // ensure new window is open
        waitUntil(webDriver -> tabCount() > tabCount);
        return switchToNextTab();
    }

    @WaitUntil
    public boolean switchToNextTab() {
        boolean result = false;
        List<String> tabs = getTabHandles();
        int currentTab = getCurrentTabIndex(tabs);
        if (tabs.size() > 1 || currentTab < 0) {
            int nextTab = currentTab + 1;
            if (nextTab == tabs.size() || nextTab < 0) {
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
        int currentTab = getCurrentTabIndex(tabs);
        if (tabs.size() > 1 || currentTab < 0) {
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
        T iframe = getElement(technicalSelector);
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
        return enter(element, value, shouldClear);
    }

    protected boolean enter(WebElement element, String value, boolean shouldClear) {
        boolean result = element != null && isInteractable(element);
        if (result) {
            if (isSelect(element)) {
                result = clickSelectOption(element, value);
            } else {
                if (shouldClear) {
                    result = clear(element);
                }
                if (result) {
                    sendValue(element, value);
                }
            }
        }
        return result;
    }

    @WaitUntil
    public boolean enterDateAs(String date, String place) {
        WebElement element = getElementToSendValue(place);
        boolean result = element != null && isInteractable(element);
        if (result) {
            getSeleniumHelper().fillDateInput(element, date);
        }
        return result;
    }

    protected T getElementToSendValue(String place) {
        return getElementToSendValue(place, null);
    }

    protected T getElementToSendValue(String place, String container) {
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
            if (Keys.CONTROL.equals(s) && sendCommandForControlOnMac) {
                s = getSeleniumHelper().getControlOrCommand();
            }
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
    protected boolean sendKeysToActiveElement(CharSequence... keys) {
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
        WebElement element = getElementToSelectFor(place);
        if (element != null) {
            Select select = new Select(element);
            if (select.isMultiple()) {
                select.deselectAll();
            }
        }
        return clickSelectOption(element, value);
    }

    @WaitUntil
    public boolean selectAsIn(String value, String place, String container) {
        return Boolean.TRUE.equals(doInContainer(container, () -> selectAs(value, place)));
    }

    @WaitUntil
    public boolean selectFor(String value, String place) {
        WebElement element = getElementToSelectFor(place);
        return clickSelectOption(element, value);
    }

    @WaitUntil
    public boolean selectForIn(String value, String place, String container) {
        return Boolean.TRUE.equals(doInContainer(container, () -> selectFor(value, place)));
    }

    @WaitUntil
    public boolean enterForHidden(String value, String idOrName) {
        return getSeleniumHelper().setHiddenInputValue(idOrName, value);
    }

    protected T getElementToSelectFor(String selectPlace) {
        return getElement(selectPlace);
    }

    protected boolean clickSelectOption(WebElement element, String optionValue) {
        boolean result = false;
        if (element != null) {
            if (isSelect(element)) {
                optionValue = cleanupValue(optionValue);
                By optionBy = new OptionBy(optionValue);
                WebElement option = optionBy.findElement(element);
                result = clickSelectOption(element, option);
            }
        }
        return result;
    }

    protected boolean clickSelectOption(WebElement element, WebElement option) {
        boolean result = false;
        if (option != null) {
            // we scroll containing select into view (not the option)
            // based on behavior for option in https://www.w3.org/TR/webdriver/#element-click
            scrollIfNotOnScreen(element);
            if (isInteractable(option)) {
                option.click();
                result = true;
            }
        }
        return result;
    }

    @WaitUntil
    public boolean click(String place) {
        return clickImp(place, null);
    }

    public void clickAtOffsetXY(String place, Integer xOffset, Integer yOffset) {
        place = cleanupValue(place);
        try {
            WebElement element = getElementToClick(place);
            getSeleniumHelper().clickAtOffsetXY(element, xOffset, yOffset);
        } catch (WebDriverException e) {
            if (!this.clickExceptionIsAboutHiddenByOtherElement(e)) {
                throw e;
            }
        }
    }

    public void doubleClickAtOffsetXY(String place, Integer xOffset, Integer yOffset) {
        place = cleanupValue(place);
        try {
            WebElement element = getElementToClick(place);
            getSeleniumHelper().doubleClickAtOffsetXY(element, xOffset, yOffset);
        } catch (WebDriverException e) {
            if (!this.clickExceptionIsAboutHiddenByOtherElement(e)) {
                throw e;
            }
        }
    }

    public void rightClickAtOffsetXY(String place, Integer xOffset, Integer yOffset) {
        place = cleanupValue(place);
        try {
            WebElement element = getElementToClick(place);
            getSeleniumHelper().rightClickAtOffsetXY(element, xOffset, yOffset);
        } catch (WebDriverException e) {
            if (!this.clickExceptionIsAboutHiddenByOtherElement(e)) {
                throw e;
            }
        }
    }

    public void dragAndDropToOffsetXY(String place, Integer xOffset, Integer yOffset) {
        place = cleanupValue(place);
        try {
            WebElement element = getElementToClick(place);
         if (dragPressDelay == null && dragDistance == null) {
             getSeleniumHelper().dragAndDropToOffsetXY(element, xOffset, yOffset);
        } else if (dragDistance != null) {
             getSeleniumHelper().dragWithDistanceAndDropToOffsetXY(element, dragDistance, xOffset, yOffset);
        } else {
             getSeleniumHelper().dragWithDelayAndDropToOffsetXY(element, dragPressDelay, xOffset, yOffset);
         }
        } catch (WebDriverException e) {
            if (!this.clickExceptionIsAboutHiddenByOtherElement(e)) {
                throw e;
            }
        }
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
            // if other element hides the element, hold back the exception so WaitUntil is not interrupted
            if (!clickExceptionIsAboutHiddenByOtherElement(e)) {
                throw e;
            }
        }
        return result;
    }

    protected boolean clickExceptionIsAboutHiddenByOtherElement(Exception e) {
        String msg = e.getMessage();
        return msg != null
                && (msg.contains(CHROME_HIDDEN_BY_OTHER_ELEMENT_ERROR)
                            || msg.contains(EDGE_HIDDEN_BY_OTHER_ELEMENT_ERROR)
                            || FIREFOX_HIDDEN_BY_OTHER_ELEMENT_ERROR_PATTERN.matcher(msg).find()
                            // IE does not throw an exception, so no need to detect any
                            // Safari does throw an exception, but not one specific to this event. Too bad :/
                            // PhantomJS just clicks the element whether it's hidden or not, so no exception either
                    );
    }

    @WaitUntil
    public boolean doubleClick(String place) {
        return doubleClickIn(place, null);
    }

    @WaitUntil
    public boolean doubleClickIn(String place, String container) {
        place = cleanupValue(place);
        WebElement element = getElementToClick(place, container);
        return doubleClick(element);
    }

    protected boolean doubleClick(WebElement element) {
        return doIfInteractable(element, () -> getSeleniumHelper().doubleClick(element));
    }

    @WaitUntil
    public boolean rightClick(String place) {
        return rightClickIn(place, null);
    }

    @WaitUntil
    public boolean rightClickIn(String place, String container) {
        place = cleanupValue(place);
        WebElement element = getElementToClick(place, container);
        return rightClick(element);
    }

    protected boolean rightClick(WebElement element) {
        return doIfInteractable(element, () -> getSeleniumHelper().rightClick(element));
    }

    @WaitUntil
    public boolean shiftClick(String place) {
        return shiftClickIn(place, null);
    }

    @WaitUntil
    public boolean shiftClickIn(String place, String container) {
        place = cleanupValue(place);
        WebElement element = getElementToClick(place, container);
        return shiftClick(element);
    }

    protected boolean shiftClick(WebElement element) {
        return doIfInteractable(element, () -> getSeleniumHelper().clickWithKeyDown(element, Keys.SHIFT));
    }

    @WaitUntil
    public boolean controlClick(String place) {
        return controlClickIn(place, null);
    }

    @WaitUntil
    public boolean controlClickIn(String place, String container) {
        place = cleanupValue(place);
        WebElement element = getElementToClick(place, container);
        return controlClick(element);
    }

    protected boolean controlClick(WebElement element) {
        return doIfInteractable(element, () -> getSeleniumHelper().clickWithKeyDown(element, controlKey()));
    }

    public void setSendCommandForControlOnMacTo(boolean sendCommand) {
        sendCommandForControlOnMac = sendCommand;
    }

    public boolean sendCommandForControlOnMac() {
        return sendCommandForControlOnMac;
    }

    protected Keys controlKey() {
        return sendCommandForControlOnMac ? getSeleniumHelper().getControlOrCommand() : Keys.CONTROL;
    }
    protected Long getDragPressDelay() {
        return dragPressDelay;
    }
    protected Integer getDragDistance() {
        return dragDistance;
    }

    public void setDragDistance(int dragDistance) {
        this.dragDistance = dragDistance;
    }

    public void setDragPressDelay(long dragPressDelay) {
        this.dragPressDelay = dragPressDelay;
    }

    public void clearDragSetup() {
        this.dragPressDelay = null;
        this.dragDistance = null;
    }

    @WaitUntil
    public boolean dragAndDropTo(String source, String destination) {
        return this.dragAndDropImpl(source, destination, false);
    }

    @WaitUntil
    public boolean html5DragAndDropTo(String source, String destination) {
        return dragAndDropImpl(source, destination, true);
    }

    protected boolean dragAndDropImpl(String source, String destination, boolean html5) {
        boolean result = false;
        Long dragPressDelay = getDragPressDelay();
        Integer dragDistance = getDragDistance();
        source = cleanupValue(source);
        WebElement sourceElement = getElementToClick(source);
        destination = cleanupValue(destination);
        WebElement destinationElement = getElementToClick(destination);

        if ((sourceElement != null) && (destinationElement != null)) {
            scrollIfNotOnScreen(sourceElement);
            if (isInteractable(sourceElement) && destinationElement.isDisplayed()) {
                if (html5 || sourceElement.getAttribute("draggable").equalsIgnoreCase("true")) {
                    try {
                        getSeleniumHelper().html5DragAndDrop(sourceElement, destinationElement);
                    } catch (IOException e) {
                        throw new SlimFixtureException(false, "The drag and drop simulator javascript could not be found.", e);
                    }
                } else if (dragPressDelay == null && dragDistance == null) {
                    getSeleniumHelper().dragAndDrop(sourceElement, destinationElement);
                } else if (dragDistance != null) {
                    getSeleniumHelper().dragWithDistanceAndDrop(sourceElement, dragDistance, destinationElement);
                } else {
                    getSeleniumHelper().dragWithDelayAndDrop(sourceElement, dragPressDelay, destinationElement);
                }
                result = true;
            }
        }
        return result;
    }

    protected T getElementToClick(String place) {
        return getSeleniumHelper().getElementToClick(place);
    }

    protected T getElementToClick(String place, String container) {
        return doInContainer(container, () -> getElementToClick(place));
    }

    /**
     * Convenience method to create custom heuristics in subclasses.
     * @param container container to use (use <code>null</code> for current container), can be a technical selector.
     * @param place place to look for inside container, can be a technical selector.
     * @param suppliers suppliers that will be used in turn until an element is found, IF place is not a technical selector.
     * @return first hit of place, technical selector or result of first supplier that provided result.
     */
    protected T findFirstInContainer(String container, String place, Supplier<? extends T>... suppliers) {
        return doInContainer(container, () -> getSeleniumHelper().findByTechnicalSelectorOr(place, suppliers));
    }

    protected <R> R doInContainer(String container, Supplier<R> action) {
        R result = null;
        if (container == null) {
            result = action.get();
        } else {
            String cleanContainer = cleanupValue(container);
            result = doInContainer(() -> getContainerElement(cleanContainer), action);
        }
        return result;
    }

    protected <R> R doInContainer(Supplier<T> containerSupplier, Supplier<R> action) {
        R result = null;
        int retryCount = minStaleContextRefreshCount;
        do {
            try {
                T containerElement = containerSupplier.get();
                if (containerElement != null) {
                    result = doInContainer(containerElement, action);
                }
                retryCount = 0;
            } catch (StaleContextException e) {
                // containerElement went stale
                retryCount--;
                if (retryCount < 1) {
                    throw e;
                }
            }
        } while (retryCount > 0);
        return result;
    }

    protected <R> R doInContainer(T container, Supplier<R> action) {
        return getSeleniumHelper().doInContext(container, action);
    }

    @WaitUntil
    public boolean setSearchContextTo(String container) {
        container = cleanupValue(container);
        WebElement containerElement = getContainerElement(container);
        boolean result = false;
        if (containerElement != null) {
            getCurrentSearchContextPath().add(container);
            setSearchContextTo(containerElement);
            result = true;
        }
        return result;
    }

    protected void setSearchContextTo(SearchContext containerElement) {
        getSeleniumHelper().setCurrentContext(containerElement);
    }

    public void clearSearchContext() {
        getCurrentSearchContextPath().clear();
        getSeleniumHelper().setCurrentContext(null);
    }

    protected T getContainerElement(String container) {
        return findByTechnicalSelectorOr(container, this::getContainerImpl);
    }

    protected T getContainerImpl(String container) {
        return findElement(ContainerBy.heuristic(container));
    }

    protected boolean clickElement(WebElement element) {
        return doIfInteractable(element, () -> element.click());
    }

    protected boolean doIfInteractable(WebElement element, Runnable action) {
        boolean result = false;
        if (element != null) {
            scrollIfNotOnScreen(element);
            if (isInteractable(element)) {
                action.run();
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

    protected boolean waitForElementWithText(By by, String expectedText) {
        String textToLookFor = cleanExpectedValue(expectedText);
        return waitUntilOrStop(webDriver -> {
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
    protected boolean waitForVisible(By by) {
        return waitUntilOrStop(webDriver -> {
            Boolean result = Boolean.FALSE;
            WebElement element = findElement(by);
            if (element != null) {
                scrollIfNotOnScreen(element);
                result = element.isDisplayed();
            }
            return result;
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

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public String normalizedValueOf(String place) {
        return normalizedValueFor(place);
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public String normalizedValueFor(String place) {
        return normalizedValueForIn(place, null);
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public String normalizedValueOfIn(String place, String container) {
        return normalizedValueForIn(place, container);
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public String normalizedValueForIn(String place, String container) {
        String value = valueForIn(place, container);
        return normalizeValue(value);
    }

    protected ArrayList<String> normalizeValues(ArrayList<String> values) {
        if (values != null) {
            for (int i = 0; i < values.size(); i++) {
                String value = values.get(i);
                String normalized = normalizeValue(value);
                values.set(i, normalized);
            }
        }
        return values;
    }

    protected String normalizeValue(String value) {
        String text = XPathBy.getNormalizedText(value);
        if (text != null && trimOnNormalize) {
            text = text.trim();
        }
        return text;
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public String tooltipFor(String place) {
        return tooltipForIn(place, null);
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public String tooltipForIn(String place, String container) {
        return valueOfAttributeOnIn("title", place, container);
    }

    @WaitUntil
    public String targetOfLink(String place) {
        WebElement linkElement = getSeleniumHelper().getLink(place);
        return getLinkTarget(linkElement);
    }

    protected String getLinkTarget(WebElement linkElement) {
        String target = null;
        if (linkElement != null) {
            target = linkElement.getAttribute("href");
            if (target == null) {
                target = linkElement.getAttribute("src");
            }
        }
        return target;
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public String valueOfAttributeOn(String attribute, String place) {
        return valueOfAttributeOnIn(attribute, place, null);
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public String valueOfAttributeOnIn(String attribute, String place, String container) {
        String result = null;
        WebElement element = getElementToRetrieveValue(place, container);
        if (element != null) {
            result = element.getAttribute(attribute);
        }
        return result;
    }

    protected T getElementToRetrieveValue(String place, String container) {
        return getElement(place, container);
    }

    protected String valueFor(By by) {
        WebElement element = getSeleniumHelper().findElement(by);
        return valueFor(element);
    }

    protected String valueFor(WebElement element) {
        String result = null;
        if (element != null) {
            if (isSelect(element)) {
                WebElement selected = getFirstSelectedOption(element);
                result = getElementText(selected);
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

    protected WebElement getFirstSelectedOption(WebElement selectElement) {
        SelectHelper s = new SelectHelper(selectElement);
        return s.getFirstSelectedOption();
    }

    protected List<WebElement> getSelectedOptions(WebElement selectElement) {
        SelectHelper s = new SelectHelper(selectElement);
        return s.getAllSelectedOptions();
    }

    protected boolean isSelect(WebElement element) {
        return SelectHelper.isSelect(element);
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
                List<WebElement> options = getSelectedOptions(element);
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
    public ArrayList<String> normalizedValuesOf(String place) {
        return normalizedValuesFor(place);
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public ArrayList<String> normalizedValuesOfIn(String place, String container) {
        return normalizedValuesForIn(place, container);
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public ArrayList<String> normalizedValuesFor(String place) {
        return normalizedValuesForIn(place, null);
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public ArrayList<String> normalizedValuesForIn(String place, String container) {
        ArrayList<String> values = valuesForIn(place, container);
        return normalizeValues(values);
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public Integer numberFor(String place) {
        Integer number = null;
        WebElement element = findElement(ListItemBy.numbered(place));
        if (element != null) {
            scrollIfNotOnScreen(element);
            number = getSeleniumHelper().getNumberFor(element);
        }
        return number;
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public Integer numberForIn(String place, String container) {
        return doInContainer(container, () -> numberFor(place));
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public ArrayList<String> availableOptionsFor(String place) {
        ArrayList<String> result = null;
        WebElement element = getElementToSelectFor(place);
        if (element != null) {
            scrollIfNotOnScreen(element);
            result = getSeleniumHelper().getAvailableOptions(element);
        }
        return result;
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public ArrayList<String> normalizedAvailableOptionsFor(String place) {
        return normalizeValues(availableOptionsFor(place));
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
            result = clear(element);
        }
        return result;
    }

    protected boolean clear(WebElement element) {
        boolean result = false;
        String tagName = element.getTagName();
        if ("input".equalsIgnoreCase(tagName) || "textarea".equalsIgnoreCase(tagName)) {
            element.clear();
            result = true;
        }
        return result;
    }

    protected T getElementToClear(String place, String container) {
        return getElementToSendValue(place, container);
    }

    @WaitUntil
    public boolean enterAsInRowWhereIs(String value, String requestedColumnName, String selectOnColumn, String selectOnValue) {

        By cellBy = waiAriaTables ?
                AriaGridBy.columnInRowWhereIs(requestedColumnName, selectOnColumn, selectOnValue) :
                GridBy.columnInRowWhereIs(requestedColumnName, selectOnColumn, selectOnValue);

        WebElement element = findElement(cellBy);
        return enter(element, value, true);
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public String valueOfColumnNumberInRowNumber(int columnIndex, int rowIndex) {
        By by = waiAriaTables ?
                AriaGridBy.coordinates(columnIndex, rowIndex) :
                GridBy.coordinates(columnIndex, rowIndex);

        return valueFor(by);
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public String valueOfInRowNumber(String requestedColumnName, int rowIndex) {
        By by = waiAriaTables ?
                AriaGridBy.columnInRow(requestedColumnName, rowIndex) :
                GridBy.columnInRow(requestedColumnName, rowIndex);

        return valueFor(by);
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public String valueOfInRowWhereIs(String requestedColumnName, String selectOnColumn, String selectOnValue) {
        By by = waiAriaTables ?
                AriaGridBy.columnInRowWhereIs(requestedColumnName, selectOnColumn, selectOnValue) :
                GridBy.columnInRowWhereIs(requestedColumnName, selectOnColumn, selectOnValue);

        return valueFor(by);
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public String normalizedValueOfColumnNumberInRowNumber(int columnIndex, int rowIndex) {
        return normalizeValue(valueOfColumnNumberInRowNumber(columnIndex, rowIndex));
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public String normalizedValueOfInRowNumber(String requestedColumnName, int rowIndex) {
        return normalizeValue(valueOfInRowNumber(requestedColumnName, rowIndex));
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public String normalizedValueOfInRowWhereIs(String requestedColumnName, String selectOnColumn, String selectOnValue) {
        return normalizeValue(valueOfInRowWhereIs(requestedColumnName, selectOnColumn, selectOnValue));
    }

    @WaitUntil(TimeoutPolicy.RETURN_FALSE)
    public boolean rowExistsWhereIs(String selectOnColumn, String selectOnValue) {
        return waiAriaTables ?
                findElement(AriaGridBy.rowWhereIs(selectOnColumn, selectOnValue)) != null :
                findElement(GridBy.rowWhereIs(selectOnColumn, selectOnValue)) != null;
    }

    @WaitUntil
    public boolean clickInRowNumber(String place, int rowIndex) {
        By rowBy = waiAriaTables ?
                AriaGridBy.rowNumber(rowIndex) :
                GridBy.rowNumber(rowIndex);

        return clickInRow(rowBy, place);
    }

    @WaitUntil
    public boolean clickInRowWhereIs(String place, String selectOnColumn, String selectOnValue) {
        By rowBy = waiAriaTables ?
                AriaGridBy.rowWhereIs(selectOnColumn, selectOnValue) :
                GridBy.rowWhereIs(selectOnColumn, selectOnValue);

        return clickInRow(rowBy, place);
    }

    protected boolean clickInRow(By rowBy, String place) {
        return Boolean.TRUE.equals(doInContainer(() -> findElement(rowBy), () -> click(place)));
    }

    /**
     * Downloads the target of a link in a grid's row.
     * @param place which link to download.
     * @param rowNumber (1-based) row number to retrieve link from.
     * @return downloaded file if any, null otherwise.
     */
    @WaitUntil
    public String downloadFromRowNumber(String place, int rowNumber) {
        return waiAriaTables ?
                downloadFromRow(AriaGridBy.linkInRow(place, rowNumber)) :
                downloadFromRow(GridBy.linkInRow(place, rowNumber));
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
        return waiAriaTables ?
                downloadFromRow(AriaGridBy.linkInRowWhereIs(place, selectOnColumn, selectOnValue)) :
                downloadFromRow(GridBy.linkInRowWhereIs(place, selectOnColumn, selectOnValue));
    }

    protected String downloadFromRow(By linkBy) {
        String result = null;
        WebElement element = findElement(linkBy);
        if (element != null) {
            result = downloadLinkTarget(element);
        }
        return result;
    }

    protected T getElement(String place) {
        return getSeleniumHelper().getElement(place);
    }

    protected T getElement(String place, String container) {
        return doInContainer(container, () -> getElement(place));
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

    protected T findByClassName(String className) {
        By by = By.className(className);
        return findElement(by);
    }

    protected T findByXPath(String xpathPattern, String... params) {
        return getSeleniumHelper().findByXPath(xpathPattern, params);
    }

    protected T findByCss(String cssPattern, String... params) {
        By by = getSeleniumHelper().byCss(cssPattern, params);
        return findElement(by);
    }

    protected T findByJavascript(String script, Object... parameters) {
        By by = getSeleniumHelper().byJavascript(script, parameters);
        return findElement(by);
    }

    protected List<T> findAllByXPath(String xpathPattern, String... params) {
        By by = getSeleniumHelper().byXpath(xpathPattern, params);
        return findElements(by);
    }

    protected List<T> findAllByCss(String cssPattern, String... params) {
        By by = getSeleniumHelper().byCss(cssPattern, params);
        return findElements(by);
    }

    protected List<T> findAllByJavascript(String script, Object... parameters) {
        By by = getSeleniumHelper().byJavascript(script, parameters);
        return findElements(by);
    }

    public void waitMilliSecondAfterScroll(int msToWait) {
        waitAfterScroll = msToWait;
    }

    protected int getWaitAfterScroll() {
        return waitAfterScroll;
    }

    protected String getElementText(WebElement element) {
        String result = null;
        if (element != null) {
            scrollIfNotOnScreen(element);
            result = getSeleniumHelper().getText(element);
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

    protected T getElementToScrollTo(String place, String container) {
        return getElementToCheckVisibility(place, container);
    }

    /**
     * Scrolls browser window so top of element becomes visible.
     * @param element element to scroll to.
     */
    protected void scrollTo(WebElement element) {
        getSeleniumHelper().scrollTo(element, scrollElementToCenter);
        waitAfterScroll(waitAfterScroll);
    }

    /**
     * Wait after the scroll if needed
     * @param msToWait amount of ms to wait after the scroll
     */
    protected void waitAfterScroll(int msToWait) {
        if (msToWait > 0) {
            waitMilliseconds(msToWait);
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
        T element = getElementToCheckVisibility(place, container);
        if (element != null) {
            if ("label".equalsIgnoreCase(element.getTagName())) {
                // for labels we want to know whether their target is enabled, not the label itself
                T labelTarget = getSeleniumHelper().getLabelledElement(element);
                if (labelTarget != null) {
                    element = labelTarget;
                }
            }
            result = element.isEnabled();
        }
        return result;
    }

    /**
     * Determines whether element is NOT enabled (i.e. can not be clicked).
     * @param place element to check.
     * @return true if element is disabled.
     */
    @WaitUntil(TimeoutPolicy.RETURN_FALSE)
    public boolean isDisabled(String place) {
        return isDisabledIn(place, null);
    }

    /**
     * Determines whether element is NOT enabled (i.e. can not be clicked).
     * @param place element to check.
     * @param container parent of place.
     * @return true if element is disabled.
     */
    @WaitUntil(TimeoutPolicy.RETURN_FALSE)
    public boolean isDisabledIn(String place, String container) {
        return !isEnabledIn(place, container);
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

    /**
     * Determines whether element is not visible (or disappears within the specified timeout)
     * @param place element to check
     * @return true if the element is not displayed (anymore)
     */
    @WaitUntil(TimeoutPolicy.RETURN_FALSE)
    public boolean isNotVisible(String place) {
        return isNotVisibleIn(place, null);
    }

    /**
     * Determines whether element is not visible (or disappears within the specified timeout)
     * @param place element to check.
     * @param container parent of place.
     * @return true if the element is not displayed (anymore)
     */
    @WaitUntil(TimeoutPolicy.RETURN_FALSE)
    public boolean isNotVisibleIn(String place, String container) {
        return !isVisibleImpl(place, container, true);
    }

    /**
     * Determines whether element is not on the page (or disappears within the specified timeout)
     * @param place element to check.
     * @return true if element is not on the page (anymore).
     */
    @WaitUntil(TimeoutPolicy.RETURN_FALSE)
    public boolean isNotVisibleOnPage(String place) {
        return isNotVisibleOnPageIn(place, null);
    }

    /**
     * Determines whether element is not on the page (or disappears within the specified timeout)
     * @param place element to check.
     * @param container parent of place.
     * @return true if the element is not on the page (anymore)
     */
    @WaitUntil(TimeoutPolicy.RETURN_FALSE)
    public boolean isNotVisibleOnPageIn(String place, String container) {
        return !isVisibleImpl(place, container, false);
    }

    protected boolean isVisibleImpl(String place, String container, boolean checkOnScreen) {
        WebElement element = getElementToCheckVisibility(place, container);
        return getSeleniumHelper().checkVisible(element, checkOnScreen);
    }

    public int numberOfTimesIsVisible(String text) {
        return numberOfTimesIsVisibleInImpl(text, true);
    }

    public int numberOfTimesIsVisibleOnPage(String text) {
        return numberOfTimesIsVisibleInImpl(text, false);
    }

    public int numberOfTimesIsVisibleIn(String text, String container) {
        return intValueOf(doInContainer(container, () -> numberOfTimesIsVisible(text)));
    }

    public int numberOfTimesIsVisibleOnPageIn(String text, String container) {
        return intValueOf(doInContainer(container, () -> numberOfTimesIsVisibleOnPage(text)));
    }

    protected int intValueOf(Integer count) {
        if (count == null) {
            count = Integer.valueOf(0);
        }
        return count;
    }

    protected int numberOfTimesIsVisibleInImpl(String text, boolean checkOnScreen) {
        int result;
        SeleniumHelper<T> helper = getSeleniumHelper();
        if (implicitFindInFrames) {
            // sum over iframes
            AtomicInteger count = new AtomicInteger();
            new AllFramesDecorator<Integer>(helper)
                    .apply(() -> count.addAndGet(helper.countVisibleOccurrences(text, checkOnScreen)));
            result = count.get();
        } else {
            result = helper.countVisibleOccurrences(text, checkOnScreen);
        }
        return result;
    }

    protected T getElementToCheckVisibility(String place) {
        return getSeleniumHelper().getElementToCheckVisibility(place);
    }

    protected T getElementToCheckVisibility(String place, String container) {
        return doInContainer(container, () -> findByTechnicalSelectorOr(place, this::getElementToCheckVisibility));
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
     * Clears HTML5's sessionStorage (for the domain of the current open page in the browser).
     */
    public void clearSessionStorage() {
        getSeleniumHelper().executeJavascript("sessionStorage.clear();");
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
        String html = getSeleniumHelper().getHtml();
        return getEnvironment().getHtml(html);
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
        return String.format("<a href=\"%s\" target=\"_blank\">%s</a>", url, linkText);
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

    /**
     * Take a screenshot and crop it to the provided element
     * @param basename filename (below screenshot base directory).
     * @param place The element to crop the screenshot image to
     * @return location of the captured image
     */
    @WaitUntil
    public String takeScreenshotOf(String basename, String place) {
        return takeScreenshotOfIn(basename, place, null);
    }

    /**
     * Take a screenshot and crop it to the provided element
     * @param basename filename (below screenshot base directory).
     * @param place The element to crop the screenshot image to.
     * @param container the elemnt to limit the search context to, when searching for place.
     * @return location of the captured image
     */
    @WaitUntil
    public String takeScreenshotOfIn(String basename, String place, String container) {
        T element = container == null ? getElement(place) : getElement(place, container);
        if (element == null) {
            return null;
        }
        scrollIfNotOnScreen(element);

        String name = getScreenshotBasename(basename);
        String imageFile = getSeleniumHelper().takeElementScreenshot(name, element);
        if (imageFile == null) {
            throw new SlimFixtureException(false, "Unable to take screenshot: does the webdriver support it?");
        } else {
            imageFile = getScreenshotLink(imageFile);
        }
        return imageFile;
    }

    private String getScreenshotLink(String screenshotFile) {
        String wikiUrl = getWikiUrl(screenshotFile);
        if (wikiUrl != null) {
            // make href to screenshot

            if ("".equals(screenshotHeight)) {
                wikiUrl = String.format("<a href=\"%s\" target=\"_blank\">%s</a>",
                        wikiUrl, screenshotFile);
            } else {
                wikiUrl = String.format("<a href=\"%1$s\" target=\"_blank\"><img src=\"%1$s\" title=\"%2$s\" height=\"%3$s\"/></a>",
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
     * @return last attempt results, if not null or false.
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
        if (lastAttemptResult == null || Boolean.FALSE.equals(lastAttemptResult)) {
            throw e;
        }
        return lastAttemptResult;
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

    public boolean refreshSearchContext() {
        // copy path so we can retrace after clearing it
        List<String> fullPath = new ArrayList<>(getCurrentSearchContextPath());
        return refreshSearchContext(fullPath, Math.min(fullPath.size(), minStaleContextRefreshCount));
    }

    protected boolean refreshSearchContext(List<String> fullPath, int maxRetries) {
        clearSearchContext();
        for (String container : fullPath) {
            try {
                setSearchContextTo(container);
            } catch (RuntimeException se) {
                if (maxRetries < 1 || !(se instanceof WebDriverException)
                        || !getSeleniumHelper().isStaleElementException((WebDriverException) se)) {
                    // not the entire context was refreshed, clear it to prevent an 'intermediate' search context
                    clearSearchContext();
                    throw new SlimFixtureException("Search context is 'stale' and could not be refreshed. Context was: " + fullPath
                            + ". Error when trying to refresh: " + container, se);
                } else {
                    // search context went stale while setting, retry
                    return refreshSearchContext(fullPath, maxRetries - 1);
                }
            }
        }
        return true;
    }

    protected <T> T handleTimeoutException(TimeoutException e) {
        String message = getTimeoutMessage(e);
        throw new TimeoutStopTestException(false, message, e);
    }

    private String getTimeoutMessage(TimeoutException e) {
        String messageBase = String.format("Timed-out waiting (after %ss)", secondsBeforeTimeout());
        try {
            return getSlimFixtureExceptionMessage("timeouts", "timeout", messageBase, e);
        } catch (RuntimeException re) {
            return messageBase + " and unable to capture screenshot and page source. " + re.toString();
        }
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
        String exceptionMsg = getExceptionMessageText(messageBase, t);
        // take a screenshot of what was on screen
        String screenshotTag = getExceptionScreenshotTag(screenshotBaseName, messageBase, t);
        String label = getExceptionPageSourceTag(screenshotBaseName, messageBase, t);

        String message = String.format("<div><div>%s.</div><div>%s:%s</div></div>", exceptionMsg, label, screenshotTag);
        return message;
    }

    protected String getExceptionMessageText(String messageBase, Throwable t) {
        String message = messageBase;
        if (message == null) {
            if (t == null) {
                message = "";
            } else {
                message = ExceptionUtils.getStackTrace(t);
            }
        }
        return formatExceptionMsg(message);
    }

    protected String getExceptionScreenshotTag(String screenshotBaseName, String messageBase, Throwable t) {
        String screenshotTag = "(Screenshot not available)";
        try {
            String screenShotFile = createScreenshot(screenshotBaseName, t);
            screenshotTag = getScreenshotLink(screenShotFile);
        } catch (UnhandledAlertException e) {
            // https://code.google.com/p/selenium/issues/detail?id=4412
            System.err.println("Unable to take screenshot while alert is present for exception: " + messageBase);
        } catch (Exception sse) {
            System.err.println("Unable to take screenshot for exception: " + messageBase);
            sse.printStackTrace();
        }
        return screenshotTag;
    }

    protected String getExceptionPageSourceTag(String screenshotBaseName, String messageBase, Throwable t) {
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
        return label;
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
    protected SeleniumHelper<T> getSeleniumHelper() {
        return seleniumHelper;
    }

    /**
     * Sets SeleniumHelper to use, for testing purposes.
     * @param helper helper to use.
     */
    protected void setSeleniumHelper(SeleniumHelper<T> helper) {
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
        WebElement element = getElementToDownload(place);
        return downloadLinkTarget(element);
    }

    protected WebElement getElementToDownload(String place) {
        SeleniumHelper<T> helper = getSeleniumHelper();
        return helper.findByTechnicalSelectorOr(place,
                () -> helper.getLink(place),
                () -> helper.findElement(AltBy.exact(place)),
                () -> helper.findElement(AltBy.partial(place)));
    }

    /**
     * Downloads the target of the supplied link.
     * @param place link to follow.
     * @param container part of screen containing link.
     * @return downloaded file if any, null otherwise.
     */
    @WaitUntil
    public String downloadIn(String place, String container) {
        return doInContainer(container, () -> download(place));
    }

    protected T findElement(By selector) {
        return getSeleniumHelper().findElement(selector);
    }

    protected List<T> findElements(By by) {
        return getSeleniumHelper().findElements(by);
    }

    public T findByTechnicalSelectorOr(String place, Function<String, ? extends T> supplierF) {
        return getSeleniumHelper().findByTechnicalSelectorOr(place, () -> supplierF.apply(place));
    }

    /**
     * Downloads the target of the supplied link.
     * @param element link to follow.
     * @return downloaded file if any, null otherwise.
     */
    protected String downloadLinkTarget(WebElement element) {
        String result;
        String href = getLinkTarget(element);
        if (href != null) {
            result = downloadContentFrom(href);
        } else {
            throw new SlimFixtureException(false, "Could not determine url to download from");
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
            HttpTest httpTest = new HttpTest();
            httpTest.copyBrowserCookies();
            result = httpTest.getFileFrom(urlOrLink);
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

    protected T getElementToSelectFile(String place, String container) {
        T result = null;
        T element = getElement(place, container);
        if (element != null
                && "input".equalsIgnoreCase(element.getTagName())
                && "file".equalsIgnoreCase(element.getAttribute("type"))) {
            result = element;
        }
        return result;
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

    public boolean refreshUntilValueOfIs(String place, String expectedValue) {
        return repeatUntil(getRefreshUntilValueIs(place, expectedValue));
    }

    public boolean refreshUntilValueOfIsNot(String place, String expectedValue) {
        return repeatUntilNot(getRefreshUntilValueIs(place, expectedValue));
    }

    protected RepeatCompletion getRefreshUntilValueIs(String place, String expectedValue) {
        return new ConditionBasedRepeatUntil(false, d-> refresh(),
                                            true, d -> checkValueIs(place, expectedValue));
    }

    /**
     * Refreshes current page until 'place' is found somewhere on the page. Do not forget to set 'repeat at most times', or else the loop may run endlessly.
     * Usage: | refresh until | [place] | is visible on page |
     * @param place The place to find.
     * @return true if place is found while repeating
     */
    public boolean refreshUntilIsVisibleOnPage(String place) {
        return repeatUntil(getRefreshUntilIsVisibleOnPage(place));
    }

    /**
     * Refreshes current page until 'place' is not found somewhere on the page. Do not forget to set 'repeat at most times', or else the loop may run endlessly.
     * Usage: | refresh until | [place] | is not visible on page |
     * @param place The place you would not like to find anymore.
     * @return true if place is not found while repeating
     */
    public boolean refreshUntilIsNotVisibleOnPage(String place) {
        return repeatUntilNot(getRefreshUntilIsVisibleOnPage(place));
    }

    protected RepeatCompletion getRefreshUntilIsVisibleOnPage(String place) {
        return new ConditionBasedRepeatUntil(false, d -> refresh(),
                                            true, d -> isVisibleOnPage(place));
    }

    public boolean clickUntilValueOfIs(String clickPlace, String checkPlace, String expectedValue) {
        return repeatUntil(getClickUntilValueIs(clickPlace, checkPlace, expectedValue));
    }

    public boolean clickUntilValueOfIsNot(String clickPlace, String checkPlace, String expectedValue) {
        return repeatUntilNot(getClickUntilValueIs(clickPlace, checkPlace, expectedValue));
    }

    public boolean executeJavascriptUntilIs(String script, String place, String value) {
        return repeatUntil(getExecuteScriptUntilValueIs(script, place, value));
    }

    public boolean executeJavascriptUntilIsNot(String script, String place, String value) {
        return repeatUntilNot(getExecuteScriptUntilValueIs(script, place, value));
    }

    protected RepeatCompletion getExecuteScriptUntilValueIs(String script, String place, String expectedValue) {
        return new ConditionBasedRepeatUntil(
                false, d -> {
                        Object r = executeScript(script);
                        return r != null ? r : true;
                    },
                true, d -> checkValueIs(place, expectedValue));
    }

    protected RepeatCompletion getClickUntilValueIs(String clickPlace, String checkPlace, String expectedValue) {
        String place = cleanupValue(clickPlace);
        return getClickUntilCompletion(place, checkPlace, expectedValue);
    }

    protected RepeatCompletion getClickUntilCompletion(String place, String checkPlace, String expectedValue) {
        return new ConditionBasedRepeatUntil(true, d -> click(place), d -> checkValueIs(checkPlace, expectedValue));
    }

    protected boolean repeatUntil(ExpectedCondition<Object> actionCondition, ExpectedCondition<Boolean> finishCondition) {
        return repeatUntil(new ConditionBasedRepeatUntil(true, actionCondition, finishCondition));
    }

    protected boolean repeatUntilIsNot(ExpectedCondition<Object> actionCondition, ExpectedCondition<Boolean> finishCondition) {
        return repeatUntilNot(new ConditionBasedRepeatUntil(true, actionCondition, finishCondition));
    }

    protected <T> ExpectedCondition<T> wrapConditionForFramesIfNeeded(ExpectedCondition<T> condition) {
        if (implicitFindInFrames) {
            condition = getSeleniumHelper().conditionForAllFrames(condition);
        }
        return condition;
    }

    @Override
    protected boolean repeatUntil(RepeatCompletion repeat) {
        // During repeating we reduce the timeout used for finding elements,
        // but the page load timeout is kept as-is (which takes extra work because secondsBeforeTimeout(int)
        // also changes that.
        int previousTimeout = secondsBeforeTimeout();
        int pageLoadTimeout = secondsBeforePageLoadTimeout();
        try {
            int timeoutDuringRepeat = Math.max((Math.toIntExact(repeatInterval() / 1000)), 1);
            secondsBeforeTimeout(timeoutDuringRepeat);
            secondsBeforePageLoadTimeout(pageLoadTimeout);
            return super.repeatUntil(repeat);
        } finally {
            secondsBeforeTimeout(previousTimeout);
            secondsBeforePageLoadTimeout(pageLoadTimeout);
        }
    }

    protected boolean checkValueIs(String place, String expectedValue) {
        boolean match;
        String actual = valueOf(place);
        if (expectedValue == null) {
            match = actual == null;
        } else {
            String cleanExpectedValue = cleanExpectedValue(expectedValue);
            match = compareActualToExpected(cleanExpectedValue, actual);
        }
        return match;
    }

    protected class ConditionBasedRepeatUntil extends FunctionalCompletion {
        public ConditionBasedRepeatUntil(boolean wrapIfNeeded,
                                         ExpectedCondition<? extends Object> repeatCondition,
                                         ExpectedCondition<Boolean> finishedCondition) {
            this(wrapIfNeeded, repeatCondition, wrapIfNeeded, finishedCondition);
        }

        public ConditionBasedRepeatUntil(boolean wrapRepeatIfNeeded,
                                         ExpectedCondition<? extends Object> repeatCondition,
                                         boolean wrapFinishedIfNeeded,
                                         ExpectedCondition<Boolean> finishedCondition) {
            if (wrapRepeatIfNeeded) {
                repeatCondition = wrapConditionForFramesIfNeeded(repeatCondition);
            }
            ExpectedCondition<?> finalRepeatCondition = repeatCondition;
            if (wrapFinishedIfNeeded) {
                finishedCondition = wrapConditionForFramesIfNeeded(finishedCondition);
            }
            ExpectedCondition<Boolean> finalFinishedCondition = finishedCondition;

            setIsFinishedSupplier(() -> waitUntilOrNull(finalFinishedCondition));
            setRepeater(() -> waitUntil(finalRepeatCondition));
        }
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

    public boolean isContinueIfReadyStateInteractive() {
        return continueIfReadyStateInteractive;
    }

    public void setContinueIfReadyStateInteractive(boolean continueIfReadyStateInteractive) {
        this.continueIfReadyStateInteractive = continueIfReadyStateInteractive;
    }

    /**
     * Executes javascript in the browser.
     * @param script you want to execute
     * @return result from script
     */
    public Object executeScript(String script) {
        String statement = cleanupValue(script);
        return getSeleniumHelper().executeJavascript(statement);
    }

    /**
     * Simulates 'select all' (e.g. Ctrl+A on Windows) on the active element.
     * @return whether an active element was found.
     */
    @WaitUntil
    public boolean selectAll() {
        return getSeleniumHelper().selectAll();
    }

    /**
     * Simulates 'copy' (e.g. Ctrl+C on Windows) on the active element, copying the current selection to the clipboard.
     * @return whether an active element was found.
     */
    @WaitUntil
    public boolean copy() {
        return getSeleniumHelper().copy();
    }

    /**
     * Simulates 'cut' (e.g. Ctrl+X on Windows) on the active element, copying the current selection to the clipboard
     * and removing that selection.
     * @return whether an active element was found.
     */
    @WaitUntil
    public boolean cut() {
        return getSeleniumHelper().cut();
    }

    /**
     * Simulates 'paste' (e.g. Ctrl+V on Windows) on the active element, copying the current clipboard
     * content to the currently active element.
     * @return whether an active element was found.
     */
    @WaitUntil
    public boolean paste() {
        return getSeleniumHelper().paste();
    }

    /**
     * @return text currently selected in browser, or empty string if no text is selected.
     */
    public String getSelectionText() {
        return getSeleniumHelper().getSelectionText();
    }

    /**
     * @return should 'normalized' functions remove starting and trailing whitespace?
     */
    public boolean trimOnNormalize() {
        return trimOnNormalize;
    }

    /**
     * @param trimOnNormalize should 'normalized' functions remove starting and trailing whitespace?
     */
    public void setTrimOnNormalize(boolean trimOnNormalize) {
        this.trimOnNormalize = trimOnNormalize;
    }

    /**
     * Set the scroll into view behaviour to 'center of viewport' (true) or 'auto' (false)
     * @param scrollElementsToCenterOfViewport True to scroll to center, False to use automatic scroll behaviour
     */
    public void scrollElementsToCenterOfViewport(boolean scrollElementsToCenterOfViewport) {
        scrollElementToCenter = scrollElementsToCenterOfViewport;
    }

    /**
     * Get the current scroll behaviour. True means 'center of viewport', False means 'auto'
     * @return the current boolean value of scrollElementToCenter
     */
    public boolean scrollElementsToCenterOfViewport() {
        return scrollElementToCenter;
    }

    /**
     * Configure browser test to expect wai aria style tables made up of divs and spans with roles like table/cell/row/etc.
     * @param waiAriaTables True to expect aria tables, false to expect classic &lt;table&gt; table tags.
     */
    public void useAriaTableStructure(boolean waiAriaTables) {
        this.waiAriaTables = waiAriaTables;
    }
}
