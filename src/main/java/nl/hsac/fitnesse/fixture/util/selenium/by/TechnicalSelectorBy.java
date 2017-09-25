package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;

import java.util.function.Function;

import static nl.hsac.fitnesse.fixture.util.FirstNonNullHelper.firstNonNull;

/**
 * By to work using 'technical selectors.
 */
public class TechnicalSelectorBy {
    private static final Function<String, By> ID_BY = byIfStartsWith("id", By::id);
    private static final Function<String, By> CSS_BY = byIfStartsWith("css", By::cssSelector);
    private static final Function<String, By> NAME_BY = byIfStartsWith("name", By::name);
    private static final Function<String, By> LINKTEXT_BY = byIfStartsWith("link", By::linkText);
    private static final Function<String, By> PARTIALLINKTEXT_BY = byIfStartsWith("partialLink", By::partialLinkText);
    private static final Function<String, By> XPATH_BY = byIfStartsWith("xpath", XPathBy::new);

    /**
     * Whether supplied place is a technical selector.
     * @param place place that might be technical selector
     * @return true if place starts with one of the technical selector prefixes.
     */
    public static boolean isTechnicalSelector(String place) {
        return StringUtils.startsWithAny(place,
                "id=", "xpath=", "css=", "name=", "link=", "partialLink=");
    }

    /**
     * @param place place that might be technical selector
     * @return By if place was a technical selector, null otherwise.
     */
    public static By forPlace(String place) {
        return firstNonNull(place,
                ID_BY,
                CSS_BY,
                NAME_BY,
                LINKTEXT_BY,
                PARTIALLINKTEXT_BY,
                XPATH_BY);
    }

    public static Function<String, By> byIfStartsWith(String prefix, Function<String, By> constr) {
        String prefixEq = prefix + "=";
        int prefixLength = prefixEq.length();
        return place -> place.startsWith(prefixEq) ? constr.apply(place.substring(prefixLength)) : null;
    }
}
