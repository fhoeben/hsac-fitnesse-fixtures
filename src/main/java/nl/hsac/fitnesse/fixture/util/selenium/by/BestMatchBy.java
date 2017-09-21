package nl.hsac.fitnesse.fixture.util.selenium.by;

import nl.hsac.fitnesse.fixture.util.selenium.JavascriptHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Decorator for a By statement, always returning a single element, or <code>null</code>.
 * @param <T> type of element to return.
 */
public class BestMatchBy<T extends WebElement> extends SingleElementOrNullBy<T> {
    private static final String TOP_ELEMENT_AT =
            "if (arguments[0].getBoundingClientRect) {\n" +
                    "  var rect = arguments[0].getBoundingClientRect();\n" +
                    "  var x = (rect.left + rect.right)/2;\n" +
                    "  var y = (rect.top + rect.bottom)/2;\n" +
                    "  return document.elementFromPoint(x,y);\n" +
                    "} else { return null; }";
    private static BiFunction<SearchContext, List<WebElement>, ? extends WebElement> BEST_FUNCTION = BestMatchBy::selectBestElement;

    private final By by;

    public BestMatchBy(By nestedBy) {
        this.by = nestedBy;
    }

    @Override
    public T findElement(SearchContext context) {
        return findElement(by, context);
    }

    /**
     * Returns 'best' result from by.
     * If there is no result: returns null,
     * if there is just one that is best,
     * otherwise the 'bestFunction' is applied to all results to determine best.
     * @param by by to use to find elements.
     * @param context context to search in.
     * @param <T> type of element expected.
     * @return 'best' element, will be <code>null</code> if no elements were found.
     */
    public static <T extends WebElement> T findElement(By by, SearchContext context) {
        WebElement element = null;
        List<WebElement> elements = context.findElements(by);
        if (elements.size() == 1) {
            element = elements.get(0);
        } else if (elements.size() > 1) {
            element = BEST_FUNCTION.apply(context, elements);
        }
        return (T) element;
    }

    /**
     * Best element is selected as follows:
     * Take the first displayed element without any elements on top of it,
     * if none: take first displayed, or
     * if none are displayed: just take the first.
     * @param context context used to find the elements.
     * @param elements elements found, from which the best must be selected.
     * @return 'best' element from elements.
     */
    public static WebElement selectBestElement(SearchContext context, List<WebElement> elements) {
        JavascriptExecutor jse = JavascriptHelper.getJavascriptExecutor(context);
        WebElement element = elements.get(0);
        WebElement firstDisplayed = null;
        WebElement firstOnTop = null;
        if (!element.isDisplayed() || !isOnTop(jse, element)) {
            for (int i = 1; i < elements.size(); i++) {
                WebElement otherElement = elements.get(i);
                if (otherElement.isDisplayed()) {
                    if (firstDisplayed == null) {
                        firstDisplayed = otherElement;
                    }
                    if (isOnTop(jse, otherElement)) {
                        firstOnTop = otherElement;
                        element = otherElement;
                        break;
                    }
                }
            }
            if (firstOnTop == null
                    && firstDisplayed != null
                    && !element.isDisplayed()) {
                // none displayed and on top
                // first was not displayed, but another was
                element = firstDisplayed;
            }
        }
        return element;
    }

    private static <T extends WebElement> boolean isOnTop(JavascriptExecutor executor, T element) {
        T e = (T) JavascriptHelper.executeScript(executor, TOP_ELEMENT_AT, element);
        return element.equals(e);
    }

    /**
     * @return function used to select best element when multiple elements were found.
     */
    public static BiFunction<SearchContext, List<WebElement>, ? extends WebElement> getBestFunction() {
        return BEST_FUNCTION;
    }

    /**
     * @param bestFunction function to use to select best element from list of elements found.
     */
    public static void setBestFunction(BiFunction<SearchContext, List<WebElement>, ? extends WebElement> bestFunction) {
        BEST_FUNCTION = bestFunction;
    }

    @Override
    public String toString() {
        return "BestMatchOf: " + by;
    }
}
