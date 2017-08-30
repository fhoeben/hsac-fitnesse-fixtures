package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.By;

/**
 * By using CSS selectors.
 */
public class CssBy extends By.ByCssSelector {
    /**
     * Creates By based on CSS selector, supporting placeholder replacement.
     * @param pattern basic CSS selector, possibly with placeholders.
     * @param parameters values for placeholders.
     * @return ByCssSelector.
     */
    public CssBy(String pattern, String... parameters) {
        super(XPathBy.fillPattern(pattern, parameters));
    }
}
