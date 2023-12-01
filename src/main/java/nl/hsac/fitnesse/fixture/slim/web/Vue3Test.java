package nl.hsac.fitnesse.fixture.slim.web;

import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

public class Vue3Test<T extends WebElement> extends BrowserTest<T>{
    @Override
    protected boolean isInteractable(WebElement element) {
        boolean interactable = super.isInteractable(element);
        if (element != null && !interactable) {
            focusElement(element);
            interactable = super.isInteractable(element);
        }
        return interactable;
    }

    @Override
    protected boolean clickElement(WebElement element) {
        return doIfInteractable(element, () -> {
            try {
                element.click();
            } catch (WebDriverException e) {
                if (clickExceptionIsAboutHiddenByOtherElement(e) && isVueHiddenElement(element)) {
                    clickWithJavascript(element);
                } else {
                    throw e;
                }
            }
        });
    }

    private Object focusElement(WebElement element) {
        return getSeleniumHelper().executeJavascript("arguments[0].focus()", element);
    }

    private boolean isVueHiddenElement(WebElement element) {
        return element.getTagName().equalsIgnoreCase("label");
    }

    private void clickWithJavascript(WebElement element) {
        getSeleniumHelper().executeJavascript("arguments[0].click()", element);
    }
}
