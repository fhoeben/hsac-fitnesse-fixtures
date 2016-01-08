package nl.hsac.fitnesse.fixture.util;

/**
 * Generates pre-formatted version of an Object.
 */
public interface Formatter {
    /**
     * Creates formatted version of the supplied value.
     * @param value value to format.
     * @return formatted version.
     */
    String format(String value);
}
