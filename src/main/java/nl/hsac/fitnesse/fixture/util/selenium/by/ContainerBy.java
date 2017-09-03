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

    /**
     * By to find container by its full name.
     * @param containerName name of container.
     * @return By finding containers matching the name.
     */
    public static By exact(String containerName) {
        return new Exact(containerName);
    }

    /**
     * By to find container by its partial name.
     * @param containerName name of container.
     * @return By finding containers matching the name.
     */
    public static By partial(String containerName) {
        return new Partial(containerName);
    }

    public static class Heuristic extends HeuristicBy {
        public Heuristic(String container) {
            super(exact(container), partial(container));
        }
    }

    public static class Exact extends HeuristicBy {
        public Exact(String container) {
            super(new XPathBy(".//fieldset[.//legend/text()[normalized(.) = '%s']]", container),
                    new XPathBy(".//details[.//summary[.//text()[normalized(.) = '%s']]]", container),
                    AriaLabelBy.exact(container));
        }
    }

    public static class Partial extends HeuristicBy {
        public Partial(String container) {
            super(new XPathBy(".//fieldset[.//legend/text()[contains(normalized(.), '%s')]]", container),
                    new XPathBy(".//details[.//summary[.//text()[contains(normalized(.), '%s')]]]", container),
                    AriaLabelBy.partial(container));
        }
    }
}
