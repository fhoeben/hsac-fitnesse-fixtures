package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

/**
 * By to get the element to obtain value from.
 */
public class ValueOfBy extends BestMatchBy {
    public ValueOfBy(By nestedBy) {
        super(nestedBy);
    }

    @Override
    public WebElement findElement(SearchContext context) {
        WebElement element = super.findElement(context);
        if (element != null) {
            WebElement nested = ConstantBy.nestedElementForValue().findElement(element);
            if (nested != null && nested.isDisplayed()) {
                element = nested;
            }
        }
        return element;
    }
}
