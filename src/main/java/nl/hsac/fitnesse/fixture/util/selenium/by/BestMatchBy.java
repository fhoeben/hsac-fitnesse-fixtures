package nl.hsac.fitnesse.fixture.util.selenium.by;

import nl.hsac.fitnesse.fixture.util.selenium.SeleniumHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Decorator for a By statement, always returning only a single element.
 * When the nested By returns multiple the first element displayed, and 'on top' is returned.
 */
public class BestMatchBy extends SingleElementOrNullBy {
    private static final String TOP_ELEMENT_AT =
            "if (arguments[0].getBoundingClientRect) {\n" +
                    "  var rect = arguments[0].getBoundingClientRect();\n" +
                    "  var x = (rect.left + rect.right)/2;\n" +
                    "  var y = (rect.top + rect.bottom)/2;\n" +
                    "  return document.elementFromPoint(x,y);\n" +
                    "} else { return null; }";

    private final By by;

    public BestMatchBy(By nestedBy) {
        this.by = nestedBy;
    }

    @Override
    public WebElement findElement(SearchContext context) {
        WebElement element = null;
        List<WebElement> elements = context.findElements(by);
        if (elements.size() == 1) {
            element = elements.get(0);
        } else if (elements.size() > 1) {
            element = selectBestElement(context, elements);
        }
        return element;
    }

    private WebElement selectBestElement(SearchContext context, List<WebElement> elements) {
        JavascriptExecutor jse = JavascriptBy.getJavascriptExecutor(context);
        // take the first displayed element without any elements on top of it,
        // if none: take first displayed
        // or if none are displayed: just take the first
        WebElement element = elements.get(0);
        WebElement firstDisplayed = null;
        WebElement firstOnTop = null;
        if (!element.isDisplayed() || !isOnTop(jse, element)) {
            for (int i = 1; i < elements.size(); i++) {
                WebElement otherElement = elements.get(i);
                if (otherElement.isDisplayed()) {
                    if (firstDisplayed == null) {
                        firstDisplayed = otherElement;
                    }
                    if (isOnTop(jse, otherElement)) {
                        firstOnTop = otherElement;
                        element = otherElement;
                        break;
                    }
                }
            }
            if (firstOnTop == null
                    && firstDisplayed != null
                    && !element.isDisplayed()) {
                // none displayed and on top
                // first was not displayed, but another was
                element = firstDisplayed;
            }
        }
        return element;
    }

    private boolean isOnTop(JavascriptExecutor executor, WebElement element) {
        WebElement e = (WebElement) SeleniumHelper.executeScript(executor, TOP_ELEMENT_AT, element);
        return element.equals(e);
    }

}
