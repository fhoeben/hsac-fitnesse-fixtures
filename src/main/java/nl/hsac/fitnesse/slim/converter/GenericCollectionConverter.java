package nl.hsac.fitnesse.slim.converter;

import java.util.*;

import fitnesse.slim.Converter;

/**
 * Slim Converter which shows nested collections with formatting based on their elements.
 * Needed until we use a FitNesse release including: https://github.com/unclebob/fitnesse/pull/739
 */
public class GenericCollectionConverter<T, C extends Collection<T>> extends fitnesse.slim.converters.GenericCollectionConverter<T, C> {
  public GenericCollectionConverter(Class<?> collectionClass, Converter<T> componentConverter) {
    super(collectionClass, componentConverter);
  }

  @Override
  public String toString(C collection) {
    if (collection == null)
      return NULL_VALUE;

    int size = collection.size();
    List<String> ret = new ArrayList<String>(size);
    for (T item : collection) {
      ret.add(getElementString(item));
    }
    return ret.toString();
  }

  private String getElementString(T item) {
    return ElementConverterHelper.elementToString(item);
  }
}
