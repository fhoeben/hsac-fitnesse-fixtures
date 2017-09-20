package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

public class LabelBy extends SingleElementOrNullBy {
    public static By exact(String text) {
        return new Exact(text);
    }

    public static By partial(String partialText) {
        return new Partial(partialText);
    }

    /**
     * Finds by exact label text.
     */
    public static class Exact extends LabelBy {
        public Exact(String text) {
            super(".//label[descendant-or-self::text()[normalized(.)='%s']]", text);
        }
    }

    /**
     * Finds by partial label text.
     */
    public static class Partial extends LabelBy {
        public Partial(String partialText) {
            super(".//label[descendant-or-self::text()[contains(normalized(.), '%s')]]", partialText);
        }
    }

    private final By by;
    private final String text;

    protected LabelBy(String pattern, String textToFind) {
        by = new XPathBy(pattern, textToFind);
        text = textToFind;
    }

    @Override
    public WebElement findElement(SearchContext context) {
        WebElement label = BestMatchBy.findElement(by, context);
        WebElement element = getLabelledElement(context, label);
        return element;
    }

    @Override
    public String toString() {
        return super.toString() + ": " + text;
    }

    public static WebElement getLabelledElement(SearchContext context, WebElement label) {
        WebElement element = null;
        if (label != null) {
            String forAttr = label.getAttribute("for");
            if (forAttr == null || "".equals(forAttr)) {
                element = ConstantBy.nestedElementForValue().findElement(label);
            } else {
                element = BestMatchBy.findElement(By.id(forAttr), context);
            }
        }
        return element;
    }
}
