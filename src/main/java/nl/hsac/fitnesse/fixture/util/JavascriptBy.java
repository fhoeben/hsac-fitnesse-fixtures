package nl.hsac.fitnesse.fixture.util;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.WrapsDriver;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Selenium By that finds elements by executing Javascript functions.
 */
public class JavascriptBy extends By {
    private final String rootElement;
    private final String script;
    private final Object[] scriptParameters;

    /**
     * Creates new, where 'root element' will be <code>null</code>.
     * @param aScript script to find with.
     * @param parameters additional (after 'root element') arguments to the script.
     */
    public JavascriptBy(String aScript, Object... parameters) {
        this(null, aScript, parameters);
    }

    /**
     * Creates new.
     * @param aRootElement first argument to the script.
     * @param aScript script to find with.
     * @param parameters additional (after 'root element') arguments to the script.
     */
    public JavascriptBy(String aRootElement, String aScript, Object... parameters) {
        rootElement = aRootElement;
        script = aScript;
        List<Object> params = Arrays.asList(parameters);
        params.add(0, rootElement);
        scriptParameters = params.toArray();
    }

    @Override
    public List<WebElement> findElements(SearchContext searchContext) {
        List<WebElement> result;
        JavascriptExecutor executor = null;
        if (searchContext instanceof JavascriptExecutor) {
            executor = (JavascriptExecutor) searchContext;
        } else {
            if (searchContext instanceof WrapsDriver) {
                WrapsDriver wraps = (WrapsDriver) searchContext;
                WebDriver wrapped = wraps.getWrappedDriver();
                if (wrapped instanceof JavascriptExecutor) {
                    executor = (JavascriptExecutor) wrapped;
                }
            }
        }
        if (executor == null) {
            throw new RuntimeException("Unable to get: " + JavascriptExecutor.class.getName()
                    + " from: " + searchContext.getClass().getName());
        }
        Object results = executor.executeScript(script, scriptParameters);
        if (results instanceof List) {
            result = (List<WebElement>) results;
        } else {
            if (results == null) {
                result = Collections.emptyList();
            } else {
                throw new RuntimeException("Script returned something else than a list: " + results);
            }
        }
        return result;
    }
}
