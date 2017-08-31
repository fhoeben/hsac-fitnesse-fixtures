package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.By;

/**
 * Finds element to get value from.
 */
public class NestedElementForValueBy extends BestMatchBy {
    private static final By NESTED = By.xpath(".//input|.//select|.//textarea");
    public static final By INSTANCE = new NestedElementForValueBy();

    private NestedElementForValueBy() {
        super(NESTED);
    }
}
