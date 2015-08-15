package nl.hsac.fitnesse.slim.converter;

import fitnesse.slim.Converter;
import fitnesse.slim.converters.ConverterRegistry;
import fitnesse.slim.converters.ElementConverterHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Slim Converter which works with HTML ordered lists (i.e. numbered lists) instead of the standard Java
 * toString() representation for lists.
 */
public class NumberedListConverter extends fitnesse.slim.converters.GenericCollectionConverter<Object, List<Object>> {
    private static final Pattern LIST_PATTERN = Pattern.compile(
                                                            "<ol( start=\"\\d+\")?\\s*>\\s*((<li>\\s*.*?\\s*</li>\\s*)*)</ol>",
                                                            Pattern.DOTALL);
    private static final Converter<Object> OBJ_CONVERTER = new ObjectConverter();

    /**
     * Makes NumberedListConverter the Converter Slim will uses for (Array)Lists.
     */
    public static void register() {
        try {
            Class<? extends List<Object>> listObjectClass;
            listObjectClass = (Class<List<Object>>) NumberedListConverter.class
                                .getMethod("toString", List.class).getParameterTypes()[0];
            NumberedListConverter converter = new NumberedListConverter();
            ConverterRegistry.addConverter(listObjectClass, converter);
            ConverterRegistry.addConverter(ArrayList.class, new NumberedArrayListConverter(converter));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * NumberedListConverter cannot implement Converter for List and ArrayList, therefore
     * this inner class ensures we have an implementation of ArrayList also.
     */
    public static class NumberedArrayListConverter implements Converter<ArrayList> {
        private final NumberedListConverter numberedListConverter;

        public NumberedArrayListConverter(NumberedListConverter aNumberedListConverter) {
            numberedListConverter = aNumberedListConverter;
        }

        @Override
        public String toString(ArrayList o) {
            return numberedListConverter.toString(o);
        }

        @Override
        public ArrayList fromString(String arg) {
            return numberedListConverter.fromString(arg);
        }
    }

    public NumberedListConverter() {
        super(ArrayList.class, OBJ_CONVERTER);
    }

    @Override
    public String toString(List<Object> list) {
        if (list == null) {
            return super.toString(list);
        }

        StringBuilder messageList = new StringBuilder("<ol start=\"0\">");
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
            String items = matcher.group(2);
            if (!"".equals(items)) {
                items = items.replaceFirst("^\\s*<li>\\s*", "");
                items = items.replaceFirst("\\s*</li>\\s*$", "");
                String[] elements = items.split("\\s*</li>\\s*<li>\\s*");
                result.addAll(Arrays.asList(elements));
            }
        } else {
            result = new ArrayList<Object>(super.fromString(arg));
        }
        return result;
    }
}
