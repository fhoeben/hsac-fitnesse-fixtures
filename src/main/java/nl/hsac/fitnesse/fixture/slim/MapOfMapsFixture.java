package nl.hsac.fitnesse.fixture.slim;

import fitnesse.slim.SlimSymbol;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Fixture which allows the definition of a map of maps using a table where the each column represents a top-level map.
 */
public class MapOfMapsFixture extends SlimTableFixture {
    private Map<String, Map<String, Object>> maps;

    public MapOfMapsFixture() {
        this(true);
    }

    public MapOfMapsFixture(boolean expandPeriodsInNamesToNestedMaps) {
        expandPeriodsInNamesToNestedMaps(expandPeriodsInNamesToNestedMaps);
    }

    @Override
    protected List<List<String>> doTableImpl(List<List<String>> table) {
        int numberOfRows = table.size();
        List<List<String>> result = new ArrayList<>(numberOfRows);
        if (!table.isEmpty()) {
            List<String> header = table.get(0);
            int colCount = header.size();
            maps = new LinkedHashMap<>();
            for (List<String> row : table) {
                int rowCounter = result.size();
                List<String> resultRow = new ArrayList<>(colCount);
                if (rowCounter == 0) {
                    handleHeader(resultRow, header);
                } else if (rowCounter == numberOfRows - 1) {
                    handleBottomRow(resultRow, header, row);

                } else {
                    handleRow(resultRow, header, row);
                }
                result.add(resultRow);
            }
        }
        return result;
    }

    protected Map<String, Map<String, Object>> getMaps() {
        return maps;
    }

    protected void handleHeader(List<String> resultRow, List<String> header) {
        String firstCell = header.get(0);
        for (int i = 1; i < header.size(); i++) {
            String headerCell = header.get(i);
            LinkedHashMap<String, Object> nestedMap = new LinkedHashMap<>();
            maps.put(headerCell, nestedMap);
            if (StringUtils.isNotEmpty(firstCell)) {
                nestedMap.put(firstCell, headerCell);
            }
        }
    }

    protected void handleBottomRow(List<String> resultRow, List<String> header, List<String> row) {
        String firstCell = row.get(0);
        if (StringUtils.isEmpty(firstCell)) {
            resultRow.add("");
        } else if (assignSymbolIfApplicable(firstCell, maps)) {
            resultRow.add("pass");
        }
        if (resultRow.isEmpty()) {
            handleRow(resultRow, header, row);
        } else {
            for (int i = 1; i < header.size() && i < row.size(); i++) {
                String cell = row.get(i);
                String columnName = header.get(i);
                Map<String, Object> map = maps.get(columnName);
                if (assignSymbolIfApplicable(cell, map)) {
                    resultRow.add("pass");
                } else {
                    resultRow.add("fail:expected symbol assignment");
                }
            }
        }
    }

    protected void handleRow(List<String> resultRow, List<String> header, List<String> row) {
        String key = null;
        for (int i = 0; i < row.size(); i++) {
            if (i == 0) {
                key = row.get(i);
            } else {
                String headerCell = header.get(i);
                Map<String, Object> map = maps.get(headerCell);
                String cell = row.get(i);
                Object value = cell;
                if (StringUtils.isNotEmpty(cell)) {
                    Matcher symbolMatcher = SlimSymbol.SYMBOL_PATTERN.matcher(cell);
                    if (symbolMatcher.matches()) {
                        value = getSymbolValue(symbolMatcher);
                    } else {
                        value = replaceSymbolsInString(cell);
                    }
                }
                getMapHelper().setValueForIn(value, key, map);
            }
        }
    }
}
