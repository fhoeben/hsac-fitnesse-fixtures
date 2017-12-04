package nl.hsac.fitnesse.fixture.util.selenium.by.grid;

import nl.hsac.fitnesse.fixture.util.selenium.by.GridBy;
import nl.hsac.fitnesse.fixture.util.selenium.by.XPathBy;

/**
 * Finds a row in a grid.
 */
public class Row {
    public static class InNumber extends XPathBy {
        public InNumber(int rowIndex) {
            super("(.//tr[boolean(td)])[%s]", Integer.toString(rowIndex));
        }
    }

    public static class WhereIs extends XPathBy {
        public WhereIs(String selectOnValue, String selectOnColumn) {
            super(GridBy.getXPathForColumnInRowByValueInOtherColumn(selectOnValue, selectOnColumn) + "/..");
        }
    }
}
