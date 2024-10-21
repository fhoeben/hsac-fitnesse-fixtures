package nl.hsac.fitnesse.fixture.slim.web;

import fitnesse.slim.fixtureInteraction.FixtureInteraction;
import nl.hsac.fitnesse.fixture.slim.SlimFixtureException;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.ScriptTimeoutException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Version of {@link BrowserTest} with added functionality to deal with RichFaces, JSF pages.
 */
public class RichFacesTest extends BrowserTest<WebElement> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RichFacesTest.class);
    protected static final String RICH_FACES_AJAX_CALL = "RichFaces.ajax(";
    private final List<String> eventsThatMayRequireWaiting = new ArrayList<>(Arrays.asList("onchange", "onclick"));
    private boolean ajaxStartWithOnly = false;
    private boolean ignoreImplicitAjaxWaitTimeouts = true;
    private boolean shouldWaitForAjax = false;
    private String previousLocation = null;

    public RichFacesTest() {
        super();
    }

    public RichFacesTest(int secondsBeforeTimeout) {
        super(secondsBeforeTimeout);
    }

    public boolean onlyWaitIfEventHandlerStartsWithAjaxCall() {
        return ajaxStartWithOnly;
    }

    public void setOnlyWaitIfEventHandlerStartsWithAjaxCall(boolean newValue) {
        ajaxStartWithOnly = newValue;
    }

    @Override
    protected void beforeInvoke(Method method, Object[] arguments) {
        setShouldWaitForAjax(false);
        super.beforeInvoke(method, arguments);
    }

    @Override
    protected Object invoke(FixtureInteraction interaction, Method method, Object[] arguments) throws Throwable {
        Object result = super.invoke(interaction, method, arguments);
        if (shouldWaitForAjax()) {
            try {
                waitForJsfAjaxImpl(true);
            } catch (ScriptTimeoutException e) {
                if (ignoreImplicitAjaxWaitTimeouts) {
                    // log message but do not throw
                    LOGGER.error("Timeout while waiting for ajax call after: " + method.getName() + " with arguments: " + Arrays.toString(arguments));
                    String msg = createAjaxTimeoutMessage(e);
                    LOGGER.error("Exception not forwarded to wiki: " + msg);
                } else {
                    throw createAjaxTimeout(e);
                }
            }
        }
        return result;
    }

    @Override
    protected void sendValue(WebElement element, String value) {
        boolean triggersAjax = willTriggerAjax(element);

        super.sendValue(element, value);

        if (triggersAjax) {
            // ensure we trigger change event
            pressTab();
            setShouldWaitForAjax(true);
        }
    }

    @Override
    protected boolean clickSelectOption(WebElement element, String optionValue) {
        boolean triggersAjax = willTriggerAjax(element);
        boolean result = super.clickSelectOption(element, optionValue);
        if (triggersAjax && result) {
            setShouldWaitForAjax(true);
        }
        return result;
    }

    @Override
    protected boolean clickElement(WebElement element) {
        boolean triggersAjax = willTriggerAjax(element);
        boolean result = super.clickElement(element);
        if (triggersAjax && result) {
            setShouldWaitForAjax(true);
        }
        return result;
    }


    @Override
    protected boolean repeatUntil(RepeatCompletion repeat) {
        // disable checking for ajax attributes, by indicating we already know we must wait.
        // this method does its own waiting, irrespective of ajax calls
        boolean previousWaitForAjax = shouldWaitForAjax();
        setShouldWaitForAjax(true);
        try {
            return super.repeatUntil(repeat);
        } finally {
            // reset wait for ajax to original value
            setShouldWaitForAjax(previousWaitForAjax);
        }
    }

    protected boolean willTriggerAjax(WebElement element) {
        // no need to inspect element attributes if we already know we must wait
        return shouldWaitForAjax() || hasRichFacesAjax(element);
    }

    protected boolean hasRichFacesAjax(WebElement element) {
        if (element == null) {
            return false;
        }
        boolean result = isAjaxEventPresent(element);
        if (!result) {
            String tagName = element.getTagName();
            if ("label".equalsIgnoreCase(tagName)) {
                WebElement labelTarget = getSeleniumHelper().getLabelledElement(element);
                if (labelTarget != null) {
                    result = isAjaxEventPresent(labelTarget);
                }
            }
        }
        if (result) {
            // store current URL so we can check against it later when waiting for Ajax
            storeLocationBeforeAction();
        }
        return result;
    }

    protected boolean isAjaxEventPresent(WebElement element) {
        boolean result = false;
        for (String event : getEventsThatMayRequireWaiting()) {
            result = eventTriggersAjax(element, event);
            if (result) {
                break;
            }
        }
        return result;
    }

    protected void storeLocationBeforeAction() {
        previousLocation = location();
    }

    protected boolean eventTriggersAjax(WebElement element, String attribute) {
        String eventHandler = element.getAttribute(attribute);
        return eventHandler != null
                && (ajaxStartWithOnly? eventHandler.startsWith(RICH_FACES_AJAX_CALL)
                                    : eventHandler.contains(RICH_FACES_AJAX_CALL));
    }

    public void waitForJsfAjax() {
        try {
            waitForJsfAjaxImpl(false);
        } catch (ScriptTimeoutException e) {
            throw createAjaxTimeout(e);
        }
    }

    protected void waitForJsfAjaxImpl(boolean checkLocation) {
        try {
            // if jsf is present on page, add an event listener that will be triggered when next Ajax request completes
            if (checkLocation) {
                waitForJavascriptCallback("if(!window.jsf||window.location.href!==arguments[0]){callback();}else{jsf.ajax.addOnEvent(function(data){if(data.status=='success')callback();});}",
                        previousLocation);
            } else {
                waitForJavascriptCallback("if(!window.jsf){callback();}else{jsf.ajax.addOnEvent(function(data){if(data.status=='success')callback();});}");
            }
        } catch (JavascriptException e) {
            String msg = e.getMessage();
            if (msg.startsWith("javascript error: document unloaded while waiting for result")) {
                // document is reloaded, no problem
            } else {
                throw e;
            }
        } finally {
            setShouldWaitForAjax(false);
        }
    }

    protected void setShouldWaitForAjax(boolean shouldWaitForAjax) {
        this.shouldWaitForAjax = shouldWaitForAjax;
    }

    protected boolean shouldWaitForAjax() {
        return shouldWaitForAjax;
    }

    public List<String> getEventsThatMayRequireWaiting() {
        return eventsThatMayRequireWaiting;
    }

    public void setIgnoreImplicitAjaxWaitTimeouts(boolean ignoreAjaxWaitTimeouts) {
        ignoreImplicitAjaxWaitTimeouts = ignoreAjaxWaitTimeouts;
    }

    public boolean willIgnoreImplicitAjaxWaitTimeouts() {
        return ignoreImplicitAjaxWaitTimeouts;
    }

    protected AjaxTimeout createAjaxTimeout(ScriptTimeoutException e) {
        return new AjaxTimeout(createAjaxTimeoutMessage(e), e);
    }

    protected String createAjaxTimeoutMessage(ScriptTimeoutException e) {
        String messageBase = "Did not detect completion of RichFaces ajax call: " + e.getMessage();
        return getSlimFixtureExceptionMessage("timeouts", "rfAjaxTimeout", messageBase, e);
    }

    /**
     * Exception to indicate timeout while waiting for RichFace's ajax call to complete.
     */
    public static class AjaxTimeout extends SlimFixtureException {
        public AjaxTimeout(String message, ScriptTimeoutException e) {
            super(false, message, e);
        }
    }
}
