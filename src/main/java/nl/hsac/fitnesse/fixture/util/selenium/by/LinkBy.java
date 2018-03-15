package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

/**
 * Container for By implementations searching links.
 */
public class LinkBy {
    public static By heuristic(String place) {
        return new Heuristic(place);
    }

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
                    new XPathBy(".//a[descendant-or-self::text()[normalized(.)='%s']]", text));
        }
    }

    /**
     * Finds by partial link text.
     */
    public static class Partial extends HeuristicBy {
        public Partial(String partialText) {
            super(By.partialLinkText(partialText),
                    new XPathBy(".//a[descendant-or-self::text()[contains(normalized(.),'%s')]]", partialText));
        }
    }

    /**
     * Finds using heuristic.
     */
    public static class Heuristic extends HeuristicBy {
        private static final By FIND_PARENT_A_BY = By.xpath("./ancestor::a");

        public Heuristic(String place) {
            super(exactText(place),
                    AriaLabelBy.exact(place),
                    TitleBy.exact(place),
                    AltBy.exact(place),
                    partialText(place),
                    AriaLabelBy.partial(place),
                    TitleBy.partial(place),
                    AltBy.partial(place));
        }

        @Override
        public WebElement findElement(SearchContext context) {
            WebElement element = super.findElement(context);
            if (element != null && !"a".equalsIgnoreCase(element.getTagName())) {
                try {
                    element = FIND_PARENT_A_BY.findElement(element);
                } catch (NoSuchElementException e) {
                    // element we found is not in a link
                    element = null;
                }
            }
            return element;
        }
    }
}
