package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

/**
 * Container class for relative By statements that take no parameters.
 */
public class ConstantBy {
    private static final BestMatchBy NESTED_ELEMENT_FOR_VALUE_BY = new BestMatchBy(By.xpath(".//input|.//select|.//textarea"));
    private static final SingleElementOrNullBy PARENT_A_BY = new ParentABy();

    /**
     * @return By which will return a nested element to obtain a value from (e.g. input or select).
     */
    public static BestMatchBy nestedElementForValue() {
        return NESTED_ELEMENT_FOR_VALUE_BY;
    }

    public static SingleElementOrNullBy parentA() {
        return PARENT_A_BY;
    }

    private static class ParentABy extends SingleElementOrNullBy {
        private static final By findParentABy = By.xpath("./ancestor::a");

        @Override
        public WebElement findElement(SearchContext context) {
            if (context == null || context instanceof WebElement) {
                WebElement element = (WebElement) context;
                if (element != null && !"a".equalsIgnoreCase(element.getTagName())) {
                    element = findParentABy.findElement(element);
                }
                return element;

            }
            throw new IllegalArgumentException("Should only be called for WebElements, but was passed: " + context);
        }
    }
}
