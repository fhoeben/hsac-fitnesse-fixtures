package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.By;

/**
 * Container for generic heuristics to find elements.
 */
public class ElementBy {
    public static By heuristic(String place) {
        return new Heuristic(place);
    }

    public static By exact(String place) {
        return new Exact(place);
    }

    public static By partial(String place) {
        return new Partial(place);
    }

    /**
     * Finds using heuristic.
     */
    public static class Heuristic extends AbstractHeuristicBy {
        public Heuristic(String place) {
            super(exact(place), partial(place));
        }
    }

    /**
     * Finds by exact link text.
     */
    public static class Exact extends AbstractHeuristicBy {
        public Exact(String place) {
            super(LabelBy.exact(place),
                    PlaceholderBy.exact(place),
                    InputBy.exactValue(place),
                    new XPathBy(".//th/descendant-or-self::text()[normalized(.)='%s']/ancestor-or-self::th[1]/../td", place),
                    new XPathBy(".//dt/descendant-or-self::text()[normalized(.)='%s']/ancestor-or-self::dt[1]/following-sibling::dd[1]", place),
                    AriaLabelBy.exact(place),
                    new CssBy("[title='%s']", place));
        }
    }

    /**
     * Finds by partial link text.
     */
    public static class Partial extends AbstractHeuristicBy {
        public Partial(String place) {
            super(LabelBy.partial(place),
                    PlaceholderBy.partial(place),
                    InputBy.partialValue(place),
                    new XPathBy(".//th/descendant-or-self::text()[contains(normalized(.), '%s')]/ancestor-or-self::th[1]/../td", place),
                    new XPathBy(".//dt/descendant-or-self::text()[contains(normalized(.), '%s')]/ancestor-or-self::dt[1]/following-sibling::dd[1]", place),
                    AriaLabelBy.partial(place),
                    new CssBy("[title*='%s']", place));
        }
    }
}
