package nl.hsac.fitnesse.fixture.util.selenium.by.grid;

import nl.hsac.fitnesse.fixture.util.selenium.by.SingleElementOrNullBy;
import nl.hsac.fitnesse.fixture.util.selenium.by.ValueOfBy;
import nl.hsac.fitnesse.fixture.util.selenium.by.XPathBy;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import static nl.hsac.fitnesse.fixture.util.selenium.by.GridBy.getXPathForColumnIndex;
import static nl.hsac.fitnesse.fixture.util.selenium.by.GridBy.getXPathForHeaderRowByHeaders;
import static nl.hsac.fitnesse.fixture.util.selenium.by.GridBy.getXPathForRowByValueInOtherColumn;

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
            String columnXPath = String.format("((.//table[.//tr/th/descendant-or-self::text()[normalized(.)='%s']])[last()]//tr[boolean(td)])[%s]/td", requestedColumnName, rowIndex);
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
            String columnXPath = getXPathForColumnInRowByValueInOtherColumn(requestedColumnName, selectOnColumn, selectOnValue);
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

    /**
     * Creates an XPath expression that will find a cell in a row, selecting the row based on the
     * text in a specific column (identified by its header text).
     * @param extraColumnName name of other header text that must be present in table's header row
     * @param columnName header text of the column to find value in.
     * @param value text to find in column with the supplied header.
     * @return XPath expression selecting a td in the row
     */
    public static String getXPathForColumnInRowByValueInOtherColumn(String extraColumnName, String columnName, String value) {
        String selectIndex = getXPathForColumnIndex(columnName);
        String rowXPath = getXPathForRowByValueInOtherColumn(selectIndex, value);
        String headerRowXPath = getXPathForHeaderRowByHeaders(columnName, extraColumnName);

        return String.format("(.//table[./%1$s and ./%2$s])[last()]/%2$s/td",
                headerRowXPath, rowXPath);
    }
}
