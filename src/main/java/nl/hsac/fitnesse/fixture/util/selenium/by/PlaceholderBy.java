package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.By;

/**
 * Finds elements by their placeholder.
 */
public class PlaceholderBy {
    /**
     * @param text exact placeholder value.
     * @return by returning input or textarea with requested text as placeholder.
     */
    public static By exact(String text) {
        return new CssBy("input[placeholder='%1$s'],textarea[placeholder='%1$s']", text);
    }

    /**
     * @param text partial placeholder value.
     * @return by returning input or textarea whose placeholder contains the requested text.
     */
    public static By partial(String text) {
        return new CssBy("input[placeholder*='%1$s'],textarea[placeholder*='%1$s']", text);
    }
}
