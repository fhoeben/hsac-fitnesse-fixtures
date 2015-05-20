package nl.hsac.fitnesse.slim.converter;

import fitnesse.slim.Converter;

  /**
   * Slim Converter which shows any Object.
   * Needed until we use a FitNesse release including: https://github.com/unclebob/fitnesse/pull/739
   */
public class DefaultConverter implements Converter<Object> {
  @Override
  public String toString(Object o) {
    return o == null ? NULL_VALUE : o.toString();
  }

  @Override
  public Object fromString(String arg) {
    return arg;
  }
}
