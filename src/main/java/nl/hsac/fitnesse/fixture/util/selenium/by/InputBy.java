package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.By;

/**
 * Finds inputs.
 */
public class InputBy {
    /**
     * @param value value to look for
     * @return non-hidden input containing value.
     */
    public static By exactValue(String value) {
        return new CssBy("input[value='%s']:not([type='hidden'])", value);
    }

    /**
     * @param value value to look for
     * @return non-hidden input containing value.
     */
    public static By partialValue(String value) {
        return new CssBy("input[value*='%s']:not([type='hidden'])", value);
    }
}
