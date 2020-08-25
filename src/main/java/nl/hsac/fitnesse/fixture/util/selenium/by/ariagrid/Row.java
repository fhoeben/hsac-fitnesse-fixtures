package nl.hsac.fitnesse.fixture.util.selenium.by.ariagrid;

import nl.hsac.fitnesse.fixture.util.selenium.by.AriaGridBy;
import nl.hsac.fitnesse.fixture.util.selenium.by.XPathBy;

/**
 * Finds a row in a grid.
 */
public class Row {
    public static class InNumber extends XPathBy {
        public InNumber(int rowIndex) {
            super("(.//div[@role='row'][boolean(span[@role='cell'])])[%s]", Integer.toString(rowIndex));
        }
    }

    public static class WhereIs extends XPathBy {
        public WhereIs(String selectOnValue, String selectOnColumn) {
            super(AriaGridBy.getXPathForColumnInRowByValueInOtherColumn(selectOnValue, selectOnColumn) + "/..");
        }
    }
}