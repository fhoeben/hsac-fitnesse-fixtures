package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.By;

/**
 * Container for Bys working on aria labels.
 */
public class AriaLabelBy {
    public static By exact(String text) {
        return new AriaLabelBy.Exact(text);
    }

    public static By partial(String partialText) {
        return new AriaLabelBy.Partial(partialText);
    }

    /**
     * Finds by exact label text.
     */
    public static class Exact extends AbstractHeuristicBy {
        public Exact(String text) {
            super(new XPathBy(".//*[@aria-labelledby and @aria-labelledby=//*[@id]/descendant-or-self::text()[normalized(.) = '%s']/ancestor-or-self::*[@id]/@id]", text),
                    new CssBy("[aria-label='%s']", text));
        }
    }

    /**
     * Finds by partial label text.
     */
    public static class Partial extends AbstractHeuristicBy {
        public Partial(String partialText) {
            super(new XPathBy(".//*[@aria-labelledby and @aria-labelledby=//*[@id]/descendant-or-self::text()[contains(normalized(.), '%s')]/ancestor-or-self::*[@id]/@id]", partialText),
                    new CssBy("[aria-label*='%s']", partialText));
        }
    }

}
