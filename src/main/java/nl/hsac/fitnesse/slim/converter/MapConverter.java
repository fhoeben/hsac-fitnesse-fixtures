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
            String key = entry.getKey().toString();
            HtmlTag keyCell = new HtmlTag("td", key.trim());
            keyCell.addAttribute("class", "hash_key");
            row.add(keyCell);

            HtmlTag valueCell = new HtmlTag("td");
            addValueContent(valueCell, entry.getValue());
            valueCell.addAttribute("class", "hash_value");
            row.add(valueCell);
        }
        return table;
    }

    protected void addValueContent(HtmlTag valueCell, Object entryValue) {
        if (entryValue != null) {
            Converter converter = getConverter(entryValue.getClass());
            String convertedValue;
            if (converter == null) {
                convertedValue = entryValue.toString();
            } else {
                convertedValue = converter.toString(entryValue);
            }
            valueCell.add(convertedValue.trim());
        } else {
            valueCell.add("null");
        }
    }

    protected Converter<?> getConverter(Class<?> clazz) {
        //use converter set in registry
        Converter<?> converter = ConverterRegistry.getConverterForClass(clazz);
        if (converter == null) {
            // use converter for superclass set in registry
            Class<?> superclass = clazz.getSuperclass();
            while (converter == null
                    && superclass != null && !Object.class.equals(superclass)) {
                converter = ConverterRegistry.getConverterForClass(superclass);
                superclass = superclass.getSuperclass();
            }
            // use converter for implemented interface set in registry
            converter = getConverterForInterface(clazz);
        }
        return converter;
    }

    protected static Converter<?> getConverterForInterface(Class<?> clazz) {
        List<Class<?>> superInterfaces = new ArrayList<Class<?>>();
        Converter<?> converterForInterface = null;
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> interf : interfaces) {
            Class<?>[] s = interf.getInterfaces();
            superInterfaces.addAll(Arrays.asList(s));
            converterForInterface = ConverterRegistry.getConverterForClass(interf);
            if (converterForInterface != null) {
                break;
            }
        }
        if (converterForInterface == null) {
            for (Class<?> supInterf : superInterfaces) {
                converterForInterface = getConverterForInterface(supInterf);
                if (converterForInterface != null) {
                    break;
                }
            }
        }
        return converterForInterface;
    }
}
