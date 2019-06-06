package nl.hsac.fitnesse.fixture.util.selenium.by;

import nl.hsac.fitnesse.fixture.util.selenium.by.grid.Link;
import nl.hsac.fitnesse.fixture.util.selenium.by.grid.Row;
import nl.hsac.fitnesse.fixture.util.selenium.by.grid.Value;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Factory for custom Bys to deal with finding elements in a table representing a grid of values.
 */
public class GridBy {
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

    public static SingleElementOrNullBy linkInRow(String place, int rowIndex) {
        return new Link.InRow(place, rowIndex);
    }

    public static SingleElementOrNullBy linkInRowWhereIs(String place, String selectOnColumn, String selectOnValue) {
        return new Link.InRowWhereIs(place, selectOnValue, selectOnColumn);
    }

    /**
     * Creates an XPath expression that will find a cell in a row, selecting the row based on the
     * text in a specific column (identified by its header text).
     * @param value text to find in column with the supplied header.
     * @param columnName header text of the column to find value in.
     * @param extraColumnNames name of other header texts that must be present in table's header row
     * @return XPath expression selecting a td in the row
     */
    public static String getXPathForColumnInRowByValueInOtherColumn(String value, String columnName, String... extraColumnNames) {
        String selectIndex = getXPathForColumnIndex(columnName);
        String rowXPath = getXPathForRowByValueInOtherColumn(selectIndex, value);
        String headerXPath = getXPathForHeaderRowByHeaders(columnName, extraColumnNames);
        return String.format("(.//table[./%1$s and ./%2$s])[last()]/%2$s/td",
                headerXPath, rowXPath);
    }

    /**
     * Creates an XPath expression-segment that will find a row, selecting the row based on the
     * text in a specific column.
     * @param selectIndex index of the column to find value in (usually obtained via {@link #getXPathForColumnIndex(String)}).
     * @param value text to find in the column.
     * @return XPath expression selecting a tr in the table.
     */
    public static String getXPathForRowByValueInOtherColumn(String selectIndex, String value) {
        return String.format("/tr[td[%1$s]/descendant-or-self::text()[normalized(.)='%2$s']]", selectIndex, value);
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
        String headerXPath = getXPathForHeaderCellWithText(columnName);
        return String.format("count(ancestor::table[1]//tr/%1$s/preceding-sibling::th)+1", headerXPath);
    }

    /**
     * Creates an XPath expression that will find a header row, selecting the row based on the
     * header texts present.
     * @param columnName first header text which must be present.
     * @param extraColumnNames name of other header texts that must be present in table's header row.
     * @return XPath expression selecting a tr in the row
     */
    public static String getXPathForHeaderRowByHeaders(String columnName, String... extraColumnNames) {
        String allHeadersPresent;
        if (extraColumnNames != null && extraColumnNames.length > 0) {
            int extraCount = extraColumnNames.length;
            String[] columnNames = new String[extraCount + 1];
            columnNames[0] = columnName;
            System.arraycopy(extraColumnNames, 0, columnNames, 1, extraCount);

            allHeadersPresent = Stream.of(columnNames)
                    .map(GridBy::getXPathForHeaderCellWithText)
                    .collect(Collectors.joining(" and "));
        } else {
            allHeadersPresent = getXPathForHeaderCellWithText(columnName);
        }
        return String.format("/tr[%1$s]", allHeadersPresent);
    }

    /**
     * Creates an XPath expression that will find a header cell based on its text.
     * @param headerText header text which must be present.
     * @return XPath expression selecting a th which has (a sub-element with) the supplied text.
     */
    public static String getXPathForHeaderCellWithText(String headerText) {
        return String.format("th[descendant-or-self::text()[normalized(.)='%1$s']]", headerText);
    }
}
