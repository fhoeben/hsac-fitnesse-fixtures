package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static nl.hsac.fitnesse.fixture.util.FirstNonNullHelper.firstNonNull;

/**
 * By which returns the first non-null result of a sequence of nested By clauses.
 * If a nested By returns mutliple elements it uses {@link BestMatchBy#findElement(By, SearchContext)} to select
 * the element to use.
 *
 * @param <T> type of element to return.
 */
public class FirstElementBy<T extends WebElement> extends SingleElementOrNullBy<T> {
    private final List<By> byList;
    private Function<? super T, ? extends T> postProcessor;

    public FirstElementBy(Function<? super T, ? extends T> postProcessor, By firstBy, By... bys) {
        int size = 1;
        if (bys != null) {
            size += bys.length;
        }
        byList = new ArrayList<>(size);
        byList.add(firstBy);
        Collections.addAll(byList, bys);
        // no need to keep by without result in list of options
        byList.removeIf(x -> ConstantBy.nothing() == x);
        setPostProcessor(postProcessor);
    }

    public FirstElementBy(By firstBy, By... bys) {
        this(Function.identity(), firstBy, bys);
    }

    @Override
    public T findElement(SearchContext context) {
        return firstNonNull(by -> postProcessor.apply(getWebElement(by, context)), byList);
    }

    public static <T extends WebElement> T getWebElement(By by, SearchContext context) {
        T byResult;
        if (by instanceof SingleElementOrNullBy) {
            byResult = (T) by.findElement(context);
        } else {
            byResult = BestMatchBy.findElement(by, context);
        }
        return byResult;
    }

    /**
     * @return Bys nested inside.
     */
    public List<By> getByList() {
        return byList;
    }

    /**
     * Sets post processor to apply to each function's/By's result.
     * This function should return null when element should not be returned.
     *
     * @param postProcessor function to apply to each element found to determine whether it should be returned.
     */
    public void setPostProcessor(Function<? super T, ? extends T> postProcessor) {
        this.postProcessor = postProcessor;
    }

    /**
     * @return post processor in use.
     */
    public Function<? super T, ? extends T> getPostProcessor() {
        return postProcessor;
    }

    @Override
    public String toString() {
        return getByName(getClass()) + byList;
    }
}
