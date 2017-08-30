package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.By;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Own subclass of By on XPath, which also supports the fictional 'normalized()' function
 * that does whitespace normalization, that also considers a '&nbsp;' whitespace.
 */
public class XPathBy extends By.ByXPath {
    // Regex to find our own 'fake xpath function' in xpath 'By' content
    private final static Pattern X_PATH_NORMALIZED = Pattern.compile("normalized\\((.+?(\\(\\))?)\\)");

    public XPathBy(String xpathExpression) {
        super(replaceNormalizedFunction(xpathExpression));
    }

    private static String replaceNormalizedFunction(String xPath) {
        if (xPath.contains("normalized(")) {
            /*
                we first check whether the pattern contains the function name, to not have the overhead of
                regex replacement when it is not needed.
            */
            Matcher m = X_PATH_NORMALIZED.matcher(xPath);
            String updatedPattern = m.replaceAll("normalize-space(translate($1, '\u00a0', ' '))");
            xPath = updatedPattern;
        }
        return xPath;
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
            result = elementText.replace('\u00a0', ' ').replaceAll("\\s+", " ");
        }
        return result;
    }
}
