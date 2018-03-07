package nl.hsac.fitnesse.fixture.util.selenium;

import nl.hsac.fitnesse.fixture.util.selenium.by.ConstantBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Helper to determine which value(s) of a select is currently selected.
 */
public class SelectHelper {
    private static final By SELECTED_OPTIONS_BY = ConstantBy.getSelectedOptionsBy();
    private final WebElement selectElement;

    public SelectHelper(WebElement element) {
        selectElement = element;
    }

    public WebElement getFirstSelectedOption() {
        List<WebElement> selectedOptions = getAllSelectedOptions();
        return selectedOptions.isEmpty()? null : selectedOptions.get(0);
    }

    public List<WebElement> getAllSelectedOptions() {
        return selectElement.findElements(SELECTED_OPTIONS_BY);
    }

    /**
     * @param element element to check
     * @return true if element is indeed a 'select'.
     */
    public static boolean isSelect(WebElement element) {
        String tagName = element.getTagName();
        return "select".equalsIgnoreCase(tagName);
    }
}
