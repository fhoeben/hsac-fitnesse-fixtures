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
 */
public class FirstElementBy extends SingleElementOrNullBy {
    private final List<By> byList;
    private Function<WebElement, WebElement> postProcessor;

    public FirstElementBy(Function<WebElement, WebElement> postProcessor, By firstBy, By... bys) {
        int size = 1;
        if (bys != null) {
            size += bys.length;
        }
        byList = new ArrayList<>(size);
        byList.add(firstBy);
        Collections.addAll(byList, bys);
        setPostProcessor(postProcessor);
    }

    public FirstElementBy(By firstBy, By... bys) {
        this(Function.identity(), firstBy, bys);
    }

    @Override
    public WebElement findElement(SearchContext context) {
        return firstNonNull(by -> postProcessor.apply(getWebElement(by, context)), byList);
    }

    protected WebElement getWebElement(By by, SearchContext context) {
        WebElement byResult;
        if (by instanceof SingleElementOrNullBy) {
            byResult = by.findElement(context);
        } else {
            byResult = BestMatchBy.findElement(by, context);
        }
        return byResult;
    }

    /**
     * Sets post processor to apply to each function's/By's result.
     * This function should return null when element should not be returned.
     * @param postProcessor function to apply to each element found to determine whether it should be returned.
     */
    public void setPostProcessor(Function<WebElement, WebElement> postProcessor) {
        this.postProcessor = postProcessor;
    }

    /**
     * @return post processor in use.
     */
    public Function<WebElement, WebElement> getPostProcessor() {
        return postProcessor;
    }

    @Override
    public String toString() {
        return getByName(getClass()) + byList;
    }

    public static String getByName(Class<?> c) {
        String name = c.getSimpleName();
        Class<?> enclosingClass = c.getEnclosingClass();
        if (enclosingClass != null) {
            name = enclosingClass.getSimpleName() + "." + name;
        }
        return name;
    }
}
