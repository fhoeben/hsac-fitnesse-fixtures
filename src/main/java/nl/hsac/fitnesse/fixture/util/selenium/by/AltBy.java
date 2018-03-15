package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.By;

/**
 * Finds elements based on their alternative text.
 */
public class AltBy {
    /**
     * @param text exact alt value.
     * @return by returning element with supplied alt attribute.
     */
    public static By exact(String text) {
        return new CssBy("[alt='%s']", text);
    }

    /**
     * @param text partial alt value.
     * @return by returning element whose alt attribute contains supplied text.
     */
    public static By partial(String text) {
        return new CssBy("[alt*='%s']", text);
    }
}
