package nl.hsac.fitnesse.fixture.fit;

import fit.Binding;
import fit.Fixture;
import fit.Parse;
import fit.exception.FitFailureException;
import nl.hsac.fitnesse.fixture.util.HttpResponse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for Fixtures that use a Map to store the set column values,
 * instead of fields.
 */
public class MapColumnFixture extends OurColumnFixture {
    private final Map<String, Object> currentRowValues = new HashMap<String, Object>();

    @Override
    public void reset() {
        currentRowValues.clear();
        setDefaults(getCurrentRowValues());
    }

    /**
     * Adds new key/value pairs to currentRowValues based on table supplied. If value in first column is present,
     * and there is no value for key of second column, the value in second column is added, and vice versa.
     * The first row describes the key for each column's values.
     * @param translationTableWithHeader table describing equivalent key/value pairs.
     */
    public void translateFromTable(String[][] translationTableWithHeader) {
        translateFromTable(getCurrentRowValues(), translationTableWithHeader);
    }

    /**
     * Adds new key/value pairs to rowValues based on table supplied. If value in first column is present,
     * and there is no value for key of second column, the value in second column is added, and vice versa.
     * The first row describes the key for each column's values.
     * @param rowValues row to add values to.
     * @param translationTableWithHeader table describing equivalent key/value pairs.
     */
    public static void translateFromTable(Map<String, Object> rowValues, String[][] translationTableWithHeader) {
        translateFromTable(rowValues, translationTableWithHeader, 0, 1);
        translateFromTable(rowValues, translationTableWithHeader, 1, 0);
    }

    private static void translateFromTable(Map<String, Object> rowValues, String[][] translationTableWithHeader,
                                           int fromColumn, int toColumn) {
        String fromKey = translationTableWithHeader[0][fromColumn];
        String toKey = translationTableWithHeader[0][toColumn];
        if (rowValues.get(toKey) == null) {
            Object valuePresent = rowValues.get(fromKey);
            String toValue = findToValue(translationTableWithHeader, fromColumn, toColumn, valuePresent);
            if (toValue != null) {
                rowValues.put(toKey, toValue);
            }
        }
    }

    private static String findToValue(String[][] translationTableWithHeader, int fromColumn, int toColumn, Object fromValue) {
        String toValue = null;
        for (int i = 1; i < translationTableWithHeader.length; i++) { // row at i==0 contains headers
            String[] row = translationTableWithHeader[i];
            if (row[fromColumn].equals(fromValue)) {
                toValue = row[toColumn];
                break;
            }
        }
        return toValue;
    }

    /**
     * Replaces values found in the currentRowValues based on the supplied table.
     * Cell 0,1 defines the key to work with.
     * Other rows define a replacement value: if value in first column is found
     * it is replaced with the one from the second.
     * @param leftAcceptedForRight table describing replacements.
     */
    public void acceptAlternativeInputValuesFromTable(String[][] leftAcceptedForRight) {
        acceptAlternativeInputValuesFromTable(getCurrentRowValues(), leftAcceptedForRight);
    }

    /**
     * Replaces values found in the rowValues based on the supplied table.
     * Cell 0,1 defines the key to work with.
     * Other rows define a replacement value: if value in first column is found
     * it is replaced with the one from the second.
     * @param rowValues values to replace in.
     * @param leftAcceptedForRight table describing replacements.
     */
    public static void acceptAlternativeInputValuesFromTable(Map<String, Object> rowValues, String[][] leftAcceptedForRight) {
        String key = leftAcceptedForRight[0][1];
        Object valuePresent = rowValues.get(key);
        String translation = findToValue(leftAcceptedForRight, 0, 1, valuePresent);
        if (translation != null) {
            rowValues.put(key, translation);
        }
    }
    
    /**
     * Places default values (if any) before each row.
     * 
     * @param values
     *            map to put defaults in.
     */
    protected void setDefaults(Map<String, Object> values) {
    }

    /**
     * @param columnName
     *            column to get current row's value of
     * @return value for specified column.
     */
    public Object get(String columnName) {
        return getCurrentRowValues().get(columnName);
    }

    /**
     * @param columnName
     *            column to get current row's value of
     * @return value for specified column.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getNested(String columnName) {
        Object value = get(columnName);
        if (value == null) {
            throw new IllegalArgumentException("No value for: " + columnName);
        }
        if (!(value instanceof Map)) {
            throw new IllegalArgumentException("Value for: " + columnName + " is not a Map, but a: " + value.getClass());
        }
        return (Map<String, Object>) value;
    }

    /**
     * @param columnName
     *            column to get current row's value of
     * @return value for specified column split by its spaces and optional whitespace.
     */
    protected List<String> getList(String columnName) {
        Object value = get(columnName);
        if (value == null) {
            throw new IllegalArgumentException("No value for: " + columnName);
        }
        return Arrays.asList(value.toString().split("\\s*,\\s*"));
    }

    @Override
    protected Binding createBinding(int column, Parse heads) throws Throwable {
        Binding result;
        String header = heads.text();
        if (header.endsWith("=")) {
            result = new ParameterBinding(header.substring(0, header.length() - 1));
        } else if (header.endsWith("?")) {
            // use default Fixture behavior
            result = originalCreateBinding(column, heads);
            if (result instanceof Binding.SaveBinding) {
                // we want our own SaveBinding that makes anchors
                Binding saveBinding = new SaveBinding();
                saveBinding.adapter = result.adapter;
                result = saveBinding;
            }
        } else {
            return new MapBinding(header);
        }
        return result;
    }

    protected final Binding originalCreateBinding(int column, Parse heads) throws Throwable {
        return super.createBinding(column, heads);
    }

    protected class SaveBinding extends Binding {
        @Override
        public void doCell(Fixture fixture, Parse cell) {
            try {
                executeIfNeeded();

                Object valueObj = getValue();
                String symbolValue = String.valueOf(valueObj);
                String symbolName = cell.text();
                Fixture.setSymbol(symbolName, symbolValue);
                cell.addToBody(Fixture.gray("<a id=\"" + symbolName + "\"> = " + symbolValue + "</a>"));
            } catch (Exception e) {
                if (cell.text().isEmpty()) {
                    cell.addToBody(gray("error"));
                } else {
                    fixture.exception(cell, e);
                }
            }
        }

        /**
         * @return value to store.
         * @throws Exception
         *             if value could not be retrieved
         */
        protected Object getValue() throws Exception {
            return adapter.get(); // ...might be validly null
        }
    }

    class ParameterBinding extends Binding {
        private final String header;

        public ParameterBinding(String headerName) {
            header = headerName;
        }

        @Override
        public void doCell(Fixture fixture, Parse cell) throws Throwable {
            String symbolName = cell.text();
            if (!"".equals(symbolName)) {
                String[] path = symbolName.split("\\.");
                String[] items = symbolName.split("\\s*,\\s*");
                if (items.length > 1 || !Fixture.hasSymbol(path[0])) {
                     String result = null;
                     for (String name : items) {
                         String[] nestedPath = name.split("\\.");
                         if (Fixture.hasSymbol(nestedPath[0])) {
                             String symbolValue = (String) Fixture.getSymbol(nestedPath[0]);
                             if (nestedPath.length > 1) {
                                 symbolValue = unmarshallParamValue(nestedPath, symbolValue).toString();
                             }
                             if (result == null) {
                                 result = symbolValue;
                                 cell.addToBody(" = ");
                             } else {
                                result = result + "," + symbolValue;
                                cell.addToBody(", ");
                             }
                             cell.addToBody(paramHRef(nestedPath[0], symbolValue));
                         } else { //symbol not found
                             fixture.exception(cell, new FitFailureException("No such symbol: " + symbolName));
                         }
                     }
                     getCurrentRowValues().put(header, result);
                } else {
                    String valueString = (String) Fixture.getSymbol(path[0]);
                    Object value = null;
                    if (!"null".equals(valueString)) {
                        value = unmarshallParamValue(path, valueString);
                    }
                    getCurrentRowValues().put(header, value);
                    cell.addToBody(" = " + paramHRef(symbolName, valueString));
                }
            }
        }

        private String paramHRef(String symbolName, String value) {
            return Fixture.gray("<a href=\"#" + symbolName + "\">" + value + "</a>");
        }

        private Object unmarshallParamValue(String[] paramNamePath, String valueString) {
            Object value;
            MapParameter mapValue = MapParameter.parse(valueString);
            if (mapValue != null) {
                if (paramNamePath.length == 1) {
                    value = mapValue;
                } else {
                    String nestedKey = paramNamePath[1];
                    value = mapValue.get(nestedKey);
                }
            } else {
                HttpResponse resp = HttpResponse.parse(valueString);
                if (resp != null) {
                    value = resp;
                } else {
                    value = valueString;
                }
            }
            return value;
        }
    }

    private class MapBinding extends Binding {
        private final String header;

        public MapBinding(String headerName) {
            header = headerName;
        }

        @Override
        public void doCell(Fixture aFixture, Parse aCell) throws Throwable {
            String text = aCell.text();
            if ("null".equals(text) || "".equals(text)) {
                text = null;
            } else if ("blank".equals(text)) {
                text = "";
            }
            currentRowValues.put(header, text);
        }
    }

    /**
     * @return the currentRowValues
     */
    public Map<String, Object> getCurrentRowValues() {
        return currentRowValues;
    }

}