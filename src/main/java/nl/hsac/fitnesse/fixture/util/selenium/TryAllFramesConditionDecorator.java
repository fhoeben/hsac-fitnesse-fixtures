package nl.hsac.fitnesse.fixture.util.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.util.List;

/**
 * Adds a decorator on top of a decorator such that it is applied to all frames and iframes nested
 * inside the current page (or active iframe).
 * @param <T> type of condition result.
 */
class TryAllFramesConditionDecorator<T> implements ExpectedCondition<T> {
    private static final By BY_FRAME = By.cssSelector("iframe,frame");

    private final SeleniumHelper helper;
    private final ExpectedCondition<T> decorated;
    private int frameDepthOnStart;

    /**
     * Creates new, working inside the aHelper's current (i)frame.
     * @param toBeDecorated condition to be applied for each (i)frame.
     */
    public TryAllFramesConditionDecorator(SeleniumHelper aHelper, ExpectedCondition<T> toBeDecorated) {
        helper = aHelper;
        decorated = toBeDecorated;
    }

    @Override
    public T apply(WebDriver webDriver) {
        T result = decorated.apply(webDriver);
        if (!isFinished(result)) {
            frameDepthOnStart = helper.getCurrentFrameDepth();
            result = invokeInFrames(webDriver);
        }
        return result;
    }

    private T invokeInFrames(WebDriver webDriver) {
        T result = null;
        List<WebElement> frames = webDriver.findElements(BY_FRAME);
        for (WebElement frame : frames) {
            helper.switchToFrame(frame);
            try {
                result = decorated.apply(webDriver);
                if (isFinished(result)) {
                    break;
                } else {
                    result = invokeInFrames(webDriver);
                    if (isFinished(result)) {
                        break;
                    }
                }
            } finally {
                // if we already had a problem with alerts at lower level, no need to try to go up again
                if (helper.getFrameDepthOnLastAlertError() == 0) {
                    int depthOnAlert = helper.getCurrentFrameDepth();
                    try {
                        helper.switchToParentFrame();
                    } catch (UnhandledAlertException e) {
                        // we can't go up if there is an alert open.
                        // we store the current depth so we might go back up when the alert is handled
                        helper.storeFrameDepthOnAlertError(depthOnAlert - frameDepthOnStart);
                        break;
                    }
                }
            }
        }
        return result;
    }

    private boolean isFinished(Object result) {
        return result != null && !Boolean.FALSE.equals(result);
    }
}
