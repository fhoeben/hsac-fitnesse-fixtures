package nl.hsac.fitnesse.slim.converter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import fitnesse.slim.Converter;

/**
 * Slim Converter which shows nested arrays with formatting based on their elements.
 * Needed until we use a FitNesse release including: https://github.com/unclebob/fitnesse/pull/739
 */
public class GenericArrayConverter<T> extends fitnesse.slim.converters.GenericArrayConverter<T> {
  public GenericArrayConverter(Class<T> componentClass, Converter<T> componentConverter) {
    super(componentClass, componentConverter);
  }

  @Override
  public String toString(Object array) {
    if (array == null)
      return NULL_VALUE;

    int size = Array.getLength(array);
    List<String> ret = new ArrayList<String>(size);
    for (int i = 0; i < size; i++) {
      ret.add(getElementString(array, i));
    }

    return ret.toString();
  }

  private String getElementString(Object array, int i) {
    T element = (T) Array.get(array, i);
    return ElementConverterHelper.elementToString(element);
  }
}
