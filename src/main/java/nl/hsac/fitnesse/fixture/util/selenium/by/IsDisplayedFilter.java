package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.WebElement;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Function to be used as post-processor when finding elements.
 * It will filter out non-visible elements.
 * @param <T> type of element to return.
 */
public class IsDisplayedFilter<T extends WebElement> implements Function<T, T>, Supplier<T> {
    private T firstFound;

    /**
     * Filters out non-displayed elements.
     * @param webElement element to check.
     * @return webElement if it is displayed, null otherwise.
     */
    @Override
    public T apply(T webElement) {
        if (firstFound == null) {
            firstFound = webElement;
        }
        return mayPass(webElement) ? webElement : null;
    }

    /**
     * @return first non-null element encountered by filter (may or may not be displayed);
     */
    @Override
    public T get() {
        return firstFound;
    }

    /**
     * Checks whether element is displayed.
     * @param element element to check.
     * @return true for visible elements, false otherwise.
     */
    public static boolean mayPass(WebElement element) {
        return element != null && element.isDisplayed();
    }
}
