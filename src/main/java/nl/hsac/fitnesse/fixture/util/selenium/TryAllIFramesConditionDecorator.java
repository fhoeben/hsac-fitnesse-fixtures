package nl.hsac.fitnesse.fixture.util.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adds a decorator on top of a decorator such that it is applied to all iframes nested
 * inside the current page (or active iframe).
 */
public class TryAllIFramesConditionDecorator implements ExpectedCondition<Object> {
    private final ExpectedCondition<Object> decorated;
    private final List<WebElement> rootPath;

    /**
     * Creates new, working on top-level page.
     * @param nested condition to be applied for each iframe.
     */
    public TryAllIFramesConditionDecorator(ExpectedCondition<Object> nested) {
        this(Collections.EMPTY_LIST, nested);
    }

    /**
     * Creates new, working inside a activated iframe.
     * @param parents path of parent iframes to get to current iframe
     * @param nested condition to be applied for each iframe.
     */
    public TryAllIFramesConditionDecorator(List<WebElement> parents, ExpectedCondition<Object> nested) {
        decorated = nested;
        rootPath = parents;
    }

    @Override
    public Object apply(WebDriver webDriver) {
        Object result = decorated.apply(webDriver);
        if (!waitUntilFinished(result)) {
            result = invokeInIFrames(webDriver, rootPath);
        }
        return result;
    }

    private Object invokeInIFrames(WebDriver webDriver, List<WebElement> parents) {
        Object result = null;
        List<WebElement> iframes = webDriver.findElements(By.tagName("iframe"));
        for (WebElement iframe : iframes) {
            webDriver.switchTo().frame(iframe);
            try {
                result = decorated.apply(webDriver);
                if (waitUntilFinished(result)) {
                    break;
                } else {
                    List<WebElement> newParents = new ArrayList<WebElement>(parents.size() + 1);
                    newParents.addAll(parents);
                    newParents.add(iframe);
                    result = invokeInIFrames(webDriver, newParents);
                    if (waitUntilFinished(result)) {
                        break;
                    }
                }
            } finally {
                // Safari and PhantomJs don't support switchTo.parentFrame, so we do this
                // it works for Phantom, but is VERY slow there (other browsers are slow but ok)
                webDriver.switchTo().defaultContent();
                for (WebElement parent : parents) {
                    webDriver.switchTo().frame(parent);
                }
            }
        }
        return result;
    }

    private boolean waitUntilFinished(Object result) {
        return result != null && !Boolean.FALSE.equals(result);
    }
}
