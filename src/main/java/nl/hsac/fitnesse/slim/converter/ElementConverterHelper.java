package nl.hsac.fitnesse.slim.converter;

import fitnesse.slim.Converter;
import fitnesse.slim.converters.ConverterRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Slim Converter Helper.
 * Needed until we use a FitNesse release including: https://github.com/unclebob/fitnesse/pull/739
 */
public class ElementConverterHelper {
  private static final DefaultConverter DEFAULT_CONVERTER = new DefaultConverter();

  public static String elementToString(Object elementValue) {
    String valueToAdd = "null";
    if (elementValue != null) {
      Converter converter = getConverter(elementValue.getClass());
      String convertedValue;
      if (converter == null) {
        convertedValue = elementValue.toString();
      } else {
        convertedValue = converter.toString(elementValue);
      }
      if (convertedValue != null) {
        valueToAdd = convertedValue;
      }
    }
    return valueToAdd;
  }

  public static Converter<?> getConverter(Class<?> clazz) {
    //use converter set in registry
    Converter<?> converter = ConverterRegistry.getConverterForClass(clazz);
    if (converter instanceof DefaultConverter
            || converter instanceof fitnesse.slim.converters.GenericArrayConverter
            || converter instanceof fitnesse.slim.converters.GenericCollectionConverter) {
      converter = null;
    }
    if (converter == null) {

      //for array, use generic array converter
      if (clazz.isArray()) {
        Class<?> componentType = clazz.getComponentType();
        Converter<?> converterForClass = getConverter(componentType);
        converter = new GenericArrayConverter(componentType, converterForClass);
      }

      //for collection, use generic collection converter
      if (Collection.class.isAssignableFrom(clazz)) {
        converter = new GenericCollectionConverter(clazz, DEFAULT_CONVERTER);
      }

      // use converter for superclass set in registry
      Class<?> superclass = clazz.getSuperclass();
      while (converter == null
              && superclass != null && !Object.class.equals(superclass)) {
        converter = ConverterRegistry.getConverterForClass(superclass);
        superclass = superclass.getSuperclass();
      }

      if (converter == null) {
        // use converter for implemented interface set in registry
        converter = getConverterForInterface(clazz);
        if (converter == null) {
          converter = DEFAULT_CONVERTER;
        }
      }
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

      if (converterForInterface == null) {
        Class<?> superclass = clazz.getSuperclass();
        while (superclass != null && !Object.class.equals(superclass)) {
          converterForInterface = getConverterForInterface(superclass);
          if (converterForInterface != null) {
            break;
          }
          superclass = superclass.getSuperclass();
        }

      }
    }
    return converterForInterface;
  }
}
