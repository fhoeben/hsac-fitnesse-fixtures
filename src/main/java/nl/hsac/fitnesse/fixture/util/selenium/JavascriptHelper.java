package nl.hsac.fitnesse.fixture.util.selenium;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.internal.WrapsDriver;

/**
 * Helper dealing with executing Javascript via Selenium.
 */
public class JavascriptHelper {
    /**
     * Obtains executor based on Selenium context.
     * @param searchContext context in which javascript should be executed.
     * @return JavascriptExecutor based on search context.
     * @throws IllegalArgumentException when no executor could be obtained from context.
     */
    public static JavascriptExecutor getJavascriptExecutor(SearchContext searchContext) {
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
            throw new IllegalArgumentException("Unable to get: " + JavascriptExecutor.class.getName()
                    + " from: " + searchContext.getClass().getName());
        }
        return executor;
    }

    /**
     * Executes Javascript in browser. If script contains the magic variable 'arguments'
     * the parameters will also be passed to the statement. In the latter case the parameters
     * must be a number, a boolean, a String, WebElement, or a List of any combination of the above.
     * @link http://selenium.googlecode.com/git/docs/api/java/org/openqa/selenium/JavascriptExecutor.html#executeScript(java.lang.String,%20java.lang.Object...)
     * @param script javascript to run.
     * @param parameters parameters for the script.
     * @return return value from statement.
     */
    public static Object executeScript(JavascriptExecutor jse, String script, Object... parameters) {
        Object result;
        try {
            result = jse.executeScript(script, parameters);
        } catch (WebDriverException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("Detected a page unload event; script execution does not work across page loads.")) {
                // page reloaded while script ran, retry it once
                result = jse.executeScript(script, parameters);
            } else {
                throw e;
            }
        }
        return result;
    }

    /**
     * Executes Javascript in browser and then waits for 'callback' to be invoked.
     * If statementPattern should reference the magic (function) variable 'callback' which should be
     * called to provide this method's result.
     * If the statementPattern contains the magic variable 'arguments'
     * the parameters will also be passed to the statement. In the latter case the parameters
     * must be a number, a boolean, a String, WebElement, or a List of any combination of the above.
     * @link http://selenium.googlecode.com/git/docs/api/java/org/openqa/selenium/JavascriptExecutor.html#executeAsyncScript(java.lang.String,%20java.lang.Object...)
     * @param jse executor to use.
     * @param statementPattern javascript to run, possibly with placeholders to be replaced.
     * @param parameters placeholder values that should be replaced before executing the script.
     * @return return value from statement.
     */
    public static Object waitForJavascriptCallback(JavascriptExecutor jse, String statementPattern, Object... parameters) {
        Object result;
        String script = "var callback = arguments[arguments.length - 1];"
                + String.format(statementPattern, parameters);
        if (statementPattern.contains("arguments")) {
            result = jse.executeAsyncScript(script, parameters);
        } else {
            result = jse.executeAsyncScript(script);
        }
        return result;
    }
}
