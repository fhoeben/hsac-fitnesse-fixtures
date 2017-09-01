package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.By;

/**
 * Container class for relative By statements that take no parameters.
 */
public class ConstantBy {
    private static final BestMatchBy NESTED_ELEMENT_FOR_VALUE_BY = new BestMatchBy(By.xpath(".//input|.//select|.//textarea"));

    /**
     * @return By which will return a nested element to obtain a value from (e.g. input or select).
     */
    public static BestMatchBy nestedElementForValue() {
        return NESTED_ELEMENT_FOR_VALUE_BY;
    }
}
