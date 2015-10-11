package nl.hsac.fitnesse.fixture.util.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.WrapsDriver;

import java.util.Collections;
import java.util.List;

/**
 * Selenium By that finds elements by executing a Javascript function.
 */
public class JavascriptBy extends By {
    private final String script;
    private final Object[] scriptParameters;

    /**
     * Creates new.
     * @param aScript script to find with.
     * @param parameters arguments to the script.
     */
    public JavascriptBy(String aScript, Object... parameters) {
        script = aScript;
        scriptParameters = parameters;
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
