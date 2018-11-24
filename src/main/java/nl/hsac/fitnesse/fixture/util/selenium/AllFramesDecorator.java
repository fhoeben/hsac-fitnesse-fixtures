package nl.hsac.fitnesse.fixture.util.selenium;

import nl.hsac.fitnesse.fixture.util.selenium.by.ConstantBy;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.function.Function;

/**
 * Applies a function to all frames and iframes nested
 * inside the current page (or active iframe).
 * @param <T> type of condition result.
 */
public class AllFramesDecorator<T> implements Function<WebDriver, T> {
    private final SeleniumHelper helper;
    private final Function<WebDriver, T> decorated;
    private final Function<T, Boolean> isFinished;
    private int frameDepthOnStart;

    /**
     * Creates new, working inside the aHelper's current (i)frame.
     */
    public AllFramesDecorator(SeleniumHelper aHelper,
                              Function<WebDriver, T> aFunction,
                              Function<T, Boolean> anIsFinishedFunction) {
        helper = aHelper;
        decorated = aFunction;
        isFinished = anIsFinishedFunction;
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
        List<WebElement> frames = helper.findElements(ConstantBy.frames());
        for (WebElement frame : frames) {
            SearchContext currentContext = helper.getCurrentContext();
            if (currentContext == helper.driver()) {
                currentContext = null;
            }
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
                        // restore search context
                        if (currentContext != null) {
                            helper.setCurrentContext(currentContext);
                        }
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

    private boolean isFinished(T result) {
        return isFinished.apply(result);
    }
}
