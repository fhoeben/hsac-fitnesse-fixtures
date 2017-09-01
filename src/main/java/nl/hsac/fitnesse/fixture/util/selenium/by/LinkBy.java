package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.By;
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

    /**
     * Finds using heuristic.
     */
    public static class Heuristic extends HeuristicBy {
        public Heuristic(String place) {
            super(exactText(place),
                    AriaLabelBy.exact(place),
                    new CssBy("[title='%s']", place),
                    partialText(place),
                    AriaLabelBy.partial(place),
                    new CssBy("[title*='%s']", place));
            setPostProcessor(new FindParentAAndCheckInteractableFilter());
        }

        private static class FindParentAAndCheckInteractableFilter extends IsInteractableFilter {
            private static final By findParentABy = By.xpath("./ancestor::a");

            @Override
            public WebElement apply(WebElement element) {
                if (element != null) {
                    if (!"a".equalsIgnoreCase(element.getTagName())) {
                        element = findParentABy.findElement(element);
                    }
                    return super.apply(element);
                }
                return null;
            }
        }
    }
}
