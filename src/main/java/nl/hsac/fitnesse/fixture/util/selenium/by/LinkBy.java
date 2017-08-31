package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.By;

/**
 * Container for By implementations searching links.
 */
public class LinkBy {
    public static By exactText(String text) {
        return new Exact(text);
    }

    public static By partialText(String partialText) {
        return new Partial(partialText);
    }

    /**
     * Finds by exact link text.
     */
    public static class Exact extends HeuristicBy {
        public Exact(String text) {
            super(By.linkText(text),
                    new XPathBy(".//text()[normalized(.)='%s']/ancestor-or-self::a", text));
        }
    }

    /**
     * Finds by partial link text.
     */
    public static class Partial extends HeuristicBy {
        public Partial(String partialText) {
            super(By.partialLinkText(partialText),
                    new XPathBy(".//text()[contains(normalized(.),'%s')]/ancestor-or-self::a", partialText));
        }
    }
}
