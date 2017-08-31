package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Own subclass of By which supports placeholders in its pattern.
 * The pattern will only be expanded on first usage.
 */
public abstract class LazyPatternBy extends By {
    private final String pattern;
    private final String[] parameters;
    private By nested;

    /**
     * Creates By based on xPath, supporting placeholder replacement.
     * It also supports the fictional 'normalized()' function that does whitespace normalization, that also
     * considers a '&nbsp;' whitespace.
     * @param pattern basic XPATH, possibly with placeholders.
     * @param parameters values for placeholders.
     * @return ByXPath.
     */
    public LazyPatternBy(String pattern, String... parameters) {
        this.pattern = pattern;
        this.parameters = parameters;
    }

    @Override
    public List<WebElement> findElements(SearchContext context) {
        return getNested().findElements(context);
    }

    protected synchronized By getNested() {
        if (nested == null) {
            String expr = createExpression(pattern, parameters);
            nested = createNested(expr);
        }
        return nested;
    }

    protected abstract By createNested(String expr);

    protected String createExpression(String pattern, String... parameters) {
        return fillPattern(pattern, parameters);
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
            patternToUse = pattern.replace("'", "\"");
        } else {
            patternToUse = pattern;
        }
        return String.format(patternToUse, escapedParams);
    }
}
