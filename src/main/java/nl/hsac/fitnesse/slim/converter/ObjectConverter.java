package nl.hsac.fitnesse.slim.converter;

/**
 * Slim converter to format an object of unknown static type using its actual type.
 * Needed until we use a FitNesse release including: https://github.com/unclebob/fitnesse/pull/739
 */
public class ObjectConverter extends DefaultConverter {
    @Override
    public String toString(Object o) {
        return o == null ? super.toString(o) : ElementConverterHelper.elementToString(o);
    }
}
