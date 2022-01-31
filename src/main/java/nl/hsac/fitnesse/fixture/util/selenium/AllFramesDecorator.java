package nl.hsac.fitnesse.fixture.util.selenium;

import nl.hsac.fitnesse.fixture.util.selenium.by.ConstantBy;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Applies a function to all frames and iframes nested
 * inside the current page (or active iframe).
 * @param <T> type of result.
 */
public class AllFramesDecorator<T> {
    private final SeleniumHelper helper;
    private final Function<T, Boolean> isFinished;
    private int frameDepthOnStart;

    /**
     * Creates new, working inside the aHelper's current (i)frame.
     * Will visit all (i)frames, this will mean apply() will return null.
     */
    public AllFramesDecorator(SeleniumHelper aHelper) {
        this(aHelper, null);
    }

    /**
     * Creates new, working inside the aHelper's current (i)frame.
     * @param anIsFinishedFunction function to indicate when visiting (i)frames should be stopped.
     */
    public AllFramesDecorator(SeleniumHelper aHelper,
                              Function<T, Boolean> anIsFinishedFunction) {
        helper = aHelper;
        isFinished = anIsFinishedFunction;
    }

    /**
     * @param aSupplier supplier to provide a value per (i)frame
     * @return supplier's value for (i)frame where finished function returned true, or null if it always returned false
     */
    public T apply(Supplier<T> aSupplier) {
        T result = aSupplier.get();
        if (!isFinished(result)) {
            frameDepthOnStart = helper.getCurrentFrameDepth();
            result = invokeInFrames(aSupplier);
        }
        return result;
    }

    private T invokeInFrames(Supplier<T> aSupplier) {
        T result = null;
        List<WebElement> frames = helper.findElements(ConstantBy.frames());
        for (WebElement frame : frames) {
            SearchContext currentContext = helper.getCurrentContext();
            if (currentContext == helper.driver()) {
                currentContext = null;
            }
            helper.switchToFrame(frame);
            try {
                result = aSupplier.get();
                if (isFinished(result)) {
                    break;
                } else {
                    result = invokeInFrames(aSupplier);
                    if (isFinished(result)) {
                        break;
                    }
                }
            } catch (WebDriverException e) {
                String msg = e.getMessage();
                if (msg == null || !msg.contains("target frame detached")) {
                    // target frame detached is a non-standard chrome error
                    // we ignore those and just continue to check other frames
                    throw e;
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
        return isFinished != null && isFinished.apply(result);
    }
}
