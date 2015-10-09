package nl.hsac.fitnesse.fixture.util;

/**
 * Formatter that does not alter its input.
 */
public class TextFormatter implements Formatter {
    @Override
    public String format(String value) {
        return value;
    }
}
