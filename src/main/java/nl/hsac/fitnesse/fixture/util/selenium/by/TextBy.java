package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.By;

/**
 * Container for Bys searching for elements by their text content.
 */
public class TextBy {
    public static By exact(String text) {
        return new XPathBy(".//text()[normalized(.)='%s']/..", text);
    }

    public static By partial(String text) {
        return new XPathBy(".//text()[contains(normalized(.),'%s')]/..", text);
    }
}
