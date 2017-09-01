package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

public class LabelBy extends SingleElementOrNullBy {
    public static By exact(String text) {
        return new LabelBy.Exact(text);
    }

    public static By partial(String partialText) {
        return new LabelBy.Partial(partialText);
    }

    /**
     * Finds by exact label text.
     */
    public static class Exact extends LabelBy {
        public Exact(String text) {
            super(".//label/descendant-or-self::text()[normalized(.)='%s']/ancestor-or-self::label", text);
        }
    }

    /**
     * Finds by partial label text.
     */
    public static class Partial extends LabelBy {
        public Partial(String partialText) {
            super(".//label/descendant-or-self::text()[contains(normalized(.), '%s')]/ancestor-or-self::label", partialText);
        }
    }

    private final SingleElementOrNullBy by;

    protected LabelBy(String xpath, String... parameters) {
        this(createXPathBy(xpath, parameters));
    }

    protected LabelBy(SingleElementOrNullBy by) {
        this.by = by;
    }

    @Override
    public WebElement findElement(SearchContext context) {
        WebElement label = by.findElement(context);
        WebElement element = getLabelledElement(label);
        return element;
    }

    public static WebElement getLabelledElement(WebElement label) {
        WebElement element = null;
        if (label != null) {
            String forAttr = label.getAttribute("for");
            if (forAttr == null || "".equals(forAttr)) {
                element = NestedElementForValueBy.INSTANCE.findElement(label);
            } else {
                // please not we DO NOT start xPath with '.' as we want to search entire document
                // and not just inside the label
                element = createXPathBy("//*[@id = '%s']", forAttr).findElement(label);
            }
        }
        return element;
    }

    private static BestMatchBy createXPathBy(String pattern, String... parameters) {
        return new BestMatchBy(new XPathBy(pattern, parameters));
    }
}
