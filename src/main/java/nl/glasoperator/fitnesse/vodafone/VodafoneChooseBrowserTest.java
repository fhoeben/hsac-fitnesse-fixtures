package nl.glasoperator.fitnesse.vodafone;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.util.List;

/**
 * Fixture to test pick and choose and product detail pages for Vodafone.
 */
public class VodafoneChooseBrowserTest extends VodafoneBrowserTest {
    private static final String CART_PRODUCT_ITEM_PATTERN = "//div[@id = 'overview_order']//li[contains(normalize-space(div/b/text()), '%s')]/div[@class = 'order-right']";

    public boolean pickPackage(final String type, final String packageName) {
        boolean result = false;
        WebElement element = findByXPath("//h3[contains(normalize-space(text()), '%s')]/..//h3[text() = '%s']", type, packageName);
        if (element != null) {
            result = clickElement(element);
            if (result) {
                // wait for element to be highlighted
                result = waitUntil(new ExpectedCondition<Boolean>() {
                    @Override
                    public Boolean apply(WebDriver webDriver) {
                        return packageName.equals(chosenPackageFor(type));
                    }
                });
                if (result
                        && "Televisie".equals(type)
                        && "Extra".equals(packageName)) {
                    result = waitForTagWithText("h3", "Welke 2 extra zenderpakketten wil je erbij?");
                }
            }
        }
        return result;
    }

    public String chosenPackageFor(String type) {
        String chosenPackage = null;
        WebElement parentElement = findByXPath("//h3[contains(normalize-space(text()), '%s')]/..//div[contains(@class, ' selected')]//h3", type);
        if (parentElement != null) {
            chosenPackage = parentElement.getText();
        }
        return chosenPackage;
    }

    public String cartTotal(String categoryName) {
        return getTextByXPath("//div[@id = 'overview_costs']//li[contains(normalize-space(text()), '%s')]/div/span",
                                categoryName);
    }

    public String cartProductsPrice(String productName) {
        return getTextByXPath(CART_PRODUCT_ITEM_PATTERN + "/span", productName);
    }

    public String cartProductsPeriod(String productName) {
        String result = getTextByXPath(CART_PRODUCT_ITEM_PATTERN, productName);
        if (result != null && result.contains("\n")) {
            result = result.split("\\n")[1];
        } else {
            result = null;
        }
        return result;
    }

    private String getTextByXPath(String xpathPattern, String... params) {
        String result = null;
        WebElement element = findByXPath(xpathPattern, params);
        if (element != null) {
            result = element.getText();
        }
        return result;
    }

    //click the option to add a extra service
    public boolean clickOptionButton(String type, String choice) {
        return clickByXPath("//h3[normalize-space(text()) = '%s']/..//a[normalize-space(text()) = '%s']/..//label",
                                type, choice);
    }

    //The selector of predefined packages
    public boolean clickIn(String buttonText, String packageName) {
        return clickByXPath("//h2[contains(normalize-space(text()), '%s')]/..//a[text() = '%s']",
                                packageName, buttonText);
    }

    public boolean clickTvPackage(String packageName) {
        return clickByXPath("//label/b[contains(normalize-space(text()), '%s')]",
                packageName);
    }

    private boolean clickByXPath(String xpathExpr, String... params) {
        boolean result = false;
        WebElement element = findByXPath(xpathExpr, params);
        if (element != null) {
            result = clickElement(element);
        }
        return result;
    }

    //check if messagebox appears
    public Boolean messageBox() {
        Boolean result = null;
        List<WebElement> elements = findAllByXPath("//div[@id = 'messagebox' and contains(@class, 'message red')]");
        if (elements != null) {
            for (WebElement element : elements) {
                if (element.isDisplayed()) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }


}
