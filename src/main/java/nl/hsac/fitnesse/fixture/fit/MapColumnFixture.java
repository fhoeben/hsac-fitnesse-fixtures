package nl.hsac.fitnesse.fixture.fit;

import fit.Binding;
import fit.Fixture;
import fit.Parse;
import fit.exception.FitFailureException;
import nl.hsac.fitnesse.fixture.util.HttpResponse;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for Fixtures that use a Map to store the set column values,
 * instead of fields.
 */
public class MapColumnFixture extends OurColumnFixture {
    private final Map<String, Object> currentRowValues = new HashMap<String, Object>();
    public static final String DEFAULT_ARRAY_SEPARATOR = ",";
    private static final String REGEX_STRING_WITH_SYMBOL = "(.*?)(\\$\\[)(.*?)(\\])(.*)"; // (Lazy) match randomText$[symbol]...
    private static final Pattern STRING_WITH_SYMBOL_PATTERN = Pattern.compile(REGEX_STRING_WITH_SYMBOL);

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
        } else if (header.endsWith("?$")) { // compare return with value in symbol
            String originalBody = heads.body; // Save originalBody so we can show it in testresult page
            heads.body = header.substring(0, header.length()-1); // Remove the $ so originalCreateBinding will create a Binding.QueryBinding
            result = originalCreateBinding(column, heads); // use originalCreateBinding so we can set the adapter
            if (result instanceof Binding.QueryBinding) {
                // We want to compare value in symbol with return value and made our own QueryBinding to do so.
                Binding queryBinding = new QueryBinding();
                queryBinding.adapter = result.adapter; // I think adapter is necessary so it can see if values matches or not
                result = queryBinding;
            }
            heads.body = originalBody;

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

                if (valueObj instanceof Object[]) {
                    // <ARRAY> Store return value as array.
                    Fixture.setSymbol(symbolName, valueObj);
                    symbolValue = Arrays.toString((Object[]) valueObj);
                } else {
                    Fixture.setSymbol(symbolName, symbolValue);
                }
                cell.addToBody(Fixture.gray("<a id=\"" + symbolName + "\"> = " + symbolValue + "</a>"));
            } catch (Exception e) {
                handleException(fixture, cell, e);
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

    public class ParameterBinding extends Binding {
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
                                 result = result + DEFAULT_ARRAY_SEPARATOR + symbolValue;
                                cell.addToBody(", ");
                             }
                             cell.addToBody(paramHRef(nestedPath[0], symbolValue));
                         } else { //symbol not found
                             fixture.exception(cell, new FitFailureException("No such symbol: " + symbolName));
                         }
                     }
                     getCurrentRowValues().put(header, result);
                } else {
                    Object symbol = Fixture.getSymbol(path[0]);
                    String valueString = "";
                    Object value = null;
                    if (symbol instanceof String) {
                        valueString = (String) Fixture.getSymbol(path[0]);
                        if (!"null".equals(valueString)) {
                            value = unmarshallParamValue(path, valueString);
                        }
                    } else if (symbol instanceof Object[]) {
                        // <ARRAY> Stored value is array.
                        if (path.length > 1) {
                            int index = getIndexFromSymbolArray(path);
                            value = getSymbolArrayValue(symbol, index);
                            valueString = (String) value;
                        } else {
                            value = (symbol);
                            valueString = Arrays.toString((Object[]) value);
                        }
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

    private class QueryBinding extends Binding.QueryBinding {

        /**
         * Special behavior: assumes the celltext is the name of a symbol and the return value of the column must be compared with the symbol value.
         * The special fitnesse values 'blank', 'null', '' are handled in the standard way though.
         */
        @Override
        public void doCell(Fixture aFixture, Parse aCell) {
            String originalCellText = aCell.text();
            String newText = null;
            String extraCellText =  "";
            try {
                if (!isSpecialBlankValueForFitnesse(originalCellText)) { // don't interfere with standard special behavior
                    // Assume the text is a symbol name of an array of those
                    if (originalCellText.startsWith("Array[") && originalCellText.endsWith("]")) {
                        // array of symbols and/of strings
                        newText = originalCellText.substring(6, originalCellText.length());
                        String separator = getArraySeperator();
                        // match string $variable1,variable2]    Note: ',' can be another array_seperator
                        Pattern pattern = Pattern.compile(String.format("\\$(.*?)(%s|])", separator));

                        Matcher matcher = pattern.matcher(newText);
                        while (matcher.find()) {
                            // replace all symbol entries by values
                            String symbolName = matcher.group();
                            int seperatorLength = matcher.group(2).length();
                            // replace the $variable name with value of getSymbolValue(variable_name)
                            newText = newText.replace(symbolName.substring(0, symbolName.length() - seperatorLength),
                                    getSymbolValue(symbolName.substring(1, symbolName.length() - seperatorLength)));
                        }
                        newText = newText.substring(0, newText.length() - 1);
                    } else {
                        newText = resolveStringWithSymbols(originalCellText);
                        if (newText == null){
                            // single element, we assume the text is a symbol
                            newText = getSymbolValue(originalCellText);
                        }
                    }
                    aCell.body = newText;
                    extraCellText = String.format(" (%s)", originalCellText);
                }
                super.doCell(aFixture, aCell);
                aCell.addToBody(extraCellText);

            } catch (NoSuchSymbolException e) {
                aCell.body = e.getMessage();
                aFixture.wrong(aCell);
            }
        }

    }

    /**
     * Functionality to compare returning cell value with a dynamic (with symbols) text.
     * See also HsacExamples.FitTests.ArraysAndSymbolsComparison#Compare result with string containing a symbol
     * @param originalCellText, ie randomText$[symbol1]MoreText where symbol1=ValueOfSymbol1
     * @return originalCellText with the symbols resolved to values, ie: randomTextValueOfSymbol1MoreText
     *      or null if the originalCellText doesn't contains text with symbols
     * @throws NoSuchSymbolException if the used symbol(s) don't exist
     */
    private String resolveStringWithSymbols(String originalCellText) throws NoSuchSymbolException {
        Matcher m = STRING_WITH_SYMBOL_PATTERN.matcher(originalCellText);
        String newText = null;
        while (m.matches()) {
            String symbolValue = getSymbolValue(m.group(3));
            newText = m.group(1) + symbolValue + m.group(5);
            m = STRING_WITH_SYMBOL_PATTERN.matcher(newText);
        }
        return newText;
    }

    private String getSymbolValue(String originalSymbolName) throws NoSuchSymbolException {
        String[] cellContent = null;
        String symbolName = null;
        String newText = null;
        if (originalSymbolName.contains(".")) {
            cellContent = StringUtils.split(originalSymbolName, '.');
            symbolName= cellContent[0];
        } else {
            symbolName = originalSymbolName;
        }
        Object symbolValue = Fixture.getSymbol(symbolName);
        if (symbolValue instanceof Object[]) {
            // cell text is element of array
            int arrayIndex = getIndexFromSymbolArray(cellContent);
            newText = (String) getSymbolArrayValue(symbolValue, arrayIndex);
        } else if (cellContent != null && cellContent.length == 2) {
            // cell text is nested object
            newText = (String) symbolValue;
            MapParameter mapValue = MapParameter.parse(newText);
            if (mapValue != null) {
                newText = mapValue.get(cellContent[1]).toString();
            }
        } else {
            newText = (String) (symbolValue);
        }
        if (newText == null) {
            throw new NoSuchSymbolException(String.format("No value for symbol '%s' found.", originalSymbolName));
        }
        return newText;
    }

    private void handleException(Fixture fixture, Parse cell, Exception e) {
        if (cell.text().isEmpty()) {
            cell.addToBody(gray("error"));
        } else {
            fixture.exception(cell, e);
        }
    }

    private class NoSuchSymbolException extends Exception {
        private static final long serialVersionUID = 1L;

        public NoSuchSymbolException(String message) {
            super(message);
        }
    }

    /**
     * Gets fixture parameter value by parameter key match.
     * Parameter should be defined in format <parameterKey>=<parameterValue>.
     * @param paramKey parameter key
     * @param defaultValue default parameter value
     * @return the fixture parameter value
     */
    private String getParameter(String paramKey, String defaultValue) {
        String paramValue = defaultValue;
        if (StringUtils.isNotBlank(paramKey) && args != null && args.length > 0) {
            for (String arg : args) {
                String[] parameter = arg.split("=");
                if (parameter != null && parameter.length == 2 && paramKey.equals(parameter[0])) {
                    paramValue = parameter[1];
                    break;
                }
            }
        }
        return paramValue;
    }

    /**
     * Fetch the array separator, which has a default value if not configured.
     * @return the array seperator
     */
    public String getArraySeperator() {
        return getParameter("ARRAY_SEPARATOR", DEFAULT_ARRAY_SEPARATOR);
    }

    /**
     * When cellContent contains a symbol that is an array, the specific element can be addressed
     * in the cell. However the index doesn't start at 0 but on 1 to make it more readable.
     * @param cellContent symbol of array with specification of element: ie arrayname.1 stands for arrayname[0]
     * @return the java index of the array (arrayname.1 returns 0, arrayname.2 return 1, etc)
     */
    private int getIndexFromSymbolArray(String[] cellContent) {
        int index = Integer.valueOf(cellContent[1]) - 1;
        return index;
    }

    /**
     * Fetch the value from arraySymbol on specified index.
     * @param arraySymbol symbol from Fixture that is array
     * @param index to find element from array
     * @return the element from the symbol array
     */
    private Object getSymbolArrayValue(Object arraySymbol, int index) {
        Object result = null;
        if (index > -1 && index < ((Object[]) arraySymbol).length) {
            result = ((Object[]) arraySymbol)[index];
        }
        return result;
    }

    boolean isSpecialBlankValueForFitnesse(String text) {
        if ("".equals(text)) {
            return true;
        } else if ("blank".equalsIgnoreCase(text)) {
            return true;
        } else if ("null".equalsIgnoreCase(text)) {
            return true;
        }
        return false;
    }

    /**
     * @return the currentRowValues
     */
    public Map<String, Object> getCurrentRowValues() {
        return currentRowValues;
    }

}