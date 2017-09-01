package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.By;

/**
 * Finds containers, i.e. named elements which contain a group of other elements.
 */
public class ContainerBy {
    /**
     * By to find container using heuristics.
     * @param containerName name of container.
     * @return By finding containers matching the name.
     */
    public static By heuristic(String containerName) {
        return new Heuristic(containerName);
    }

    public static class Heuristic extends HeuristicBy {
        public Heuristic(String container) {
            super(new XPathBy(".//fieldset[.//legend/text()[normalized(.) = '%s']]", container),
                    AriaLabelBy.exact(container),
                    new XPathBy(".//fieldset[.//legend/text()[contains(normalized(.), '%s')]]", container),
                    AriaLabelBy.partial(container));
        }
    }
}
