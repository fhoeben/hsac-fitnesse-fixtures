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
}
