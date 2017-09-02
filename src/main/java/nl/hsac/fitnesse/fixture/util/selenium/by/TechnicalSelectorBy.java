package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;

/**
 * By to work using 'technical selectors.
 */
public class TechnicalSelectorBy {

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
        By result = null;
        if (place.startsWith("id=")) {
            result = By.id(place.substring(3));
        } else if (place.startsWith("css=")) {
            result = By.cssSelector(place.substring(4));
        } else if (place.startsWith("name=")) {
            result = By.name(place.substring(5));
        } else if (place.startsWith("link=")) {
            result = By.linkText(place.substring(5));
        } else if (place.startsWith("partialLink=")) {
            result = By.partialLinkText(place.substring(12));
        } else if (place.startsWith("xpath=")) {
            result = new XPathBy(place.substring(6));
        }
        return result;
    }
}
