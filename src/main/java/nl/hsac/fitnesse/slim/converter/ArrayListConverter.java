package nl.hsac.fitnesse.slim.converter;

import fitnesse.slim.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArrayListConverter extends fitnesse.slim.converters.GenericCollectionConverter<Object, ArrayList<Object>> {
    private static final Pattern LIST_PATTERN = Pattern.compile(
                                                            "<ol>\\s*((<li>\\s*.*?\\s*</li>\\s*)*)</ol>",
                                                            Pattern.DOTALL);
    private static final Converter<Object> OBJ_CONVERTER = new ObjectConverter();

    public ArrayListConverter() {
        super(ArrayList.class, OBJ_CONVERTER);
    }

    @Override
    public String toString(ArrayList<Object> list) {
        if (list == null) {
            return super.toString(list);
        }

        StringBuilder messageList = new StringBuilder("<ol>");
        for (Object element : list) {
            messageList.append("<li>");
            String formattedElement = ElementConverterHelper.elementToString(element);
            messageList.append(formattedElement);
            messageList.append("</li>");
        }
        messageList.append("</ol>");
        return messageList.toString();
    }

    @Override
    public ArrayList<Object> fromString(String arg) {
        ArrayList<Object> result;
        Matcher matcher = LIST_PATTERN.matcher(arg);
        if (matcher.matches()) {
            result = new ArrayList<Object>();
            String items = matcher.group(1);
            if (!"".equals(items)) {
                items = items.replaceFirst("^\\s*<li>\\s*", "");
                items = items.replaceFirst("\\s*</li>\\s*$", "");
                String[] elements = items.split("\\s*</li>\\s*<li>\\s*");
                result.addAll(Arrays.asList(elements));
            }
        } else {
            result = super.fromString(arg);
        }
        return result;
    }
}
