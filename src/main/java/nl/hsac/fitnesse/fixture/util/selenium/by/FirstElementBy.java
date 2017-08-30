package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

import static nl.hsac.fitnesse.fixture.util.FirstNonNullHelper.firstNonNull;

/**
 * By which returns the first non-null result of a sequence of element functions, or nested By clauses.
 */
public class FirstElementBy extends SingleElementOrNullBy {
    private final Function<SearchContext, WebElement>[] functions;
    private Function<WebElement, WebElement> postProcessor;

    public FirstElementBy(Function<WebElement, WebElement> postProcessor, Function<SearchContext, WebElement>... functions) {
        this.functions = functions;
        setPostProcessor(postProcessor);
    }

    public FirstElementBy(Function<SearchContext, WebElement>... functions) {
        this(Function.identity(), functions);
    }

    public FirstElementBy(By... bys) {
        this(Stream.of(bys).map(SingleElementOrNullBy::byToFunction).toArray(Function[]::new));
    }

    @Override
    public WebElement findElement(SearchContext context) {
        return firstNonNull(c -> postProcessor.apply(c.apply(context)), functions);
    }

    /**
     * Sets post processor to apply to each function's/By's result.
     * This function should return
     * @param postProcessor function to apply to each element found to determine whether it should be returned.
     */
    public void setPostProcessor(Function<WebElement, WebElement> postProcessor) {
        this.postProcessor = postProcessor;
    }

    @Override
    public String toString() {
        return "FirstElementBy[" + Arrays.toString(functions) + "]";
    }
}
