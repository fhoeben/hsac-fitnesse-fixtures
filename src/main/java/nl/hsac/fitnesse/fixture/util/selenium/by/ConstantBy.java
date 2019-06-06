package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

/**
 * Container class for relative By statements that take no parameters.
 */
public class ConstantBy {
    private static final By FRAME_BY = By.cssSelector("iframe,frame");
    private static final BestMatchBy NESTED_ELEMENT_FOR_VALUE_BY = new BestMatchBy(By.cssSelector("input:not([type='hidden']),select,textarea"));
    private static final BestMatchBy SUBMIT_BUTTON_BY = new BestMatchBy(By.cssSelector("input[type='submit']:not([value])"));
    private static final BestMatchBy RESET_BUTTON_BY = new BestMatchBy(By.cssSelector("input[type='reset']:not([value])"));
    private static final BestMatchBy CHECKBOX_BY = new BestMatchBy(By.cssSelector("input[type='checkbox']"));
    private static final SingleElementOrNullBy NULL_BY = new FindsNothing();
    private static final By SELECTED_OPTIONS_BY = By.cssSelector("option:checked");

    /**
     * @return By which will return all frames and iframes.
     */
    public static By frames() {
        return FRAME_BY;
    }

    /**
     * @return By which will return a nested element to obtain a value from (e.g. input or select).
     */
    public static BestMatchBy nestedElementForValue() {
        return NESTED_ELEMENT_FOR_VALUE_BY;
    }

    /**
     * @param placeWanted which place is requested.
     * @return if placeWanted is either submit or reset the corresponding By is retruned, otherwise {@link #nothing()}.
     */
    public static SingleElementOrNullBy submitOrReset(String placeWanted) {
        SingleElementOrNullBy result = nothing();
        placeWanted = placeWanted.toLowerCase();
        if ("submit".equals(placeWanted)) {
            result = submitButton();
        } else if ("reset".equals(placeWanted)) {
            result = resetButton();
        }
        return result;
    }

    /**
     * @return By which will return a submit button created by having an input of type submit without 'value' attribute.
     */
    public static BestMatchBy submitButton() {
        return SUBMIT_BUTTON_BY;
    }

    /**
     * @return By which will return a reset button created by having an input of type reset without 'value' attribute.
     */
    public static BestMatchBy resetButton() {
        return RESET_BUTTON_BY;
    }

    /**
     * @param placeWanted which place is requested.
     * @return if placeWanted is checkbox the corresponding By is retruned, otherwise {@link #nothing()}.
     */
    public static SingleElementOrNullBy checkboxOrNothing(String placeWanted) {
        placeWanted = placeWanted.toLowerCase();
        SingleElementOrNullBy result;
        if ("checkbox".equals(placeWanted)) {
            result = checkbox();
        } else {
            result = nothing();
        }
        return result;
    }

    /**
     * @return By which will return a checkbox created by having an input of type 'checkbox'.
     */
    public static BestMatchBy checkbox() {
        return CHECKBOX_BY;
    }

    /**
     * @return by which returns all option elements that are selected.
     */
    public static By getSelectedOptionsBy() {
        return SELECTED_OPTIONS_BY;
    }

    /**
     * @return By which will never return any elements.
     */
    public static SingleElementOrNullBy nothing() {
        return NULL_BY;
    }

    private static final class FindsNothing extends SingleElementOrNullBy {
        @Override
        public WebElement findElement(SearchContext context) {
            return null;
        }

        @Override
        public String toString() {
            return "FindsNothingBy";
        }
    }
}
