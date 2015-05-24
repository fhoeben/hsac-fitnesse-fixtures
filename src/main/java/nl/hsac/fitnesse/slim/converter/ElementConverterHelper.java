package nl.hsac.fitnesse.slim.converter;

import fitnesse.slim.Converter;
import fitnesse.slim.converters.ConverterRegistry;

import java.util.*;

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
    Map<Class<?>, Converter<?>> registeredConverters = ConverterRegistry.getConverters();
    //use converter set in registry
    Converter<?> converter = registeredConverters.get(clazz);
    if (converter == null) {
      // use converter for superclass set in registry
      Class<?> superclass = clazz.getSuperclass();
      while (converter == null
              && superclass != null && !Object.class.equals(superclass)) {
        converter = registeredConverters.get(superclass);
        superclass = superclass.getSuperclass();
      }

      if (converter == null) {
        // use converter for implemented interface set in registry
        converter = getConverterForInterface(clazz, registeredConverters);
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

          if (converter == null) {
            converter = DEFAULT_CONVERTER;
          }
        }
      }
    }
    return converter;
  }

  protected static Converter<?> getConverterForInterface(Class<?> clazz, Map<Class<?>, Converter<?>> registeredConverters) {
    List<Class<?>> superInterfaces = new ArrayList<Class<?>>();
    Converter<?> converterForInterface = null;
    Class<?>[] interfaces = clazz.getInterfaces();
    for (Class<?> interf : interfaces) {
      Class<?>[] s = interf.getInterfaces();
      superInterfaces.addAll(Arrays.asList(s));
      converterForInterface = registeredConverters.get(interf);
      if (converterForInterface != null) {
        break;
      }
    }
    if (converterForInterface == null) {
      for (Class<?> supInterf : superInterfaces) {
        converterForInterface = getConverterForInterface(supInterf, registeredConverters);
        if (converterForInterface != null) {
          break;
        }
      }

      if (converterForInterface == null) {
        Class<?> superclass = clazz.getSuperclass();
        while (superclass != null && !Object.class.equals(superclass)) {
          converterForInterface = getConverterForInterface(superclass, registeredConverters);
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
