package nl.hsac.fitnesse.slim.converter;

import fitnesse.slim.converters.DefaultConverter;
import fitnesse.slim.converters.ElementConverterHelper;

/**
 * Slim converter to format an object of unknown static type using its actual type.
 */
public class ObjectConverter extends DefaultConverter {
    @Override
    public String toString(Object o) {
        return o == null ? super.toString(o) : ElementConverterHelper.elementToString(o);
    }
}
