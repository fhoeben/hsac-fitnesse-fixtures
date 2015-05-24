package nl.hsac.fitnesse.slim.converter;

import fitnesse.slim.Converter;
import fitnesse.slim.converters.ConverterRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListConverter extends fitnesse.slim.converters.GenericCollectionConverter<Object, List<Object>> {
    private static final Pattern LIST_PATTERN = Pattern.compile(
                                                            "<ol>\\s*((<li>\\s*.*?\\s*</li>\\s*)*)</ol>",
                                                            Pattern.DOTALL);
    private static final Converter<Object> OBJ_CONVERTER = new ObjectConverter();

    public static void register() {
        try {
            Class<? extends List<Object>> listObjectClass;
            listObjectClass = (Class<List<Object>>) ListConverter.class
                                .getMethod("fromString", String.class).getReturnType();
            ListConverter converter = new ListConverter();
            ConverterRegistry.addConverter(listObjectClass, converter);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ListConverter() {
        super(ArrayList.class, OBJ_CONVERTER);
    }

    @Override
    public String toString(List<Object> list) {
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
    public List<Object> fromString(String arg) {
        List<Object> result;
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
