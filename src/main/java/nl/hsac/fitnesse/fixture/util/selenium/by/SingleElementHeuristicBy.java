package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

import static nl.hsac.fitnesse.fixture.util.FirstNonNullHelper.firstNonNull;

/**
 * By which returns the first non-null result of a sequence of element functions.
 */
public class SingleElementHeuristicBy extends SingleElementOrNullBy {
    private final Function<SearchContext, WebElement>[] functions;

    public SingleElementHeuristicBy(Function<SearchContext, WebElement>... functions) {
        this.functions = functions;
    }

    public SingleElementHeuristicBy(By... bys) {
        this(Stream.of(bys).map(SingleElementOrNullBy::byToFunction).toArray(Function[]::new));
    }

    @Override
    public WebElement findElement(SearchContext context) {
        return firstNonNull(c -> c.apply(context), functions);
    }

    @Override
    public String toString() {
        return "SingleElementHeuristicBy[" + Arrays.toString(functions) + "]";
    }
}
