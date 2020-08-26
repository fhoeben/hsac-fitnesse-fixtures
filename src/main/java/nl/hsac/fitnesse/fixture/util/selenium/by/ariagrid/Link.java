package nl.hsac.fitnesse.fixture.util.selenium.by.ariagrid;

import nl.hsac.fitnesse.fixture.util.selenium.by.AriaGridBy;
import nl.hsac.fitnesse.fixture.util.selenium.by.HeuristicBy;
import nl.hsac.fitnesse.fixture.util.selenium.by.XPathBy;

public class Link extends HeuristicBy {
    protected Link(String place, String cellXPath) {
        super(new XPathBy("%s//a[descendant-or-self::text()[contains(normalized(.),'%s')]]", cellXPath, place),
                new XPathBy("%s[%s]//a", cellXPath, AriaGridBy.getXPathForColumnIndex(place)),
                new XPathBy("%s//a[contains(@title, '%s')]", cellXPath, place));
    }

    public static class InRow extends Link {
        public InRow(String place, int rowIndex) {
            super(place, String.format("(.//div[@role='row'][boolean(span[@role='cell'])])[%s]/span[@role='cell']", rowIndex));
        }

    }

    public static class InRowWhereIs extends Link {
        public InRowWhereIs(String place, String selectOnValue, String selectOnColumn) {
            super(place, AriaGridBy.getXPathForColumnInRowByValueInOtherColumn(selectOnValue, selectOnColumn));
        }
    }
}
