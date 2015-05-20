package nl.hsac.fitnesse.slim.converter;

import fitnesse.html.HtmlTag;
import fitnesse.slim.Converter;
import fitnesse.slim.converters.ConverterRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Slim Converter which shows nested maps as nested tables.
 * Needed until we use a FitNesse release including: https://github.com/unclebob/fitnesse/pull/739
 */
public class MapConverter extends fitnesse.slim.converters.MapConverter {
    @Override
    public String toString(Map hash) {
        if (hash == null) {
            return NULL_VALUE;
        }

        HtmlTag table = createTag(hash, 0);

        return table.html().trim();
    }

    protected HtmlTag createTag(Map<?, ?> hash, int depth) {
        // Use HtmlTag, same as we do for fitnesse.wikitext.parser.HashTable.
        HtmlTag table = new HtmlTag("table");
        table.addAttribute("class", "hash_table");
        for (Map.Entry<?, ?> entry : hash.entrySet()) {
            HtmlTag row = new HtmlTag("tr");
            row.addAttribute("class", "hash_row");
            table.add(row);
            HtmlTag keyCell = new HtmlTag("td");
            addCellContent(keyCell, entry.getKey());
            keyCell.addAttribute("class", "hash_key");
            row.add(keyCell);

            HtmlTag valueCell = new HtmlTag("td");
            addCellContent(valueCell, entry.getValue());
            valueCell.addAttribute("class", "hash_value");
            row.add(valueCell);
        }
        return table;
    }

    protected void addCellContent(HtmlTag valueCell, Object cellValue) {
        String valueToAdd = ElementConverterHelper.elementToString(cellValue);
        valueCell.add(valueToAdd.trim());
    }
}
