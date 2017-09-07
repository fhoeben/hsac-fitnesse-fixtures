package nl.hsac.fitnesse.fixture.util.selenium.by;

import nl.hsac.fitnesse.fixture.util.selenium.by.grid.Link;
import nl.hsac.fitnesse.fixture.util.selenium.by.grid.Row;
import nl.hsac.fitnesse.fixture.util.selenium.by.grid.Value;

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
