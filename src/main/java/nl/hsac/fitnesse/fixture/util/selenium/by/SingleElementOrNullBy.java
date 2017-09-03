package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * By which returns either a single element or null.
 */
public abstract class SingleElementOrNullBy extends By {
    /**
     * Converts By to Function returning first result (or null).
     * @param by by to convert.
     * @return function returning first result of by.
     */
    public static Function<SearchContext, WebElement> byToFunction(By by) {
        Function<SearchContext, WebElement> function;
        if (by instanceof SingleElementOrNullBy) {
            // will not throw exception, but return null when no element is found
            function = by::findElement;
        } else {
            function = sc -> {
                // single element case will throw exception when no element is found
                List<WebElement> elements = by.findElements(sc);
                return (elements != null && !elements.isEmpty())? elements.get(0): null;
            };
        }
        return function;
    }

    /**
     * Returns element found in search context.
     * PLEASE NOTE: this implementation will NOT throw an exception when no element is found, but return null.
     * @param context context to find in.
     * @return element found, if any, null otherwise (unlike normal By which throws exception).
     */
    @Override
    public abstract WebElement findElement(SearchContext context);

    @Override
    public List<WebElement> findElements(SearchContext searchContext) {
        WebElement element = findElement(searchContext);
        return element == null? Collections.emptyList(): Collections.singletonList(element);
    }

    @Override
    public String toString() {
        return "SingleElementBy";
    }
}
