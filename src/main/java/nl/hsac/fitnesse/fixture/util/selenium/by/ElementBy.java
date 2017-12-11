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
    public static class Heuristic extends HeuristicBy {
        public Heuristic(String place) {
            super(exact(place), partial(place));
        }
    }

    /**
     * Finds elements exactly matching supplied place.
     */
    public static class Exact extends HeuristicBy {
        public Exact(String place) {
            super(LabelBy.exact(place),
                    PlaceholderBy.exact(place),
                    InputBy.exactValue(place),
                    ConstantBy.checkboxOrNothing(place),
                    new XPathBy(".//th[descendant-or-self::text()[normalized(.)='%s']]/../td", place),
                    new XPathBy(".//dt[descendant-or-self::text()[normalized(.)='%s']]/following-sibling::dd[1]", place),
                    AriaLabelBy.exact(place),
                    TitleBy.exact(place));
        }
    }

    /**
     * Finds elements also showing supplied place.
     */
    public static class Partial extends HeuristicBy {
        public Partial(String place) {
            super(LabelBy.partial(place),
                    PlaceholderBy.partial(place),
                    InputBy.partialValue(place),
                    new XPathBy(".//th[descendant-or-self::text()[contains(normalized(.), '%s')]]/../td", place),
                    new XPathBy(".//dt[descendant-or-self::text()[contains(normalized(.), '%s')]]/following-sibling::dd[1]", place),
                    AriaLabelBy.partial(place),
                    TitleBy.partial(place));
        }
    }
}
