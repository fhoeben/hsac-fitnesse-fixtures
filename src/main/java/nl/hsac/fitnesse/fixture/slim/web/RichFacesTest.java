package nl.hsac.fitnesse.fixture.slim.web;

import fitnesse.slim.fixtureInteraction.FixtureInteraction;
import nl.hsac.fitnesse.fixture.util.selenium.by.LabelBy;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.WebElement;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Triodos specific version of {@link BrowserTest} with added functionality to deal
 * with RichFaces, JSF pages.
 */
public class RichFacesTest extends BrowserTest<WebElement> {
    private final List<String> eventsThatMayRequireWaiting = new ArrayList<>(Arrays.asList("onchange", "onclick"));
    private boolean shouldWaitForAjax = false;
    private String previousLocation = null;

    public RichFacesTest() {
        super();
    }

    public RichFacesTest(int secondsBeforeTimeout) {
        super(secondsBeforeTimeout);
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
            waitForJsfAjax();
        }
        return result;
    }

    @Override
    protected void sendValue(WebElement element, String value) {
        boolean triggersAjax = hasRichFacesAjax(element);

        super.sendValue(element, value);

        if (triggersAjax) {
            // ensure we trigger change event
            pressTab();
            setShouldWaitForAjax(true);
        }
    }

    @Override
    protected boolean clickSelectOption(WebElement element, String optionValue) {
        boolean triggersAjax = hasRichFacesAjax(element);
        boolean result = super.clickSelectOption(element, optionValue);
        if (triggersAjax && result) {
            setShouldWaitForAjax(true);
        }
        return result;
    }

    @Override
    protected boolean clickElement(WebElement element) {
        boolean triggersAjax = hasRichFacesAjax(element);
        boolean result = super.clickElement(element);
        if (triggersAjax && result) {
            setShouldWaitForAjax(true);
        }
        return result;
    }

    protected boolean hasRichFacesAjax(WebElement element) {
        if (element == null) {
            return false;
        }
        boolean result = isAjaxEventPresent(element);
        if (!result) {
            String tagName = element.getTagName();
            if ("label".equals(tagName)) {
                WebElement labelTarget = LabelBy.getLabelledElement(getSeleniumHelper().getCurrentContext(), element);
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
        return eventHandler != null && eventHandler.startsWith("RichFaces.ajax(");
    }

    public void waitForJsfAjax() {
        try {
            // if jsf is present on page, add an event listener that will be triggered when next Ajax request completes
            waitForJavascriptCallback("if(!window.jsf || window.location.href !== arguments[0]){callback();}else{jsf.ajax.addOnEvent(function(data){if(data.status!='begin')callback();});}",
                    previousLocation);
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
}
