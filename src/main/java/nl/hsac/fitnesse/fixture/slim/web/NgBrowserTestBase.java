package nl.hsac.fitnesse.fixture.slim.web;

import nl.hsac.fitnesse.fixture.slim.StopTestException;
import nl.hsac.fitnesse.fixture.slim.web.annotation.NoNgWait;
import nl.hsac.fitnesse.slim.interaction.ReflectionHelper;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

/**
 * Base class for fixtures testing sites using AngularJs.
 */
public abstract class NgBrowserTestBase extends BrowserTest<WebElement> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NgBrowserTestBase.class);
    private final static Set<String> METHODS_NO_WAIT;

    private final String waitForAngularScript;
    private String angularRoot;

    static {
        METHODS_NO_WAIT = ReflectionHelper.validateMethodNames(NgBrowserTestBase.class,
                "open",
                "takeScreenshot",
                "location",
                "back",
                "forward",
                "refresh",
                "alertText",
                "confirmAlert",
                "dismissAlert",
                "openInNewTab",
                "ensureActiveTabIsNotClosed",
                "currentTabIndex",
                "tabCount",
                "ensureOnlyOneTab",
                "closeTab",
                "setAngularRoot",
                "switchToNextTab",
                "switchToPreviousTab",
                "switchToDefaultContent",
                "switchToFrame",
                "switchToParentFrame",
                "waitForAngularRequestsToFinish",
                "secondsBeforeTimeout",
                "secondsBeforePageLoadTimeout",
                "waitForPage",
                "waitForTagWithText",
                "waitForClassWithText",
                "waitForClass",
                "waitForVisible",
                "waitSeconds",
                "waitMilliseconds",
                "waitMilliSecondAfterScroll",
                "screenshotBaseDirectory",
                "screenshotShowHeight",
                "setBrowserWidth",
                "setBrowserHeight",
                "setBrowserSizeToBy",
                "setBrowserSizeToMaximum",
                "setGlobalValueTo",
                "setSendCommandForControlOnMacTo",
                "sendCommandForControlOnMac",
                "isImplicitWaitForAngularEnabled",
                "setImplicitWaitForAngularTo",
                "globalValue",
                "clearSearchContext",
                "setAngularRoot",
                "getAngularRoot",
                "executeScript",
                "secondsBeforeTimeout",
                "secondsBeforePageLoadTimeout",
                "trimOnNormalize",
                "setImplicitFindInFramesTo",
                "setTrimOnNormalize",
                "setRepeatIntervalToMilliseconds",
                "repeatAtMostTimes",
                "repeatAtMostTimes",
                "timeSpentRepeating");
    }

    public NgBrowserTestBase(String waitForAngular) {
        setImplicitWaitForAngularTo(true);
        waitForAngularScript = waitForAngular;
    }

    public NgBrowserTestBase(int secondsBeforeTimeout, String waitForAngular) {
        super(secondsBeforeTimeout);
        setImplicitWaitForAngularTo(true);
        waitForAngularScript = waitForAngular;
    }

    @Override
    protected void waitForAngularIfNeeded(Method method) {
        if (isImplicitWaitForAngularEnabled()
                && requiresWaitForAngular(method)) {
            waitForAngularRequestsToFinish();
        }
    }

    /**
     * Determines whether method requires waiting for all Angular requests to finish
     * before it is invoked.
     * @param method method to be invoked.
     * @return true, if waiting for Angular is required, false otherwise.
     */
    protected boolean requiresWaitForAngular(Method method) {
        String methodName = method.getName();
        return !METHODS_NO_WAIT.contains(methodName) && !hasNoWaitAnnotation(method);
    }

    protected boolean hasNoWaitAnnotation(Method method) {
    	return method.isAnnotationPresent(NoNgWait.class);
    }

    @Override
    public boolean open(String address) {
        boolean result = super.open(address);
        if (result && isImplicitWaitForAngularEnabled()) {
            waitForAngularRequestsToFinish();
        }
        return result;
    }

    public void waitForAngularRequestsToFinish() {
        String root = getAngularRoot();
        try {
            waitForAngularRequestsToFinish(root);
        } catch (RuntimeException e) {
            handleExceptionWhileWaiting(root, e);
        }
    }

    protected void waitForAngularRequestsToFinish(String root) {
        Object result = waitForJavascriptCallback(waitForAngularScript, root);
        if (result != null && !(result instanceof Boolean)) {
            String msg = getSlimFixtureExceptionMessage("angular", result.toString(), null);
            throw new StopTestException(false, msg);
        }
    }

    protected void handleExceptionWhileWaiting(String rootSelector, RuntimeException e) {
        List<WebElement> roots;
        try {
            roots = findAllByCss(rootSelector);
        } catch (RuntimeException ex) {
            LOGGER.error("Problem using rootSelector: " + rootSelector, ex);
            throw e;
        }

        if (roots.isEmpty()) {
            LOGGER.error("Unable to locate Angular root element. Please configure it explicitly using setAngularRoot(selector)");
        } else if (roots.size() == 1) {
            LOGGER.error("Found Angular. Single root element found, but error while waiting for requests to finish.");
        } else {
            LOGGER.error("Found Angular. Multiple root elements seem to be present: "
                    + roots.size()
                    + " using root selector: " + rootSelector);
        }
        LOGGER.error("Retrying once");
        waitForAngularRequestsToFinish(rootSelector);
    }

    /**
     * @return CSS selector expression used to find root of application.
     */
    public String getAngularRoot() {
        return angularRoot;
    }

    /**
     * Defines which CSS selector to use to find the application root.
     * @param anAngularRoot CSS selector expression.
     */
    public void setAngularRoot(String anAngularRoot) {
        angularRoot = anAngularRoot;
    }
}
