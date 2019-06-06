package nl.hsac.fitnesse.fixture.util.selenium.by.grid;

import nl.hsac.fitnesse.fixture.util.selenium.by.GridBy;
import nl.hsac.fitnesse.fixture.util.selenium.by.HeuristicBy;
import nl.hsac.fitnesse.fixture.util.selenium.by.XPathBy;

/**
 * Finds Link inside a grid.
 */
public class Link extends HeuristicBy {
    protected Link(String place, String cellXPath) {
        super(new XPathBy("%s//a[descendant-or-self::text()[contains(normalized(.),'%s')]]", cellXPath, place),
                new XPathBy("%s[%s]//a", cellXPath, GridBy.getXPathForColumnIndex(place)),
                new XPathBy("%s//a[contains(@title, '%s')]", cellXPath, place));
    }

    public static class InRow extends Link {
        public InRow(String place, int rowIndex) {
            super(place, String.format("(.//tr[boolean(td)])[%s]/td", rowIndex));
        }

    }

    public static class InRowWhereIs extends Link {
        public InRowWhereIs(String place, String selectOnValue, String selectOnColumn) {
            super(place, GridBy.getXPathForColumnInRowByValueInOtherColumn(selectOnValue, selectOnColumn));
        }
    }
}
