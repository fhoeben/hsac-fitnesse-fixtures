package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.By;

/**
 * Finds elements based on their title (aka tooltip).
 */
public class TitleBy {
    /**
     * @param text exact title value.
     * @return by returning element with supplied title attribute.
     */
    public static By exact(String text) {
        return new CssBy("[title='%s']", text);
    }

    /**
     * @param text partial title value.
     * @return by returning element whose title attribute contains supplied text.
     */
    public static By partial(String text) {
        return new CssBy("[title*='%s']", text);
    }
}
