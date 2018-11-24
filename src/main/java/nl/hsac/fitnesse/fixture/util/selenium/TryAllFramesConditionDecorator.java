package nl.hsac.fitnesse.fixture.util.selenium;

import org.openqa.selenium.support.ui.ExpectedCondition;

/**
 * Adds a decorator on top of a decorator such that it is applied to all frames and iframes nested
 * inside the current page (or active iframe).
 * @param <T> type of condition result.
 */
public class TryAllFramesConditionDecorator<T> extends AllFramesDecorator<T> implements ExpectedCondition<T> {

    /**
     * Creates new, working inside the aHelper's current (i)frame.
     * @param aHelper selenium helper to get frames from
     * @param toBeDecorated condition to be applied for each (i)frame.
     */
    public TryAllFramesConditionDecorator(SeleniumHelper aHelper, ExpectedCondition<T> toBeDecorated) {
        super(aHelper, toBeDecorated,
                result -> result != null && !Boolean.FALSE.equals(result));
    }
}
