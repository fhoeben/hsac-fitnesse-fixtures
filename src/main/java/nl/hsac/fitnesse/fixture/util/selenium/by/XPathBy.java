package nl.hsac.fitnesse.fixture.util.selenium.by;

import nl.hsac.fitnesse.fixture.util.CacheHelper;
import org.openqa.selenium.By;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Own subclass of By on XPath, which also supports the fictional 'normalized()' function
 * that does whitespace normalization, that also considers a '&nbsp;' whitespace.
 */
public class XPathBy extends LazyPatternBy {
    private static final Map<String, String> CACHE = CacheHelper.lruCache(1000);
    // Regex to find our own 'fake xpath function' in xpath 'By' content
    private static final Pattern X_PATH_NORMALIZED = Pattern.compile("normalized\\((.+?(\\(\\))?)\\)");
    private static final String NBSP_CHAR = "\u00a0";
    private static final String NORMALIZED_REPLACEMENT = "normalize-space(translate($1, '" + NBSP_CHAR + "', ' '))";
    private static final Pattern WHITESPACE_REPLACE = Pattern.compile("[" + NBSP_CHAR + "\\s]+");

    /**
     * Creates By based on xPath, supporting placeholder replacement.
     * It also supports the fictional 'normalized()' function that does whitespace normalization, that also
     * considers a '&nbsp;' whitespace.
     * @param pattern basic XPATH, possibly with placeholders.
     * @param parameters values for placeholders.
     * @return ByXPath.
     */
    public XPathBy(String pattern, String... parameters) {
        super(pattern, parameters);
    }

    @Override
    protected By createNested(String expr) {
        return By.xpath(expr);
    }

    @Override
    protected String createExpression(String pattern, String... parameters) {
        pattern = CACHE.computeIfAbsent(pattern, XPathBy::replaceNormalizedFunction);
        for (int i = 0; i < parameters.length; i++) {
            // caching of parameters seems less likely to be worth while...
            parameters[i] = replaceNormalizedFunction(parameters[i]);
        }
        String xpath = super.createExpression(pattern, parameters);
        return xpath;
    }

    private static String replaceNormalizedFunction(String xPath) {
        String result;
        if (xPath.contains("normalized(")) {
        /*
            we first check whether the pattern contains the function name, to not have the overhead of
            regex replacement when it is not needed.
        */
            Matcher m = X_PATH_NORMALIZED.matcher(xPath);
            String updatedPattern = m.replaceAll(NORMALIZED_REPLACEMENT);
            result = updatedPattern;
        } else {
            result = xPath;
        }
        return result;
    }

    /**
     * Mimics effect of 'normalized()` xPath function on Java String.
     * Replaces &nbsp; by normal space, and collapses whitespace sequences to single space
     * @param elementText text in element.
     * @return normalized text.
     */
    public static String getNormalizedText(String elementText) {
        String result = null;
        if (elementText != null) {
            result = WHITESPACE_REPLACE.matcher(elementText).replaceAll(" ");
        }
        return result;
    }
}
