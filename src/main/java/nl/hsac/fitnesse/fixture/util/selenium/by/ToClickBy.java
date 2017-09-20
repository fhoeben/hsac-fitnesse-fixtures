package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.By;

/**
 * Finds elements to click.
 */
public class ToClickBy {
    public static By heuristic(String place) {
        return new Heuristic(place);
    }

    public static class Heuristic extends HeuristicBy {
        public Heuristic(String place) {
            super(LinkBy.exactText(place),
                    new XPathBy(".//button[descendant-or-self::text()[normalized(.)='%s']]", place),
                    new XPathBy(".//label[descendant-or-self::text()[normalized(.)='%s']]", place),
                    new XPathBy(".//summary[descendant-or-self::text()[normalized(.)='%s']]", place),
                    ElementBy.exact(place),
                    ConstantBy.submitOrReset(place),
                    LinkBy.partialText(place),
                    new XPathBy(".//button[descendant-or-self::text()[contains(normalized(.), '%s')]]", place),
                    new XPathBy(".//label[descendant-or-self::text()[contains(normalized(.), '%s')]]", place),
                    new XPathBy(".//summary[descendant-or-self::text()[contains(normalized(.), '%s')]]", place),
                    ElementBy.partial(place),
                    new XPathBy(".//text()[@onclick and normalized(.)='%s']/..", place),
                    new XPathBy(".//text()[@onclick and contains(normalized(.),'%s')]/..", place),
                    TextBy.exact(place),
                    TextBy.partial(place));
        }
    }
}
