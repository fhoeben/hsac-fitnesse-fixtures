package nl.hsac.fitnesse.fixture.util.selenium.by.grid;

import nl.hsac.fitnesse.fixture.util.selenium.by.SingleElementOrNullBy;
import nl.hsac.fitnesse.fixture.util.selenium.by.ValueOfBy;
import nl.hsac.fitnesse.fixture.util.selenium.by.XPathBy;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import static nl.hsac.fitnesse.fixture.util.selenium.by.GridBy.getXPathForColumnInRowByValueInOtherColumn;
import static nl.hsac.fitnesse.fixture.util.selenium.by.GridBy.getXPathForColumnIndex;
import static nl.hsac.fitnesse.fixture.util.selenium.by.GridBy.getXPathForHeaderRowByHeaders;

/**
 * Finds elements to get value from in Grid.
 */
public abstract class Value extends SingleElementOrNullBy {
    public static class AtCoordinates extends Value {
        private final int columnIndex;
        private final int rowIndex;

        public AtCoordinates(int columnIndex, int rowIndex) {
            this.columnIndex = columnIndex;
            this.rowIndex = rowIndex;
        }

        @Override
        public WebElement findElement(SearchContext context) {
            String row = Integer.toString(rowIndex);
            String column = Integer.toString(columnIndex);
            return getValueByXPath(context, "(.//tr[boolean(td)])[%s]/td[%s]", row, column);
        }
    }

    public static class OfInRowNumber extends Value {
        private final String requestedColumnName;
        private final int rowIndex;

        public OfInRowNumber(String requestedColumnName, int rowIndex) {
            this.requestedColumnName = requestedColumnName;
            this.rowIndex = rowIndex;
        }

        @Override
        public WebElement findElement(SearchContext context) {
            String headerXPath = getXPathForHeaderRowByHeaders(requestedColumnName);
            String columnXPath = String.format("((.//table[./%1$s])[last()]//tr[boolean(td)])[%2$s]/td", headerXPath, rowIndex);
            return valueInRow(context, columnXPath, requestedColumnName);
        }
    }

    public static class OfInRowWhereIs extends Value {
        private final String requestedColumnName;
        private final String selectOnColumn;
        private final String selectOnValue;

        public OfInRowWhereIs(String requestedColumnName, String selectOnValue, String selectOnColumn) {
            this.requestedColumnName = requestedColumnName;
            this.selectOnColumn = selectOnColumn;
            this.selectOnValue = selectOnValue;
        }

        @Override
        public WebElement findElement(SearchContext context) {
            String columnXPath = getXPathForColumnInRowByValueInOtherColumn(selectOnValue, selectOnColumn, requestedColumnName);
            return valueInRow(context, columnXPath, requestedColumnName);
        }
    }

    protected WebElement valueInRow(SearchContext context, String columnXPath, String requestedColumnName) {
        String requestedIndex = getXPathForColumnIndex(requestedColumnName);
        return getValueByXPath(context, "%s[%s]", columnXPath, requestedIndex);
    }

    protected WebElement getValueByXPath(SearchContext context, String xpathPattern, String... params) {
        By xPathBy = new XPathBy(xpathPattern, params);
        return new ValueOfBy(xPathBy).findElement(context);
    }
}
