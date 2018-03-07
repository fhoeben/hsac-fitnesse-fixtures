package nl.hsac.fitnesse.fixture.util.selenium;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.Collections;
import java.util.List;

/**
 * Customization of standard select helper.
 */
public class SelectHelper extends Select {
    /**
     * @param element element to check
     * @return true if element is indeed a 'select'.
     */
    public static boolean isSelect(WebElement element) {
        if (element == null) {
            return false;
        }

        String tagName = element.getTagName();
        return tagName != null && "select".equals(tagName.toLowerCase());
    }

    public SelectHelper(WebElement element) {
        super(element);
    }

    @Override
    public WebElement getFirstSelectedOption() {
        WebElement result = null;
        try {
            result = super.getFirstSelectedOption();
        } catch (NoSuchElementException e) {
            // ignore
        }
        return result;
    }

    @Override
    public List<WebElement> getAllSelectedOptions() {
        List<WebElement> result;
        if (isMultiple()) {
            result = super.getAllSelectedOptions();
        } else {
            WebElement selectedOption = getFirstSelectedOption();
            if (selectedOption == null) {
                result = Collections.emptyList();
            } else {
                result = Collections.singletonList(selectedOption);
            }
        }
        return result;
    }
}
