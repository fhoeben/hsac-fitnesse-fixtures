package nl.hsac.fitnesse.fixture.util.selenium.by;

import nl.hsac.fitnesse.fixture.util.selenium.JavascriptHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

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
        JavascriptExecutor executor = JavascriptHelper.getJavascriptExecutor(searchContext);
        Object results = JavascriptHelper.executeScript(executor, script, scriptParameters);
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
