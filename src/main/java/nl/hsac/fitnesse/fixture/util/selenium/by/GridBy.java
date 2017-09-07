package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

/**
 * Custom By to deal with finding elements in a table representing a grid of values.
 */
public abstract class GridBy {
    public static SingleElementOrNullBy coordinates(int columnIndex, int rowIndex) {
        return new Value.AtCoordinates(columnIndex, rowIndex);
    }

    public static SingleElementOrNullBy columnInRow(String requestedColumnName, int rowIndex) {
        return new Value.OfInRowNumber(requestedColumnName, rowIndex);
    }

    public static SingleElementOrNullBy columnInRowWhereIs(String requestedColumnName, String selectOnColumn, String selectOnValue) {
        return new Value.OfInRowWhereIs(requestedColumnName, selectOnValue, selectOnColumn);
    }

    public static XPathBy rowNumber(int rowIndex) {
        return new Row.InNumber(rowIndex);
    }

    public static XPathBy rowWhereIs(String selectOnColumn, String selectOnValue) {
        return new Row.WhereIs(selectOnValue, selectOnColumn);
    }

    public static abstract class Value extends SingleElementOrNullBy {
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
    }

    public static abstract class Row {
        public static class InNumber extends XPathBy {
            public InNumber(int rowIndex) {
                super("(.//tr[boolean(td)])[%s]", Integer.toString(rowIndex));
            }
        }

        public static class WhereIs extends XPathBy {
            public WhereIs(String selectOnValue, String selectOnColumn) {
                super(getXPathForColumnInRowByValueInOtherColumn(selectOnColumn, selectOnValue) + "/..");
            }
        }
    }

    /**
     * Creates an XPath expression that will find a cell in a row, selecting the row based on the
     * text in a specific column (identified by its header text).
     * @param columnName header text of the column to find value in.
     * @param value text to find in column with the supplied header.
     * @return XPath expression selecting a td in the row
     */
    public static String getXPathForColumnInRowByValueInOtherColumn(String columnName, String value) {
        String selectIndex = getXPathForColumnIndex(columnName);
        return String.format("(.//table[.//tr/th/descendant-or-self::text()[normalized(.)='%3$s']])[last()]//tr[td[%1$s]/descendant-or-self::text()[normalized(.)='%2$s']]/td",
                selectIndex, value, columnName);
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
        return String.format("(.//table[.//tr[th/descendant-or-self::text()[normalized(.)='%3$s'] and th/descendant-or-self::text()[normalized(.)='%4$s']]])[last()]//tr[td[%1$s]/descendant-or-self::text()[normalized(.)='%2$s']]/td",
                selectIndex, value, columnName, extraColumnName);
    }

    /**
     * Creates an XPath expression that will determine, for a row, which index to use to select the cell in the column
     * with the supplied header text value.
     * @param columnName name of column in header (th)
     * @return XPath expression which can be used to select a td in a row
     */
    public static String getXPathForColumnIndex(String columnName) {
        // determine how many columns are before the column with the requested name
        // the column with the requested name will have an index of the value +1 (since XPath indexes are 1 based)
        return String.format("count(ancestor::table[1]//tr/th/descendant-or-self::text()[normalized(.)='%s']/ancestor-or-self::th[1]/preceding-sibling::th)+1", columnName);
    }
}
