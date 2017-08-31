package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.WebElement;

import java.util.function.Function;

/**
 * Function to be used as post-processor when finding elements.
 * It will filter out non-interactable elements.
 */
public class IsInteractableFilter implements Function<WebElement, WebElement> {
    /**
     * Filters out non-interactable elements.
     * @param webElement element to check.
     * @return webElement if it is interactable, null otherwise.
     */
    @Override
    public WebElement apply(WebElement webElement) {
        return mayPass(webElement) ? webElement : null;
    }

    /**
     * Checks whether element is interactable.
     * @param element element to check.
     * @return true for interactable elements, false otherwise.
     */
    public static boolean mayPass(WebElement element) {
        return element != null && element.isDisplayed() && element.isEnabled();
    }
}
