package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Own subclass of By which supports placeholders in its pattern.
 * The pattern will only be expanded on first usage.
 */
public abstract class LazyPatternBy extends By {
    private final static Pattern SINGLE_QUOTE_PATTERN = Pattern.compile("'");
    private final String pattern;
    private final String[] parameters;
    private By nested;

    /**
     * Creates By based on pattern, supporting placeholder replacement.
     * Pattern will only be filled in when By is evaluated.
     * @param pattern basic pattern, possibly with placeholders {@link String#format}.
     * @param parameters values for placeholders.
     */
    public LazyPatternBy(String pattern, String... parameters) {
        this.pattern = pattern;
        this.parameters = parameters;
    }

    @Override
    public List<WebElement> findElements(SearchContext context) {
        return getNested().findElements(context);
    }

    @Override
    public String toString() {
        return getNested().toString();
    }

    @Override
    public int hashCode() {
        return getNested().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else {
            By nested = getNested();
            if (o instanceof LazyPatternBy) {
                return nested.equals(((LazyPatternBy) o).getNested());
            } else {
                return nested.equals(o);
            }
        }
    }

    private final synchronized By getNested() {
        if (nested == null) {
            String expr = createExpression(pattern, parameters);
            nested = createNested(expr);
        }
        return nested;
    }

    protected abstract By createNested(String expr);

    protected String createExpression(String pattern, String... parameters) {
        String result;
        if (parameters.length == 0) {
            result = pattern;
        } else {
            result = fillPattern(pattern, parameters);
        }
        return result;
    }

    /**
     * Fills in placeholders in pattern using the supplied parameters.
     * @param pattern pattern to fill (in String.format style).
     * @param parameters parameters to use.
     * @return filled in pattern.
     */
    private String fillPattern(String pattern, String[] parameters) {
        boolean containsSingleQuote = false;
        boolean containsDoubleQuote = false;
        Object[] escapedParams = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            String param = parameters[i];
            containsSingleQuote = containsSingleQuote || param.contains("'");
            containsDoubleQuote = containsDoubleQuote || param.contains("\"");
            escapedParams[i] = param;
        }
        if (containsDoubleQuote && containsSingleQuote) {
            throw new RuntimeException("Unsupported combination of single and double quotes");
        }
        String patternToUse;
        if (containsSingleQuote) {
            patternToUse = SINGLE_QUOTE_PATTERN.matcher(pattern).replaceAll("\"");
        } else {
            patternToUse = pattern;
        }
        return String.format(patternToUse, escapedParams);
    }
}
